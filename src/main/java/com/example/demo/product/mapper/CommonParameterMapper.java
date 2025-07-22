package com.example.demo.product.mapper;

import com.example.demo.product.internal.LazadaCommonParameter;
import com.example.demo.product.internal.LazadaSyncProductRequest;
import com.example.demo.product.internal.ShopeeCommonParameter;
import com.example.demo.product.internal.ShopeeSyncProductRequest;
import com.example.demo.product.internal.TikTokCommonParameter;
import com.example.demo.product.internal.TikTokSyncProductRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CommonParameterMapper {

    CommonParameterMapper INSTANCE = Mappers.getMapper(CommonParameterMapper.class);


    ShopeeSyncProductRequest toShopeeSyncProductRequest(ShopeeCommonParameter shopeeCommonParameter);

    LazadaSyncProductRequest toLazadaSyncProductRequest(LazadaCommonParameter lazadaCommonParameter);

    TikTokSyncProductRequest toTikTokSyncProductRequest(TikTokCommonParameter tikTokCommonParameter);
}
