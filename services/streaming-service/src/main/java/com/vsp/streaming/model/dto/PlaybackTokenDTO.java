package com.vsp.streaming.model.dto;

import java.time.Instant;

public record PlaybackTokenDTO(
        String token,
        String videoId,
        Instant issuedAt,
        Instant expiresAt,
        long maxBitrateKbps
) {
}
