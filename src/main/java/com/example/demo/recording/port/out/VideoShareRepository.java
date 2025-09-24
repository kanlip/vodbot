package com.example.demo.recording.port.out;

import com.example.demo.recording.domain.VideoShare;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VideoShareRepository {
    VideoShare save(VideoShare videoShare);
    Optional<VideoShare> findById(UUID id);
    Optional<VideoShare> findByShareToken(String shareToken);
    List<VideoShare> findBySessionId(UUID sessionId);
    List<VideoShare> findActiveBySessionId(UUID sessionId);
    void deleteById(UUID id);
    void deleteExpiredShares();
}