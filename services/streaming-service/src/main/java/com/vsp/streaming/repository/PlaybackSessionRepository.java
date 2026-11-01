package com.vsp.streaming.repository;

import com.vsp.streaming.model.dto.PlaybackTokenDTO;

import java.time.Instant;
import java.util.Optional;

/**
 * Repository contract for playback session tokens.
 * Backed by Redis via StringRedisTemplate in production.
 */
public interface PlaybackSessionRepository {

    PlaybackTokenDTO save(PlaybackTokenDTO token);

    Optional<PlaybackTokenDTO> findById(String token);

    void deleteById(String token);

    void deleteExpiredBefore(Instant now);
}
