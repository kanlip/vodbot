package com.example.demo.recording.domain;

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
public class VideoShare {
    private UUID id;
    private UUID sessionId;
    private ShareType shareType;
    private String recipient;
    private String shareToken;
    private int accessCount;
    private Instant expiresAt;
    private Instant sentAt;
    private Instant lastAccessedAt;
    private boolean active;
    private Instant createdAt;

    public enum ShareType {
        CUSTOMER_LINK,
        EMAIL,
        SMS,
        PLATFORM_MESSAGE
    }

    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    public boolean isActive() {
        return active && !isExpired();
    }

    public void recordAccess() {
        this.accessCount++;
        this.lastAccessedAt = Instant.now();
    }

    public void deactivate() {
        this.active = false;
    }

    public void markSent() {
        this.sentAt = Instant.now();
    }

    public String generateShareUrl(String baseUrl) {
        return String.format("%s/shared-video/%s", baseUrl, shareToken);
    }
}