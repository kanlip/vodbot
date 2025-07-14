package com.example.demo.product;

import com.example.demo.order.internal.Platform;
import com.example.demo.product.internal.CommonParameter;
import com.example.demo.product.internal.IShopeeProductApi;
import com.example.demo.product.internal.ShopeeProductResponse;
import com.example.demo.product.internal.SyncProductRequest;
import com.example.demo.product.mapper.CommonParameterMapper;
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
    @Override
    public void productSync(Platform platform, CommonParameter commonParameter) {
        switch (platform) {
            case SHOPEE -> {
                SyncProductRequest syncProductRequest = CommonParameterMapper
                        .INSTANCE
                        .toSyncProductRequest(commonParameter);

                ShopeeProductResponse productResponse = shopeeProduct.getItemList(syncProductRequest);
                if (productResponse == null || productResponse.getResponse() == null) {
                    log.warn("Failed to sync product for shop {}", commonParameter.getShopId());
                    throw new IllegalStateException("Failed to sync products from Shopee.");
                }
                productRepository.saveAll(Arrays.stream(productResponse.getResponse().getItem())
                        .map(ShopeeProductMapper.INSTANCE::toBarcodeEntity)
                        .toList());
                log.debug("Successfully synced products for shop {}", commonParameter.getShopId());
            }
            case LAZADA -> {
                // Implement Lazada product sync logic here
                // For example, you might call a Lazada API client similar to shopeeProduct
                throw new UnsupportedOperationException("Lazada product sync not implemented yet.");
            }
            case TIKTOK ->  {
                throw new UnsupportedOperationException("TIKTOK product sync not implemented yet.");
            }
            default -> throw new UnsupportedOperationException("product sync not implemented yet.");
        }
    }
}
