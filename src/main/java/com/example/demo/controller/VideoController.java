package com.example.demo.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.S3Service;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    private final S3Service s3Service;
    public VideoController(S3Service s3Service) {
        this.s3Service = s3Service;
       // Constructor logic if needed
    }
    
    @PostMapping("/presigned-uri")
    public String getPresignedUri() {
        
        return s3Service.getPresignedUri(
            "your-bucket-name", // Replace with your actual bucket name
            "your-video-key"    // Replace with the actual key for the video file
        );
    }

}
