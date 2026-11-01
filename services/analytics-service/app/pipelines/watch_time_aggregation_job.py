import logging
import threading
import time

import pandas as pd

from app.consumers.view_event_consumer import ViewEventConsumer

logger = logging.getLogger(__name__)


class WatchTimeAggregationJob:
    """Periodically aggregates buffered view events into per-video metrics."""

    def __init__(self, interval_seconds: int = 60) -> None:
        self.interval_seconds = interval_seconds
        self.consumer = ViewEventConsumer()
        self._thread: threading.Thread | None = None

    def schedule(self) -> None:
        if self._thread and self._thread.is_alive():
            return
        self._thread = threading.Thread(target=self._run, daemon=True)
        self._thread.start()
        logger.info("Watch time aggregation scheduled every %ss", self.interval_seconds)

    def _run(self) -> None:
        while True:
            time.sleep(self.interval_seconds)
            try:
                self.run_once()
            except Exception:
                logger.exception("Aggregation job failed")

    def run_once(self) -> None:
        events = self.consumer.drain()
        if not events:
            return
        df = pd.DataFrame(events)
        agg = (
            df.groupby("videoId")
            .agg(
                total_watch_seconds=("watchSeconds", "sum"),
                view_count=("watchSeconds", "count"),
                unique_viewers=("userId", "nunique"),
            )
            .reset_index()
        )
        records = agg.to_dict(orient="records")
        for record in records:
            logger.info("Aggregated %s", record)
        self._persist(records)

    def _persist(self, records: list[dict]) -> None:
        # Persist into the analytics store (e.g. ClickHouse/PostgreSQL) here.
        pass
