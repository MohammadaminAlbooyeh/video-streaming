import json
import logging
import threading

from confluent_kafka import Consumer, KafkaError, KafkaException

from app.services.recommendation_service import RecommendationService

logger = logging.getLogger(__name__)


class ViewEventConsumer:
    """Consumes Kafka topic view.events to update user recommendations."""

    def __init__(self) -> None:
        self.topic = "view.events"
        self.consumer = Consumer(
            {
                "bootstrap.servers": "kafka:9092",
                "group.id": "recommendation-service",
                "auto.offset.reset": "earliest",
                "enable.auto.commit": False,
            }
        )
        self.recommendations = RecommendationService()
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
        self.recommendations.record_watch(event)
