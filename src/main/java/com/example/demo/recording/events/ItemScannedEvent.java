package com.example.demo.recording.events;

import org.bson.types.ObjectId;
import org.jmolecules.event.annotation.DomainEvent;

import java.time.Instant;

/**
 * Event published when an item is scanned during a recording session.
 */
@DomainEvent
public record ItemScannedEvent(
    ObjectId sessionId,
    ObjectId barcodeEntityId,
    String barcodeValue,
    String sku,
    Integer quantity,
    Integer timestampOffsetSeconds,
    ObjectId scannedBy,
    Instant scannedAt
) {
}