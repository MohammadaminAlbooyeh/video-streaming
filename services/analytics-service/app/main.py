import logging

from fastapi import FastAPI

from app.consumers.view_event_consumer import ViewEventConsumer
from app.pipelines.watch_time_aggregation_job import WatchTimeAggregationJob

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="Analytics Service", version="0.1.0")


@app.on_event("startup")
async def startup() -> None:
    consumer = ViewEventConsumer()
    consumer.start()
    job = WatchTimeAggregationJob()
    job.schedule()
    logger.info("Analytics service started, consuming view.events")


@app.get("/health")
async def health() -> dict:
    return {"status": "ok"}
