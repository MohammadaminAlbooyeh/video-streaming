"""Builds Elasticsearch query DSL bodies."""

from typing import Any


class QueryBuilder:
    @staticmethod
    def multi_match(query: str, fields: list[str], fuzziness: str = "AUTO") -> dict[str, Any]:
        return {
            "query": {
                "multi_match": {
                    "query": query,
                    "fields": fields,
                    "fuzziness": fuzziness,
                    "type": "best_fields",
                }
            }
        }

    @staticmethod
    def filtered(
        query: str,
        fields: list[str],
        min_view_count: int | None = None,
        channel: str | None = None,
        tag: str | None = None,
    ) -> dict[str, Any]:
        must = [
            {"multi_match": {"query": query, "fields": fields, "fuzziness": "AUTO"}}
        ]
        filters: list[dict[str, Any]] = []
        if min_view_count is not None:
            filters.append({"range": {"viewCount": {"gte": min_view_count}}})
        if channel:
            filters.append({"term": {"channelName.keyword": channel}})
        if tag:
            filters.append({"term": {"tags": tag}})

        body: dict[str, Any] = {"query": {"bool": {"must": must}}}
        if filters:
            body["query"]["bool"]["filter"] = filters
        return body

    @staticmethod
    def suggest(query: str) -> dict[str, Any]:
        return {
            "suggest": {
                "title-suggest": {
                    "prefix": query,
                    "completion": {"field": "title.keyword"},
                }
            }
        }
