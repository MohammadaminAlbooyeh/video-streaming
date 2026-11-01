package com.vsp.streaming.delivery;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class ManifestBuilder {

    public String buildHlsMaster(List<Variant> variants) {
        StringBuilder sb = new StringBuilder();
        sb.append("#EXTM3U\n");
        sb.append("#EXT-X-VERSION:7\n");
        for (Variant v : variants) {
            sb.append("#EXT-X-STREAM-INF:BANDWIDTH=").append(v.bitrateKbps() * 1000)
                    .append(",RESOLUTION=").append(v.width()).append("x").append(v.height())
                    .append(",NAME=\"").append(v.label()).append("\"\n");
            sb.append(v.playlistUrl()).append("\n");
        }
        return sb.toString();
    }

    public String buildHlsMediaPlaylist(String variantLabel, int targetDurationSeconds, int segmentCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("#EXTM3U\n");
        sb.append("#EXT-X-VERSION:7\n");
        sb.append("#EXT-X-TARGETDURATION:").append(targetDurationSeconds).append("\n");
        sb.append("#EXT-X-MEDIA-SEQUENCE:0\n");
        for (int i = 0; i < segmentCount; i++) {
            sb.append("#EXTINF:").append(targetDurationSeconds).append(",\n");
            sb.append("seg_").append(variantLabel).append("_").append(i).append(".ts\n");
        }
        sb.append("#EXT-X-ENDLIST\n");
        return sb.toString();
    }

    public String buildDashMpd(List<Variant> variants, int segmentDurationSeconds) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<MPD xmlns=\"urn:mpeg:dash:schema:mpd:2011\" profiles=\"urn:mpeg:dash:profile:isoff-live:2011\"")
                .append(" type=\"static\" mediaPresentationDuration=\"PT").append(segmentDurationSeconds * 10).append("S\">\n");
        sb.append("  <Period>\n");
        sb.append("    <AdaptationSet mimeType=\"video/mp4\" segmentAlignment=\"true\">\n");
        for (Variant v : variants) {
            sb.append("      <Representation id=\"").append(v.label())
                    .append("\" bandwidth=\"").append(v.bitrateKbps() * 1000)
                    .append("\" width=\"").append(v.width())
                    .append("\" height=\"").append(v.height())
                    .append("\" codecs=\"avc1.64001f\">\n");
            sb.append("        <BaseURL>").append(v.playlistUrl()).append("</BaseURL>\n");
            sb.append("      </Representation>\n");
        }
        sb.append("    </AdaptationSet>\n");
        sb.append("  </Period>\n");
        sb.append("</MPD>\n");
        return sb.toString();
    }

    public record Variant(String label, int width, int height, long bitrateKbps, String playlistUrl) {
    }
}
