package com.example.demo.recording.adapter.out.persistence;

import com.example.demo.recording.domain.RecordingSession;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RecordingSessionMapper {
    RecordingSession toDomain(RecordingSessionEntity entity);
    RecordingSessionEntity toEntity(RecordingSession domain);
}