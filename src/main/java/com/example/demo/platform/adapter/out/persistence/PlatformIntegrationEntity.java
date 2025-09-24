package com.example.demo.platform.adapter.out.persistence;

import com.example.demo.platform.domain.PlatformIntegration;
import com.example.demo.shared.domain.Platform;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "platform_integrations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformIntegrationEntity {
    @Id
    private UUID id;

    @Column(name = "org_id", nullable = false)
    private UUID orgId;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false)
    private Platform platform;

    @Column(name = "seller_id")
    private String sellerId;

    @Column(name = "api_credentials", columnDefinition = "TEXT")
    private String apiCredentials;

    @Column(name = "webhook_secret")
    private String webhookSecret;

    @Column(name = "is_active")
    private boolean active;

    @Column(name = "last_sync_at")
    private Instant lastSyncAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status")
    private PlatformIntegration.SyncStatus syncStatus;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // OAuth fields
    @Column(name = "oauth_state")
    private String oauthState;

    @Column(name = "auth_code")
    private String authCode;

    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken;

    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name = "token_expires_at")
    private Instant tokenExpiresAt;

    @Column(name = "shop_id")
    private String shopId;

    @Column(name = "shop_name")
    private String shopName;

    @Enumerated(EnumType.STRING)
    @Column(name = "authorization_status")
    private PlatformIntegration.AuthorizationStatus authorizationStatus;

    @Column(name = "authorized_at")
    private Instant authorizedAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}