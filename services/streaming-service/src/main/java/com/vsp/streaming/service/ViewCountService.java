package com.vsp.streaming.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class ViewCountService {

    private static final String VIEW_COUNT_PREFIX = "views:video:";
    private static final String LIVE_VIEWERS_PREFIX = "live:video:";

    private final StringRedisTemplate redisTemplate;

    public ViewCountService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void registerView(Long videoId) {
        String key = VIEW_COUNT_PREFIX + videoId;
        redisTemplate.opsForValue().increment(key);
    }

    public long getViewCount(Long videoId) {
        String value = redisTemplate.opsForValue().get(VIEW_COUNT_PREFIX + videoId);
        return value == null ? 0 : Long.parseLong(value);
    }

    public void incrementLiveViewers(Long videoId) {
        String key = LIVE_VIEWERS_PREFIX + videoId;
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, Duration.ofMinutes(10));
    }

    public long getLiveViewers(Long videoId) {
        String value = redisTemplate.opsForValue().get(LIVE_VIEWERS_PREFIX + videoId);
        return value == null ? 0 : Long.parseLong(value);
    }

    @Scheduled(fixedDelay = 60000)
    public void flushViewCountsToDatabase() {
        // In a real deployment this batches increments into the analytics service
        // or writes through to the video-service database.
    }
}
