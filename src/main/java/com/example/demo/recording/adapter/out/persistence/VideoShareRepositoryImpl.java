package com.example.demo.recording.adapter.out.persistence;

import com.example.demo.recording.domain.VideoShare;
import com.example.demo.recording.port.out.VideoShareRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VideoShareRepositoryImpl implements VideoShareRepository {

    private final VideoShareJpaRepository jpaRepository;
    private final VideoShareMapper mapper;

    @Override
    public VideoShare save(VideoShare videoShare) {
        VideoShareEntity entity = mapper.toEntity(videoShare);
        VideoShareEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<VideoShare> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<VideoShare> findByShareToken(String shareToken) {
        return jpaRepository.findByShareToken(shareToken)
                .map(mapper::toDomain);
    }

    @Override
    public List<VideoShare> findBySessionId(UUID sessionId) {
        return jpaRepository.findBySessionId(sessionId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<VideoShare> findActiveBySessionId(UUID sessionId) {
        return jpaRepository.findActiveBySessionId(sessionId, Instant.now())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteExpiredShares() {
        jpaRepository.deleteExpiredShares(Instant.now());
    }
}