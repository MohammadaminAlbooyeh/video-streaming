import logging
import os
import pickle
from typing import Any

import numpy as np
from sklearn.ensemble import GradientBoostingRegressor
from sklearn.model_selection import train_test_split

logger = logging.getLogger(__name__)


class WatchPredictor:
    """ML model that predicts expected watch-time for a candidate video."""

    FEATURES = ["view_count", "like_ratio", "duration", "channel_popularity", "hour_of_day"]

    def __init__(self) -> None:
        self.model = GradientBoostingRegressor(
            n_estimators=200, learning_rate=0.05, max_depth=4, random_state=42
        )
        self._trained = False

    def train(self, samples: list[dict[str, Any]]) -> None:
        if len(samples) < 10:
            logger.info("Not enough samples to train (%s)", len(samples))
            return
        X = np.array([[s.get(f, 0.0) for f in self.FEATURES] for s in samples])
        y = np.array([s["watch_seconds"] for s in samples])
        X_train, _, y_train, _ = train_test_split(X, y, test_size=0.2, random_state=42)
        self.model.fit(X_train, y_train)
        self._trained = True
        logger.info("Watch predictor trained on %s samples", len(samples))

    def predict(self, features: dict[str, float]) -> float:
        if not self._trained:
            return self._heuristic(features)
        vector = np.array([[features.get(f, 0.0) for f in self.FEATURES]])
        return float(self.model.predict(vector)[0])

    def _heuristic(self, features: dict[str, float]) -> float:
        return float(features.get("view_count", 0)) * 0.3 + float(features.get("duration", 0)) * 0.05

    def save(self, path: str) -> None:
        with open(path, "wb") as f:
            pickle.dump(self.model, f)

    def load(self, path: str) -> bool:
        if not os.path.exists(path):
            return False
        with open(path, "rb") as f:
            self.model = pickle.load(f)
        self._trained = True
        return True
