package com.example.demo.platform.adapter.out.persistence;

import com.example.demo.platform.domain.PlatformIntegration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PlatformIntegrationMapper {
    PlatformIntegration toDomain(PlatformIntegrationEntity entity);
    PlatformIntegrationEntity toEntity(PlatformIntegration domain);
}