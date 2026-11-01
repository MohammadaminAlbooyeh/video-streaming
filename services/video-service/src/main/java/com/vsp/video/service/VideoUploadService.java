package com.vsp.video.service;

import com.vsp.video.messaging.VideoUploadedEventProducer;
import com.vsp.video.model.dto.VideoUploadRequest;
import com.vsp.video.model.entity.UploadSession;
import com.vsp.video.model.entity.Video;
import com.vsp.video.model.enums.VideoStatus;
import com.vsp.video.repository.UploadSessionRepository;
import com.vsp.video.repository.VideoRepository;
import com.vsp.video.upload.ChunkedUploadHandler;
import com.vsp.video.upload.UploadSessionManager;
import com.vsp.video.upload.VideoValidationService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class VideoUploadService {

    private final VideoRepository videoRepository;
    private final UploadSessionManager sessionManager;
    private final ChunkedUploadHandler chunkedUploadHandler;
    private final VideoValidationService validationService;
    private final VideoUploadedEventProducer eventProducer;

    public VideoUploadService(VideoRepository videoRepository,
                              UploadSessionManager sessionManager,
                              ChunkedUploadHandler chunkedUploadHandler,
                              VideoValidationService validationService,
                              VideoUploadedEventProducer eventProducer) {
        this.videoRepository = videoRepository;
        this.sessionManager = sessionManager;
        this.chunkedUploadHandler = chunkedUploadHandler;
        this.validationService = validationService;
        this.eventProducer = eventProducer;
    }

    public Video initializeUpload(VideoUploadRequest request, Long uploaderId) {
        validationService.validateSize(request.fileSizeBytes());
        validationService.validateMimeType(new SimpleMultipartFile(request.mimeType()));

        Video video = new Video();
        video.setUploaderId(uploaderId);
        video.setChannelId(request.channelId());
        video.setTitle(request.title());
        video.setDescription(request.description());
        video.setMimeType(request.mimeType());
        video.setFileSizeBytes(request.fileSizeBytes());
        video.setStatus(VideoStatus.UPLOADING);
        return videoRepository.save(video);
    }

    public Video getVideo(Long videoId) {
        return videoRepository.findById(videoId)
                .orElseThrow(() -> new IllegalArgumentException("Video not found: " + videoId));
    }

    public UploadSession createSession(Video video, String fileName, long totalChunks, long chunkSizeBytes) throws IOException {
        return sessionManager.createSession(video, fileName, video.getFileSizeBytes(), totalChunks, chunkSizeBytes);
    }

    public UploadSession uploadChunk(String sessionId, long chunkIndex, MultipartFile chunk) throws IOException {
        validationService.validateChunk(chunk);
        UploadSession session = sessionManager.getSession(sessionId);
        chunkedUploadHandler.appendChunk(session, chunkIndex, chunk);
        return sessionManager.markChunkUploaded(sessionId);
    }

    public Video completeUpload(String sessionId, String destinationBucket) throws IOException {
        UploadSession session = sessionManager.getSession(sessionId);
        if (!sessionManager.isComplete(session)) {
            throw new IllegalStateException("Upload not complete");
        }

        Path assembled = chunkedUploadHandler.assemble(session);
        Video video = videoRepository.findById(session.getVideoId())
                .orElseThrow(() -> new IllegalArgumentException("Video not found"));

        String objectKey = "videos/" + video.getId() + "/source/" + session.getFileName();
        uploadToObjectStorage(assembled, objectKey, destinationBucket);

        video.setSourceObjectKey(objectKey);
        video.setStatus(VideoStatus.PROCESSING);
        videoRepository.save(video);

        eventProducer.publishUploaded(video);
        chunkedUploadHandler.cleanup(sessionId);
        return video;
    }

    private void uploadToObjectStorage(Path source, String objectKey, String bucket) throws IOException {
        Path destination = Path.of("/tmp", bucket, objectKey);
        Files.createDirectories(destination.getParent());
        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
    }

    private record SimpleMultipartFile(String contentType) implements MultipartFile {
        @Override
        public String getName() {
            return "";
        }

        @Override
        public String getOriginalFilename() {
            return "";
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public long getSize() {
            return 0;
        }

        @Override
        public byte[] getBytes() {
            return new byte[0];
        }

        @Override
        public java.io.InputStream getInputStream() {
            return java.io.InputStream.nullInputStream();
        }

        @Override
        public void transferTo(java.io.File dest) throws IOException {
        }

        @Override
        public void transferTo(Path dest) throws IOException {
        }
    }
}
