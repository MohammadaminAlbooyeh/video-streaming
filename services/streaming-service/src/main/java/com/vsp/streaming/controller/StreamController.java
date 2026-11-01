package com.vsp.streaming.controller;

import com.vsp.streaming.model.dto.PlaybackTokenDTO;
import com.vsp.streaming.model.dto.StreamManifestDTO;
import com.vsp.streaming.service.AdaptiveBitrateService;
import com.vsp.streaming.service.PlaybackSessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stream")
public class StreamController {

    private final PlaybackSessionService playbackSessionService;

    public StreamController(PlaybackSessionService playbackSessionService) {
        this.playbackSessionService = playbackSessionService;
    }

    @GetMapping("/token")
    public ResponseEntity<PlaybackTokenDTO> getPlaybackToken(@RequestParam("videoId") Long videoId,
                                                             @RequestHeader(name = "X-Bandwidth-Kbps", defaultValue = "2000") long bandwidthKbps) {
        return ResponseEntity.ok(playbackSessionService.createPlaybackToken(videoId, bandwidthKbps));
    }

    @GetMapping("/watch/{videoId}")
    public ResponseEntity<StreamManifestDTO> watch(@PathVariable Long videoId,
                                                   @RequestParam(defaultValue = "hls") String protocol) {
        return ResponseEntity.ok(playbackSessionService.buildManifest(String.valueOf(videoId), protocol));
    }

    @GetMapping("/{videoId}/watch-events")
    public ResponseEntity<Void> reportWatch(@PathVariable Long videoId,
                                            @RequestParam Long userId,
                                            @RequestParam long watchSeconds) {
        playbackSessionService.recordViewEvent(videoId, userId, watchSeconds);
        return ResponseEntity.ok().build();
    }
}
