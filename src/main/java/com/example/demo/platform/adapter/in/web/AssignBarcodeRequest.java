package com.example.demo.platform.adapter.in.web;

import com.example.demo.platform.domain.Product;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AssignBarcodeRequest {
    private String barcode;
    private Product.BarcodeType barcodeType;
    private UUID userId;
}