package com.example.demo.recording.port.in;

import com.example.demo.recording.port.out.VideoUploadService.PresignedUploadUrl;
import com.example.demo.recording.port.out.VideoUploadService.PresignedDownloadUrl;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

public interface VideoUploadUseCase {

    /**
     * Request presigned URL for video upload
     */
    PresignedUploadUrl requestVideoUpload(VideoUploadRequest request);

    /**
     * Handle S3 upload completion notification
     */
    void handleUploadCompletion(VideoUploadCompletionEvent event);

    /**
     * Generate streaming URL for video playback
     */
    PresignedDownloadUrl generateStreamingUrl(UUID sessionId, String videoFileName);

    /**
     * Delete video file
     */
    void deleteVideo(UUID sessionId, String videoFileName);

    record VideoUploadRequest(
            UUID sessionId,
            String fileName,
            String contentType,
            Long fileSizeBytes,
            Map<String, Object> metadata
    ) {}

    record VideoUploadCompletionEvent(
            String s3Key,
            String s3Bucket,
            Long fileSizeBytes,
            String contentType,
            Map<String, Object> metadata
    ) {}
}