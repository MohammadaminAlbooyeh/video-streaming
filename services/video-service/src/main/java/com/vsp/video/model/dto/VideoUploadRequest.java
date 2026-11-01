package com.vsp.video.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VideoUploadRequest(
        @NotBlank String title,
        String description,
        @NotNull Long uploaderId,
        @NotNull Long channelId,
        @Size(max = 255) String originalFileName,
        String mimeType,
        long fileSizeBytes,
        int totalChunks,
        int chunkSizeBytes,
        String uploadId
) {
}
