package com.vsp.streaming.delivery;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;

@Component
public class CdnUrlSigner {

    private final String secret;
    private final String cdnBaseUrl;
    private final long ttlSeconds;

    public CdnUrlSigner(@Value("${cdn.signing-secret}") String secret,
                        @Value("${cdn.base-url:http://localhost:9000}") String cdnBaseUrl,
                        @Value("${cdn.url-ttl-seconds:3600}") long ttlSeconds) {
        this.secret = secret;
        this.cdnBaseUrl = cdnBaseUrl;
        this.ttlSeconds = ttlSeconds;
    }

    public String sign(String objectPath) {
        long expiry = Instant.now().getEpochSecond() + ttlSeconds;
        String path = "/" + objectPath;
        String signature = hmac(path + ":" + expiry);
        return cdnBaseUrl + path + "?expires=" + expiry + "&signature=" + signature;
    }

    public boolean verify(String objectPath, long expiry, String signature) {
        if (expiry < Instant.now().getEpochSecond()) {
            return false;
        }
        String expected = hmac("/" + objectPath + ":" + expiry);
        return constantTimeEquals(expected, signature);
    }

    private String hmac(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to sign URL", e);
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
