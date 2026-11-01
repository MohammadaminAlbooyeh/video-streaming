package com.vsp.video.service;

import com.vsp.video.model.dto.VideoMetadataDTO;
import com.vsp.video.model.entity.Video;
import com.vsp.video.model.enums.VideoStatus;
import com.vsp.video.repository.VideoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VideoMetadataService {

    private final VideoRepository videoRepository;

    public VideoMetadataService(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    public VideoMetadataDTO getById(Long id) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Video not found: " + id));
        return toDto(video);
    }

    public List<VideoMetadataDTO> listByChannel(Long channelId) {
        return videoRepository.findByChannelIdOrderByCreatedAtDesc(channelId).stream()
                .map(this::toDto)
                .toList();
    }

    public List<VideoMetadataDTO> listReady() {
        return videoRepository.findByStatusOrderByCreatedAtDesc(VideoStatus.READY).stream()
                .map(this::toDto)
                .toList();
    }

    public VideoMetadataDTO updateMetadata(Long id, String title, String description, String thumbnailUrl) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Video not found: " + id));
        if (title != null) {
            video.setTitle(title);
        }
        if (description != null) {
            video.setDescription(description);
        }
        if (thumbnailUrl != null) {
            video.setThumbnailUrl(thumbnailUrl);
        }
        return toDto(videoRepository.save(video));
    }

    public void markReady(Long id, long durationSeconds) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Video not found: " + id));
        video.setStatus(VideoStatus.READY);
        video.setDurationSeconds(durationSeconds);
        videoRepository.save(video);
    }

    public void markFailed(Long id, String reason) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Video not found: " + id));
        video.setStatus(VideoStatus.FAILED);
        video.setFailureReason(reason);
        videoRepository.save(video);
    }

    private VideoMetadataDTO toDto(Video video) {
        return new VideoMetadataDTO(
                video.getId(),
                video.getTitle(),
                video.getDescription(),
                video.getChannelId(),
                video.getThumbnailUrl(),
                video.getDurationSeconds(),
                video.getStatus(),
                video.getViewCount(),
                video.getLikeCount(),
                video.getCreatedAt()
        );
    }
}
