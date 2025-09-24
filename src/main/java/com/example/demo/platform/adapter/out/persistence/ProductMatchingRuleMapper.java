package com.example.demo.platform.adapter.out.persistence;

import com.example.demo.platform.domain.ProductMatchingRule;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductMatchingRuleMapper {
    ProductMatchingRule toDomain(ProductMatchingRuleEntity entity);
    ProductMatchingRuleEntity toEntity(ProductMatchingRule domain);
}