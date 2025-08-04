package com.example.demo.recording;

import com.example.demo.recording.entity.RecordingSessionEntity;
import com.example.demo.recording.repository.RecordingSessionRepository;
import com.example.demo.recording.service.RecordingSessionService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration test for RecordingSession functionality with MongoDB TestContainers
 */
@SpringBootTest
@Testcontainers
class RecordingSessionIntegrationTest {
    
    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");
    
    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }
    
    @Autowired
    private RecordingSessionService recordingSessionService;
    
    @Autowired
    private RecordingSessionRepository recordingSessionRepository;
    
    private ObjectId testUserId;
    private ObjectId testCompanyId;
    private ObjectId testOrderId;
    private String testPlatformOrderId;
    
    @BeforeEach
    void setUp() {
        // Clean up existing data
        recordingSessionRepository.deleteAll();
        
        // Setup test data
        testUserId = new ObjectId();
        testCompanyId = new ObjectId();
        testOrderId = new ObjectId();
        testPlatformOrderId = "SHOPEE_ORDER_12345";
    }
    
    @Test
    void shouldStartRecordingSession() {
        // When
        RecordingSessionEntity session = recordingSessionService.startRecordingSession(
            testUserId, testCompanyId, testOrderId, testPlatformOrderId);
        
        // Then
        assertThat(session).isNotNull();
        assertThat(session.getId()).isNotNull();
        assertThat(session.getUserId()).isEqualTo(testUserId);
        assertThat(session.getCompanyId()).isEqualTo(testCompanyId);
        assertThat(session.getOrderId()).isEqualTo(testOrderId);
        assertThat(session.getPlatformOrderId()).isEqualTo(testPlatformOrderId);
        assertThat(session.getStatus()).isEqualTo(RecordingSessionEntity.SessionStatus.STARTED);
        assertThat(session.getStartedAt()).isNotNull();
        assertThat(session.getCreatedAt()).isNotNull();
        assertThat(session.getUpdatedAt()).isNotNull();
        assertThat(session.getScannedItems()).isEmpty();
    }
    
    @Test
    void shouldPreventDuplicateActiveSessionsForSameOrder() {
        // Given
        recordingSessionService.startRecordingSession(
            testUserId, testCompanyId, testOrderId, testPlatformOrderId);
        
        // When & Then
        assertThatThrownBy(() -> 
            recordingSessionService.startRecordingSession(
                testUserId, testCompanyId, testOrderId, testPlatformOrderId))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Recording session already active");
    }
    
    @Test
    void shouldAddScannedItemToSession() {
        // Given
        RecordingSessionEntity session = recordingSessionService.startRecordingSession(
            testUserId, testCompanyId, testOrderId, testPlatformOrderId);
        
        ObjectId barcodeEntityId = new ObjectId();
        String barcodeValue = "SYS_test_barcode";
        String sku = "TEST_SKU_001";
        
        // When
        RecordingSessionEntity updatedSession = recordingSessionService.addScannedItem(
            session.getId(), barcodeEntityId, barcodeValue, sku, 2, 15, testUserId);
        
        // Then
        assertThat(updatedSession.getStatus()).isEqualTo(RecordingSessionEntity.SessionStatus.IN_PROGRESS);
        assertThat(updatedSession.getScannedItems()).hasSize(1);
        
        RecordingSessionEntity.ScannedItem scannedItem = updatedSession.getScannedItems().get(0);
        assertThat(scannedItem.getBarcodeEntityId()).isEqualTo(barcodeEntityId);
        assertThat(scannedItem.getBarcodeValue()).isEqualTo(barcodeValue);
        assertThat(scannedItem.getSku()).isEqualTo(sku);
        assertThat(scannedItem.getQuantity()).isEqualTo(2);
        assertThat(scannedItem.getTimestampOffsetSeconds()).isEqualTo(15);
        assertThat(scannedItem.getScannedBy()).isEqualTo(testUserId);
        assertThat(scannedItem.getScannedAt()).isNotNull();
        assertThat(scannedItem.getStatus()).isEqualTo("scanned");
    }
    
    @Test
    void shouldCompleteRecordingSession() {
        // Given
        RecordingSessionEntity session = recordingSessionService.startRecordingSession(
            testUserId, testCompanyId, testOrderId, testPlatformOrderId);
        
        recordingSessionService.addScannedItem(
            session.getId(), new ObjectId(), "TEST_BARCODE", "TEST_SKU", 1, 10, testUserId);
        
        String notes = "Recording completed successfully";
        
        // When
        RecordingSessionEntity completedSession = recordingSessionService.completeRecordingSession(
            session.getId(), testUserId, notes);
        
        // Then
        assertThat(completedSession.getStatus()).isEqualTo(RecordingSessionEntity.SessionStatus.COMPLETED);
        assertThat(completedSession.getEndedAt()).isNotNull();
        assertThat(completedSession.getNotes()).isEqualTo(notes);
        assertThat(completedSession.getLastModifiedBy()).isEqualTo(testUserId);
    }
    
    @Test
    void shouldCancelRecordingSession() {
        // Given
        RecordingSessionEntity session = recordingSessionService.startRecordingSession(
            testUserId, testCompanyId, testOrderId, testPlatformOrderId);
        
        String reason = "User cancelled recording";
        
        // When
        RecordingSessionEntity cancelledSession = recordingSessionService.cancelRecordingSession(
            session.getId(), testUserId, reason);
        
        // Then
        assertThat(cancelledSession.getStatus()).isEqualTo(RecordingSessionEntity.SessionStatus.CANCELLED);
        assertThat(cancelledSession.getEndedAt()).isNotNull();
        assertThat(cancelledSession.getNotes()).isEqualTo(reason);
    }
    
    @Test
    void shouldFindRecordingSessionByPlatformOrderId() {
        // Given
        RecordingSessionEntity session = recordingSessionService.startRecordingSession(
            testUserId, testCompanyId, testOrderId, testPlatformOrderId);
        
        // When
        Optional<RecordingSessionEntity> foundSession = recordingSessionService
            .getRecordingSessionByPlatformOrderId(testPlatformOrderId);
        
        // Then
        assertThat(foundSession).isPresent();
        assertThat(foundSession.get().getId()).isEqualTo(session.getId());
    }
    
    @Test
    void shouldGetActiveRecordingSessionsForUser() {
        // Given
        RecordingSessionEntity session = recordingSessionService.startRecordingSession(
            testUserId, testCompanyId, testOrderId, testPlatformOrderId);
        
        recordingSessionService.addScannedItem(
            session.getId(), new ObjectId(), "TEST", "SKU", 1, 5, testUserId);
        
        // When
        var activeSessions = recordingSessionService.getActiveRecordingSessionsForUser(testUserId);
        
        // Then
        assertThat(activeSessions).hasSize(1);
        assertThat(activeSessions.get(0).getId()).isEqualTo(session.getId());
        assertThat(activeSessions.get(0).getStatus()).isEqualTo(RecordingSessionEntity.SessionStatus.IN_PROGRESS);
    }
    
    @Test
    void shouldRejectItemAdditionToCompletedSession() {
        // Given
        RecordingSessionEntity session = recordingSessionService.startRecordingSession(
            testUserId, testCompanyId, testOrderId, testPlatformOrderId);
        
        recordingSessionService.completeRecordingSession(session.getId(), testUserId, "Completed");
        
        // When & Then
        assertThatThrownBy(() -> 
            recordingSessionService.addScannedItem(
                session.getId(), new ObjectId(), "BARCODE", "SKU", 1, 10, testUserId))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Cannot add items to recording session in status");
    }
}