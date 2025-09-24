package com.example.demo.recording.adapter.out.persistence;

import com.example.demo.recording.domain.VideoFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VideoFileJpaRepository extends JpaRepository<VideoFileEntity, UUID> {
    List<VideoFileEntity> findBySessionId(UUID sessionId);
    Optional<VideoFileEntity> findBySessionIdAndFileName(UUID sessionId, String fileName);
    List<VideoFileEntity> findByS3Key(String s3Key);
    List<VideoFileEntity> findByStatus(VideoFile.VideoStatus status);
    void deleteBySessionId(UUID sessionId);
}