package com.example.demo.product.mapper;

import com.example.demo.product.entity.BarcodeEntity;
import com.example.demo.product.internal.ShopeeProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ShopeeProductMapper {

    ShopeeProductMapper INSTANCE = Mappers.getMapper(ShopeeProductMapper.class);

    BarcodeEntity toBarcodeEntity(ShopeeProductResponse.Item item);
}
