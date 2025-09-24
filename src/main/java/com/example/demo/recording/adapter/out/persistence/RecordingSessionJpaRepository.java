package com.example.demo.recording.adapter.out.persistence;

import com.example.demo.recording.domain.RecordingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RecordingSessionJpaRepository extends JpaRepository<RecordingSessionEntity, UUID> {
    List<RecordingSessionEntity> findByOrderId(UUID orderId);
    List<RecordingSessionEntity> findByPackerId(UUID packerId);
    List<RecordingSessionEntity> findByStatus(RecordingSession.SessionStatus status);

    @Query("SELECT s FROM RecordingSessionEntity s WHERE s.packerId = :packerId AND s.status IN ('RECORDING', 'PROCESSING')")
    List<RecordingSessionEntity> findActiveSessionsByPacker(@Param("packerId") UUID packerId);
}