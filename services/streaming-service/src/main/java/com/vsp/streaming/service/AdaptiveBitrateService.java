package com.vsp.streaming.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdaptiveBitrateService {

    private static final List<VariantProfile> LADDER = List.of(
            new VariantProfile("144p", 256, 144, 400),
            new VariantProfile("240p", 426, 240, 700),
            new VariantProfile("360p", 640, 360, 1200),
            new VariantProfile("480p", 854, 480, 2000),
            new VariantProfile("720p", 1280, 720, 3500),
            new VariantProfile("1080p", 1920, 1080, 6000),
            new VariantProfile("4K", 3840, 2160, 16000)
    );

    public VariantProfile selectForBandwidth(long bandwidthKbps) {
        VariantProfile selected = LADDER.get(0);
        for (VariantProfile profile : LADDER) {
            if (profile.bitrateKbps() <= bandwidthKbps) {
                selected = profile;
            } else {
                break;
            }
        }
        return selected;
    }

    public List<VariantProfile> ladder() {
        return LADDER;
    }

    public record VariantProfile(String label, int width, int height, long bitrateKbps) {
    }
}
