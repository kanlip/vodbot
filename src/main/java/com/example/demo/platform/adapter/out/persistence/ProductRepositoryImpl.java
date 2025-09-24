package com.example.demo.platform.adapter.out.persistence;

import com.example.demo.platform.domain.Product;
import com.example.demo.platform.port.out.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository jpaRepository;
    private final ProductMapper mapper;

    @Override
    public Product save(Product product) {
        ProductEntity entity = mapper.toEntity(product);
        ProductEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Product> findByOrgId(UUID orgId) {
        return jpaRepository.findByOrgId(orgId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Product> findByOrgIdAndStatus(UUID orgId, Product.ProductStatus status) {
        return jpaRepository.findByOrgIdAndStatus(orgId, status)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Product> findByOrgIdAndMasterSku(UUID orgId, String masterSku) {
        return jpaRepository.findByOrgIdAndMasterSku(orgId, masterSku)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Product> findByBarcode(String barcode) {
        return jpaRepository.findByBarcode(barcode)
                .map(mapper::toDomain);
    }

    @Override
    public List<Product> findByOrgIdAndCategory(UUID orgId, String category) {
        return jpaRepository.findByOrgIdAndCategory(orgId, category)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Product> findByOrgIdAndBrand(UUID orgId, String brand) {
        return jpaRepository.findByOrgIdAndBrand(orgId, brand)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Product> findProductsWithoutBarcodes(UUID orgId) {
        return jpaRepository.findProductsWithoutBarcodes(orgId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}