import logging

from fastapi import FastAPI

from app.consumers.view_event_consumer import ViewEventConsumer

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="Recommendation Service", version="0.1.0")


@app.on_event("startup")
async def startup() -> None:
    consumer = ViewEventConsumer()
    consumer.start()
    logger.info("Recommendation service started, consuming view.events")


@app.get("/health")
async def health() -> dict:
    return {"status": "ok"}
