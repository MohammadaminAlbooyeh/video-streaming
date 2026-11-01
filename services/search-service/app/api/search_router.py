from typing import Any

from fastapi import APIRouter, HTTPException, Query

from app.services.search_service import search_service

router = APIRouter(prefix="/api/search", tags=["search"])


@router.get("")
async def search(
    q: str = Query(..., min_length=1),
    limit: int = Query(20, ge=1, le=50),
    offset: int = Query(0, ge=0),
    min_view_count: int | None = Query(None, ge=0),
    channel: str | None = Query(None),
    tag: str | None = Query(None),
) -> dict[str, Any]:
    try:
        return search_service.filtered_search(
            q, min_view_count=min_view_count, channel=channel, tag=tag, limit=limit, offset=offset
        )
    except Exception as exc:
        raise HTTPException(status_code=503, detail=f"Search unavailable: {exc}") from exc


@router.get("/autocomplete")
async def autocomplete(q: str = Query(..., min_length=1), limit: int = Query(10, ge=1, le=20)) -> dict[str, Any]:
    try:
        return search_service.autocomplete(q, limit=limit)
    except Exception as exc:
        raise HTTPException(status_code=503, detail=f"Search unavailable: {exc}") from exc


@router.post("/index")
async def index_document(doc: dict[str, Any]) -> dict[str, str]:
    if "videoId" not in doc:
        raise HTTPException(status_code=422, detail="videoId required")
    search_service.index_document(doc)
    return {"status": "indexed"}


@router.delete("/index/{video_id}")
async def delete_document(video_id: int) -> dict[str, str]:
    search_service.delete_document(video_id)
    return {"status": "deleted"}
