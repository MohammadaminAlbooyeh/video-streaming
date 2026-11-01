import logging

from fastapi import FastAPI

from app.api.search_router import router

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="Search Service", version="0.1.0")
app.include_router(router)


@app.get("/health")
async def health() -> dict:
    return {"status": "ok"}
