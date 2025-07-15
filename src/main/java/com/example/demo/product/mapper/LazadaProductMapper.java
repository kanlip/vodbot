package com.example.demo.product.mapper;


import com.example.demo.product.entity.BarcodeEntity;
import com.example.demo.product.internal.LazadaProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface LazadaProductMapper {
    LazadaProductMapper INSTANCE = Mappers.getMapper(LazadaProductMapper.class);



    @Mapping(source = "itemId", target = "platformProductId")

    BarcodeEntity toBarcodeEntity(LazadaProductResponse.LazadaProduct lazadaProduct);

    @Mapping(target = "platformProductId", source = "product.itemId")
    @Mapping(target = "platformSkuId", source = "sku.skuId")
    @Mapping(target = "productName", source = "product.attributes.name")
    @Mapping(target = "status", source = "sku.status")
    BarcodeEntity toBarcodeEntity(LazadaProductResponse.LazadaProduct product, LazadaProductResponse.Sku sku);
}
