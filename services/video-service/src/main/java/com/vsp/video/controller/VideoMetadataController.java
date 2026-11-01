package com.vsp.video.controller;

import com.vsp.video.model.dto.VideoMetadataDTO;
import com.vsp.video.service.VideoMetadataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/videos")
public class VideoMetadataController {

    private final VideoMetadataService metadataService;

    public VideoMetadataController(VideoMetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @GetMapping
    public List<VideoMetadataDTO> listReady() {
        return metadataService.listReady();
    }

    @GetMapping("/{id}")
    public VideoMetadataDTO getById(@PathVariable Long id) {
        return metadataService.getById(id);
    }

    @GetMapping("/channel/{channelId}")
    public List<VideoMetadataDTO> listByChannel(@PathVariable Long channelId) {
        return metadataService.listByChannel(channelId);
    }

    @PatchMapping("/{id}/metadata")
    public VideoMetadataDTO updateMetadata(@PathVariable Long id, @RequestBody UpdateMetadataRequest request) {
        return metadataService.updateMetadata(id, request.title(), request.description(), request.thumbnailUrl());
    }

    public record UpdateMetadataRequest(String title, String description, String thumbnailUrl) {
    }
}
