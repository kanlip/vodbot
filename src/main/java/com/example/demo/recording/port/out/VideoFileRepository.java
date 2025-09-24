package com.example.demo.recording.port.out;

import com.example.demo.recording.domain.VideoFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VideoFileRepository {
    VideoFile save(VideoFile videoFile);
    Optional<VideoFile> findById(UUID id);
    List<VideoFile> findBySessionId(UUID sessionId);
    Optional<VideoFile> findBySessionIdAndFileName(UUID sessionId, String fileName);
    List<VideoFile> findByS3Key(String s3Key);
    List<VideoFile> findByStatus(VideoFile.VideoStatus status);
    void deleteById(UUID id);
    void deleteBySessionId(UUID sessionId);
}