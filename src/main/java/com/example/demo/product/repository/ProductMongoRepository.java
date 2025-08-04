package com.example.demo.product.repository;


import com.example.demo.product.IProductRepository;
import com.example.demo.product.entity.BarcodeEntity;
import com.example.demo.common.Platform;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductMongoRepository extends IProductRepository, MongoRepository<BarcodeEntity, ObjectId> {
    
    Optional<BarcodeEntity> findByBarcodeValueAndStatus(String barcodeValue, String status);
    List<BarcodeEntity> findByCompanyAndStatus(ObjectId company, String status);
    List<BarcodeEntity> findByPlatformAndStatus(Platform platform, String status);
    Optional<BarcodeEntity> findByPlatformProductIdAndPlatform(String platformProductId, Platform platform);
    List<BarcodeEntity> findByCompanyAndPlatformAndStatus(ObjectId company, Platform platform, String status);
}
