package com.example.demo.platform.adapter.in.web;

import com.example.demo.platform.domain.BarcodeGenerationLog;
import com.example.demo.platform.domain.Product;
import com.example.demo.platform.port.in.BarcodeManagementUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/barcodes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Barcode Management", description = "Product barcode generation and lookup")
public class BarcodeController {

    private final BarcodeManagementUseCase barcodeManagementUseCase;

    @PostMapping("/products/{productId}/generate")
    @Operation(summary = "Generate barcode for product")
    public ResponseEntity<BarcodeResponse> generateBarcode(
            @PathVariable UUID productId,
            @RequestParam(required = false) Product.BarcodeType type) {

        String barcode = type != null
                ? barcodeManagementUseCase.generateBarcodeForProduct(productId, type)
                : barcodeManagementUseCase.generateBarcodeForProduct(productId);

        return ResponseEntity.ok(BarcodeResponse.builder()
                .barcode(barcode)
                .productId(productId)
                .message("Barcode generated successfully")
                .build());
    }

    @PostMapping("/products/{productId}/assign")
    @Operation(summary = "Manually assign barcode to product")
    public ResponseEntity<BarcodeResponse> assignBarcode(
            @PathVariable UUID productId,
            @RequestBody AssignBarcodeRequest request) {

        barcodeManagementUseCase.assignManualBarcode(
                productId,
                request.getBarcode(),
                request.getBarcodeType(),
                request.getUserId()
        );

        return ResponseEntity.ok(BarcodeResponse.builder()
                .barcode(request.getBarcode())
                .productId(productId)
                .message("Barcode assigned successfully")
                .build());
    }

    @GetMapping("/lookup/{barcode}")
    @Operation(summary = "Find product by barcode")
    public ResponseEntity<ProductLookupResponse> lookupProduct(@PathVariable String barcode) {
        log.info("Looking up product with barcode: {}", barcode);

        Optional<Product> product = barcodeManagementUseCase.findProductByBarcode(barcode);

        if (product.isPresent()) {
            return ResponseEntity.ok(ProductLookupResponse.from(product.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/validate/{barcode}")
    @Operation(summary = "Validate barcode format and uniqueness")
    public ResponseEntity<BarcodeValidationResponse> validateBarcode(
            @PathVariable String barcode,
            @RequestParam Product.BarcodeType type) {

        boolean isValid = barcodeManagementUseCase.isValidBarcode(barcode, type);
        boolean isUnique = barcodeManagementUseCase.isBarcodeUnique(barcode);

        return ResponseEntity.ok(BarcodeValidationResponse.builder()
                .barcode(barcode)
                .barcodeType(type)
                .validFormat(isValid)
                .unique(isUnique)
                .valid(isValid && isUnique)
                .build());
    }

    @GetMapping("/organizations/{orgId}/missing")
    @Operation(summary = "Get products without barcodes")
    public ResponseEntity<List<ProductWithoutBarcodeResponse>> getProductsWithoutBarcodes(
            @PathVariable UUID orgId) {

        List<Product> products = barcodeManagementUseCase.findProductsWithoutBarcodes(orgId);

        List<ProductWithoutBarcodeResponse> response = products.stream()
                .map(ProductWithoutBarcodeResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/organizations/{orgId}/generate-missing")
    @Operation(summary = "Generate barcodes for all products without barcodes")
    public ResponseEntity<BulkBarcodeGenerationResponse> generateMissingBarcodes(
            @PathVariable UUID orgId) {

        List<Product> productsWithoutBarcodes = barcodeManagementUseCase.findProductsWithoutBarcodes(orgId);
        List<UUID> productIds = productsWithoutBarcodes.stream()
                .map(Product::getId)
                .toList();

        barcodeManagementUseCase.generateBarcodesForProducts(productIds);

        return ResponseEntity.ok(BulkBarcodeGenerationResponse.builder()
                .totalProducts(productIds.size())
                .message("Barcode generation initiated for " + productIds.size() + " products")
                .build());
    }

    @GetMapping("/products/{productId}/history")
    @Operation(summary = "Get barcode generation history for product")
    public ResponseEntity<List<BarcodeHistoryResponse>> getBarcodeHistory(@PathVariable UUID productId) {
        List<BarcodeGenerationLog> history = barcodeManagementUseCase.getBarcodeHistory(productId);

        List<BarcodeHistoryResponse> response = history.stream()
                .map(BarcodeHistoryResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/bulk-generate")
    @Operation(summary = "Generate barcodes for specific products")
    public ResponseEntity<BulkBarcodeGenerationResponse> bulkGenerateBarcodes(
            @RequestBody BulkGenerateBarcodeRequest request) {

        barcodeManagementUseCase.generateBarcodesForProducts(request.getProductIds());

        return ResponseEntity.ok(BulkBarcodeGenerationResponse.builder()
                .totalProducts(request.getProductIds().size())
                .message("Barcode generation initiated for " + request.getProductIds().size() + " products")
                .build());
    }
}