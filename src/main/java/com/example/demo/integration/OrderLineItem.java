package com.example.demo.integration;

public record OrderLineItem(
    String skuId,
    String skuName,
    String quantity
) {
}
