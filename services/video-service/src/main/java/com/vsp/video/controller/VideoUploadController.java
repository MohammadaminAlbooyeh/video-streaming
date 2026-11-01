package com.vsp.video.controller;

import com.vsp.video.model.dto.VideoUploadRequest;
import com.vsp.video.model.entity.UploadSession;
import com.vsp.video.model.entity.Video;
import com.vsp.video.service.VideoUploadService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/videos")
public class VideoUploadController {

    private final VideoUploadService uploadService;

    public VideoUploadController(VideoUploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping("/init")
    public ResponseEntity<Map<String, Object>> initializeUpload(@Valid @RequestBody VideoUploadRequest request) {
        Video video = uploadService.initializeUpload(request, request.uploaderId());
        return ResponseEntity.ok(Map.of(
                "videoId", video.getId(),
                "message", "Metadata registered, ready for chunked upload"
        ));
    }

    @PostMapping("/{videoId}/upload-session")
    public ResponseEntity<Map<String, Object>> createSession(@PathVariable Long videoId,
                                                             @RequestBody SessionRequest request) throws IOException {
        Video video = uploadService.getVideo(videoId);
        UploadSession session = uploadService.createSession(video, request.fileName(),
                request.totalChunks(), request.chunkSizeBytes());
        return ResponseEntity.ok(Map.of(
                "sessionId", session.getSessionId(),
                "totalChunks", session.getTotalChunks(),
                "chunkSizeBytes", session.getChunkSizeBytes()
        ));
    }

    @PostMapping("/upload/{sessionId}/chunk")
    public ResponseEntity<Map<String, Object>> uploadChunk(@PathVariable String sessionId,
                                                           @RequestParam("chunkIndex") long chunkIndex,
                                                           @RequestPart("chunk") MultipartFile chunk) throws IOException {
        UploadSession session = uploadService.uploadChunk(sessionId, chunkIndex, chunk);
        return ResponseEntity.ok(Map.of(
                "sessionId", sessionId,
                "uploadedChunks", session.getUploadedChunks(),
                "totalChunks", session.getTotalChunks(),
                "status", session.getStatus()
        ));
    }

    @PostMapping("/upload/{sessionId}/complete")
    public ResponseEntity<Map<String, Object>> completeUpload(@PathVariable String sessionId,
                                                              @RequestParam(defaultValue = "vsp-media") String bucket) throws IOException {
        Video video = uploadService.completeUpload(sessionId, bucket);
        return ResponseEntity.ok(Map.of(
                "videoId", video.getId(),
                "status", video.getStatus().name(),
                "sourceObjectKey", video.getSourceObjectKey()
        ));
    }

    public record SessionRequest(String fileName, long totalChunks, long chunkSizeBytes) {
    }
}
