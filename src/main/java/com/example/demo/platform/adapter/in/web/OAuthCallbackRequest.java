package com.example.demo.platform.adapter.in.web;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OAuthCallbackRequest {
    private String state;
    private String code;
    private String shopId;
    private String error;
    private String errorDescription;
}