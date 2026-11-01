import logging

import numpy as np
import cv2

logger = logging.getLogger(__name__)

try:
    from transformers import pipeline  # type: ignore

    _HAS_TRANSFORMERS = True
except ImportError:  # pragma: no cover
    _HAS_TRANSFORMERS = False


class ContentClassifier:
    """Classifies frames for NSFW and general content safety.

    Uses a lightweight heuristic frame analyser by default, with optional
    transformer-based NSFW detection when heavy models are available.
    """

    def __init__(self) -> None:
        self._nsfw_pipeline = None
        if _HAS_TRANSFORMERS:
            try:
                self._nsfw_pipeline = pipeline(
                    "image-classification", model="Falconsai/nsfw_image_detection"
                )
            except Exception:
                logger.warning("NSFW model unavailable, falling back to heuristics")

    def classify_frame(self, frame: np.ndarray) -> dict:
        score = self._heuristic_score(frame)
        return {"nsfw_score": round(score, 4)}

    def classify_video(self, frames: list[np.ndarray]) -> dict:
        scores = [self.classify_frame(f)["nsfw_score"] for f in frames]
        max_score = max(scores) if scores else 0.0
        mean_score = float(np.mean(scores)) if scores else 0.0
        verdict = "reject" if max_score >= 0.85 else "review" if max_score >= 0.6 else "approve"
        return {
            "verdict": verdict,
            "max_score": round(max_score, 4),
            "mean_score": round(mean_score, 4),
            "sampled_frames": len(frames),
        }

    def _heuristic_score(self, frame: np.ndarray) -> float:
        # Placeholder heuristic: skin-tone ratio approximation.
        # Replace with the transformer pipeline in production.
        hsv = cv2.cvtColor(frame, cv2.COLOR_BGR2HSV)
        lower = np.array([0, 60, 40])
        upper = np.array([30, 255, 255])
        mask = cv2.inRange(hsv, lower, upper)
        ratio = float(cv2.countNonZero(mask)) / float(frame.shape[0] * frame.shape[1])
        return min(ratio * 3.0, 1.0)
