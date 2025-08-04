package com.example.demo.recording.service;

import com.example.demo.recording.events.RecordingSessionCompletedEvent;
import com.example.demo.recording.repository.RecordingSessionRepository;
import com.example.demo.video.entity.VideoEntity;
import com.example.demo.video.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Event listener that handles recording session events and creates corresponding VideoEntity.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RecordingSessionEventListener {
    
    private final VideoRepository videoRepository;
    private final RecordingSessionRepository recordingSessionRepository;
    
    /**
     * Handle recording session completion by creating a VideoEntity
     */
    @EventListener
    @Transactional
    public void handleRecordingSessionCompleted(RecordingSessionCompletedEvent event) {
        log.info("Handling recording session completed event for session: {}", event.sessionId());
        
        try {
            // Get the completed recording session to extract scanned items
            var recordingSession = recordingSessionRepository.findById(event.sessionId())
                .orElseThrow(() -> new IllegalStateException("Recording session not found: " + event.sessionId()));
            
            // Create VideoEntity from the recording session
            VideoEntity videoEntity = new VideoEntity();
            videoEntity.setCompanyId(event.companyId());
            videoEntity.setOrderId(event.orderId());
            videoEntity.setPlatformOrderId(event.platformOrderId());
            videoEntity.setRecordedByUserId(event.userId());
            videoEntity.setRecordedAt(event.startedAt());
            videoEntity.setStatus("processing"); // Will be updated when actual video is uploaded
            videoEntity.setNotes(event.notes());
            
            // Calculate duration from start to end time
            long durationSeconds = event.endedAt().getEpochSecond() - event.startedAt().getEpochSecond();
            videoEntity.setDurationSeconds((int) durationSeconds);
            
            // Convert scanned items to ItemScan
            var itemScans = recordingSession.getScannedItems().stream()
                .map(scannedItem -> {
                    VideoEntity.ItemScan itemScan = new VideoEntity.ItemScan();
                    itemScan.setTimestampOffsetSeconds(scannedItem.getTimestampOffsetSeconds());
                    itemScan.setSku(scannedItem.getSku());
                    itemScan.setQuantity(scannedItem.getQuantity());
                    itemScan.setStatus(scannedItem.getStatus());
                    itemScan.setBarcodeEntityId(scannedItem.getBarcodeEntityId());
                    return itemScan;
                })
                .collect(Collectors.toList());
            
            videoEntity.setItemScans(itemScans);
            videoEntity.setCreatedAt(Instant.now());
            videoEntity.setUpdatedAt(Instant.now());
            
            VideoEntity savedVideo = videoRepository.save(videoEntity);
            
            // Update the recording session with the video entity reference
            recordingSession.setVideoEntityId(new ObjectId(savedVideo.getId()));
            recordingSessionRepository.save(recordingSession);
            
            log.info("Created VideoEntity {} for recording session {}", 
                    savedVideo.getId(), event.sessionId());
            
        } catch (Exception e) {
            log.error("Failed to create VideoEntity for recording session {}: {}", 
                    event.sessionId(), e.getMessage(), e);
            // Don't throw exception to avoid rolling back the recording session completion
        }
    }
}