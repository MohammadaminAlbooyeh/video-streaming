package com.vsp.video.controller;

import com.vsp.video.model.entity.Video;
import com.vsp.video.service.PlaylistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @GetMapping
    public ResponseEntity<List<Video>> getPlaylistVideos(@RequestParam("videoIds") String videoIds) {
        List<Long> ids = Arrays.stream(videoIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .toList();
        return ResponseEntity.ok(playlistService.getPlaylistVideos(ids));
    }

    @GetMapping("/channel/{channelId}")
    public ResponseEntity<List<Video>> getChannelPlaylist(@PathVariable Long channelId) {
        return ResponseEntity.ok(playlistService.getChannelPlaylist(channelId));
    }
}
