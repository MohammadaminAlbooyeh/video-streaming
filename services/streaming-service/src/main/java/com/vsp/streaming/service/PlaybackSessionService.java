package com.vsp.streaming.service;

import com.vsp.streaming.delivery.CdnUrlSigner;
import com.vsp.streaming.delivery.ManifestBuilder;
import com.vsp.streaming.model.dto.PlaybackTokenDTO;
import com.vsp.streaming.model.dto.StreamManifestDTO;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PlaybackSessionService {

    private final CdnUrlSigner cdnUrlSigner;
    private final ManifestBuilder manifestBuilder;
    private final AdaptiveBitrateService adaptiveBitrateService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PlaybackSessionService(CdnUrlSigner cdnUrlSigner,
                                  ManifestBuilder manifestBuilder,
                                  AdaptiveBitrateService adaptiveBitrateService,
                                  KafkaTemplate<String, Object> kafkaTemplate) {
        this.cdnUrlSigner = cdnUrlSigner;
        this.manifestBuilder = manifestBuilder;
        this.adaptiveBitrateService = adaptiveBitrateService;
        this.kafkaTemplate = kafkaTemplate;
    }

    public PlaybackTokenDTO createPlaybackToken(Long videoId, long bandwidthKbps) {
        Instant issued = Instant.now();
        Instant expires = issued.plusSeconds(3600);
        AdaptiveBitrateService.VariantProfile profile = adaptiveBitrateService.selectForBandwidth(bandwidthKbps);
        return new PlaybackTokenDTO(
                UUID.randomUUID().toString(),
                String.valueOf(videoId),
                issued,
                expires,
                profile.bitrateKbps()
        );
    }

    public StreamManifestDTO buildManifest(String videoId, String protocol) {
        String manifestPath = "videos/" + videoId + "/hls/master.m3u8";
        if ("dash".equalsIgnoreCase(protocol)) {
            manifestPath = "videos/" + videoId + "/dash/manifest.mpd";
        }

        List<ManifestBuilder.Variant> variants = adaptiveBitrateService.ladder().stream()
                .map(p -> new ManifestBuilder.Variant(
                        p.label(), p.width(), p.height(), p.bitrateKbps(),
                        cdnUrlSigner.sign("videos/" + videoId + "/hls/" + p.label() + "/index.m3u8")))
                .toList();

        String manifestUrl = cdnUrlSigner.sign(manifestPath);
        List<StreamManifestDTO.VariantDTO> variantDtos = variants.stream()
                .map(v -> new StreamManifestDTO.VariantDTO(
                        v.label(), v.width(), v.height(), v.bitrateKbps(), v.playlistUrl()))
                .toList();

        return new StreamManifestDTO(videoId, protocol, manifestUrl, variantDtos);
    }

    public void recordViewEvent(Long videoId, Long userId, long watchSeconds) {
        kafkaTemplate.send("view.events", String.valueOf(videoId),
                java.util.Map.of(
                        "videoId", videoId,
                        "userId", userId,
                        "watchSeconds", watchSeconds,
                        "timestamp", System.currentTimeMillis()
                ));
    }
}
