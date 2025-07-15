package com.example.demo.common.controller;

import com.example.demo.common.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/s3")
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;
    
    @PostMapping("/presigned-uri")
    public String getPresignedUri() {

        return s3Service.getPresignedUri("test-key.txt");
    }

}
