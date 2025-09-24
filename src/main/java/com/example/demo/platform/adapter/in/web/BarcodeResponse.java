package com.example.demo.platform.adapter.in.web;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class BarcodeResponse {
    private final String barcode;
    private final UUID productId;
    private final String message;
}