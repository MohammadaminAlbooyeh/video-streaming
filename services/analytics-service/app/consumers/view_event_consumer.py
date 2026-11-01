import json
import logging
import threading

from confluent_kafka import Consumer, KafkaError, KafkaException

logger = logging.getLogger(__name__)


class ViewEventConsumer:
    """Consumes Kafka topic view.events into a local buffer for aggregation."""

    def __init__(self) -> None:
        self.topic = "view.events"
        self.buffer: list[dict] = []
        self.consumer = Consumer(
            {
                "bootstrap.servers": "kafka:9092",
                "group.id": "analytics-service",
                "auto.offset.reset": "earliest",
                "enable.auto.commit": False,
            }
        )
        self._thread: threading.Thread | None = None

    def start(self) -> None:
        if self._thread and self._thread.is_alive():
            return
        self._thread = threading.Thread(target=self._run, daemon=True)
        self._thread.start()

    def drain(self) -> list[dict]:
        events = self.buffer
        self.buffer = []
        return events

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
                self.buffer.append(json.loads(msg.value()))
                if len(self.buffer) >= 1000:
                    self.consumer.commit(asynchronous=False)
        finally:
            self.consumer.close()
