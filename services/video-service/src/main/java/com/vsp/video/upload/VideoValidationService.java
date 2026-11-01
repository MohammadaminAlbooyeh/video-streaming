package com.vsp.video.upload;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Component
public class VideoValidationService {

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "video/mp4", "video/webm", "video/quicktime", "video/x-matroska"
    );

    private final long maxFileSizeBytes;
    private final long maxDurationSeconds;
    private final int maxResolutionPx;

    public VideoValidationService(@Value("${video.max-file-size-bytes:10737418240}") long maxFileSizeBytes,
                                  @Value("${video.max-duration-seconds:43200}") long maxDurationSeconds,
                                  @Value("${video.max-resolution-px:4320}") int maxResolutionPx) {
        this.maxFileSizeBytes = maxFileSizeBytes;
        this.maxDurationSeconds = maxDurationSeconds;
        this.maxResolutionPx = maxResolutionPx;
    }

    public void validateMimeType(MultipartFile file) {
        String mime = file.getContentType();
        if (mime == null || !ALLOWED_MIME_TYPES.contains(mime)) {
            throw new IllegalArgumentException("Unsupported file type: " + mime);
        }
    }

    public void validateSize(long sizeBytes) {
        if (sizeBytes <= 0 || sizeBytes > maxFileSizeBytes) {
            throw new IllegalArgumentException("File size out of allowed range");
        }
    }

    public void validateChunk(MultipartFile chunk) {
        if (chunk.isEmpty()) {
            throw new IllegalArgumentException("Empty chunk");
        }
    }

    public void validateDuration(long durationSeconds) {
        if (durationSeconds <= 0 || durationSeconds > maxDurationSeconds) {
            throw new IllegalArgumentException("Duration out of allowed range");
        }
    }

    public void validateResolution(int heightPx) {
        if (heightPx <= 0 || heightPx > maxResolutionPx) {
            throw new IllegalArgumentException("Resolution out of allowed range");
        }
    }
}
