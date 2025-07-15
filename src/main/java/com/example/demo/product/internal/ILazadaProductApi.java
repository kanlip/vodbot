package com.example.demo.product.internal;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "lazada-product",
        url = "${lazada.product.url}"
)
public interface ILazadaProductApi {


    // Define methods for interacting with the Lazada product API here
    // For example, you might have methods to get product details, search products, etc.
    // Each method should be annotated with the appropriate HTTP method and endpoint.

    // Example:
     @GetMapping("/products/get")
     LazadaProductResponse getProducts(@SpringQueryMap LazadaSyncProductRequest request);

}
