package com.vsp.engagement.service;

import com.vsp.engagement.model.entity.Like;
import com.vsp.engagement.repository.LikeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LikeService {

    private final LikeRepository likeRepository;

    public LikeService(LikeRepository likeRepository) {
        this.likeRepository = likeRepository;
    }

    @Transactional
    public boolean toggleLike(Long videoId, Long userId) {
        if (likeRepository.existsByVideoIdAndUserId(videoId, userId)) {
            likeRepository.deleteByVideoIdAndUserId(videoId, userId);
            return false;
        }
        Like like = new Like();
        like.setVideoId(videoId);
        like.setUserId(userId);
        likeRepository.save(like);
        return true;
    }

    public long countLikes(Long videoId) {
        return likeRepository.countByVideoId(videoId);
    }

    public boolean hasLiked(Long videoId, Long userId) {
        return likeRepository.existsByVideoIdAndUserId(videoId, userId);
    }
}
