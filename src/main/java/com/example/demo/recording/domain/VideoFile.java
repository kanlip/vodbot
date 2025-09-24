package com.example.demo.recording.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoFile {
    private UUID id;
    private UUID sessionId;
    private String fileName;
    private String s3Bucket;
    private String s3Key;
    private Long fileSizeBytes;
    private String contentType;
    private Integer durationSeconds;
    private String resolution;
    private VideoStatus status;
    private String publicUrl;
    private Instant uploadedAt;
    private Instant processedAt;
    private Instant deletedAt;
    private Instant createdAt;
    private Map<String, Object> metadata;

    public enum VideoStatus {
        UPLOADING,
        UPLOADED,
        PROCESSING,
        READY,
        FAILED,
        DELETED
    }

    public String getS3Url() {
        return String.format("s3://%s/%s", s3Bucket, s3Key);
    }

    public boolean isReady() {
        return status == VideoStatus.READY;
    }

    public boolean isDeleted() {
        return status == VideoStatus.DELETED;
    }

    public void markAsUploaded() {
        this.status = VideoStatus.UPLOADED;
        this.uploadedAt = Instant.now();
    }

    public void markProcessingStarted() {
        this.status = VideoStatus.PROCESSING;
    }

    public void markAsReady() {
        this.status = VideoStatus.READY;
        this.processedAt = Instant.now();
    }

    public void markAsFailed() {
        this.status = VideoStatus.FAILED;
    }

    public void markAsDeleted() {
        this.status = VideoStatus.DELETED;
        this.deletedAt = Instant.now();
    }
}