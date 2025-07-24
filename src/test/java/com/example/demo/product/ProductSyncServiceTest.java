package com.example.demo.product;

import com.example.demo.common.Platform;
import com.example.demo.product.internal.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductSyncServiceTest {

    @Mock
    private IShopeeProductApi shopeeProduct;

    @Mock
    private IProductRepository productRepository;

    @Mock
    private ILazadaProductApi lazadaProduct;

    @Mock
    private ITikTokProductApi tikTokProduct;

    @InjectMocks
    private ProductSyncService productSyncService;

    @Test
    void testTikTokProductSync_Success() {
        // Arrange
        TikTokProductResponse.TikTokSku sku = new TikTokProductResponse.TikTokSku();
        sku.setId("sku123");
        sku.setSellerSku("seller-sku-123");

        TikTokProductResponse.TikTokProduct product = new TikTokProductResponse.TikTokProduct();
        product.setProductId("product123");
        product.setProductName("Test Product");
        product.setProductStatus("LIVE");
        product.setSkus(Arrays.asList(sku));

        TikTokProductResponse.TikTokProductData data = new TikTokProductResponse.TikTokProductData();
        data.setProducts(Arrays.asList(product));
        data.setTotalCount(1);

        TikTokProductResponse response = new TikTokProductResponse();
        response.setCode(0);
        response.setMessage("success");
        response.setData(data);

        when(tikTokProduct.getProducts(any(TikTokApiRequest.class), 
                eq("application/json"), eq(""), any(TikTokSearchRequestBody.class)))
                .thenReturn(response);

        // Act
        assertDoesNotThrow(() -> productSyncService.productSync(Platform.TIKTOK));

        // Assert
        verify(tikTokProduct, times(1)).getProducts(any(TikTokApiRequest.class), 
                eq("application/json"), eq(""), any(TikTokSearchRequestBody.class));
        verify(productRepository, times(1)).insert(anyList());
    }

    @Test
    void testTikTokProductSync_ApiFailure() {
        // Arrange
        TikTokProductResponse response = new TikTokProductResponse();
        response.setCode(1);
        response.setMessage("error");
        response.setData(null);

        when(tikTokProduct.getProducts(any(TikTokApiRequest.class), 
                eq("application/json"), eq(""), any(TikTokSearchRequestBody.class)))
                .thenReturn(response);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> productSyncService.productSync(Platform.TIKTOK));

        assertEquals("Failed to sync products from TikTok Shop.", exception.getMessage());
        verify(tikTokProduct, times(1)).getProducts(any(TikTokApiRequest.class), 
                eq("application/json"), eq(""), any(TikTokSearchRequestBody.class));
        verify(productRepository, never()).insert(anyList());
    }

    @Test
    void testTikTokProductSync_NullResponse() {
        // Arrange
        when(tikTokProduct.getProducts(any(TikTokApiRequest.class), 
                eq("application/json"), eq(""), any(TikTokSearchRequestBody.class)))
                .thenReturn(null);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> productSyncService.productSync(Platform.TIKTOK));

        assertEquals("Failed to sync products from TikTok Shop.", exception.getMessage());
        verify(tikTokProduct, times(1)).getProducts(any(TikTokApiRequest.class), 
                eq("application/json"), eq(""), any(TikTokSearchRequestBody.class));
        verify(productRepository, never()).insert(anyList());
    }
}
