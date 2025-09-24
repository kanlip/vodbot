package com.example.demo.recording.application;

import com.example.demo.recording.domain.VideoFile;
import com.example.demo.recording.domain.VideoShare;
import com.example.demo.recording.port.in.CreateShareCommand;
import com.example.demo.recording.port.in.UploadVideoCommand;
import com.example.demo.recording.port.in.VideoManagementUseCase;
import com.example.demo.recording.port.out.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VideoManagementService implements VideoManagementUseCase {

    private final VideoFileRepository videoFileRepository;
    private final VideoShareRepository videoShareRepository;
    private final VideoStorageService storageService;
    private final VideoProcessingService processingService;
    private final NotificationService notificationService;

    @Override
    public VideoFile uploadVideo(UploadVideoCommand command) {
        log.info("Uploading video for session: {}", command.getSessionId());

        String bucket = "vodbot-videos";
        String key = generateVideoKey(command.getSessionId(), command.getFileName());

        // Upload to S3
        String s3Url = storageService.uploadVideo(bucket, key, command.getFileStream(),
                command.getContentType(), command.getFileSizeBytes());

        VideoFile videoFile = VideoFile.builder()
                .id(UUID.randomUUID())
                .sessionId(command.getSessionId())
                .fileName(command.getFileName())
                .s3Bucket(bucket)
                .s3Key(key)
                .fileSizeBytes(command.getFileSizeBytes())
                .contentType(command.getContentType())
                .durationSeconds(command.getDurationSeconds())
                .resolution(command.getResolution())
                .status(VideoFile.VideoStatus.UPLOADED)
                .uploadedAt(Instant.now())
                .createdAt(Instant.now())
                .build();

        videoFile = videoFileRepository.save(videoFile);
        log.info("Video uploaded successfully: {}", videoFile.getId());

        // Trigger video processing
        processingService.processVideo(videoFile.getId());

        return videoFile;
    }

    @Override
    public VideoFile processVideo(UUID videoFileId) {
        log.info("Processing video: {}", videoFileId);

        VideoFile videoFile = videoFileRepository.findById(videoFileId)
                .orElseThrow(() -> new IllegalArgumentException("Video file not found: " + videoFileId));

        videoFile.markProcessingStarted();
        videoFile = videoFileRepository.save(videoFile);

        try {
            // Generate thumbnail for video files
            processingService.generateThumbnail(videoFileId);

            // Mark as ready
            String publicUrl = storageService.generatePresignedUrl(
                    videoFile.getS3Bucket(),
                    videoFile.getS3Key(),
                    java.time.Duration.ofDays(7)
            ).toString();

            videoFile.setPublicUrl(publicUrl);
            videoFile.markAsReady();
            videoFile = videoFileRepository.save(videoFile);

            log.info("Video processing completed: {}", videoFileId);

        } catch (Exception e) {
            log.error("Video processing failed: {}", videoFileId, e);
            videoFile.markAsFailed();
            videoFileRepository.save(videoFile);
        }

        return videoFile;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoFile> getSessionVideos(UUID sessionId) {
        return videoFileRepository.findBySessionId(sessionId);
    }

    @Override
    @Transactional(readOnly = true)
    public VideoFile getVideoFile(UUID videoFileId) {
        return videoFileRepository.findById(videoFileId)
                .orElseThrow(() -> new IllegalArgumentException("Video file not found: " + videoFileId));
    }

    @Override
    public void deleteVideo(UUID videoFileId) {
        log.info("Deleting video: {}", videoFileId);

        VideoFile videoFile = videoFileRepository.findById(videoFileId)
                .orElseThrow(() -> new IllegalArgumentException("Video file not found: " + videoFileId));

        // Delete from storage
        storageService.deleteVideo(videoFile.getS3Bucket(), videoFile.getS3Key());

        // Delete from database
        videoFileRepository.deleteById(videoFileId);

        log.info("Video deleted: {}", videoFileId);
    }

    @Override
    public VideoShare createShare(CreateShareCommand command) {
        log.info("Creating video share for session: {}", command.getSessionId());

        VideoShare share = VideoShare.builder()
                .id(UUID.randomUUID())
                .sessionId(command.getSessionId())
                .shareType(command.getShareType())
                .recipient(command.getRecipient())
                .shareToken(generateShareToken())
                .accessCount(0)
                .expiresAt(command.getExpiresAt())
                .active(true)
                .createdAt(Instant.now())
                .build();

        share = videoShareRepository.save(share);

        // Send notification if recipient is specified
        if (command.getRecipient() != null) {
            notificationService.notifyVideoReady(share);
            share.markSent();
            videoShareRepository.save(share);
        }

        log.info("Video share created: {}", share.getId());
        return share;
    }

    @Override
    @Transactional(readOnly = true)
    public VideoShare getShare(String shareToken) {
        return videoShareRepository.findByShareToken(shareToken)
                .orElseThrow(() -> new IllegalArgumentException("Share not found: " + shareToken));
    }

    @Override
    public void recordShareAccess(String shareToken) {
        VideoShare share = videoShareRepository.findByShareToken(shareToken)
                .orElseThrow(() -> new IllegalArgumentException("Share not found: " + shareToken));

        if (!share.isActive()) {
            throw new IllegalStateException("Share is not active or has expired");
        }

        share.recordAccess();
        videoShareRepository.save(share);
    }

    @Override
    public void deactivateShare(UUID shareId) {
        VideoShare share = videoShareRepository.findById(shareId)
                .orElseThrow(() -> new IllegalArgumentException("Share not found: " + shareId));

        share.deactivate();
        videoShareRepository.save(share);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoShare> getSessionShares(UUID sessionId) {
        return videoShareRepository.findBySessionId(sessionId);
    }

    private String generateVideoKey(UUID sessionId, String fileName) {
        String prefix = String.format("sessions/%s/videos/", sessionId);
        return prefix + fileName;
    }

    private String extractFormat(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot != -1 ? fileName.substring(lastDot + 1).toLowerCase() : "unknown";
    }

    private String generateShareToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}