package com.example.demo.recording.adapter.out.persistence;

import com.example.demo.recording.domain.VideoShare;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "video_shares")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoShareEntity {
    @Id
    private UUID id;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "share_type", nullable = false)
    private VideoShare.ShareType shareType;

    @Column(name = "recipient")
    private String recipient;

    @Column(name = "share_token", nullable = false, unique = true)
    private String shareToken;

    @Column(name = "access_count", nullable = false)
    private int accessCount;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "last_accessed_at")
    private Instant lastAccessedAt;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (accessCount == 0) {
            accessCount = 0;
        }
    }
}