package com.example.demo.recording.port.in;

import com.example.demo.recording.domain.RecordingSession;
import java.util.List;
import java.util.UUID;

public interface RecordingSessionUseCase {
    RecordingSession startSession(StartSessionCommand command);
    RecordingSession endSession(UUID sessionId);
    RecordingSession getSession(UUID sessionId);
    List<RecordingSession> getSessionsByOrder(UUID orderId);
    List<RecordingSession> getSessionsByPacker(UUID packerId);
    void markSessionCompleted(UUID sessionId);
    void markSessionFailed(UUID sessionId, String reason);
}