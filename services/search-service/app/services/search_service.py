import logging
from typing import Any

from elasticsearch import Elasticsearch

from app.core.elasticsearch_client import get_es
from app.services.query_builder import QueryBuilder

logger = logging.getLogger(__name__)

SEARCH_FIELDS = ["title^3", "description", "tags", "channelName"]


class SearchService:
    def __init__(self) -> None:
        self.es: Elasticsearch = get_es().client
        self.index = get_es().index

    def search(self, query: str, limit: int = 20, offset: int = 0) -> dict[str, Any]:
        body = QueryBuilder.multi_match(query, SEARCH_FIELDS)
        return self._execute(body, limit, offset)

    def filtered_search(
        self,
        query: str,
        min_view_count: int | None = None,
        channel: str | None = None,
        tag: str | None = None,
        limit: int = 20,
        offset: int = 0,
    ) -> dict[str, Any]:
        body = QueryBuilder.filtered(query, SEARCH_FIELDS, min_view_count, channel, tag)
        return self._execute(body, limit, offset)

    def autocomplete(self, query: str, limit: int = 10) -> dict[str, Any]:
        body = QueryBuilder.suggest(query)
        resp = self.es.search(index=self.index, body=body, size=limit)
        return resp

    def index_document(self, doc: dict[str, Any]) -> None:
        doc_id = doc["videoId"]
        self.es.index(index=self.index, id=str(doc_id), document=doc)
        logger.info("Indexed videoId=%s", doc_id)

    def delete_document(self, video_id: int) -> None:
        self.es.delete(index=self.index, id=str(video_id), ignore=[404])

    def _execute(self, body: dict[str, Any], limit: int, offset: int) -> dict[str, Any]:
        resp = self.es.search(index=self.index, body=body, size=limit, from_=offset)
        hits = resp.get("hits", {}).get("hits", [])
        total = resp.get("hits", {}).get("total", {}).get("value", 0)
        return {
            "total": total,
            "offset": offset,
            "results": [{"id": h["_id"], "source": h["_source"]} for h in hits],
        }


search_service = SearchService()
