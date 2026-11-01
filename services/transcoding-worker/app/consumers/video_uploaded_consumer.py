import json
import logging
import threading
import time

from confluent_kafka import Consumer, KafkaError, KafkaException

from app.producers.video_processed_producer import VideoProcessedProducer
from app.storage.object_storage_client import ObjectStorageClient
from app.transcoding.ffmpeg_wrapper import FfmpegWrapper
from app.transcoding.hls_dash_packager import HlsDashPackager
from app.transcoding.resolution_ladder import ResolutionLadder

logger = logging.getLogger(__name__)


class VideoUploadedConsumer:
    """Consumes Kafka topic video.uploaded and kicks off the transcoding pipeline."""

    def __init__(self) -> None:
        self.topic = "video.uploaded"
        self.consumer = Consumer(
            {
                "bootstrap.servers": "kafka:9092",
                "group.id": "transcoding-worker",
                "auto.offset.reset": "earliest",
                "enable.auto.commit": False,
            }
        )
        self.storage = ObjectStorageClient()
        self.ffmpeg = FfmpegWrapper()
        self.ladder = ResolutionLadder()
        self.packager = HlsDashPackager()
        self.producer = VideoProcessedProducer()
        self._thread: threading.Thread | None = None

    def start(self) -> None:
        if self._thread and self._thread.is_alive():
            return
        self._thread = threading.Thread(target=self._run, daemon=True)
        self._thread.start()

    def _run(self) -> None:
        self.consumer.subscribe([self.topic])
        try:
            while True:
                msg = self.consumer.poll(timeout=1.0)
                if msg is None:
                    continue
                if msg.error():
                    if msg.error().code() == KafkaError._PARTITION_EOF:
                        continue
                    raise KafkaException(msg.error())
                self._handle_event(json.loads(msg.value()))
                self.consumer.commit(asynchronous=False)
        finally:
            self.consumer.close()

    def _handle_event(self, event: dict) -> None:
        video_id = event["videoId"]
        source_key = event["sourceObjectKey"]
        logger.info("Processing upload for videoId=%s source=%s", video_id, source_key)

        try:
            source_path = self.storage.download(source_key, f"/tmp/{video_id}/source")
            probe = self.ffmpeg.probe(source_path)
            height = probe.get("height", 1080)
            duration = probe.get("duration_seconds", 0)

            variants = self.ladder.build_for_source_height(height)
            variant_paths: list[dict] = []
            for variant in variants:
                output_path = f"/tmp/{video_id}/{variant['label']}/master"
                self.ffmpeg.transcode(
                    source_path,
                    output_path,
                    width=variant["width"],
                    height=variant["height"],
                    video_bitrate=variant["bitrate_kbps"] * 1000,
                )
                variant_paths.append(
                    {"label": variant["label"], "path": output_path, "bitrate_kbps": variant["bitrate_kbps"]}
                )

            hls_root, dash_root = self.packager.package(video_id, variant_paths)
            for variant in variants:
                self.storage.upload_tree(
                    f"/tmp/{video_id}/{variant['label']}",
                    f"videos/{video_id}/hls/{variant['label']}",
                )
            self.storage.upload_tree(hls_root, f"videos/{video_id}/hls")
            self.storage.upload_tree(dash_root, f"videos/{video_id}/dash")

            self.producer.publish_ready(
                video_id=video_id,
                duration_seconds=duration,
                height=height,
                manifest_path=f"videos/{video_id}/hls/master.m3u8",
                variants=[v["label"] for v in variants],
            )
            logger.info("Video %s ready", video_id)
        except Exception:
            logger.exception("Transcoding failed for videoId=%s", video_id)
            self.producer.publish_failed(video_id, "transcoding error")
