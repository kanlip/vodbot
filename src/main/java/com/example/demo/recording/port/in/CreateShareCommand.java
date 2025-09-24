package com.example.demo.recording.port.in;

import com.example.demo.recording.domain.VideoShare;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class CreateShareCommand {
    private final UUID sessionId;
    private final VideoShare.ShareType shareType;
    private final String recipient;
    private final Instant expiresAt;

    public static CreateShareCommand customerLink(UUID sessionId, Instant expiresAt) {
        return CreateShareCommand.builder()
            .sessionId(sessionId)
            .shareType(VideoShare.ShareType.CUSTOMER_LINK)
            .expiresAt(expiresAt)
            .build();
    }

    public static CreateShareCommand email(UUID sessionId, String email, Instant expiresAt) {
        return CreateShareCommand.builder()
            .sessionId(sessionId)
            .shareType(VideoShare.ShareType.EMAIL)
            .recipient(email)
            .expiresAt(expiresAt)
            .build();
    }
}