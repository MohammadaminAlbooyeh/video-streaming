import json
import logging
import time

from confluent_kafka import Producer

logger = logging.getLogger(__name__)


class VideoProcessedProducer:
    """Publishes events to Kafka topic video.ready after transcoding completes."""

    def __init__(self) -> None:
        self.topic = "video.ready"
        self.producer = Producer({"bootstrap.servers": "kafka:9092"})

    def publish_ready(
        self,
        video_id: str,
        duration_seconds: float,
        height: int,
        manifest_path: str,
        variants: list[str],
    ) -> None:
        payload = {
            "videoId": video_id,
            "status": "READY",
            "durationSeconds": int(duration_seconds),
            "sourceHeight": height,
            "manifestPath": manifest_path,
            "variants": variants,
            "timestamp": time.time(),
        }
        logger.info("Publishing video.ready for videoId=%s", video_id)
        self.producer.produce(self.topic, key=str(video_id), value=json.dumps(payload))
        self.producer.flush()

    def publish_failed(self, video_id: str, reason: str) -> None:
        payload = {
            "videoId": video_id,
            "status": "FAILED",
            "reason": reason,
            "timestamp": time.time(),
        }
        self.producer.produce(self.topic, key=str(video_id), value=json.dumps(payload))
        self.producer.flush()
