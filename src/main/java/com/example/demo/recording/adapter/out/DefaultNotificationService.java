package com.example.demo.recording.adapter.out;

import com.example.demo.recording.domain.VideoShare;
import com.example.demo.recording.port.out.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DefaultNotificationService implements NotificationService {

    @Override
    public void sendVideoShareEmail(String email, String shareUrl, String orderInfo) {
        log.info("Sending video share email to: {} for order: {} with URL: {}", email, orderInfo, shareUrl);

        // In a real implementation, this would use an email service like SendGrid, AWS SES, etc.
        log.info("Email sent successfully to: {}", email);
    }

    @Override
    public void sendVideoShareSMS(String phoneNumber, String shareUrl, String orderInfo) {
        log.info("Sending video share SMS to: {} for order: {} with URL: {}", phoneNumber, orderInfo, shareUrl);

        // In a real implementation, this would use an SMS service like Twilio, AWS SNS, etc.
        log.info("SMS sent successfully to: {}", phoneNumber);
    }

    @Override
    public void sendPlatformMessage(String platform, String customerId, String shareUrl, String orderInfo) {
        log.info("Sending platform message on {} to customer: {} for order: {} with URL: {}",
                platform, customerId, orderInfo, shareUrl);

        // In a real implementation, this would integrate with platform-specific APIs
        // For example: Shopee messaging API, Lazada messaging API, etc.
        log.info("Platform message sent successfully on {} to customer: {}", platform, customerId);
    }

    @Override
    public void notifyVideoReady(VideoShare videoShare) {
        log.info("Notifying that video is ready for share: {} of type: {}",
                videoShare.getShareToken(), videoShare.getShareType());

        switch (videoShare.getShareType()) {
            case EMAIL -> {
                if (videoShare.getRecipient() != null) {
                    sendVideoShareEmail(
                        videoShare.getRecipient(),
                        generateShareUrl(videoShare),
                        "Session " + videoShare.getSessionId()
                    );
                }
            }
            case SMS -> {
                if (videoShare.getRecipient() != null) {
                    sendVideoShareSMS(
                        videoShare.getRecipient(),
                        generateShareUrl(videoShare),
                        "Session " + videoShare.getSessionId()
                    );
                }
            }
            case PLATFORM_MESSAGE -> {
                if (videoShare.getRecipient() != null) {
                    // Extract platform from recipient or use default
                    sendPlatformMessage(
                        "shopee", // Default platform, should be extracted from context
                        videoShare.getRecipient(),
                        generateShareUrl(videoShare),
                        "Session " + videoShare.getSessionId()
                    );
                }
            }
            case CUSTOMER_LINK -> {
                log.info("Customer link ready: {}", generateShareUrl(videoShare));
            }
        }

        log.info("Video ready notification sent for share: {}", videoShare.getShareToken());
    }

    @Override
    public void notifyProcessingComplete(String email, String orderId) {
        log.info("Notifying processing complete to: {} for order: {}", email, orderId);

        // In a real implementation, this would send a completion email
        log.info("Processing completion notification sent to: {}", email);
    }

    @Override
    public void notifyProcessingFailed(String email, String orderId, String reason) {
        log.warn("Notifying processing failed to: {} for order: {} - Reason: {}", email, orderId, reason);

        // In a real implementation, this would send a failure notification email
        log.info("Processing failure notification sent to: {}", email);
    }

    private String generateShareUrl(VideoShare videoShare) {
        // In a real implementation, this would get the base URL from configuration
        String baseUrl = "https://vodbot.example.com";
        return videoShare.generateShareUrl(baseUrl);
    }
}