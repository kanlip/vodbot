package com.example.demo.common.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.common.S3Service;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {

    private final S3Service s3Service;
    
    @PostMapping("/presigned-uri")
    public String getPresignedUri() {
        
        return s3Service.getPresignedUri(
            "your-bucket-name", // Replace with your actual bucket name
            "your-video-key"    // Replace with the actual key for the video file
        );
    }

}
