package com.example.demo.recording.adapter.out.persistence;

import com.example.demo.recording.domain.VideoShare;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface VideoShareMapper {
    VideoShare toDomain(VideoShareEntity entity);
    VideoShareEntity toEntity(VideoShare domain);
}