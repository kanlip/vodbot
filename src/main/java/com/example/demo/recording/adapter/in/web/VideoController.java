package com.example.demo.recording.adapter.in.web;

import com.example.demo.recording.domain.VideoFile;
import com.example.demo.recording.domain.VideoShare;
import com.example.demo.recording.port.in.CreateShareCommand;
import com.example.demo.recording.port.in.UploadVideoCommand;
import com.example.demo.recording.port.in.VideoManagementUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/recording/videos")
@RequiredArgsConstructor
@Tag(name = "Video Management", description = "Video upload, processing, and sharing")
public class VideoController {

    private final VideoManagementUseCase videoManagementUseCase;

    @PostMapping("/upload")
    @Operation(summary = "Upload a video file (deprecated - use presigned URL upload instead)")
    @Deprecated
    public ResponseEntity<VideoFile> uploadVideo(
            @RequestParam UUID sessionId,
            @RequestParam MultipartFile file) throws IOException {

        UploadVideoCommand command = UploadVideoCommand.builder()
                .sessionId(sessionId)
                .fileName(file.getOriginalFilename())
                .fileStream(file.getInputStream())
                .fileSizeBytes(file.getSize())
                .contentType(file.getContentType())
                .build();

        VideoFile videoFile = videoManagementUseCase.uploadVideo(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(videoFile);
    }

    @GetMapping("/session/{sessionId}")
    @Operation(summary = "Get all videos for a session")
    public ResponseEntity<List<VideoFile>> getSessionVideos(@PathVariable UUID sessionId) {
        List<VideoFile> videos = videoManagementUseCase.getSessionVideos(sessionId);
        return ResponseEntity.ok(videos);
    }

    @GetMapping("/{videoFileId}")
    @Operation(summary = "Get video file details")
    public ResponseEntity<VideoFile> getVideoFile(@PathVariable UUID videoFileId) {
        VideoFile videoFile = videoManagementUseCase.getVideoFile(videoFileId);
        return ResponseEntity.ok(videoFile);
    }

    @DeleteMapping("/{videoFileId}")
    @Operation(summary = "Delete a video file")
    public ResponseEntity<Void> deleteVideo(@PathVariable UUID videoFileId) {
        videoManagementUseCase.deleteVideo(videoFileId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/share")
    @Operation(summary = "Create a video share link")
    public ResponseEntity<VideoShare> createShare(@RequestBody CreateShareRequest request) {
        Instant expiresAt = request.getExpiresAt() != null
            ? request.getExpiresAt()
            : Instant.now().plus(7, ChronoUnit.DAYS);

        CreateShareCommand command = CreateShareCommand.builder()
                .sessionId(request.getSessionId())
                .shareType(request.getShareType())
                .recipient(request.getRecipient())
                .expiresAt(expiresAt)
                .build();

        VideoShare share = videoManagementUseCase.createShare(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(share);
    }

    @GetMapping("/share/{shareToken}")
    @Operation(summary = "Access a shared video")
    public ResponseEntity<VideoShare> accessShare(@PathVariable String shareToken) {
        VideoShare share = videoManagementUseCase.getShare(shareToken);

        if (!share.isActive()) {
            return ResponseEntity.status(HttpStatus.GONE).build();
        }

        videoManagementUseCase.recordShareAccess(shareToken);
        return ResponseEntity.ok(share);
    }

    @GetMapping("/shares/session/{sessionId}")
    @Operation(summary = "Get all shares for a session")
    public ResponseEntity<List<VideoShare>> getSessionShares(@PathVariable UUID sessionId) {
        List<VideoShare> shares = videoManagementUseCase.getSessionShares(sessionId);
        return ResponseEntity.ok(shares);
    }

    @DeleteMapping("/share/{shareId}")
    @Operation(summary = "Deactivate a video share")
    public ResponseEntity<Void> deactivateShare(@PathVariable UUID shareId) {
        videoManagementUseCase.deactivateShare(shareId);
        return ResponseEntity.ok().build();
    }
}