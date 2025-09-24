package com.example.demo.recording.adapter.out.persistence;

import com.example.demo.recording.domain.VideoFile;
import com.example.demo.recording.port.out.VideoFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VideoFileRepositoryImpl implements VideoFileRepository {

    private final VideoFileJpaRepository jpaRepository;
    private final VideoFileMapper mapper;

    @Override
    public VideoFile save(VideoFile videoFile) {
        VideoFileEntity entity = mapper.toEntity(videoFile);
        VideoFileEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<VideoFile> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<VideoFile> findBySessionId(UUID sessionId) {
        return jpaRepository.findBySessionId(sessionId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<VideoFile> findBySessionIdAndFileName(UUID sessionId, String fileName) {
        return jpaRepository.findBySessionIdAndFileName(sessionId, fileName)
                .map(mapper::toDomain);
    }

    @Override
    public List<VideoFile> findByS3Key(String s3Key) {
        return jpaRepository.findByS3Key(s3Key)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<VideoFile> findByStatus(VideoFile.VideoStatus status) {
        return jpaRepository.findByStatus(status)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteBySessionId(UUID sessionId) {
        jpaRepository.deleteBySessionId(sessionId);
    }
}