package com.example.demo.recording.adapter.out;

import com.example.demo.recording.port.out.VideoUploadService;
import com.example.demo.recording.port.out.VideoFileRepository;
import com.example.demo.recording.domain.VideoFile;
import com.example.demo.recording.domain.RecordingSession;
import com.example.demo.recording.port.out.RecordingSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3VideoUploadService implements VideoUploadService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final VideoFileRepository videoFileRepository;
    private final RecordingSessionRepository sessionRepository;

    @Value("${vodbot.s3.bucket}")
    private String bucketName;

    @Value("${vodbot.s3.video-prefix:videos/}")
    private String videoPrefix;

    @Override
    public PresignedUploadUrl generatePresignedUploadUrl(UUID sessionId, String fileName, String contentType, Duration expiration) {
        log.info("Generating presigned upload URL for session: {} file: {}", sessionId, fileName);

        // Generate unique S3 key
        String s3Key = generateS3Key(sessionId, fileName);

        // Create metadata for the upload
        Map<String, String> metadata = new HashMap<>();
        metadata.put("session-id", sessionId.toString());
        metadata.put("original-filename", fileName);
        metadata.put("upload-timestamp", Instant.now().toString());

        try {
            // Create put object request
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(contentType)
                    .metadata(metadata)
                    .serverSideEncryption(ServerSideEncryption.AES256)
                    .build();

            // Create presigned request
            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(expiration)
                    .putObjectRequest(putObjectRequest)
                    .build();

            // Generate presigned URL
            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

            log.info("Generated presigned upload URL for S3 key: {}", s3Key);

            return new PresignedUploadUrl(
                    presignedRequest.url().toString(),
                    s3Key,
                    extractFormFields(presignedRequest),
                    expiration
            );

        } catch (Exception e) {
            log.error("Failed to generate presigned upload URL for session: {} file: {}", sessionId, fileName, e);
            throw new RuntimeException("Failed to generate presigned upload URL", e);
        }
    }

    @Override
    public PresignedDownloadUrl generatePresignedDownloadUrl(String s3Key, Duration expiration) {
        log.info("Generating presigned download URL for S3 key: {}", s3Key);

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(expiration)
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);

            log.info("Generated presigned download URL for S3 key: {}", s3Key);

            return new PresignedDownloadUrl(
                    presignedRequest.url().toString(),
                    expiration
            );

        } catch (Exception e) {
            log.error("Failed to generate presigned download URL for S3 key: {}", s3Key, e);
            throw new RuntimeException("Failed to generate presigned download URL", e);
        }
    }

    @Override
    public void processUploadCompletion(String s3Key, Map<String, Object> metadata) {
        log.info("Processing upload completion for S3 key: {}", s3Key);

        try {
            // Get object metadata from S3
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            HeadObjectResponse headResponse = s3Client.headObject(headRequest);

            // Extract session ID from metadata
            String sessionIdStr = headResponse.metadata().get("session-id");
            if (sessionIdStr == null) {
                log.error("No session ID found in S3 object metadata for key: {}", s3Key);
                return;
            }

            UUID sessionId = UUID.fromString(sessionIdStr);
            String originalFilename = headResponse.metadata().get("original-filename");

            // Get recording session
            RecordingSession session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new IllegalArgumentException("Recording session not found: " + sessionId));

            // Create video file record
            VideoFile videoFile = VideoFile.builder()
                    .id(UUID.randomUUID())
                    .sessionId(sessionId)
                    .fileName(originalFilename)
                    .s3Key(s3Key)
                    .s3Bucket(bucketName)
                    .fileSizeBytes(headResponse.contentLength())
                    .contentType(headResponse.contentType())
                    .uploadedAt(Instant.now())
                    .status(VideoFile.VideoStatus.UPLOADED)
                    .metadata(convertMetadata(headResponse.metadata()))
                    .build();

            videoFileRepository.save(videoFile);

            // Update session status if this was the first video
            if (session.getStatus() == RecordingSession.SessionStatus.STARTED) {
                session.updateStatus(RecordingSession.SessionStatus.RECORDING);
                sessionRepository.save(session);
            }

            log.info("Successfully processed upload completion for session: {} S3 key: {}", sessionId, s3Key);

        } catch (Exception e) {
            log.error("Failed to process upload completion for S3 key: {}", s3Key, e);
            throw new RuntimeException("Failed to process upload completion", e);
        }
    }

    @Override
    public void deleteVideo(String s3Key) {
        log.info("Deleting video from S3: {}", s3Key);

        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.deleteObject(deleteRequest);

            log.info("Successfully deleted video from S3: {}", s3Key);

        } catch (Exception e) {
            log.error("Failed to delete video from S3: {}", s3Key, e);
            throw new RuntimeException("Failed to delete video from S3", e);
        }
    }

    private String generateS3Key(UUID sessionId, String fileName) {
        // Extract file extension
        String extension = "";
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            extension = fileName.substring(lastDotIndex);
        }

        // Generate unique key: videos/{sessionId}/{timestamp}_{originalName}
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String sanitizedFileName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");

        return videoPrefix + sessionId + "/" + timestamp + "_" + sanitizedFileName;
    }

    private Map<String, String> extractFormFields(PresignedPutObjectRequest presignedRequest) {
        // For simple presigned PUT URLs, form fields are typically empty
        // This would be used for multipart form uploads
        return new HashMap<>();
    }

    private Map<String, Object> convertMetadata(Map<String, String> s3Metadata) {
        Map<String, Object> metadata = new HashMap<>();
        if (s3Metadata != null) {
            metadata.putAll(s3Metadata);
        }
        return metadata;
    }
}