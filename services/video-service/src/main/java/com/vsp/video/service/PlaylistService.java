package com.vsp.video.service;

import com.vsp.video.model.entity.Video;
import com.vsp.video.repository.VideoRepository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class PlaylistService {

    private final VideoRepository videoRepository;

    public PlaylistService(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    public List<Video> getPlaylistVideos(List<Long> videoIds) {
        if (videoIds == null || videoIds.isEmpty()) {
            return List.of();
        }
        List<Video> ordered = new ArrayList<>();
        for (Long id : videoIds) {
            videoRepository.findById(id).ifPresent(ordered::add);
        }
        return ordered;
    }

    public List<Video> getChannelPlaylist(Long channelId) {
        return videoRepository.findByChannelIdOrderByCreatedAtDesc(channelId);
    }
}
