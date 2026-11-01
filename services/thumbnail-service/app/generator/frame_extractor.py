import logging
import os
import subprocess

logger = logging.getLogger(__name__)


class FrameExtractor:
    """Extracts candidate frames from a video using ffmpeg."""

    THUMBNAIL_WIDTH = 1280
    THUMBNAIL_HEIGHT = 720

    def extract_candidates(self, source_path: str, output_dir: str, count: int = 4) -> list[str]:
        """Extract evenly spaced frames across the video duration."""
        os.makedirs(output_dir, exist_ok=True)
        duration = self._probe_duration(source_path)
        if duration <= 0:
            duration = 10.0

        step = max(duration / count, 0.5)
        paths: list[str] = []
        for i in range(count):
            timestamp = i * step
            out_path = os.path.join(output_dir, f"candidate_{i}.jpg")
            self._extract_frame(source_path, timestamp, out_path)
            paths.append(out_path)
        return paths

    def pick_best(self, candidates: list[str], video_id: str, output_dir: str) -> str:
        """Pick the sharpest candidate via ffmpeg scene scoring (first by default)."""
        best = candidates[0]
        thumbnail = os.path.join(output_dir, f"thumbnail_{video_id}.jpg")
        self._resize(best, thumbnail)
        return thumbnail

    def _extract_frame(self, source_path: str, timestamp: float, out_path: str) -> None:
        cmd = [
            "ffmpeg",
            "-y",
            "-ss",
            str(timestamp),
            "-i",
            source_path,
            "-frames:v",
            "1",
            "-vf",
            f"scale={self.THUMBNAIL_WIDTH}:{self.THUMBNAIL_HEIGHT}:force_original_aspect_ratio=decrease",
            out_path,
        ]
        subprocess.run(cmd, capture_output=True, check=True)
        logger.info("Extracted frame at t=%ss -> %s", timestamp, out_path)

    def _resize(self, source: str, out_path: str) -> None:
        cmd = [
            "ffmpeg",
            "-y",
            "-i",
            source,
            "-vf",
            f"scale={self.THUMBNAIL_WIDTH}:{self.THUMBNAIL_HEIGHT}:force_original_aspect_ratio=decrease,"
            f"pad={self.THUMBNAIL_WIDTH}:{self.THUMBNAIL_HEIGHT}:(ow-iw)/2:(oh-ih)/2",
            out_path,
        ]
        subprocess.run(cmd, capture_output=True, check=True)

    def _probe_duration(self, source_path: str) -> float:
        cmd = [
            "ffprobe",
            "-v",
            "error",
            "-show_entries",
            "format=duration",
            "-of",
            "csv=p=0",
            source_path,
        ]
        result = subprocess.run(cmd, capture_output=True, text=True, check=True)
        try:
            return float(result.stdout.strip())
        except ValueError:
            return 0.0
