package com.example.demo.shared.port.out;

import java.io.InputStream;

public interface IS3Service {
    public void uploadFile(String key, InputStream inputStream);
    public String getPresignedUri(String keyName);
    public String getPresignedUriForBarcode(String barcodeValue);
    public String getPresignedUriForPackage(String packageId);
}
