import asyncio
import json
import logging
import threading

from confluent_kafka import Consumer, KafkaError, KafkaException

from app.channels.email_sender import EmailSender
from app.channels.push_sender import PushSender

logger = logging.getLogger(__name__)


class VideoUploadedConsumer:
    """Consumes video.uploaded events and notifies channel subscribers."""

    def __init__(self) -> None:
        self.topic = "video.uploaded"
        self.consumer = Consumer(
            {
                "bootstrap.servers": "kafka:9092",
                "group.id": "notification-service",
                "auto.offset.reset": "earliest",
                "enable.auto.commit": False,
            }
        )
        self.push = PushSender()
        self.email = EmailSender()
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
        subscribers = self._fetch_subscribers(event["channelId"])
        title = f"New video from channel {event['channelId']}"
        body = event.get("title", "A new video was published")
        for subscriber in subscribers:
            if subscriber.get("deviceToken"):
                asyncio.run(self.push.send(subscriber["deviceToken"], title, body))
            if subscriber.get("email"):
                asyncio.run(self.email.send(subscriber["email"], title, body))
        logger.info("Notified %s subscribers for channel=%s", len(subscribers), event["channelId"])

    def _fetch_subscribers(self, channel_id: int) -> list[dict]:
        # Call user-service REST API: GET /api/channels/{id}/subscribers
        return []
