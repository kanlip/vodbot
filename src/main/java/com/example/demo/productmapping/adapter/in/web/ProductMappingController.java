package com.example.demo.productmapping.adapter.in.web;

import com.example.demo.productmapping.domain.ProductMapping;
import com.example.demo.productmapping.port.in.ProductMappingUseCase;
import com.example.demo.shared.domain.Platform;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for Product Mapping operations.
 */
@RestController
@RequestMapping("/api/product-mappings")
@RequiredArgsConstructor
@Slf4j
public class ProductMappingController {
    
    private final ProductMappingUseCase productMappingUseCase;
    
    @GetMapping
    public ResponseEntity<List<ProductMapping>> getAllMappings() {
        List<ProductMapping> mappings = productMappingUseCase.findAll();
        return ResponseEntity.ok(mappings);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProductMapping> getMappingById(@PathVariable UUID id) {
        ProductMapping mapping = productMappingUseCase.findById(id);
        return ResponseEntity.ok(mapping);
    }
    
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<ProductMapping>> getMappingsBySeller(@PathVariable String sellerId) {
        List<ProductMapping> mappings = productMappingUseCase.findBySellerId(sellerId);
        return ResponseEntity.ok(mappings);
    }
    
    @GetMapping("/seller/{sellerId}/platform/{platform}")
    public ResponseEntity<List<ProductMapping>> getMappingsBySellerAndPlatform(
            @PathVariable String sellerId, 
            @PathVariable Platform platform) {
        List<ProductMapping> mappings = productMappingUseCase.findBySellerIdAndPlatform(sellerId, platform);
        return ResponseEntity.ok(mappings);
    }
    
    @PostMapping
    public ResponseEntity<ProductMapping> createMapping(@Valid @RequestBody ProductMapping productMapping) {
        try {
            ProductMapping created = productMappingUseCase.createMapping(productMapping);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid product mapping creation request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ProductMapping> updateMapping(
            @PathVariable UUID id, 
            @Valid @RequestBody ProductMapping productMapping) {
        try {
            ProductMapping updated = productMappingUseCase.updateMapping(id, productMapping);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid product mapping update request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMapping(@PathVariable UUID id) {
        productMappingUseCase.deleteMapping(id);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateMapping(@PathVariable UUID id) {
        productMappingUseCase.deactivateMapping(id);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/sync")
    public ResponseEntity<ProductMapping> syncFromPlatform(
            @RequestParam String sellerId,
            @RequestParam Platform platform,
            @RequestParam String platformProductId,
            @RequestParam String productName) {
        ProductMapping mapping = productMappingUseCase.syncFromPlatform(sellerId, platform, platformProductId, productName);
        return ResponseEntity.ok(mapping);
    }
    
    @GetMapping("/conflicts")
    public ResponseEntity<List<ProductMapping>> findConflicts(
            @RequestParam String sellerId,
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) String barcode) {
        List<ProductMapping> conflicts = productMappingUseCase.findConflictingMappings(sellerId, sku, barcode);
        return ResponseEntity.ok(conflicts);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}