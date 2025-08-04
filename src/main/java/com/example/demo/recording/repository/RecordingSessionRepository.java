package com.example.demo.recording.repository;

import com.example.demo.recording.entity.RecordingSessionEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for RecordingSessionEntity providing data access operations.
 */
@Repository
public interface RecordingSessionRepository extends MongoRepository<RecordingSessionEntity, ObjectId> {
    
    /**
     * Find recording sessions by user ID
     */
    List<RecordingSessionEntity> findByUserId(ObjectId userId);
    
    /**
     * Find recording sessions by company ID
     */
    List<RecordingSessionEntity> findByCompanyId(ObjectId companyId);
    
    /**
     * Find recording sessions by order ID
     */
    List<RecordingSessionEntity> findByOrderId(ObjectId orderId);
    
    /**
     * Find recording sessions by status
     */
    List<RecordingSessionEntity> findByStatus(RecordingSessionEntity.SessionStatus status);
    
    /**
     * Find active recording sessions for a user
     */
    List<RecordingSessionEntity> findByUserIdAndStatus(ObjectId userId, RecordingSessionEntity.SessionStatus status);
    
    /**
     * Find active recording sessions for a company
     */
    List<RecordingSessionEntity> findByCompanyIdAndStatus(ObjectId companyId, RecordingSessionEntity.SessionStatus status);
    
    /**
     * Find recording session by platform order ID
     */
    Optional<RecordingSessionEntity> findByPlatformOrderId(String platformOrderId);
    
    /**
     * Find recording sessions by user and company
     */
    List<RecordingSessionEntity> findByUserIdAndCompanyId(ObjectId userId, ObjectId companyId);
}