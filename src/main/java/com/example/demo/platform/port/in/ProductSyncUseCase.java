package com.example.demo.platform.port.in;

import com.example.demo.platform.domain.Product;
import com.example.demo.platform.domain.PlatformProduct;
import com.example.demo.platform.domain.ProductMatchingRule;

import java.util.List;
import java.util.UUID;

public interface ProductSyncUseCase {
    void syncAllProducts(UUID integrationId);
    void syncProductById(UUID integrationId, String platformProductId);
    Product importPlatformProduct(ImportProductCommand command);
    List<Product> getOrganizationProducts(UUID orgId);
    Product getProduct(UUID productId);
    List<PlatformProduct> getProductPlatformMappings(UUID productId);

    ProductMatchingRule createMatchingRule(CreateMatchingRuleCommand command);
    List<ProductMatchingRule> getOrganizationMatchingRules(UUID orgId);
    void updateMatchingRule(UUID ruleId, UpdateMatchingRuleCommand command);
    void deleteMatchingRule(UUID ruleId);
}