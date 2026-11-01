package com.vsp.video.messaging;

import com.vsp.video.model.entity.Video;
import com.vsp.video.model.enums.VideoStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class VideoUploadedEventProducer {

    private static final Logger log = LoggerFactory.getLogger(VideoUploadedEventProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public VideoUploadedEventProducer(KafkaTemplate<String, Object> kafkaTemplate,
                                      @Value("${kafka.topics.video-uploaded:video.uploaded}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publishUploaded(Video video) {
        Map<String, Object> payload = Map.of(
                "videoId", video.getId(),
                "uploaderId", video.getUploaderId(),
                "channelId", video.getChannelId(),
                "title", video.getTitle(),
                "mimeType", video.getMimeType(),
                "sourceObjectKey", video.getSourceObjectKey(),
                "fileSizeBytes", video.getFileSizeBytes(),
                "status", VideoStatus.PROCESSING.name(),
                "timestamp", System.currentTimeMillis()
        );
        log.info("Publishing video.uploaded event for videoId={}", video.getId());
        kafkaTemplate.send(topic, String.valueOf(video.getId()), payload);
    }
}
