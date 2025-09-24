package com.example.demo.platform.adapter.out.persistence;

import com.example.demo.platform.domain.BarcodeGenerationLog;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BarcodeGenerationLogMapper {
    BarcodeGenerationLog toDomain(BarcodeGenerationLogEntity entity);
    BarcodeGenerationLogEntity toEntity(BarcodeGenerationLog domain);
}