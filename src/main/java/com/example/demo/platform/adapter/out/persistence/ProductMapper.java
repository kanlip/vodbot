package com.example.demo.platform.adapter.out.persistence;

import com.example.demo.platform.domain.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductMapper {

    @Mapping(target = "dimensions", expression = "java(mapDimensions(entity.getDimensions()))")
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "platformMappings", ignore = true)
    Product toDomain(ProductEntity entity);

    @Mapping(target = "dimensions", expression = "java(mapDimensionsToEntity(domain.getDimensions()))")
    ProductEntity toEntity(Product domain);

    default Product.Dimensions mapDimensions(java.util.Map<String, Object> dimensionsMap) {
        if (dimensionsMap == null) {
            return null;
        }

        return Product.Dimensions.builder()
                .lengthCm(getDoubleValue(dimensionsMap, "lengthCm"))
                .widthCm(getDoubleValue(dimensionsMap, "widthCm"))
                .heightCm(getDoubleValue(dimensionsMap, "heightCm"))
                .build();
    }

    default java.util.Map<String, Object> mapDimensionsToEntity(Product.Dimensions dimensions) {
        if (dimensions == null) {
            return null;
        }

        java.util.Map<String, Object> map = new java.util.HashMap<>();
        if (dimensions.getLengthCm() != null) map.put("lengthCm", dimensions.getLengthCm());
        if (dimensions.getWidthCm() != null) map.put("widthCm", dimensions.getWidthCm());
        if (dimensions.getHeightCm() != null) map.put("heightCm", dimensions.getHeightCm());
        return map;
    }

    default Double getDoubleValue(java.util.Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return null;
    }
}