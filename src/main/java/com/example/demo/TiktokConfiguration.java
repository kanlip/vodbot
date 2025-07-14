package com.example.demo;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Configuration
@RequiredArgsConstructor
@Slf4j
@ConfigurationProperties(value = "integration.tiktok")
public class TiktokConfiguration {

    private String appKey;

    private String appSecret;

    public boolean verifySignature(String signature, String payload) {
        try {
            String baseString = appKey + payload;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(baseString.getBytes(StandardCharsets.UTF_8));
            String calculatedSignature = HexFormat.of().formatHex(hash);
            return signature.equalsIgnoreCase(calculatedSignature);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error verifying signature", e);
            return false;
        }
    }
}
