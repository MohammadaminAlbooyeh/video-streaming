package com.vsp.engagement.repository;

import com.vsp.engagement.model.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByVideoIdAndUserId(Long videoId, Long userId);

    long countByVideoId(Long videoId);

    boolean existsByVideoIdAndUserId(Long videoId, Long userId);

    void deleteByVideoIdAndUserId(Long videoId, Long userId);
}
