package com.example.demo.recording.adapter.out.storage;

import com.example.demo.recording.port.out.VideoStorageService;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3VideoStorageService implements VideoStorageService {

    private final S3Template s3Template;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Override
    public String uploadVideo(String bucket, String key, InputStream inputStream,
                             String contentType, long contentLength) {
        log.info("Uploading video to S3: bucket={}, key={}", bucket, key);

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .contentLength(contentLength)
                    .build();

            s3Client.putObject(request, software.amazon.awssdk.core.sync.RequestBody.fromInputStream(inputStream, contentLength));

            String url = String.format("s3://%s/%s", bucket, key);
            log.info("Video uploaded successfully: {}", url);
            return url;

        } catch (Exception e) {
            log.error("Failed to upload video to S3: bucket={}, key={}", bucket, key, e);
            throw new RuntimeException("Failed to upload video", e);
        }
    }

    @Override
    public InputStream downloadVideo(String bucket, String key) {
        log.info("Downloading video from S3: bucket={}, key={}", bucket, key);

        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            return s3Client.getObject(request);

        } catch (Exception e) {
            log.error("Failed to download video from S3: bucket={}, key={}", bucket, key, e);
            throw new RuntimeException("Failed to download video", e);
        }
    }

    @Override
    public URL generatePresignedUrl(String bucket, String key, Duration expiration) {
        log.info("Generating presigned URL: bucket={}, key={}, expiration={}", bucket, key, expiration);

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(expiration)
                    .getObjectRequest(getObjectRequest)
                    .build();

            return s3Presigner.presignGetObject(presignRequest).url();

        } catch (Exception e) {
            log.error("Failed to generate presigned URL: bucket={}, key={}", bucket, key, e);
            throw new RuntimeException("Failed to generate presigned URL", e);
        }
    }

    @Override
    public void deleteVideo(String bucket, String key) {
        log.info("Deleting video from S3: bucket={}, key={}", bucket, key);

        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            s3Client.deleteObject(request);
            log.info("Video deleted successfully: bucket={}, key={}", bucket, key);

        } catch (Exception e) {
            log.error("Failed to delete video from S3: bucket={}, key={}", bucket, key, e);
            throw new RuntimeException("Failed to delete video", e);
        }
    }

    @Override
    public boolean videoExists(String bucket, String key) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            s3Client.headObject(request);
            return true;

        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error("Error checking if video exists: bucket={}, key={}", bucket, key, e);
            throw new RuntimeException("Error checking video existence", e);
        }
    }

    @Override
    public long getVideoSize(String bucket, String key) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            HeadObjectResponse response = s3Client.headObject(request);
            return response.contentLength();

        } catch (Exception e) {
            log.error("Failed to get video size: bucket={}, key={}", bucket, key, e);
            throw new RuntimeException("Failed to get video size", e);
        }
    }
}