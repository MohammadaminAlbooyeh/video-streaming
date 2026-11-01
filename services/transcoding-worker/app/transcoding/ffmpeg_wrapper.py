import json
import logging
import shutil
import subprocess
from typing import Any

logger = logging.getLogger(__name__)


class FfmpegWrapper:
    """Thin wrapper around the ffmpeg/ffprobe CLI."""

    def probe(self, source_path: str) -> dict[str, Any]:
        """Return stream metadata for a source file."""
        cmd = [
            "ffprobe",
            "-v",
            "error",
            "-select_streams",
            "v:0",
            "-show_entries",
            "stream=width,height,r_frame_rate,duration",
            "-show_entries",
            "format=duration",
            "-of",
            "json",
            source_path,
        ]
        result = subprocess.run(cmd, capture_output=True, text=True, check=True)
        data = json.loads(result.stdout)

        stream = (data.get("streams") or [{}])[0]
        duration = float(data.get("format", {}).get("duration") or stream.get("duration") or 0)
        return {
            "width": int(stream.get("width") or 0),
            "height": int(stream.get("height") or 0),
            "duration_seconds": int(duration),
        }

    def transcode(
        self,
        source_path: str,
        output_path: str,
        width: int,
        height: int,
        video_bitrate: int,
        audio_bitrate: str = "128k",
    ) -> None:
        """Transcode source into an MP4/H.264 rendition at the given resolution."""
        out = f"{output_path}.mp4"
        cmd = [
            "ffmpeg",
            "-y",
            "-i",
            source_path,
            "-vf",
            f"scale={width}:{height}:force_original_aspect_ratio=decrease,"
            f"pad={width}:{height}:(ow-iw)/2:(oh-ih)/2",
            "-c:v",
            "libx264",
            "-preset",
            "medium",
            "-b:v",
            str(video_bitrate),
            "-maxrate",
            str(video_bitrate),
            "-bufsize",
            str(video_bitrate * 2),
            "-c:a",
            "aac",
            "-b:a",
            audio_bitrate,
            "-movflags",
            "+faststart",
            out,
        ]
        logger.info("Running ffmpeg: %s", " ".join(cmd))
        subprocess.run(cmd, capture_output=True, check=True)
        logger.info("Transcoded %s -> %s", source_path, out)
        return None

    def available(self) -> bool:
        return shutil.which("ffmpeg") is not None and shutil.which("ffprobe") is not None
