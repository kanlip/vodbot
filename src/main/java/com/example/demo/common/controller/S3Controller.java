package com.example.demo.common.controller;

import com.example.demo.common.IS3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class S3Controller {

    private final IS3Service s3Service;

    @PostMapping("/presigned-uri")
    public String getPresignedUri(@RequestBody String fileName) {

        return s3Service.getPresignedUri(fileName);
    }

}
