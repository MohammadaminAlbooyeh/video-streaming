package com.vsp.video.upload;

import com.vsp.video.model.entity.UploadSession;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ChunkedUploadHandler {

    private final String uploadDir;
    private final Set<String> inFlightChunks = ConcurrentHashMap.newKeySet();

    public ChunkedUploadHandler(@Value("${upload.dir:/tmp/uploads}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String startSession(String fileName, long totalSizeBytes, long totalChunks) throws IOException {
        String sessionId = UUID.randomUUID().toString();
        Path sessionDir = Path.of(uploadDir, sessionId);
        Files.createDirectories(sessionDir);
        Path lockFile = sessionDir.resolve(".upload.lock");
        if (!Files.exists(lockFile)) {
            Files.createFile(lockFile);
        }
        return sessionId;
    }

    public boolean appendChunk(UploadSession session, long chunkIndex, MultipartFile chunk) throws IOException {
        String key = session.getSessionId() + ":" + chunkIndex;
        if (!inFlightChunks.add(key)) {
            return false;
        }
        try {
            Path chunkPath = Path.of(uploadDir, session.getSessionId(), "chunk_" + chunkIndex);
            chunk.transferTo(chunkPath.toAbsolutePath());
            return true;
        } finally {
            inFlightChunks.remove(key);
        }
    }

    public Path assemble(UploadSession session) throws IOException {
        Path assembled = Path.of(uploadDir, session.getSessionId(), "assembled_" + session.getFileName());
        Files.deleteIfExists(assembled);
        Files.createFile(assembled);
        for (long i = 0; i < session.getTotalChunks(); i++) {
            Path chunkPath = Path.of(uploadDir, session.getSessionId(), "chunk_" + i);
            if (!Files.exists(chunkPath)) {
                throw new IOException("Missing chunk " + i + " for session " + session.getSessionId());
            }
            Files.write(assembled, Files.readAllBytes(chunkPath), StandardOpenOption.APPEND);
        }
        return assembled;
    }

    public void cleanup(String sessionId) throws IOException {
        Path sessionDir = Path.of(uploadDir, sessionId);
        if (Files.exists(sessionDir)) {
            try (var walk = Files.walk(sessionDir)) {
                walk.sorted(java.util.Comparator.reverseOrder())
                        .forEach(p -> {
                            try {
                                Files.deleteIfExists(p);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
            }
        }
    }
}
