package com.vsp.streaming.model.dto;

import java.util.List;

public record StreamManifestDTO(
        String videoId,
        String protocol,
        String manifestUrl,
        List<VariantDTO> variants
) {

    public record VariantDTO(
            String label,
            int width,
            int height,
            long bitrateKbps,
            String url
    ) {
    }
}
