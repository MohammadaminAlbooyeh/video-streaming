package com.vsp.video.repository;

import com.vsp.video.model.entity.Video;
import com.vsp.video.model.enums.VideoStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoRepository extends JpaRepository<Video, Long> {

    List<Video> findByChannelIdOrderByCreatedAtDesc(Long channelId);

    List<Video> findByStatusOrderByCreatedAtDesc(VideoStatus status);

    List<Video> findByUploaderIdOrderByCreatedAtDesc(Long uploaderId);
}
