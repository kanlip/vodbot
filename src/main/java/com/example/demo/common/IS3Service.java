package com.example.demo.common;

import java.net.URI;

public interface IS3Service {
    public void uploadFile(String bucketName,
                           String key, URI filePathURI);
    public String getPresignedUri(String bucketName, String keyName);
}
