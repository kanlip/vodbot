package com.example.demo.recording.port.out;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

public interface VideoUploadService {

    /**
     * Generate presigned URL for video upload
     */
    PresignedUploadUrl generatePresignedUploadUrl(UUID sessionId, String fileName, String contentType, Duration expiration);

    /**
     * Process completed video upload
     */
    void processUploadCompletion(String s3Key, Map<String, Object> metadata);

    /**
     * Generate presigned URL for video download/streaming
     */
    PresignedDownloadUrl generatePresignedDownloadUrl(String s3Key, Duration expiration);

    /**
     * Delete video file from S3
     */
    void deleteVideo(String s3Key);

    record PresignedUploadUrl(
            String uploadUrl,
            String s3Key,
            Map<String, String> formFields,
            Duration expiration
    ) {}

    record PresignedDownloadUrl(
            String downloadUrl,
            Duration expiration
    ) {}
}