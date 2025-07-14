package com.example.demo.product.internal;


import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "lazada-product",
        url = "${lazada.product.url}/api/v2/product"
)
public interface ILazadaProductApi {
}
