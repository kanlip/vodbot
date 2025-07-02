package com.example.demo.webhook;


import com.example.demo.TiktokConfiguration;
import com.example.demo.webhook.internal.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final @NonNull HandleWebhook handleWebhook;
    private final @NonNull TiktokConfiguration tiktokConfiguration;
    private final @NonNull ObjectMapper objectMapper;



    @PostMapping("/lazada")
    @ResponseStatus(HttpStatus.OK)
    public String lazadaWebhook(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody LazadaWebhookData<Map<String, Object>> webhookData
            ) {
        log.debug("Lazada webhook received");
        Map<String, Object> data = webhookData.data();

        switch (webhookData.messageType()) {
            case LazadaMessageType.TRADE_ORDER_NOTIFICATION ->
                handleWebhook.handle(data, LazadaTradeOrderEvent.class);
            case LazadaMessageType.REVERSE_ORDER ->
                handleWebhook.handle(data, LazadaReverseOrderEvent.class);
            default -> throw new NotImplementedException("Unsupported message type: " + webhookData.messageType());
        }
        return "Lazada webhook received";
    }

    @PostMapping("/shopee")
    @ResponseStatus(HttpStatus.OK)
    public String shopee(
            @RequestBody ShopeeWebhookData<Map<String, Object>> webhookData
    ) {
        log.debug("Shopee webhook received");
        Map<String, Object> data = webhookData.data();
        switch(webhookData.code()) {
            case ORDER_STATUS_PUSH -> handleWebhook.handle(data, ShopeeOrderEvent.class);
            default -> throw new NotImplementedException("Unsupported message type: " + webhookData.code());
        }
        return "Shopee webhook received";
    }

    @PostMapping("/tiktok")
    @ResponseStatus(HttpStatus.OK)
    public String tiktok(
            @RequestHeader("Authorization") String signature,
            @RequestBody String payload
    ) {
        log.debug("TikTok webhook received");

        if (!tiktokConfiguration.verifySignature(signature, payload)) {
            log.error("Invalid TikTok webhook signature");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid signature");
        }
        try {
            TiktokWebhookData<Map<String, Object>> webhookData = objectMapper
                    .readValue(payload, new TypeReference<>() {});
            log.info("Received TikTok webhook data: {}", webhookData);
            // Handle webhook data here
            switch (webhookData.type()) {
                case ORDER_STATUS -> handleWebhook.handle(webhookData.data(), TiktokOrderStatusEvent.class);
                default -> throw new NotImplementedException("Unsupported TikTok message type: " + webhookData.type());
            }
            return "TikTok webhook received";
        } catch (JsonProcessingException e) {
            log.error("Error processing TikTok webhook", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payload");
        }
    }
}
