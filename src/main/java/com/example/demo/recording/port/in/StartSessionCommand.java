package com.example.demo.recording.port.in;

import com.example.demo.recording.domain.RecordingSession;
import com.example.demo.recording.domain.SessionMetadata;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class StartSessionCommand {
    private final UUID orderId;
    private final UUID packerId;
    private final RecordingSession.SessionType sessionType;
    private final SessionMetadata metadata;

    public static StartSessionCommand packingSession(UUID orderId, UUID packerId) {
        return StartSessionCommand.builder()
            .orderId(orderId)
            .packerId(packerId)
            .sessionType(RecordingSession.SessionType.PACKING)
            .build();
    }

    public static StartSessionCommand qualityCheckSession(UUID orderId, UUID packerId) {
        return StartSessionCommand.builder()
            .orderId(orderId)
            .packerId(packerId)
            .sessionType(RecordingSession.SessionType.QUALITY_CHECK)
            .build();
    }
}