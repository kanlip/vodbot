package com.example.demo.recording.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VideoShareJpaRepository extends JpaRepository<VideoShareEntity, UUID> {
    Optional<VideoShareEntity> findByShareToken(String shareToken);
    List<VideoShareEntity> findBySessionId(UUID sessionId);

    @Query("SELECT vs FROM VideoShareEntity vs WHERE vs.sessionId = :sessionId AND vs.active = true AND (vs.expiresAt IS NULL OR vs.expiresAt > :now)")
    List<VideoShareEntity> findActiveBySessionId(@Param("sessionId") UUID sessionId, @Param("now") Instant now);

    @Modifying
    @Query("DELETE FROM VideoShareEntity vs WHERE vs.expiresAt IS NOT NULL AND vs.expiresAt < :now")
    void deleteExpiredShares(@Param("now") Instant now);
}