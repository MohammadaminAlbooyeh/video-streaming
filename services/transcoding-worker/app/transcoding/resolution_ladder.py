"""Resolution ladder: 144p -> 4K variants with target bitrates."""

LADDER = [
    {"label": "144p", "width": 256, "height": 144, "bitrate_kbps": 400},
    {"label": "240p", "width": 426, "height": 240, "bitrate_kbps": 700},
    {"label": "360p", "width": 640, "height": 360, "bitrate_kbps": 1200},
    {"label": "480p", "width": 854, "height": 480, "bitrate_kbps": 2000},
    {"label": "720p", "width": 1280, "height": 720, "bitrate_kbps": 3500},
    {"label": "1080p", "width": 1920, "height": 1080, "bitrate_kbps": 6000},
    {"label": "4K", "width": 3840, "height": 2160, "bitrate_kbps": 16000},
]


class ResolutionLadder:
    """Selects which variants to generate based on the source resolution."""

    def build_for_source_height(self, source_height: int) -> list[dict]:
        if source_height >= 2160:
            max_index = 6
        elif source_height >= 1080:
            max_index = 5
        elif source_height >= 720:
            max_index = 4
        elif source_height >= 480:
            max_index = 3
        elif source_height >= 360:
            max_index = 2
        elif source_height >= 240:
            max_index = 1
        else:
            max_index = 0
        return [dict(variant) for variant in LADDER[: max_index + 1]]
