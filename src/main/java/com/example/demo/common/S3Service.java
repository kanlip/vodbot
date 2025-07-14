package com.example.demo.common;

import com.example.demo.AwsConfiguration;
import com.example.demo.order.internal.Platform;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class S3Service implements IS3Service {

    private final S3Template s3Template;
    private final AwsConfiguration awsConfiguration;

    public void uploadFile(String key, InputStream inputStream) {
        String bucketName = awsConfiguration.bucket();
        s3Template.upload(bucketName, key, inputStream);
    }

    public String getPresignedUri(String fileName) {
        String bucketName = awsConfiguration.bucket();
        String userId = "user-id"; // Replace with actual user ID if needed
        String sellerId = "seller-id"; // Replace with actual seller ID if needed
        Platform platform = Platform.LAZADA;
        String orderId = "order-id"; // Replace with actual order ID if needed
        String packageId = "package-id"; // Replace with actual package ID if needed
        String currentDate = java.time.LocalDate.now().toString();
        String extension = "mp4"; // Replace with actual file extension if needed
        String keyName = String.format("%s/%s/%s/%s_%s_%s.%s", userId,
                currentDate,
                platform.name().toLowerCase(),
                sellerId,
                orderId,
                packageId,
                extension);

        String url = s3Template.createSignedPutURL(bucketName, keyName, Duration.ofMinutes(10)).toExternalForm();
        log.info("Presigned URL: [{}]", url);
        return url;
    }
}
