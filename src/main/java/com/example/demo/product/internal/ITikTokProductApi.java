package com.example.demo.product.internal;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "tiktok-product",
        url = "${tiktok.product.url}"
)
public interface ITikTokProductApi {

    @PostMapping(value = "/product/202502/products/search", consumes = "application/json")
    TikTokProductResponse getProducts(
            @SpringQueryMap TikTokApiRequest request,
            @RequestHeader("Content-Type") String contentType,
            @RequestHeader("x-tts-access-token") String accessToken,
            @RequestBody TikTokSearchRequestBody requestBody
    );
}
