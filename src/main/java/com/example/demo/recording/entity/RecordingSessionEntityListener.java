package com.example.demo.recording.entity;

import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Entity listener for RecordingSessionEntity to handle automatic field management
 */
@Component
public class RecordingSessionEntityListener extends AbstractMongoEventListener<RecordingSessionEntity> {
    
    @Override
    public void onBeforeConvert(BeforeConvertEvent<RecordingSessionEntity> event) {
        RecordingSessionEntity entity = event.getSource();
        Instant now = Instant.now();
        
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(now);
        }
        
        entity.setUpdatedAt(now);
    }
}