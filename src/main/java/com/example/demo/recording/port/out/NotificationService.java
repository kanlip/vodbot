package com.example.demo.recording.port.out;

import com.example.demo.recording.domain.VideoShare;

public interface NotificationService {
    void sendVideoShareEmail(String email, String shareUrl, String orderInfo);
    void sendVideoShareSMS(String phoneNumber, String shareUrl, String orderInfo);
    void sendPlatformMessage(String platform, String customerId, String shareUrl, String orderInfo);
    void notifyVideoReady(VideoShare videoShare);
    void notifyProcessingComplete(String email, String orderId);
    void notifyProcessingFailed(String email, String orderId, String reason);
}