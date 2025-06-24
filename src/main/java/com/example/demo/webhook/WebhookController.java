package com.example.demo.webhook;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final @NonNull ApplicationEventPublisher event;

    @PostMapping("/lazada")
    @ResponseStatus(HttpStatus.OK)
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
            log.info("Lazada trade order received");
            event.publishEvent(tradeOrder);
            // handle trade order
        }
        return "Lazada webhook received";
    }
}
