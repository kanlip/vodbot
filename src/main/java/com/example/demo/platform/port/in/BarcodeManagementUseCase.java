package com.example.demo.platform.port.in;

import com.example.demo.platform.domain.BarcodeGenerationLog;
import com.example.demo.platform.domain.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BarcodeManagementUseCase {
    String generateBarcodeForProduct(UUID productId, Product.BarcodeType type);
    String generateBarcodeForProduct(UUID productId); // Uses default type
    void assignManualBarcode(UUID productId, String barcode, Product.BarcodeType type, UUID userId);
    Optional<Product> findProductByBarcode(String barcode);
    List<Product> findProductsWithoutBarcodes(UUID orgId);
    void generateBarcodesForProducts(List<UUID> productIds);
    List<BarcodeGenerationLog> getBarcodeHistory(UUID productId);
    boolean isValidBarcode(String barcode, Product.BarcodeType type);
    boolean isBarcodeUnique(String barcode);
}