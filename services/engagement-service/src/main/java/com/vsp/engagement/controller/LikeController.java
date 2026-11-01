package com.vsp.engagement.controller;

import com.vsp.engagement.service.LikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/likes")
public class LikeController {

    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping("/toggle")
    public ResponseEntity<Map<String, Object>> toggle(@RequestParam Long videoId, @RequestParam Long userId) {
        boolean liked = likeService.toggleLike(videoId, userId);
        return ResponseEntity.ok(Map.of(
                "liked", liked,
                "count", likeService.countLikes(videoId)
        ));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> status(@RequestParam Long videoId, @RequestParam Long userId) {
        return ResponseEntity.ok(Map.of(
                "liked", likeService.hasLiked(videoId, userId),
                "count", likeService.countLikes(videoId)
        ));
    }
}
