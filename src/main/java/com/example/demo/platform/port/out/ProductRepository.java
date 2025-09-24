package com.example.demo.platform.port.out;

import com.example.demo.platform.domain.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(UUID id);
    List<Product> findByOrgId(UUID orgId);
    List<Product> findByOrgIdAndStatus(UUID orgId, Product.ProductStatus status);
    Optional<Product> findByOrgIdAndMasterSku(UUID orgId, String masterSku);
    Optional<Product> findByBarcode(String barcode);
    List<Product> findByOrgIdAndCategory(UUID orgId, String category);
    List<Product> findByOrgIdAndBrand(UUID orgId, String brand);
    List<Product> findProductsWithoutBarcodes(UUID orgId);
    void deleteById(UUID id);
}