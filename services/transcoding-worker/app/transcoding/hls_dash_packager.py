import logging
import os
import subprocess
from typing import Any

logger = logging.getLogger(__name__)


class HlsDashPackager:
    """Segments transcoded renditions into HLS (.ts) and DASH (.m4s) chunks."""

    def package(self, video_id: str, variants: list[dict[str, Any]]) -> tuple[str, str]:
        """Return (hls_root, dash_root) directories containing packaged output."""
        base = f"/tmp/{video_id}"
        hls_root = os.path.join(base, "hls")
        dash_root = os.path.join(base, "dash")
        os.makedirs(hls_root, exist_ok=True)
        os.makedirs(dash_root, exist_ok=True)

        hls_variants: list[str] = []
        for variant in variants:
            source = f"{variant['path']}.mp4"
            label = variant["label"]

            hls_dir = os.path.join(hls_root, label)
            os.makedirs(hls_dir, exist_ok=True)
            hls_playlist = os.path.join(hls_dir, "index.m3u8")
            self._segment_hls(source, hls_dir, hls_playlist, label)
            hls_variants.append(hls_playlist)

        self._write_master_playlist(hls_root, hls_variants, variants)
        self._build_dash(base, variants)
        return hls_root, dash_root

    def _segment_hls(self, source: str, out_dir: str, playlist: str, label: str) -> None:
        cmd = [
            "ffmpeg",
            "-y",
            "-i",
            source,
            "-codec",
            "copy",
            "-f",
            "hls",
            "-hls_time",
            "6",
            "-hls_playlist_type",
            "vod",
            "-hls_segment_filename",
            os.path.join(out_dir, f"seg_{label}_%03d.ts"),
            playlist,
        ]
        subprocess.run(cmd, capture_output=True, check=True)

    def _write_master_playlist(
        self, hls_root: str, playlists: list[str], variants: list[dict[str, Any]]
    ) -> None:
        lines = ["#EXTM3U", "#EXT-X-VERSION:7"]
        for playlist, variant in zip(playlists, variants):
            relative = os.path.relpath(playlist, hls_root)
            lines.append(
                f"#EXT-X-STREAM-INF:BANDWIDTH={variant['bitrate_kbps'] * 1000},"
                f"RESOLUTION={variant['width']}x{variant['height']},NAME=\"{variant['label']}\""
            )
            lines.append(relative)
        with open(os.path.join(hls_root, "master.m3u8"), "w") as f:
            f.write("\n".join(lines) + "\n")

    def _build_dash(self, base: str, variants: list[dict[str, Any]]) -> None:
        dash_dir = os.path.join(base, "dash")
        for variant in variants:
            source = f"{variant['path']}.mp4"
            label = variant["label"]
            out = os.path.join(dash_dir, label)
            cmd = [
                "ffmpeg",
                "-y",
                "-i",
                source,
                "-c",
                "copy",
                "-f",
                "dash",
                "-seg_duration",
                "6",
                "-adaptation_sets",
                "id=0,streams=v",
                os.path.join(out, "manifest.mpd"),
            ]
            subprocess.run(cmd, capture_output=True, check=True)
            logger.info("Packaged DASH variant %s", label)
