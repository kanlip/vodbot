package com.example.demo.platform.port.in;

import com.example.demo.shared.domain.Platform;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class InitiateOAuthCommand {
    private final UUID orgId;
    private final Platform platform;
    private final String sellerId;
    private final String redirectUrl;
    private final String webhookSecret;
}