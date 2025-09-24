package com.example.demo.platform.adapter.in.web;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class BulkGenerateBarcodeRequest {
    private List<UUID> productIds;
}