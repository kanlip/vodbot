package com.example.demo.recording.port.out;

import com.example.demo.recording.domain.RecordingSession;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecordingSessionRepository {
    RecordingSession save(RecordingSession session);
    Optional<RecordingSession> findById(UUID id);
    List<RecordingSession> findByOrderId(UUID orderId);
    List<RecordingSession> findByPackerId(UUID packerId);
    List<RecordingSession> findByStatus(RecordingSession.SessionStatus status);
    List<RecordingSession> findActiveSessionsByPacker(UUID packerId);
    void deleteById(UUID id);
    boolean existsById(UUID id);
}