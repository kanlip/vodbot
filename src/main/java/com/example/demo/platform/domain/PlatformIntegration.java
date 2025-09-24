package com.example.demo.platform.domain;

import com.example.demo.shared.domain.Platform;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformIntegration {
    private UUID id;
    private UUID orgId;
    private Platform platform;
    private String sellerId;
    private String apiCredentials; // Encrypted JSON
    private String webhookSecret;
    private boolean active;
    private Instant lastSyncAt;
    private SyncStatus syncStatus;
    private Instant createdAt;
    private Instant updatedAt;

    // OAuth fields
    private String oauthState;
    private String authCode;
    private String accessToken;
    private String refreshToken;
    private Instant tokenExpiresAt;
    private String shopId;
    private String shopName;
    private AuthorizationStatus authorizationStatus;
    private Instant authorizedAt;

    public enum SyncStatus {
        NEVER_SYNCED,
        SYNCING,
        SUCCESS,
        FAILED,
        PARTIAL_SUCCESS
    }

    public enum AuthorizationStatus {
        PENDING,
        AUTHORIZED,
        EXPIRED,
        REVOKED
    }

    public boolean isAuthorized() {
        return authorizationStatus == AuthorizationStatus.AUTHORIZED &&
               accessToken != null &&
               (tokenExpiresAt == null || Instant.now().isBefore(tokenExpiresAt));
    }

    public boolean needsTokenRefresh() {
        return tokenExpiresAt != null &&
               Instant.now().isAfter(tokenExpiresAt.minusSeconds(300)) && // 5 minutes buffer
               refreshToken != null;
    }

    public void authorize(String accessToken, String refreshToken, Instant expiresAt, String shopId, String shopName) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenExpiresAt = expiresAt;
        this.shopId = shopId;
        this.shopName = shopName;
        this.authorizationStatus = AuthorizationStatus.AUTHORIZED;
        this.authorizedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void refreshToken(String newAccessToken, Instant newExpiresAt) {
        this.accessToken = newAccessToken;
        this.tokenExpiresAt = newExpiresAt;
        this.updatedAt = Instant.now();
    }

    public void revoke() {
        this.authorizationStatus = AuthorizationStatus.REVOKED;
        this.accessToken = null;
        this.refreshToken = null;
        this.tokenExpiresAt = null;
        this.updatedAt = Instant.now();
    }

    public void updateSyncStatus(SyncStatus status) {
        this.syncStatus = status;
        this.lastSyncAt = Instant.now();
        this.updatedAt = Instant.now();
    }
}