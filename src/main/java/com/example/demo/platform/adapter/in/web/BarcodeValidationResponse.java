package com.example.demo.platform.adapter.in.web;

import com.example.demo.platform.domain.Product;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BarcodeValidationResponse {
    private final String barcode;
    private final Product.BarcodeType barcodeType;
    private final boolean validFormat;
    private final boolean unique;
    private final boolean valid; // validFormat && unique
}