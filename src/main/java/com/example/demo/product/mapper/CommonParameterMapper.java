package com.example.demo.product.mapper;

import com.example.demo.product.internal.CommonParameter;
import com.example.demo.product.internal.SyncProductRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CommonParameterMapper {

    CommonParameterMapper INSTANCE = Mappers.getMapper(CommonParameterMapper.class);


    SyncProductRequest toSyncProductRequest(CommonParameter commonParameter);

}
