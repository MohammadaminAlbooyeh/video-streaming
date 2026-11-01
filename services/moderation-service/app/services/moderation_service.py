import logging

import cv2
import numpy as np

from app.models.content_classifier import ContentClassifier

logger = logging.getLogger(__name__)


class ModerationService:
    """Runs the moderation pipeline over a downloaded video."""

    def __init__(self) -> None:
        self.classifier = ContentClassifier()
        self.frame_sample_size = 12

    def moderate(self, source_path: str) -> dict:
        frames = self._sample_frames(source_path)
        result = self.classifier.classify_video(frames)
        logger.info(
            "Moderation verdict=%s (max=%s) for %s",
            result["verdict"],
            result["max_score"],
            source_path,
        )
        return result

    def _sample_frames(self, source_path: str) -> list[np.ndarray]:
        cap = cv2.VideoCapture(source_path)
        if not cap.isOpened():
            return []
        total = int(cap.get(cv2.CAP_PROP_FRAME_COUNT)) or 0
        if total == 0:
            cap.release()
            return []

        step = max(total // self.frame_sample_size, 1)
        frames: list[np.ndarray] = []
        frame_idx = 0
        while True:
            ret, frame = cap.read()
            if not ret:
                break
            if frame_idx % step == 0:
                frames.append(cv2.resize(frame, (320, 180)))
            frame_idx += 1
            if len(frames) >= self.frame_sample_size:
                break
        cap.release()
        return frames
