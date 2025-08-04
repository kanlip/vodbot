package com.example.demo.product;

import com.example.demo.common.Platform;
import com.example.demo.product.entity.BarcodeEntity;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
public class BarcodeEntityMongoIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private IProductRepository productRepository;

    @Test
    void shouldSaveAndRetrieveBarcodeEntity() {
        // Given
        BarcodeEntity barcode = BarcodeEntity.builder()
                .company(new ObjectId())
                .barcodeValue("TEST123456789")
                .type("system_generated")
                .status("active")
                .platform(Platform.SHOPEE)
                .platformProductId("PROD123")
                .platformSkuId("SKU123")
                .productName("Test Product")
                .variantDetails(BarcodeEntity.VariantDetails.builder()
                        .color("Red")
                        .size("L")
                        .build())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        // When - Save to MongoDB
        BarcodeEntity savedBarcode = productRepository.insert(barcode);

        // Then - Verify it was saved with an ID
        assertThat(savedBarcode.getId()).isNotNull();
        assertThat(savedBarcode.getBarcodeValue()).isEqualTo("TEST123456789");

        // When - Retrieve by barcode value and status
        Optional<BarcodeEntity> retrievedBarcode = productRepository.findByBarcodeValueAndStatus("TEST123456789", "active");

        // Then - Verify it was retrieved correctly
        assertThat(retrievedBarcode).isPresent();
        assertThat(retrievedBarcode.get().getId()).isEqualTo(savedBarcode.getId());
        assertThat(retrievedBarcode.get().getBarcodeValue()).isEqualTo("TEST123456789");
        assertThat(retrievedBarcode.get().getPlatform()).isEqualTo(Platform.SHOPEE);
        assertThat(retrievedBarcode.get().getVariantDetails().getColor()).isEqualTo("Red");

        // Cleanup
        productRepository.deleteById(savedBarcode.getId());
    }

    @Test
    void shouldReturnEmptyWhenBarcodeNotFound() {
        // When
        Optional<BarcodeEntity> notFound = productRepository.findByBarcodeValueAndStatus("NONEXISTENT", "active");

        // Then
        assertThat(notFound).isEmpty();
    }

    @Test
    void shouldCreateBarcodeWithListenerDefaults() {
        // Given - Create barcode entity without barcode value, type, or status
        BarcodeEntity barcode = BarcodeEntity.builder()
                .company(new ObjectId())
                .platform(Platform.TIKTOK)
                .platformProductId("PROD789")
                .platformSkuId("SKU789")
                .productName("Test Product with Defaults")
                .build();

        // When - Save to MongoDB (triggers entity listener)
        BarcodeEntity savedBarcode = productRepository.insert(barcode);

        // Then - Verify listener set defaults
        assertThat(savedBarcode.getBarcodeValue()).isNotNull();
        assertThat(savedBarcode.getBarcodeValue()).startsWith("SYS_");
        assertThat(savedBarcode.getType()).isEqualTo("system_generated");
        assertThat(savedBarcode.getStatus()).isEqualTo("active");
        assertThat(savedBarcode.getCreatedAt()).isNotNull();
        assertThat(savedBarcode.getUpdatedAt()).isNotNull();

        // Cleanup
        productRepository.deleteById(savedBarcode.getId());
    }

    @Test
    void shouldFindBarcodesByCompanyAndStatus() {
        // Given
        ObjectId companyId = new ObjectId();
        BarcodeEntity activeBarcode = BarcodeEntity.builder()
                .company(companyId)
                .barcodeValue("COMPANY_ACTIVE_123")
                .type("user_defined")
                .status("active")
                .platform(Platform.SHOPEE)
                .platformProductId("PROD_COMP_1")
                .platformSkuId("SKU_COMP_1")
                .productName("Company Active Product")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        BarcodeEntity inactiveBarcode = BarcodeEntity.builder()
                .company(companyId)
                .barcodeValue("COMPANY_INACTIVE_456")
                .type("user_defined")
                .status("inactive")
                .platform(Platform.SHOPEE)
                .platformProductId("PROD_COMP_2")
                .platformSkuId("SKU_COMP_2")
                .productName("Company Inactive Product")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        productRepository.insert(activeBarcode);
        productRepository.insert(inactiveBarcode);

        // When - Query by company and status
        List<BarcodeEntity> activeBarcodes = productRepository.findByCompanyAndStatus(companyId, "active");
        List<BarcodeEntity> inactiveBarcodes = productRepository.findByCompanyAndStatus(companyId, "inactive");

        // Then
        assertThat(activeBarcodes).hasSize(1);
        assertThat(activeBarcodes.get(0).getBarcodeValue()).isEqualTo("COMPANY_ACTIVE_123");
        
        assertThat(inactiveBarcodes).hasSize(1);
        assertThat(inactiveBarcodes.get(0).getBarcodeValue()).isEqualTo("COMPANY_INACTIVE_456");

        // Cleanup
        productRepository.deleteById(activeBarcode.getId());
        productRepository.deleteById(inactiveBarcode.getId());
    }

    @Test
    void shouldFindBarcodesByPlatformAndStatus() {
        // Given
        BarcodeEntity lazadaBarcode = BarcodeEntity.builder()
                .company(new ObjectId())
                .barcodeValue("LAZADA_123")
                .type("platform_sync")
                .status("active")
                .platform(Platform.LAZADA)
                .platformProductId("LAZADA_PROD_1")
                .platformSkuId("LAZADA_SKU_1")
                .productName("Lazada Product")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        productRepository.insert(lazadaBarcode);

        // When
        List<BarcodeEntity> lazadaBarcodes = productRepository.findByPlatformAndStatus(Platform.LAZADA, "active");

        // Then
        assertThat(lazadaBarcodes).isNotEmpty();
        assertThat(lazadaBarcodes).anyMatch(b -> b.getBarcodeValue().equals("LAZADA_123"));

        // Cleanup
        productRepository.deleteById(lazadaBarcode.getId());
    }
}