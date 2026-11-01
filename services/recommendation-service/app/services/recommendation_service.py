import logging
import os
from typing import Any

import redis

from app.models.watch_predictor import WatchPredictor

logger = logging.getLogger(__name__)


class RecommendationService:
    """Generates per-user recommendations using watch predictions + redis cache."""

    def __init__(self) -> None:
        self.predictor = WatchPredictor()
        self.redis = redis.Redis.from_url(os.getenv("REDIS_URL", "redis://redis:6379/0"))
        self.cache_ttl = int(os.getenv("RECOMMENDATION_CACHE_TTL", "300"))
        model_path = os.getenv("WATCH_PREDICTOR_MODEL", "/models/watch_predictor.pkl")
        self.predictor.load(model_path)

    def recommend_for_user(self, user_id: int, candidates: list[dict[str, Any]], top_k: int = 20) -> list[dict[str, Any]]:
        cache_key = f"recommendations:{user_id}"
        cached = self.redis.get(cache_key)
        if cached:
            return self._decode_cache(cached)

        scored: list[tuple[float, dict[str, Any]]] = []
        for video in candidates:
            features = {
                "view_count": video.get("viewCount", 0),
                "like_ratio": video.get("likeRatio", 0),
                "duration": video.get("durationSeconds", 0),
                "channel_popularity": video.get("channelPopularity", 0),
                "hour_of_day": video.get("hourOfDay", 12),
            }
            score = self.predictor.predict(features)
            scored.append((score, video))

        scored.sort(key=lambda item: item[0], reverse=True)
        results = [video for _, video in scored[:top_k]]
        self.redis.setex(cache_key, self.cache_ttl, self._encode_cache(results))
        return results

    def record_watch(self, event: dict[str, Any]) -> None:
        self.redis.rpush("recent.watch.events", self._encode_cache(event))
        self.redis.delete(f"recommendations:{event.get('userId')}")
        logger.info("Recorded watch event for user=%s video=%s", event.get("userId"), event.get("videoId"))

    def _encode_cache(self, value: Any) -> str:
        import json

        return json.dumps(value, default=str)

    def _decode_cache(self, raw: bytes) -> Any:
        import json

        return json.loads(raw)
