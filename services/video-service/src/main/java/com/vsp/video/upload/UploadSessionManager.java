package com.vsp.video.upload;

import com.vsp.video.model.entity.UploadSession;
import com.vsp.video.model.entity.Video;
import com.vsp.video.repository.UploadSessionRepository;

import java.io.IOException;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class UploadSessionManager {

    private final UploadSessionRepository repository;
    private final ChunkedUploadHandler chunkedUploadHandler;

    public UploadSessionManager(UploadSessionRepository repository, ChunkedUploadHandler chunkedUploadHandler) {
        this.repository = repository;
        this.chunkedUploadHandler = chunkedUploadHandler;
    }

    public UploadSession createSession(Video video, String fileName, long totalSizeBytes,
                                       long totalChunks, long chunkSizeBytes) throws IOException {
        String sessionId = chunkedUploadHandler.startSession(fileName, totalSizeBytes, totalChunks);

        UploadSession session = new UploadSession();
        session.setSessionId(sessionId);
        session.setVideoId(video.getId());
        session.setFileName(fileName);
        session.setTotalChunks(totalChunks);
        session.setChunkSizeBytes(chunkSizeBytes);
        session.setTotalSizeBytes(totalSizeBytes);
        session.setUploadedChunks(0);
        session.setStatus("ACTIVE");
        return repository.save(session);
    }

    public UploadSession getSession(String sessionId) {
        return repository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Upload session not found: " + sessionId));
    }

    public UploadSession markChunkUploaded(String sessionId) {
        UploadSession session = getSession(sessionId);
        session.setUploadedChunks(session.getUploadedChunks() + 1);
        if (session.getUploadedChunks() >= session.getTotalChunks()) {
            session.setStatus("COMPLETE");
        }
        return repository.save(session);
    }

    public boolean isComplete(UploadSession session) {
        return session.getUploadedChunks() >= session.getTotalChunks();
    }

    public String newUploadId() {
        return UUID.randomUUID().toString();
    }

    public void expire(String sessionId) throws IOException {
        chunkedUploadHandler.cleanup(sessionId);
        UploadSession session = getSession(sessionId);
        session.setStatus("EXPIRED");
        repository.save(session);
    }
}
