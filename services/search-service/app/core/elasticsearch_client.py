import os

from elasticsearch import Elasticsearch


class ElasticsearchClient:
    """Shared Elasticsearch connection helper."""

    def __init__(self) -> None:
        self.client = Elasticsearch(
            hosts=[os.getenv("ELASTICSEARCH_URL", "http://elasticsearch:9200")],
            request_timeout=30,
        )
        self.index = os.getenv("ELASTICSEARCH_INDEX", "videos")

    def ensure_index(self) -> None:
        if not self.client.indices.exists(index=self.index):
            self.client.indices.create(
                index=self.index,
                mappings={
                    "properties": {
                        "videoId": {"type": "long"},
                        "title": {
                            "type": "text",
                            "analyzer": "standard",
                            "fields": {"keyword": {"type": "keyword"}},
                        },
                        "description": {"type": "text"},
                        "channelName": {"type": "keyword"},
                        "tags": {"type": "keyword"},
                        "durationSeconds": {"type": "integer"},
                        "viewCount": {"type": "long"},
                        "publishedAt": {"type": "date"},
                    }
                },
            )


_client: ElasticsearchClient | None = None


def get_es() -> ElasticsearchClient:
    global _client
    if _client is None:
        _client = ElasticsearchClient()
        _client.ensure_index()
    return _client
