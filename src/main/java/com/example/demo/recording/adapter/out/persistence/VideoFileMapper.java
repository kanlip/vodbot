package com.example.demo.recording.adapter.out.persistence;

import com.example.demo.recording.domain.VideoFile;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface VideoFileMapper {
    VideoFile toDomain(VideoFileEntity entity);
    VideoFileEntity toEntity(VideoFile domain);
}