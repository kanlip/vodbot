package com.example.demo.recording.application;

import com.example.demo.recording.domain.RecordingSession;
import com.example.demo.recording.domain.VideoFile;
import com.example.demo.recording.port.in.VideoUploadUseCase;
import com.example.demo.recording.port.out.RecordingSessionRepository;
import com.example.demo.recording.port.out.VideoFileRepository;
import com.example.demo.recording.port.out.VideoUploadService;
import com.example.demo.recording.port.out.VideoUploadService.PresignedUploadUrl;
import com.example.demo.recording.port.out.VideoUploadService.PresignedDownloadUrl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VideoUploadApplicationService implements VideoUploadUseCase {

    private final com.example.demo.recording.port.out.VideoUploadService s3VideoUploadService;
    private final RecordingSessionRepository sessionRepository;
    private final VideoFileRepository videoFileRepository;

    private static final Duration DEFAULT_UPLOAD_EXPIRATION = Duration.ofHours(2);
    private static final Duration DEFAULT_DOWNLOAD_EXPIRATION = Duration.ofHours(1);
    private static final long MAX_VIDEO_SIZE_BYTES = 5L * 1024 * 1024 * 1024; // 5GB

    @Override
    public PresignedUploadUrl requestVideoUpload(VideoUploadRequest request) {
        log.info("Processing video upload request for session: {} file: {}",
                request.sessionId(), request.fileName());

        // Validate session exists and is in valid state
        RecordingSession session = sessionRepository.findById(request.sessionId())
                .orElseThrow(() -> new IllegalArgumentException("Recording session not found: " + request.sessionId()));

        if (!isValidUploadState(session.getStatus())) {
            throw new IllegalStateException("Cannot upload video for session in state: " + session.getStatus());
        }

        // Validate file size
        if (request.fileSizeBytes() != null && request.fileSizeBytes() > MAX_VIDEO_SIZE_BYTES) {
            throw new IllegalArgumentException("File size exceeds maximum allowed: " + MAX_VIDEO_SIZE_BYTES);
        }

        // Validate content type
        if (!isValidVideoContentType(request.contentType())) {
            throw new IllegalArgumentException("Invalid content type: " + request.contentType());
        }

        try {
            // Generate presigned URL
            PresignedUploadUrl uploadUrl = s3VideoUploadService.generatePresignedUploadUrl(
                    request.sessionId(),
                    request.fileName(),
                    request.contentType(),
                    DEFAULT_UPLOAD_EXPIRATION
            );

            log.info("Generated presigned upload URL for session: {} S3 key: {}",
                    request.sessionId(), uploadUrl.s3Key());

            return uploadUrl;

        } catch (Exception e) {
            log.error("Failed to generate presigned upload URL for session: {}", request.sessionId(), e);
            throw new RuntimeException("Failed to generate video upload URL", e);
        }
    }

    @Override
    public void handleUploadCompletion(VideoUploadCompletionEvent event) {
        log.info("Handling upload completion for S3 key: {}", event.s3Key());

        try {
            s3VideoUploadService.processUploadCompletion(event.s3Key(), event.metadata());
            log.info("Successfully processed upload completion for S3 key: {}", event.s3Key());

        } catch (Exception e) {
            log.error("Failed to process upload completion for S3 key: {}", event.s3Key(), e);
            throw new RuntimeException("Failed to process upload completion", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PresignedDownloadUrl generateStreamingUrl(UUID sessionId, String videoFileName) {
        log.info("Generating streaming URL for session: {} file: {}", sessionId, videoFileName);

        // Find video file
        VideoFile videoFile = videoFileRepository.findBySessionIdAndFileName(sessionId, videoFileName)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Video file not found: " + videoFileName + " for session: " + sessionId));

        if (videoFile.getStatus() != VideoFile.VideoStatus.UPLOADED) {
            throw new IllegalStateException("Video is not ready for streaming: " + videoFile.getStatus());
        }

        try {
            PresignedDownloadUrl downloadUrl = s3VideoUploadService.generatePresignedDownloadUrl(
                    videoFile.getS3Key(),
                    DEFAULT_DOWNLOAD_EXPIRATION
            );

            log.info("Generated streaming URL for session: {} file: {}", sessionId, videoFileName);
            return downloadUrl;

        } catch (Exception e) {
            log.error("Failed to generate streaming URL for session: {} file: {}", sessionId, videoFileName, e);
            throw new RuntimeException("Failed to generate streaming URL", e);
        }
    }

    @Override
    public void deleteVideo(UUID sessionId, String videoFileName) {
        log.info("Deleting video for session: {} file: {}", sessionId, videoFileName);

        // Find and validate video file
        VideoFile videoFile = videoFileRepository.findBySessionIdAndFileName(sessionId, videoFileName)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Video file not found: " + videoFileName + " for session: " + sessionId));

        try {
            // Delete from S3
            s3VideoUploadService.deleteVideo(videoFile.getS3Key());

            // Update status in database
            videoFile.markAsDeleted();
            videoFileRepository.save(videoFile);

            log.info("Successfully deleted video for session: {} file: {}", sessionId, videoFileName);

        } catch (Exception e) {
            log.error("Failed to delete video for session: {} file: {}", sessionId, videoFileName, e);
            throw new RuntimeException("Failed to delete video", e);
        }
    }

    private boolean isValidUploadState(RecordingSession.SessionStatus status) {
        return status == RecordingSession.SessionStatus.STARTED ||
               status == RecordingSession.SessionStatus.RECORDING ||
               status == RecordingSession.SessionStatus.PAUSED;
    }

    private boolean isValidVideoContentType(String contentType) {
        return contentType != null && (
                contentType.startsWith("video/") ||
                contentType.equals("application/octet-stream") // Allow generic binary for video uploads
        );
    }
}