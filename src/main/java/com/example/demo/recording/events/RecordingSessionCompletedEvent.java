package com.example.demo.recording.events;

import org.bson.types.ObjectId;
import org.jmolecules.event.annotation.DomainEvent;

import java.time.Instant;

/**
 * Event published when a recording session is completed.
 * This can trigger creation of VideoEntity and other cleanup tasks.
 */
@DomainEvent
public record RecordingSessionCompletedEvent(
    ObjectId sessionId,
    ObjectId userId,
    ObjectId companyId,
    ObjectId orderId,
    String platformOrderId,
    Instant startedAt,
    Instant endedAt,
    int totalItemsScanned,
    String notes
) {
}