package com.example.demo.productmapping.adapter.out.persistence;

import com.example.demo.productmapping.domain.ProductMapping;
import org.mapstruct.*;

/**
 * MapStruct mapper for converting between ProductMapping domain objects and ProductMappingEntity.
 */
@Mapper(componentModel = "spring")
public interface ProductMappingMapper {
    
    ProductMapping toDomain(ProductMappingEntity entity);
    
    ProductMappingEntity toEntity(ProductMapping domain);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDomain(@MappingTarget ProductMappingEntity entity, ProductMapping domain);
}