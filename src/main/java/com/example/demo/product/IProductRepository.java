package com.example.demo.product;

import com.example.demo.product.entity.BarcodeEntity;
import org.bson.types.ObjectId;

import java.util.List;

public interface IProductRepository {

     List<BarcodeEntity> findAll();
     BarcodeEntity save(BarcodeEntity product);
     BarcodeEntity saveAll(List<BarcodeEntity> products);
     void deleteById(ObjectId id);
}
