package com.example.demo.product.mapper;

import com.example.demo.order.internal.Platform;
import com.example.demo.product.entity.BarcodeEntity;
import com.example.demo.product.internal.TikTokProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.time.Instant;

@Mapper
public interface TikTokProductMapper {
    TikTokProductMapper INSTANCE = Mappers.getMapper(TikTokProductMapper.class);

    @Mapping(target = "platform", constant = "TIKTOK")
    @Mapping(target = "platformProductId", source = "product.productId")
    @Mapping(target = "platformSkuId", source = "sku.id")
    @Mapping(target = "productName", source = "product.productName")
    @Mapping(target = "type", constant = "platform_sync")
    @Mapping(target = "status", source = "product.productStatus")
    @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "barcodeValue", source = "sku.sellerSku")
    @Mapping(target = "variantDetails", expression = "java(mapVariantDetails(sku))")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    BarcodeEntity toBarcodeEntity(TikTokProductResponse.TikTokProduct product, TikTokProductResponse.TikTokSku sku);

    default BarcodeEntity.VariantDetails mapVariantDetails(TikTokProductResponse.TikTokSku sku) {
        if (sku.getSalesAttributes() == null || sku.getSalesAttributes().isEmpty()) {
            return null;
        }
        
        BarcodeEntity.VariantDetails.VariantDetailsBuilder builder = BarcodeEntity.VariantDetails.builder();
        
        for (TikTokProductResponse.TikTokSalesAttribute attribute : sku.getSalesAttributes()) {
            if ("Color".equalsIgnoreCase(attribute.getAttributeName())) {
                builder.color(attribute.getValueName());
            } else if ("Size".equalsIgnoreCase(attribute.getAttributeName())) {
                builder.size(attribute.getValueName());
            }
        }
        
        return builder.build();
    }
}