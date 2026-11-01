package com.vsp.video.repository;

import com.vsp.video.model.entity.UploadSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UploadSessionRepository extends JpaRepository<UploadSession, Long> {

    Optional<UploadSession> findBySessionId(String sessionId);
}
