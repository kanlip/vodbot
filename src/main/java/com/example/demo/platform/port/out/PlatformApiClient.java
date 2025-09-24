package com.example.demo.platform.port.out;

import java.util.List;

public interface PlatformApiClient {
    List<PlatformProductData> fetchAllProducts();
    PlatformProductData fetchProductById(String productId);
    List<PlatformOrderData> fetchOrders(String since);
    PlatformOrderData fetchOrderById(String orderId);
    boolean validateWebhookSignature(String payload, String signature, String secret);
}