package com.example.demo.common;

import java.io.InputStream;

public interface IS3Service {
    public void uploadFile(String key, InputStream inputStream);
    public String getPresignedUri(String keyName);
}
