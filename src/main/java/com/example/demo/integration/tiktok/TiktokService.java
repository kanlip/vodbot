package com.example.demo.integration.tiktok;

import com.example.demo.integration.OrderIntegrationRepository;
import com.example.demo.integration.OrderLineItem;

import java.util.List;

public class TiktokService implements OrderIntegrationRepository<OrderLineItem, String> {

    @Override
    public List<OrderLineItem> getOrdersByTrackingId(String packageId) {
        // Implement the logic to fetch orders from TikTok using the tracking ID
        // This is a placeholder implementation
        Order or = new Order(1, "Sample Order Info", "Sample Request ID", null);
        List<OrderInfo> infos = or.data().orderInfoList();
        return infos.stream()
                .flatMap(info -> info.skuList().stream()
                        .map(sku -> new OrderLineItem(sku.skuId(), sku.skuName(), sku.quantity())))
                .toList();
    }
}
