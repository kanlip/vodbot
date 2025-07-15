package com.example.demo.product.repository;


import com.example.demo.product.IProductRepository;
import com.example.demo.product.entity.BarcodeEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductMongoRepository extends IProductRepository, MongoRepository<BarcodeEntity, ObjectId> {

}
