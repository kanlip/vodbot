package com.example.demo.recording.events;

import org.bson.types.ObjectId;
import org.jmolecules.event.annotation.DomainEvent;

import java.time.Instant;

/**
 * Event published when a recording session is started.
 * This event can be consumed by other modules for cross-cutting concerns.
 */
@DomainEvent
public record RecordingSessionStartedEvent(
    ObjectId sessionId,
    ObjectId userId,
    ObjectId companyId,
    ObjectId orderId,
    String platformOrderId,
    Instant startedAt
) {
}