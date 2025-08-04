package com.example.demo.recording;

import com.example.demo.recording.entity.RecordingSessionEntity;
import com.example.demo.recording.service.RecordingSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Demo component to showcase RecordingSession functionality
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RecordingSessionDemo {
    
    private final RecordingSessionService recordingSessionService;
    
    @EventListener(ApplicationReadyEvent.class)
    public void demonstrateRecordingSessionFunctionality() {
        log.info("=== Demonstrating RecordingSession MongoDB Connection and Audit Trail ===");
        
        try {
            // Demo data
            ObjectId userId = new ObjectId();
            ObjectId companyId = new ObjectId();
            ObjectId orderId = new ObjectId();
            String platformOrderId = "DEMO_ORDER_" + System.currentTimeMillis();
            
            // 1. Start a recording session
            log.info("Starting recording session for order: {}", platformOrderId);
            RecordingSessionEntity session = recordingSessionService.startRecordingSession(
                userId, companyId, orderId, platformOrderId);
            
            log.info("✓ Recording session started: ID={}, Status={}, User={}, Company={}", 
                session.getId(), session.getStatus(), session.getUserId(), session.getCompanyId());
            
            // 2. Simulate scanning items during recording
            log.info("Simulating item scans during recording...");
            
            // Scan first item
            recordingSessionService.addScannedItem(
                session.getId(), 
                new ObjectId(), 
                "SYS_demo_barcode_001", 
                "DEMO_SKU_001", 
                2, 
                10, 
                userId);
            
            // Scan second item
            recordingSessionService.addScannedItem(
                session.getId(), 
                new ObjectId(), 
                "SYS_demo_barcode_002", 
                "DEMO_SKU_002", 
                1, 
                25, 
                userId);
            
            log.info("✓ Scanned 2 items during recording session");
            
            // 3. Complete the recording session
            String notes = "Demo recording completed successfully with audit trail";
            RecordingSessionEntity completedSession = recordingSessionService.completeRecordingSession(
                session.getId(), userId, notes);
            
            log.info("✓ Recording session completed: Status={}, TotalItems={}, Duration={}s", 
                completedSession.getStatus(), 
                completedSession.getScannedItems().size(),
                completedSession.getEndedAt().getEpochSecond() - completedSession.getStartedAt().getEpochSecond());
            
            // 4. Verify audit trail information
            log.info("Audit Trail Information:");
            log.info("  - Created At: {}", completedSession.getCreatedAt());
            log.info("  - Updated At: {}", completedSession.getUpdatedAt());
            log.info("  - Last Modified By: {}", completedSession.getLastModifiedBy());
            log.info("  - Started At: {}", completedSession.getStartedAt());
            log.info("  - Ended At: {}", completedSession.getEndedAt());
            
            // 5. Show scanned items with timestamps
            log.info("Scanned Items Details:");
            completedSession.getScannedItems().forEach(item -> 
                log.info("  - SKU: {}, Barcode: {}, Quantity: {}, Timestamp: {}s, Scanned By: {}", 
                    item.getSku(), item.getBarcodeValue(), item.getQuantity(), 
                    item.getTimestampOffsetSeconds(), item.getScannedBy()));
            
            // 6. Verify VideoEntity was created (via event listener)
            if (completedSession.getVideoEntityId() != null) {
                log.info("✓ VideoEntity created: {}", completedSession.getVideoEntityId());
            }
            
            // 7. Clean up demo data
            log.info("Cleaning up demo recording session...");
            // Note: In a real application, you might want to keep this data for audit purposes
            
            log.info("=== RecordingSession MongoDB Connection and Audit Trail Demo Complete ===");
            
        } catch (Exception e) {
            log.error("Error demonstrating RecordingSession functionality: {}", e.getMessage(), e);
        }
    }
}