package com.example.demo.recording.application;

import com.example.demo.recording.domain.RecordingSession;
import com.example.demo.recording.port.in.RecordingSessionUseCase;
import com.example.demo.recording.port.in.StartSessionCommand;
import com.example.demo.recording.port.out.RecordingSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RecordingSessionService implements RecordingSessionUseCase {

    private final RecordingSessionRepository sessionRepository;

    @Override
    public RecordingSession startSession(StartSessionCommand command) {
        log.info("Starting recording session for order {} with packer {}",
                command.getOrderId(), command.getPackerId());

        // Check if there's already an active session for this packer
        List<RecordingSession> activeSessions = sessionRepository.findActiveSessionsByPacker(command.getPackerId());
        if (!activeSessions.isEmpty()) {
            throw new IllegalStateException("Packer already has an active recording session");
        }

        RecordingSession session = RecordingSession.builder()
                .id(UUID.randomUUID())
                .orderId(command.getOrderId())
                .packerId(command.getPackerId())
                .sessionType(command.getSessionType())
                .status(RecordingSession.SessionStatus.RECORDING)
                .startedAt(Instant.now())
                .metadata(command.getMetadata() != null ? command.getMetadata().toMap() : null)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        session = sessionRepository.save(session);
        log.info("Recording session started with ID: {}", session.getId());

        return session;
    }

    @Override
    public RecordingSession endSession(UUID sessionId) {
        log.info("Ending recording session: {}", sessionId);

        RecordingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Recording session not found: " + sessionId));

        if (session.getStatus() != RecordingSession.SessionStatus.RECORDING) {
            throw new IllegalStateException("Cannot end session that is not currently recording");
        }

        session.endSession();
        session = sessionRepository.save(session);

        log.info("Recording session ended: {}", sessionId);
        return session;
    }

    @Override
    @Transactional(readOnly = true)
    public RecordingSession getSession(UUID sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Recording session not found: " + sessionId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecordingSession> getSessionsByOrder(UUID orderId) {
        return sessionRepository.findByOrderId(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecordingSession> getSessionsByPacker(UUID packerId) {
        return sessionRepository.findByPackerId(packerId);
    }

    @Override
    public void markSessionCompleted(UUID sessionId) {
        log.info("Marking session as completed: {}", sessionId);

        RecordingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Recording session not found: " + sessionId));

        session.markCompleted();
        sessionRepository.save(session);

        log.info("Session marked as completed: {}", sessionId);
    }

    @Override
    public void markSessionFailed(UUID sessionId, String reason) {
        log.warn("Marking session as failed: {} - Reason: {}", sessionId, reason);

        RecordingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Recording session not found: " + sessionId));

        session.markFailed();
        sessionRepository.save(session);

        log.warn("Session marked as failed: {}", sessionId);
    }
}