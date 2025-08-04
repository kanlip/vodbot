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
    void shouldReturnEmptyWhenBarcodeInactive() {
        // Given
        BarcodeEntity inactiveBarcode = BarcodeEntity.builder()
                .company(new ObjectId())
                .barcodeValue("INACTIVE123")
                .type("system_generated")
                .status("inactive")
                .platform(Platform.LAZADA)
                .platformProductId("PROD456")
                .platformSkuId("SKU456")
                .productName("Inactive Product")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        BarcodeEntity savedBarcode = productRepository.insert(inactiveBarcode);

        // When - Try to find active barcode (should not find it)
        Optional<BarcodeEntity> notFound = productRepository.findByBarcodeValueAndStatus("INACTIVE123", "active");

        // Then
        assertThat(notFound).isEmpty();

        // But should find it when searching for inactive
        Optional<BarcodeEntity> found = productRepository.findByBarcodeValueAndStatus("INACTIVE123", "inactive");
        assertThat(found).isPresent();

        // Cleanup
        productRepository.deleteById(savedBarcode.getId());
    }
}