package com.example.demo.webhook.controller;


import com.example.demo.webhook.model.lazada.LazadaReverseOrderData;
import com.example.demo.webhook.model.lazada.LazadaTradeOrderData;
import com.example.demo.webhook.model.lazada.LazadaWebhookData;
import com.example.demo.webhook.service.WebhookEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final WebhookEvent webhookEvent;

    @RequestMapping("/lazada")
    public String lazadaWebhook(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody LazadaWebhookData<Map<String, Object>> webhookData
            ) {
        log.debug("Lazada webhook received");
        Map<String, Object> data = webhookData.data();
        ObjectMapper mapper = new ObjectMapper();

        if (data.containsKey("reverse_order_id")) {
            LazadaReverseOrderData reverseOrder = mapper.convertValue(data, LazadaReverseOrderData.class);
            // handle reverse order
        } else {
            LazadaTradeOrderData tradeOrder = mapper.convertValue(data, LazadaTradeOrderData.class);
            webhookEvent.publishWebhookEvent(tradeOrder);
            // handle trade order
        }
        return "Lazada webhook received";
    }
}
