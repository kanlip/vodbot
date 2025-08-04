package com.example.demo.product;

import com.example.demo.common.Platform;
import com.example.demo.product.entity.BarcodeEntity;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Demonstration component showing BarcodeEntity MongoDB connectivity
 */
@Component
@Slf4j
public class BarcodeMongoDemo implements CommandLineRunner {

    private final IProductRepository productRepository;

    public BarcodeMongoDemo(IProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("=== Demonstrating BarcodeEntity MongoDB Connection ===");
        
        // Create a sample barcode entity - entity listener will set defaults
        BarcodeEntity demoBarcode = BarcodeEntity.builder()
                .company(new ObjectId())
                .platform(Platform.SHOPEE)
                .platformProductId("DEMO_PROD_001")
                .platformSkuId("DEMO_SKU_001")
                .productName("Demo Product")
                .build();
        
        log.info("Creating BarcodeEntity without barcode value (will be auto-generated)...");
        BarcodeEntity savedBarcode = productRepository.insert(demoBarcode);
        log.info("✓ Saved BarcodeEntity: ID={}, BarcodeValue={}, Status={}, Type={}", 
                savedBarcode.getId(), savedBarcode.getBarcodeValue(), 
                savedBarcode.getStatus(), savedBarcode.getType());
        
        // Test querying by different methods
        log.info("Testing enhanced repository query methods...");
        
        // Test findByBarcodeValueAndStatus
        var foundByBarcode = productRepository.findByBarcodeValueAndStatus(
                savedBarcode.getBarcodeValue(), "active");
        log.info("✓ Found by barcode value: {}", foundByBarcode.isPresent());
        
        // Test findByPlatformAndStatus
        var foundByPlatform = productRepository.findByPlatformAndStatus(Platform.SHOPEE, "active");
        log.info("✓ Found {} active SHOPEE barcodes", foundByPlatform.size());
        
        // Test findByCompanyAndStatus
        var foundByCompany = productRepository.findByCompanyAndStatus(
                savedBarcode.getCompany(), "active");
        log.info("✓ Found {} active barcodes for company", foundByCompany.size());
        
        // Clean up
        productRepository.deleteById(savedBarcode.getId());
        log.info("✓ Cleaned up demo barcode");
        
        log.info("=== BarcodeEntity MongoDB Connection Demo Complete ===");
    }
}