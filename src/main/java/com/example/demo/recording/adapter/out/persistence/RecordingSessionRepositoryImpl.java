package com.example.demo.recording.adapter.out.persistence;

import com.example.demo.recording.domain.RecordingSession;
import com.example.demo.recording.port.out.RecordingSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RecordingSessionRepositoryImpl implements RecordingSessionRepository {

    private final RecordingSessionJpaRepository jpaRepository;
    private final RecordingSessionMapper mapper;

    @Override
    public RecordingSession save(RecordingSession session) {
        RecordingSessionEntity entity = mapper.toEntity(session);
        RecordingSessionEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<RecordingSession> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<RecordingSession> findByOrderId(UUID orderId) {
        return jpaRepository.findByOrderId(orderId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<RecordingSession> findByPackerId(UUID packerId) {
        return jpaRepository.findByPackerId(packerId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<RecordingSession> findByStatus(RecordingSession.SessionStatus status) {
        return jpaRepository.findByStatus(status)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<RecordingSession> findActiveSessionsByPacker(UUID packerId) {
        return jpaRepository.findActiveSessionsByPacker(packerId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }
}