import json
import logging
import os
import threading

import boto3
from botocore.client import Config
from confluent_kafka import Consumer, KafkaError, KafkaException

from app.services.moderation_service import ModerationService

logger = logging.getLogger(__name__)


class VideoUploadedConsumer:
    """Consumes video.uploaded events and runs content moderation."""

    def __init__(self) -> None:
        self.topic = "video.uploaded"
        self.consumer = Consumer(
            {
                "bootstrap.servers": "kafka:9092",
                "group.id": "moderation-service",
                "auto.offset.reset": "earliest",
                "enable.auto.commit": False,
            }
        )
        self.storage = boto3.client(
            "s3",
            endpoint_url=os.getenv("OBJECT_STORAGE_ENDPOINT", "http://minio:9000"),
            aws_access_key_id=os.getenv("OBJECT_STORAGE_ACCESS_KEY", "minioadmin"),
            aws_secret_access_key=os.getenv("OBJECT_STORAGE_SECRET_KEY", "minioadmin"),
            config=Config(signature_version="s3v4"),
            region_name="us-east-1",
        )
        self.bucket = os.getenv("OBJECT_STORAGE_BUCKET", "vsp-media")
        self.moderation = ModerationService()
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
        work_dir = f"/tmp/moderation/{video_id}"
        os.makedirs(work_dir, exist_ok=True)

        source_path = os.path.join(work_dir, os.path.basename(source_key))
        self.storage.download_file(self.bucket, source_key, source_path)

        result = self.moderation.moderate(source_path)
        # Persist verdict in the video-service via REST or a moderation.result topic.
        logger.info("Moderation complete for videoId=%s: %s", video_id, result)
