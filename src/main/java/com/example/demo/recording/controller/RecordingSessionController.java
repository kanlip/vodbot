package com.example.demo.recording.controller;

import com.example.demo.recording.entity.RecordingSessionEntity;
import com.example.demo.recording.service.RecordingSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing recording sessions
 */
@RestController
@RequestMapping("/api/recording-sessions")
@RequiredArgsConstructor
@Slf4j
public class RecordingSessionController {
    
    private final RecordingSessionService recordingSessionService;
    
    /**
     * Start a new recording session
     */
    @PostMapping("/start")
    public ResponseEntity<RecordingSessionEntity> startRecordingSession(
            @RequestBody StartRecordingRequest request) {
        
        log.info("Starting recording session for order: {}", request.platformOrderId());
        
        RecordingSessionEntity session = recordingSessionService.startRecordingSession(
            new ObjectId(request.userId()),
            new ObjectId(request.companyId()),
            new ObjectId(request.orderId()),
            request.platformOrderId()
        );
        
        return ResponseEntity.ok(session);
    }
    
    /**
     * Add a scanned item to an active recording session
     */
    @PostMapping("/{sessionId}/scan-item")
    public ResponseEntity<RecordingSessionEntity> addScannedItem(
            @PathVariable String sessionId,
            @RequestBody ScanItemRequest request) {
        
        log.info("Adding scanned item to session: {}", sessionId);
        
        RecordingSessionEntity session = recordingSessionService.addScannedItem(
            new ObjectId(sessionId),
            new ObjectId(request.barcodeEntityId()),
            request.barcodeValue(),
            request.sku(),
            request.quantity(),
            request.timestampOffsetSeconds(),
            new ObjectId(request.scannedBy())
        );
        
        return ResponseEntity.ok(session);
    }
    
    /**
     * Complete a recording session
     */
    @PostMapping("/{sessionId}/complete")
    public ResponseEntity<RecordingSessionEntity> completeRecordingSession(
            @PathVariable String sessionId,
            @RequestBody CompleteRecordingRequest request) {
        
        log.info("Completing recording session: {}", sessionId);
        
        RecordingSessionEntity session = recordingSessionService.completeRecordingSession(
            new ObjectId(sessionId),
            new ObjectId(request.completedBy()),
            request.notes()
        );
        
        return ResponseEntity.ok(session);
    }
    
    /**
     * Cancel a recording session
     */
    @PostMapping("/{sessionId}/cancel")
    public ResponseEntity<RecordingSessionEntity> cancelRecordingSession(
            @PathVariable String sessionId,
            @RequestBody CancelRecordingRequest request) {
        
        log.info("Cancelling recording session: {}", sessionId);
        
        RecordingSessionEntity session = recordingSessionService.cancelRecordingSession(
            new ObjectId(sessionId),
            new ObjectId(request.cancelledBy()),
            request.reason()
        );
        
        return ResponseEntity.ok(session);
    }
    
    /**
     * Get recording session by ID
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<RecordingSessionEntity> getRecordingSession(@PathVariable String sessionId) {
        return recordingSessionService.getRecordingSession(new ObjectId(sessionId))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get active recording sessions for a user
     */
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<RecordingSessionEntity>> getActiveRecordingSessionsForUser(
            @PathVariable String userId) {
        
        List<RecordingSessionEntity> sessions = recordingSessionService
            .getActiveRecordingSessionsForUser(new ObjectId(userId));
        
        return ResponseEntity.ok(sessions);
    }
    
    /**
     * Get recording session by platform order ID
     */
    @GetMapping("/platform-order/{platformOrderId}")
    public ResponseEntity<RecordingSessionEntity> getRecordingSessionByPlatformOrderId(
            @PathVariable String platformOrderId) {
        
        return recordingSessionService.getRecordingSessionByPlatformOrderId(platformOrderId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    // Request DTOs
    public record StartRecordingRequest(
        String userId,
        String companyId,
        String orderId,
        String platformOrderId
    ) {}
    
    public record ScanItemRequest(
        String barcodeEntityId,
        String barcodeValue,
        String sku,
        Integer quantity,
        Integer timestampOffsetSeconds,
        String scannedBy
    ) {}
    
    public record CompleteRecordingRequest(
        String completedBy,
        String notes
    ) {}
    
    public record CancelRecordingRequest(
        String cancelledBy,
        String reason
    ) {}
}