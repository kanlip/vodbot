package com.example.demo.platform.application;

import com.example.demo.platform.domain.BarcodeGenerationLog;
import com.example.demo.platform.domain.BarcodeGenerator;
import com.example.demo.platform.domain.Product;
import com.example.demo.platform.port.in.BarcodeManagementUseCase;
import com.example.demo.platform.port.out.BarcodeGenerationLogRepository;
import com.example.demo.platform.port.out.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BarcodeManagementService implements BarcodeManagementUseCase {

    private final ProductRepository productRepository;
    private final BarcodeGenerationLogRepository barcodeLogRepository;
    private final BarcodeGenerator barcodeGenerator;

    @Override
    public String generateBarcodeForProduct(UUID productId, Product.BarcodeType type) {
        log.info("Generating barcode for product: {} with type: {}", productId, type);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        if (product.hasBarcode()) {
            log.info("Product {} already has barcode: {}", productId, product.getBarcode());
            return product.getBarcode();
        }

        // Create validator for uniqueness check
        BarcodeGenerator.BarcodeValidator validator = this::isBarcodeUnique;

        // Generate unique barcode
        String barcode = barcodeGenerator.generateUniqueBarcode(product, type, validator);

        // Assign barcode to product
        product.assignBarcode(barcode, type);
        productRepository.save(product);

        // Log the generation
        BarcodeGenerationLog logGeneration = BarcodeGenerationLog.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .barcode(barcode)
                .barcodeType(type)
                .generationMethod(BarcodeGenerationLog.GenerationMethod.AUTO_GENERATED)
                .createdAt(Instant.now())
                .build();

        barcodeLogRepository.save(logGeneration);

        log.info("Generated barcode {} for product {}", barcode, product.getMasterSku());
        return barcode;
    }

    @Override
    public String generateBarcodeForProduct(UUID productId) {
        return generateBarcodeForProduct(productId, Product.BarcodeType.CODE128);
    }

    @Override
    public void assignManualBarcode(UUID productId, String barcode, Product.BarcodeType type, UUID userId) {
        log.info("Manually assigning barcode {} to product {} by user {}", barcode, productId, userId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        if (!isValidBarcode(barcode, type)) {
            throw new IllegalArgumentException("Invalid barcode format: " + barcode);
        }

        if (!isBarcodeUnique(barcode)) {
            throw new IllegalArgumentException("Barcode already exists: " + barcode);
        }

        // Assign barcode to product
        product.assignBarcode(barcode, type);
        productRepository.save(product);

        // Log the manual assignment
        BarcodeGenerationLog logGeneration = BarcodeGenerationLog.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .barcode(barcode)
                .barcodeType(type)
                .generationMethod(BarcodeGenerationLog.GenerationMethod.MANUAL)
                .generatedBy(userId)
                .createdAt(Instant.now())
                .build();

        barcodeLogRepository.save(logGeneration);

        log.info("Manually assigned barcode {} to product {}", barcode, product.getMasterSku());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findProductByBarcode(String barcode) {
        return productRepository.findByBarcode(barcode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findProductsWithoutBarcodes(UUID orgId) {
        return productRepository.findProductsWithoutBarcodes(orgId);
    }

    @Override
    public void generateBarcodesForProducts(List<UUID> productIds) {
        log.info("Generating barcodes for {} products", productIds.size());

        int generated = 0;
        int skipped = 0;

        for (UUID productId : productIds) {
            try {
                Product product = productRepository.findById(productId).orElse(null);
                if (product != null && product.needsBarcode()) {
                    generateBarcodeForProduct(productId);
                    generated++;
                } else {
                    skipped++;
                }
            } catch (Exception e) {
                log.error("Failed to generate barcode for product {}: {}", productId, e.getMessage());
            }
        }

        log.info("Barcode generation completed: {} generated, {} skipped", generated, skipped);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BarcodeGenerationLog> getBarcodeHistory(UUID productId) {
        return barcodeLogRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isValidBarcode(String barcode, Product.BarcodeType type) {
        if (barcode == null || barcode.trim().isEmpty()) {
            return false;
        }

        barcode = barcode.trim();

        return switch (type) {
            case CODE128 -> isValidCode128(barcode);
            case CODE39 -> isValidCode39(barcode);
            case EAN13 -> isValidEAN13(barcode);
            case EAN8 -> isValidEAN8(barcode);
            case UPC_A -> isValidUPCA(barcode);
            case UPC_E -> isValidUPCE(barcode);
        };
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isBarcodeUnique(String barcode) {
        return !barcodeLogRepository.existsByBarcode(barcode) &&
               productRepository.findByBarcode(barcode).isEmpty();
    }

    private boolean isValidCode128(String barcode) {
        // CODE128 can encode any ASCII character, length varies
        return barcode.length() >= 1 && barcode.length() <= 20 &&
               barcode.chars().allMatch(c -> c >= 32 && c <= 126);
    }

    private boolean isValidCode39(String barcode) {
        // CODE39 supports A-Z, 0-9, space, and -.$/+%
        String validChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 -.$/+%";
        return barcode.length() >= 1 && barcode.length() <= 20 &&
               barcode.chars().allMatch(c -> validChars.indexOf(c) >= 0);
    }

    private boolean isValidEAN13(String barcode) {
        if (barcode.length() != 13 || !barcode.matches("\\d+")) {
            return false;
        }

        // Verify check digit
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = Character.getNumericValue(barcode.charAt(i));
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int expectedCheckDigit = (10 - (sum % 10)) % 10;
        int actualCheckDigit = Character.getNumericValue(barcode.charAt(12));

        return expectedCheckDigit == actualCheckDigit;
    }

    private boolean isValidEAN8(String barcode) {
        if (barcode.length() != 8 || !barcode.matches("\\d+")) {
            return false;
        }

        // Verify check digit
        int sum = 0;
        for (int i = 0; i < 7; i++) {
            int digit = Character.getNumericValue(barcode.charAt(i));
            sum += (i % 2 == 0) ? digit * 3 : digit;
        }
        int expectedCheckDigit = (10 - (sum % 10)) % 10;
        int actualCheckDigit = Character.getNumericValue(barcode.charAt(7));

        return expectedCheckDigit == actualCheckDigit;
    }

    private boolean isValidUPCA(String barcode) {
        if (barcode.length() != 12 || !barcode.matches("\\d+")) {
            return false;
        }

        // Verify check digit
        int sum = 0;
        for (int i = 0; i < 11; i++) {
            int digit = Character.getNumericValue(barcode.charAt(i));
            sum += (i % 2 == 0) ? digit * 3 : digit;
        }
        int expectedCheckDigit = (10 - (sum % 10)) % 10;
        int actualCheckDigit = Character.getNumericValue(barcode.charAt(11));

        return expectedCheckDigit == actualCheckDigit;
    }

    private boolean isValidUPCE(String barcode) {
        return barcode.length() == 8 && barcode.matches("\\d+");
    }
}