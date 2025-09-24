package com.example.demo.recording.adapter.in;

import com.example.demo.recording.port.in.VideoUploadUseCase;
import com.example.demo.recording.port.in.VideoUploadUseCase.VideoUploadRequest;
import com.example.demo.recording.port.in.VideoUploadUseCase.VideoUploadCompletionEvent;
import com.example.demo.recording.port.out.VideoUploadService.PresignedUploadUrl;
import com.example.demo.recording.port.out.VideoUploadService.PresignedDownloadUrl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/recording/videos")
@RequiredArgsConstructor
@Slf4j
public class VideoUploadController {

    private final VideoUploadUseCase videoUploadUseCase;

    @PostMapping("/upload-url")
    public ResponseEntity<PresignedUploadUrlResponse> requestUploadUrl(
            @Valid @RequestBody VideoUploadUrlRequest request) {

        log.info("Received upload URL request for session: {} file: {}",
                request.sessionId(), request.fileName());

        VideoUploadRequest uploadRequest = new VideoUploadRequest(
                request.sessionId(),
                request.fileName(),
                request.contentType(),
                request.fileSizeBytes(),
                request.metadata()
        );

        PresignedUploadUrl uploadUrl = videoUploadUseCase.requestVideoUpload(uploadRequest);

        PresignedUploadUrlResponse response = new PresignedUploadUrlResponse(
                uploadUrl.uploadUrl(),
                uploadUrl.s3Key(),
                uploadUrl.formFields(),
                uploadUrl.expiration().getSeconds()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload-complete")
    public ResponseEntity<Void> handleUploadCompletion(
            @Valid @RequestBody VideoUploadCompletionRequest request) {

        log.info("Received upload completion notification for S3 key: {}", request.s3Key());

        VideoUploadCompletionEvent event = new VideoUploadCompletionEvent(
                request.s3Key(),
                request.s3Bucket(),
                request.fileSizeBytes(),
                request.contentType(),
                request.metadata()
        );

        videoUploadUseCase.handleUploadCompletion(event);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{sessionId}/{fileName}/stream-url")
    public ResponseEntity<PresignedDownloadUrlResponse> getStreamingUrl(
            @PathVariable UUID sessionId,
            @PathVariable String fileName) {

        log.info("Generating streaming URL for session: {} file: {}", sessionId, fileName);

        PresignedDownloadUrl downloadUrl = videoUploadUseCase.generateStreamingUrl(sessionId, fileName);

        PresignedDownloadUrlResponse response = new PresignedDownloadUrlResponse(
                downloadUrl.downloadUrl(),
                downloadUrl.expiration().getSeconds()
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{sessionId}/{fileName}")
    public ResponseEntity<Void> deleteVideo(
            @PathVariable UUID sessionId,
            @PathVariable String fileName) {

        log.info("Deleting video for session: {} file: {}", sessionId, fileName);

        videoUploadUseCase.deleteVideo(sessionId, fileName);

        return ResponseEntity.noContent().build();
    }

    // Request/Response DTOs
    public record VideoUploadUrlRequest(
            @NotNull UUID sessionId,
            @NotBlank String fileName,
            @NotBlank String contentType,
            @Positive Long fileSizeBytes,
            Map<String, Object> metadata
    ) {}

    public record PresignedUploadUrlResponse(
            String uploadUrl,
            String s3Key,
            Map<String, String> formFields,
            long expirationSeconds
    ) {}

    public record VideoUploadCompletionRequest(
            @NotBlank String s3Key,
            @NotBlank String s3Bucket,
            @Positive Long fileSizeBytes,
            String contentType,
            Map<String, Object> metadata
    ) {}

    public record PresignedDownloadUrlResponse(
            String downloadUrl,
            long expirationSeconds
    ) {}
}