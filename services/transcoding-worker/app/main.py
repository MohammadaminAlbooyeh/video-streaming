import logging

from fastapi import FastAPI

from app.consumers.video_uploaded_consumer import VideoUploadedConsumer

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="Transcoding Worker", version="0.1.0")


@app.on_event("startup")
async def startup() -> None:
    consumer = VideoUploadedConsumer()
    consumer.start()
    logger.info("Transcoding worker started, consuming video.uploaded events")


@app.get("/health")
async def health() -> dict:
    return {"status": "ok"}
