package com.example.demo.barcode.infrastructure;

import com.example.demo.barcode.domain.ProductPort;
import com.example.demo.product.IProductRepository;
import com.example.demo.product.entity.BarcodeEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Infrastructure adapter implementing ProductPort using the product repository
 */
@Component
@Slf4j
public class ProductAdapter implements ProductPort {

    private final IProductRepository productRepository;

    public ProductAdapter(IProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public boolean isValidActiveBarcode(String barcodeValue) {
        BarcodeEntity barcodeEntity = productRepository.findByBarcodeValueAndStatus(barcodeValue, "active");
        return barcodeEntity != null;
    }

    @Override
    public String getSkuForBarcode(String barcodeValue) {
        BarcodeEntity barcodeEntity = findBarcodeEntity(barcodeValue);
        return barcodeEntity != null ? barcodeEntity.getPlatformSkuId() : null;
    }

    @Override
    public String getBarcodeEntityId(String barcodeValue) {
        BarcodeEntity barcodeEntity = findBarcodeEntity(barcodeValue);
        return barcodeEntity != null ? barcodeEntity.getId().toString() : null;
    }

    private BarcodeEntity findBarcodeEntity(String barcodeValue) {
        return productRepository.findByBarcodeValueAndStatus(barcodeValue, "active");
    }
}