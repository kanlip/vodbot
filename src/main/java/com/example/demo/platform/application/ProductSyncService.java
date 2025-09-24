package com.example.demo.platform.application;

import com.example.demo.platform.domain.*;
import com.example.demo.platform.port.in.*;
import com.example.demo.platform.port.out.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductSyncService implements ProductSyncUseCase {

    private final ProductRepository productRepository;
    private final PlatformProductRepository platformProductRepository;
    private final ProductMatchingRuleRepository matchingRuleRepository;
    private final PlatformIntegrationRepository integrationRepository;
    private final PlatformApiClientFactory apiClientFactory;
    private final ProductMatcher productMatcher;
    private final BarcodeManagementUseCase barcodeManagementUseCase;

    @Override
    public void syncAllProducts(UUID integrationId) {
        log.info("Starting full product sync for integration: {}", integrationId);

        PlatformIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new IllegalArgumentException("Integration not found: " + integrationId));

        if (!integration.isAuthorized()) {
            throw new IllegalStateException("Integration is not authorized: " + integrationId);
        }

        try {
            PlatformApiClient apiClient = apiClientFactory.createClient(integration.getPlatform(), integration);
            List<PlatformProductData> platformProducts = apiClient.fetchAllProducts();

            log.info("Fetched {} products from {}", platformProducts.size(), integration.getPlatform());

            int imported = 0;
            int duplicates = 0;

            for (PlatformProductData productData : platformProducts) {
                try {
                    ImportProductCommand command = ImportProductCommand.builder()
                            .integrationId(integrationId)
                            .platformProductId(productData.getId())
                            .platformSku(productData.getSku())
                            .productName(productData.getName())
                            .description(productData.getDescription())
                            .category(productData.getCategory())
                            .brand(productData.getBrand())
                            .price(productData.getPrice())
                            .currency(productData.getCurrency())
                            .stockQuantity(productData.getStockQuantity())
                            .imageUrls(productData.getImageUrls())
                            .attributes(productData.getAttributes())
                            .platformData(productData.getRawData())
                            .build();

                    Product result = importPlatformProduct(command);
                    if (result != null) {
                        imported++;
                    } else {
                        duplicates++;
                    }

                } catch (Exception e) {
                    log.error("Failed to import product {}: {}", productData.getId(), e.getMessage());
                }
            }

            integration.updateSyncStatus(PlatformIntegration.SyncStatus.SUCCESS);
            integrationRepository.save(integration);

            log.info("Product sync completed for integration {}: {} imported, {} duplicates found",
                    integrationId, imported, duplicates);

        } catch (Exception e) {
            log.error("Product sync failed for integration {}: {}", integrationId, e.getMessage(), e);
            integration.updateSyncStatus(PlatformIntegration.SyncStatus.FAILED);
            integrationRepository.save(integration);
            throw e;
        }
    }

    @Override
    public void syncProductById(UUID integrationId, String platformProductId) {
        log.info("Syncing single product {} for integration {}", platformProductId, integrationId);

        PlatformIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new IllegalArgumentException("Integration not found: " + integrationId));

        PlatformApiClient apiClient = apiClientFactory.createClient(integration.getPlatform(), integration);
        PlatformProductData productData = apiClient.fetchProductById(platformProductId);

        ImportProductCommand command = ImportProductCommand.builder()
                .integrationId(integrationId)
                .platformProductId(productData.getId())
                .platformSku(productData.getSku())
                .productName(productData.getName())
                .description(productData.getDescription())
                .category(productData.getCategory())
                .brand(productData.getBrand())
                .price(productData.getPrice())
                .currency(productData.getCurrency())
                .stockQuantity(productData.getStockQuantity())
                .imageUrls(productData.getImageUrls())
                .attributes(productData.getAttributes())
                .platformData(productData.getRawData())
                .build();

        importPlatformProduct(command);
    }

    @Override
    public Product importPlatformProduct(ImportProductCommand command) {
        log.info("Importing product {} from integration {}",
                command.getPlatformProductId(), command.getIntegrationId());

        PlatformIntegration integration = integrationRepository.findById(command.getIntegrationId())
                .orElseThrow(() -> new IllegalArgumentException("Integration not found: " + command.getIntegrationId()));

        // Check if platform product already exists
        Optional<PlatformProduct> existingPlatformProduct = platformProductRepository
                .findByIntegrationIdAndPlatformProductId(command.getIntegrationId(), command.getPlatformProductId());

        if (existingPlatformProduct.isPresent()) {
            // Update existing platform product
            PlatformProduct platformProduct = existingPlatformProduct.get();
            platformProduct.updateFromPlatform(
                    command.getProductName(),
                    command.getPrice(),
                    command.getStockQuantity(),
                    "ACTIVE",
                    command.getPlatformData()
            );
            platformProductRepository.save(platformProduct);

            return productRepository.findById(platformProduct.getProductId()).orElse(null);
        }

        // Create candidate product for matching
        Product candidate = Product.builder()
                .orgId(integration.getOrgId())
                .masterSku(generateMasterSku(command.getPlatformSku(), integration.getPlatform().name()))
                .productName(command.getProductName())
                .description(command.getDescription())
                .category(command.getCategory())
                .brand(command.getBrand())
                .attributes(command.getAttributes())
                .status(Product.ProductStatus.ACTIVE)
                .build();

        // Try to find existing product match
        List<Product> existingProducts = productRepository.findByOrgId(integration.getOrgId());
        List<ProductMatchingRule> matchingRules = matchingRuleRepository.findActiveByOrgId(integration.getOrgId());

        Optional<Product> matchedProduct = productMatcher.findBestMatch(candidate, existingProducts, matchingRules);

        Product product;
        if (matchedProduct.isPresent()) {
            // Found a match - use existing product
            product = matchedProduct.get();
            log.info("Product {} matched to existing product: {}", command.getPlatformProductId(), product.getMasterSku());
        } else {
            // No match found - create new product
            candidate.setId(UUID.randomUUID());
            candidate.setCreatedAt(Instant.now());
            candidate.setUpdatedAt(Instant.now());
            product = productRepository.save(candidate);

            // Create images for new product
            if (command.getImageUrls() != null && !command.getImageUrls().isEmpty()) {
                createProductImages(product.getId(), command.getImageUrls());
            }

            // Generate barcode for new product
            try {
                barcodeManagementUseCase.generateBarcodeForProduct(product.getId());
                log.info("Generated barcode for new product: {}", product.getMasterSku());
            } catch (Exception e) {
                log.warn("Failed to generate barcode for product {}: {}", product.getMasterSku(), e.getMessage());
            }

            log.info("Created new product: {} for platform product: {}",
                    product.getMasterSku(), command.getPlatformProductId());
        }

        // Create platform product mapping
        PlatformProduct platformProduct = PlatformProduct.builder()
                .id(UUID.randomUUID())
                .productId(product.getId())
                .platformIntegrationId(command.getIntegrationId())
                .platformProductId(command.getPlatformProductId())
                .platformSku(command.getPlatformSku())
                .platformName(command.getProductName())
                .platformPrice(command.getPrice())
                .currency(command.getCurrency())
                .stockQuantity(command.getStockQuantity())
                .platformBarcode(command.getPlatformBarcode())
                .platformStatus("ACTIVE")
                .platformData(command.getPlatformData())
                .syncStatus(PlatformProduct.SyncStatus.SUCCESS)
                .lastSyncedAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        platformProductRepository.save(platformProduct);

        return product;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getOrganizationProducts(UUID orgId) {
        return productRepository.findByOrgId(orgId);
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProduct(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlatformProduct> getProductPlatformMappings(UUID productId) {
        return platformProductRepository.findByProductId(productId);
    }

    @Override
    public ProductMatchingRule createMatchingRule(CreateMatchingRuleCommand command) {
        ProductMatchingRule rule = ProductMatchingRule.builder()
                .id(UUID.randomUUID())
                .orgId(command.getOrgId())
                .ruleName(command.getRuleName())
                .matchFields(command.getMatchFields())
                .similarityThreshold(command.getSimilarityThreshold())
                .active(true)
                .priority(command.getPriority())
                .createdAt(Instant.now())
                .build();

        return matchingRuleRepository.save(rule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductMatchingRule> getOrganizationMatchingRules(UUID orgId) {
        return matchingRuleRepository.findByOrgId(orgId);
    }

    @Override
    public void updateMatchingRule(UUID ruleId, UpdateMatchingRuleCommand command) {
        ProductMatchingRule rule = matchingRuleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Matching rule not found: " + ruleId));

        if (command.getRuleName() != null) {
            rule.setRuleName(command.getRuleName());
        }
        if (command.getMatchFields() != null) {
            rule.setMatchFields(command.getMatchFields());
        }
        if (command.getSimilarityThreshold() != null) {
            rule.setSimilarityThreshold(command.getSimilarityThreshold());
        }
        if (command.getPriority() != null) {
            rule.setPriority(command.getPriority());
        }
        if (command.getActive() != null) {
            rule.setActive(command.getActive());
        }

        matchingRuleRepository.save(rule);
    }

    @Override
    public void deleteMatchingRule(UUID ruleId) {
        matchingRuleRepository.deleteById(ruleId);
    }

    private String generateMasterSku(String platformSku, String platform) {
        if (platformSku != null && !platformSku.isEmpty()) {
            return String.format("%s-%s", platform, platformSku).toUpperCase();
        }
        return String.format("%s-%s", platform, UUID.randomUUID().toString().substring(0, 8)).toUpperCase();
    }

    private void createProductImages(UUID productId, List<String> imageUrls) {
        IntStream.range(0, imageUrls.size())
                .forEach(i -> {
                    ProductImage image = ProductImage.builder()
                            .id(UUID.randomUUID())
                            .productId(productId)
                            .imageUrl(imageUrls.get(i))
                            .sortOrder(i)
                            .primary(i == 0) // First image is primary
                            .createdAt(Instant.now())
                            .build();

                    // Note: Would need ProductImageRepository to save this
                    // productImageRepository.save(image);
                });
    }
}