package com.example.demo.product.internal;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "shopee-product",
        url = "${shopee.product.url}/api/v2/product")
public interface IShopeeProductApi {

    @GetMapping("/get_item_list")
    ShopeeProductResponse getItemList(@SpringQueryMap SyncProductRequest syncProductRequest);
}
