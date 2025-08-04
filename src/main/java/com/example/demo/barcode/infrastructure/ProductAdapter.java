package com.example.demo.barcode.infrastructure;

import com.example.demo.barcode.domain.ProductPort;
import com.example.demo.product.IProductRepository;
import com.example.demo.product.entity.BarcodeEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

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
        Optional<BarcodeEntity> barcodeEntity = productRepository.findByBarcodeValueAndStatus(barcodeValue, "active");
        return barcodeEntity.isPresent();
    }

    @Override
    public String getSkuForBarcode(String barcodeValue) {
        Optional<BarcodeEntity> barcodeEntity = findBarcodeEntity(barcodeValue);
        return barcodeEntity.map(BarcodeEntity::getPlatformSkuId).orElse(null);
    }

    @Override
    public String getBarcodeEntityId(String barcodeValue) {
        Optional<BarcodeEntity> barcodeEntity = findBarcodeEntity(barcodeValue);
        return barcodeEntity.map(entity -> entity.getId().toString()).orElse(null);
    }

    private Optional<BarcodeEntity> findBarcodeEntity(String barcodeValue) {
        return productRepository.findByBarcodeValueAndStatus(barcodeValue, "active");
    }
}