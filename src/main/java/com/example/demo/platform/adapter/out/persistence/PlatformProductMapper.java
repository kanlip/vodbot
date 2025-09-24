package com.example.demo.platform.adapter.out.persistence;

import com.example.demo.platform.domain.PlatformProduct;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PlatformProductMapper {
    PlatformProduct toDomain(PlatformProductEntity entity);
    PlatformProductEntity toEntity(PlatformProduct domain);
}