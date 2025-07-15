package com.example.demo.product;

import com.example.demo.order.internal.Platform;
import com.example.demo.product.internal.*;
import com.example.demo.product.mapper.CommonParameterMapper;
import com.example.demo.product.mapper.LazadaProductMapper;
import com.example.demo.product.mapper.ShopeeProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;


@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSyncService implements IProductSync {
    private final IShopeeProductApi shopeeProduct;
    private final IProductRepository productRepository;
    private final ILazadaProductApi lazadaProduct;
    @Override
    public void productSync(Platform platform) {
        switch (platform) {
            case SHOPEE -> {
                ShopeeCommonParameter shopeeCommonParameter = ShopeeCommonParameter.builder()
                        .sign("")
                        .accessToken("")
                        .build();
                ShopeeSyncProductRequest shopeeSyncProductRequest = CommonParameterMapper
                        .INSTANCE
                        .toShopeeSyncProductRequest(shopeeCommonParameter);

                ShopeeProductResponse productResponse = shopeeProduct.getItemList(shopeeSyncProductRequest);
                if (productResponse == null || productResponse.getResponse() == null) {
                    log.warn("Failed to sync product for shop {}", shopeeCommonParameter.getShopId());
                    throw new IllegalStateException("Failed to sync products from Shopee.");
                }
                productRepository.insert(Arrays.stream(productResponse.getResponse().getItem())
                        .map(ShopeeProductMapper.INSTANCE::toBarcodeEntity)
                        .toList());
                log.debug("Successfully synced products for shop {}", shopeeCommonParameter.getShopId());
            }
            case LAZADA -> {
                LazadaCommonParameter lazadaCommonParameter = LazadaCommonParameter.builder()
                        .accessToken("")
                        .build();
                LazadaSyncProductRequest lazadaSyncProductRequest = CommonParameterMapper
                        .INSTANCE
                        .toLazadaSyncProductRequest(lazadaCommonParameter);
                LazadaProductResponse productResponse = lazadaProduct.getProducts(lazadaSyncProductRequest);
                productRepository.insert(
                        productResponse.getData().getProducts().stream()
                                .flatMap(product -> product.getSkus().stream()
                                        .map(sku -> LazadaProductMapper.INSTANCE.toBarcodeEntity(product, sku)))
                                .toList()
                );
                log.debug("Successfully synced products for shop");
            }
            case TIKTOK ->  {
                throw new UnsupportedOperationException("TIKTOK product sync not implemented yet.");
            }
            default -> throw new UnsupportedOperationException("product sync not implemented yet.");
        }
    }
}
