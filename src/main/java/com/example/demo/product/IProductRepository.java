package com.example.demo.product;

import com.example.demo.product.entity.BarcodeEntity;
import com.example.demo.common.Platform;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

public interface IProductRepository {

     List<BarcodeEntity> findAll();
     BarcodeEntity insert(BarcodeEntity product);
     List<BarcodeEntity> insert(List<BarcodeEntity> products);
     void deleteById(ObjectId id);
     Optional<BarcodeEntity> findByBarcodeValueAndStatus(String barcodeValue, String status);
     
     // Additional useful query methods
     List<BarcodeEntity> findByCompanyAndStatus(ObjectId company, String status);
     List<BarcodeEntity> findByPlatformAndStatus(Platform platform, String status);
     Optional<BarcodeEntity> findByPlatformProductIdAndPlatform(String platformProductId, Platform platform);
     List<BarcodeEntity> findByCompanyAndPlatformAndStatus(ObjectId company, Platform platform, String status);
}
