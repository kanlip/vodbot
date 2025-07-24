package com.example.demo.product;

import com.example.demo.common.Platform;
import com.example.demo.product.internal.*;
import com.example.demo.product.mapper.CommonParameterMapper;
import com.example.demo.product.mapper.LazadaProductMapper;
import com.example.demo.product.mapper.ShopeeProductMapper;
import com.example.demo.product.mapper.TikTokProductMapper;
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
    private final ITikTokProductApi tikTokProduct;
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
            case TIKTOK -> {
                // Create consolidated request object with all query parameters
                TikTokApiRequest tikTokApiRequest = TikTokApiRequest.builder()
                        .shopCipher("")
                        .appKey("")
                        .appSecret("")
                        .accessToken("")
                        .shopId("")
                        .version("202309")
                        .timestamp(String.valueOf(System.currentTimeMillis() / 1000))
                        .sign("")
                        .pageSize(20)
                        .pageToken(null)
                        .build();

                // Create request body with search parameters
                TikTokSearchRequestBody requestBody = TikTokSearchRequestBody.builder()
                        .status("LIVE")
                        .build();

                TikTokProductResponse productResponse = tikTokProduct.getProducts(
                        tikTokApiRequest,
                        "application/json", // content-type
                        tikTokApiRequest.getAccessToken(),
                        requestBody
                );

                if (productResponse == null || productResponse.getData() == null || productResponse.getCode() != 0) {
                    log.warn("Failed to sync product for TikTok shop {}", tikTokApiRequest.getShopId());
                    throw new IllegalStateException("Failed to sync products from TikTok Shop.");
                }

                productRepository.insert(
                        productResponse.getData().getProducts().stream()
                                .flatMap(product -> product.getSkus().stream()
                                        .map(sku -> TikTokProductMapper.INSTANCE.toBarcodeEntity(product, sku)))
                                .toList()
                );
                log.debug("Successfully synced {} products for TikTok shop {}", 
                         productResponse.getData().getProducts().size(), tikTokApiRequest.getShopId());
            }
            default -> throw new UnsupportedOperationException("product sync not implemented yet.");
        }
    }
}
