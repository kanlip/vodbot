package com.example.demo.product;

import com.example.demo.product.entity.BarcodeEntity;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

public interface IProductRepository {

     List<BarcodeEntity> findAll();
     BarcodeEntity insert(BarcodeEntity product);
     List<BarcodeEntity> insert(List<BarcodeEntity> products);
     void deleteById(ObjectId id);
     Optional<BarcodeEntity> findByBarcodeValueAndStatus(String barcodeValue, String status);
}
