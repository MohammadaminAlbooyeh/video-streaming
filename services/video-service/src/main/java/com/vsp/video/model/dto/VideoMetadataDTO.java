package com.vsp.video.model.dto;

import com.vsp.video.model.enums.VideoStatus;

import java.time.Instant;

public record VideoMetadataDTO(
        Long id,
        String title,
        String description,
        Long channelId,
        String thumbnailUrl,
        long durationSeconds,
        VideoStatus status,
        long viewCount,
        long likeCount,
        Instant createdAt
) {
}
