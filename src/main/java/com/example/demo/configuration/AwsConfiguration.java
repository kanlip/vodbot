package com.example.demo.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.transfer.s3.S3TransferManager;

@Configuration
public class AwsConfiguration {


    @Bean
    public S3AsyncClient s3AsyncClient() {
        return S3AsyncClient.crtBuilder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(Region.of("ap-southeast-7"))
                .targetThroughputInGbps(20.0)
                .minimumPartSizeInBytes(8 * 1025 * 1024L)
                .build();
    }

    @Bean
    public S3TransferManager transferManager() {
        return S3TransferManager.builder()
                .s3Client(s3AsyncClient())
                .build();
    }
}
