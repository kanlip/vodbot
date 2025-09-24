package com.example.demo.platform.adapter.in.web;

import com.example.demo.platform.domain.PlatformIntegration;
import com.example.demo.shared.domain.Platform;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class PlatformIntegrationResponse {
    private final UUID id;
    private final Platform platform;
    private final String sellerId;
    private final String shopId;
    private final String shopName;
    private final boolean active;
    private final PlatformIntegration.AuthorizationStatus authorizationStatus;
    private final PlatformIntegration.SyncStatus syncStatus;
    private final Instant lastSyncAt;
    private final Instant authorizedAt;
    private final boolean tokenExpired;

    public static PlatformIntegrationResponse from(PlatformIntegration integration) {
        return PlatformIntegrationResponse.builder()
                .id(integration.getId())
                .platform(integration.getPlatform())
                .sellerId(integration.getSellerId())
                .shopId(integration.getShopId())
                .shopName(integration.getShopName())
                .active(integration.isActive())
                .authorizationStatus(integration.getAuthorizationStatus())
                .syncStatus(integration.getSyncStatus())
                .lastSyncAt(integration.getLastSyncAt())
                .authorizedAt(integration.getAuthorizedAt())
                .tokenExpired(integration.needsTokenRefresh())
                .build();
    }
}