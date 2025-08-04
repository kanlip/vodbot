package com.example.demo.recording.service;

import com.example.demo.recording.entity.RecordingSessionEntity;
import com.example.demo.recording.events.ItemScannedEvent;
import com.example.demo.recording.events.RecordingSessionCompletedEvent;
import com.example.demo.recording.events.RecordingSessionStartedEvent;
import com.example.demo.recording.repository.RecordingSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing recording sessions with audit trail capabilities.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecordingSessionService {
    
    private final RecordingSessionRepository recordingSessionRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    /**
     * Start a new recording session
     */
    @Transactional
    public RecordingSessionEntity startRecordingSession(ObjectId userId, ObjectId companyId, 
                                                      ObjectId orderId, String platformOrderId) {
        log.info("Starting recording session for user: {}, company: {}, order: {}", 
                userId, companyId, orderId);
        
        // Check if there's already an active session for this order
        Optional<RecordingSessionEntity> existingSession = recordingSessionRepository
            .findByPlatformOrderId(platformOrderId);
        
        if (existingSession.isPresent() && 
            (existingSession.get().getStatus() == RecordingSessionEntity.SessionStatus.STARTED ||
             existingSession.get().getStatus() == RecordingSessionEntity.SessionStatus.IN_PROGRESS)) {
            throw new IllegalStateException("Recording session already active for order: " + platformOrderId);
        }
        
        Instant now = Instant.now();
        
        RecordingSessionEntity session = RecordingSessionEntity.builder()
            .userId(userId)
            .companyId(companyId)
            .orderId(orderId)
            .platformOrderId(platformOrderId)
            .status(RecordingSessionEntity.SessionStatus.STARTED)
            .startedAt(now)
            .createdAt(now)
            .updatedAt(now)
            .lastModifiedBy(userId)
            .build();
        
        RecordingSessionEntity savedSession = recordingSessionRepository.save(session);
        
        // Publish domain event
        eventPublisher.publishEvent(new RecordingSessionStartedEvent(
            savedSession.getId(),
            userId,
            companyId,
            orderId,
            platformOrderId,
            now
        ));
        
        log.info("Recording session started with ID: {}", savedSession.getId());
        return savedSession;
    }
    
    /**
     * Add a scanned item to the recording session
     */
    @Transactional
    public RecordingSessionEntity addScannedItem(ObjectId sessionId, ObjectId barcodeEntityId,
                                               String barcodeValue, String sku, Integer quantity,
                                               Integer timestampOffsetSeconds, ObjectId scannedBy) {
        log.info("Adding scanned item to session: {}, barcode: {}", sessionId, barcodeValue);
        
        RecordingSessionEntity session = recordingSessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Recording session not found: " + sessionId));
        
        if (session.getStatus() != RecordingSessionEntity.SessionStatus.STARTED &&
            session.getStatus() != RecordingSessionEntity.SessionStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot add items to recording session in status: " + session.getStatus());
        }
        
        Instant now = Instant.now();
        
        RecordingSessionEntity.ScannedItem scannedItem = RecordingSessionEntity.ScannedItem.builder()
            .barcodeEntityId(barcodeEntityId)
            .barcodeValue(barcodeValue)
            .sku(sku)
            .quantity(quantity != null ? quantity : 1)
            .timestampOffsetSeconds(timestampOffsetSeconds)
            .scannedAt(now)
            .scannedBy(scannedBy)
            .status("scanned")
            .build();
        
        session.getScannedItems().add(scannedItem);
        session.setStatus(RecordingSessionEntity.SessionStatus.IN_PROGRESS);
        session.setUpdatedAt(now);
        session.setLastModifiedBy(scannedBy);
        
        RecordingSessionEntity savedSession = recordingSessionRepository.save(session);
        
        // Publish domain event
        eventPublisher.publishEvent(new ItemScannedEvent(
            sessionId,
            barcodeEntityId,
            barcodeValue,
            sku,
            quantity != null ? quantity : 1,
            timestampOffsetSeconds,
            scannedBy,
            now
        ));
        
        log.info("Item scanned successfully. Session now has {} items", 
                savedSession.getScannedItems().size());
        return savedSession;
    }
    
    /**
     * Complete a recording session
     */
    @Transactional
    public RecordingSessionEntity completeRecordingSession(ObjectId sessionId, ObjectId completedBy, String notes) {
        log.info("Completing recording session: {}", sessionId);
        
        RecordingSessionEntity session = recordingSessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Recording session not found: " + sessionId));
        
        if (session.getStatus() == RecordingSessionEntity.SessionStatus.COMPLETED) {
            throw new IllegalStateException("Recording session already completed");
        }
        
        Instant now = Instant.now();
        
        session.setStatus(RecordingSessionEntity.SessionStatus.COMPLETED);
        session.setEndedAt(now);
        session.setUpdatedAt(now);
        session.setLastModifiedBy(completedBy);
        session.setNotes(notes);
        
        RecordingSessionEntity savedSession = recordingSessionRepository.save(session);
        
        // Publish domain event
        eventPublisher.publishEvent(new RecordingSessionCompletedEvent(
            sessionId,
            session.getUserId(),
            session.getCompanyId(),
            session.getOrderId(),
            session.getPlatformOrderId(),
            session.getStartedAt(),
            now,
            session.getScannedItems().size(),
            notes
        ));
        
        log.info("Recording session completed. Total items scanned: {}", 
                savedSession.getScannedItems().size());
        return savedSession;
    }
    
    /**
     * Cancel a recording session
     */
    @Transactional
    public RecordingSessionEntity cancelRecordingSession(ObjectId sessionId, ObjectId cancelledBy, String reason) {
        log.info("Cancelling recording session: {}", sessionId);
        
        RecordingSessionEntity session = recordingSessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Recording session not found: " + sessionId));
        
        Instant now = Instant.now();
        
        session.setStatus(RecordingSessionEntity.SessionStatus.CANCELLED);
        session.setEndedAt(now);
        session.setUpdatedAt(now);
        session.setLastModifiedBy(cancelledBy);
        session.setNotes(reason);
        
        return recordingSessionRepository.save(session);
    }
    
    /**
     * Get recording session by ID
     */
    public Optional<RecordingSessionEntity> getRecordingSession(ObjectId sessionId) {
        return recordingSessionRepository.findById(sessionId);
    }
    
    /**
     * Get active recording sessions for a user
     */
    public List<RecordingSessionEntity> getActiveRecordingSessionsForUser(ObjectId userId) {
        return recordingSessionRepository.findByUserIdAndStatus(userId, RecordingSessionEntity.SessionStatus.IN_PROGRESS);
    }
    
    /**
     * Get all recording sessions for a company
     */
    public List<RecordingSessionEntity> getRecordingSessionsForCompany(ObjectId companyId) {
        return recordingSessionRepository.findByCompanyId(companyId);
    }
    
    /**
     * Get recording session by platform order ID
     */
    public Optional<RecordingSessionEntity> getRecordingSessionByPlatformOrderId(String platformOrderId) {
        return recordingSessionRepository.findByPlatformOrderId(platformOrderId);
    }
}