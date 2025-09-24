package com.example.demo.platform.adapter.out.api;

import com.example.demo.platform.domain.PlatformIntegration;
import com.example.demo.platform.port.out.PlatformApiClient;
import com.example.demo.platform.port.out.PlatformOrderData;
import com.example.demo.platform.port.out.PlatformProductData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class ShopeeApiClient implements PlatformApiClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final PlatformIntegration integration;

    public ShopeeApiClient(RestTemplate restTemplate, ObjectMapper objectMapper, PlatformIntegration integration) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.integration = integration;
    }

    private static final String SHOPEE_BASE_URL = "https://partner.shopeemobile.com";
    private static final String API_VERSION = "/api/v2";


    @Override
    public List<PlatformProductData> fetchAllProducts() {
        log.info("Fetching all products from Shopee for shop: {}", integration.getShopId());

        try {
            // First get item list
            String itemListPath = "/product/get_item_list";
            Map<String, Object> itemListParams = new HashMap<>();
            itemListParams.put("offset", 0);
            itemListParams.put("page_size", 100);
            itemListParams.put("update_time_from", Instant.now().minusSeconds(86400 * 30).getEpochSecond()); // Last 30 days
            itemListParams.put("update_time_to", Instant.now().getEpochSecond());
            itemListParams.put("item_status", Arrays.asList("NORMAL", "BANNED", "DELETED", "UNLIST"));

            String itemListUrl = buildSignedUrl(itemListPath, itemListParams);

            @SuppressWarnings("unchecked")
            Map<String, Object> itemListResponse = restTemplate.getForObject(itemListUrl, Map.class);

            if (!isSuccessResponse(itemListResponse)) {
                throw new RuntimeException("Failed to fetch item list from Shopee: " + itemListResponse);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> response = (Map<String, Object>) itemListResponse.get("response");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("item");

            if (items == null || items.isEmpty()) {
                log.info("No products found in Shopee shop: {}", integration.getShopId());
                return new ArrayList<>();
            }

            // Get item IDs for detailed fetch
            List<Long> itemIds = items.stream()
                    .map(item -> ((Number) item.get("item_id")).longValue())
                    .collect(Collectors.toList());

            // Fetch detailed item info
            return fetchItemDetails(itemIds);

        } catch (Exception e) {
            log.error("Error fetching products from Shopee: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch products from Shopee", e);
        }
    }

    @Override
    public PlatformProductData fetchProductById(String productId) {
        log.info("Fetching product {} from Shopee", productId);

        try {
            List<Long> itemIds = Arrays.asList(Long.parseLong(productId));
            List<PlatformProductData> products = fetchItemDetails(itemIds);

            return products.isEmpty() ? null : products.get(0);

        } catch (Exception e) {
            log.error("Error fetching product {} from Shopee: {}", productId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch product from Shopee", e);
        }
    }

    @Override
    public List<PlatformOrderData> fetchOrders(String since) {
        log.info("Fetching orders from Shopee since: {}", since);

        try {
            String orderListPath = "/order/get_order_list";
            Map<String, Object> params = new HashMap<>();
            params.put("time_range_field", "update_time");
            params.put("time_from", Long.parseLong(since));
            params.put("time_to", Instant.now().getEpochSecond());
            params.put("page_size", 100);
            params.put("cursor", "");
            params.put("order_status", "READY_TO_SHIP");

            String url = buildSignedUrl(orderListPath, params);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (!isSuccessResponse(response)) {
                throw new RuntimeException("Failed to fetch orders from Shopee: " + response);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> responseData = (Map<String, Object>) response.get("response");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> orders = (List<Map<String, Object>>) responseData.get("order_list");

            return orders.stream()
                    .map(this::mapToOrderData)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error fetching orders from Shopee: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch orders from Shopee", e);
        }
    }

    @Override
    public PlatformOrderData fetchOrderById(String orderId) {
        log.info("Fetching order {} from Shopee", orderId);

        try {
            String orderDetailsPath = "/order/get_order_detail";
            Map<String, Object> params = new HashMap<>();
            params.put("order_sn_list", Arrays.asList(orderId));

            String url = buildSignedUrl(orderDetailsPath, params);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (!isSuccessResponse(response)) {
                throw new RuntimeException("Failed to fetch order from Shopee: " + response);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> responseData = (Map<String, Object>) response.get("response");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> orders = (List<Map<String, Object>>) responseData.get("order_list");

            return orders.isEmpty() ? null : mapToOrderData(orders.get(0));

        } catch (Exception e) {
            log.error("Error fetching order {} from Shopee: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch order from Shopee", e);
        }
    }

    @Override
    public boolean validateWebhookSignature(String payload, String signature, String secret) {
        try {
            String expectedSignature = generateShopeeSignature(payload, secret);
            return expectedSignature.equals(signature);
        } catch (Exception e) {
            log.error("Error validating Shopee webhook signature: {}", e.getMessage());
            return false;
        }
    }

    private List<PlatformProductData> fetchItemDetails(List<Long> itemIds) {
        String itemInfoPath = "/product/get_item_base_info";
        Map<String, Object> params = new HashMap<>();
        params.put("item_id_list", itemIds);
        params.put("need_tax_info", false);
        params.put("need_complaint_policy", false);

        String url = buildSignedUrl(itemInfoPath, params);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (!isSuccessResponse(response)) {
            throw new RuntimeException("Failed to fetch item details from Shopee: " + response);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> responseData = (Map<String, Object>) response.get("response");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) responseData.get("item_list");

        return items.stream()
                .map(this::mapToProductData)
                .collect(Collectors.toList());
    }

    private PlatformProductData mapToProductData(Map<String, Object> item) {
        return PlatformProductData.builder()
                .id(String.valueOf(item.get("item_id")))
                .sku(String.valueOf(item.get("item_sku")))
                .name(String.valueOf(item.get("item_name")))
                .description(String.valueOf(item.getOrDefault("description", "")))
                .category(extractCategoryName(item))
                .brand(String.valueOf(item.getOrDefault("brand", "")))
                .price(extractPrice(item))
                .currency("THB") // Shopee Thailand
                .stockQuantity(extractStock(item))
                .status(String.valueOf(item.get("item_status")))
                .imageUrls(extractImageUrls(item))
                .attributes(extractAttributes(item))
                .rawData(item)
                .build();
    }

    private PlatformOrderData mapToOrderData(Map<String, Object> order) {
        return PlatformOrderData.builder()
                .id(String.valueOf(order.get("order_sn")))
                .sellerId(integration.getSellerId())
                .status(String.valueOf(order.get("order_status")))
                .orderDate(Instant.ofEpochSecond(((Number) order.get("create_time")).longValue()))
                .customerName(extractCustomerName(order))
                .totalAmount(extractTotalAmount(order))
                .currency("THB")
                .items(extractOrderItems(order))
                .rawData(order)
                .build();
    }

    private String buildSignedUrl(String path, Map<String, Object> params) {
        long timestamp = Instant.now().getEpochSecond();
        String accessToken = integration.getAccessToken();
        String shopId = integration.getShopId();
        String partnerId = extractPartnerId();

        // Add required parameters
        params.put("partner_id", Integer.parseInt(partnerId));
        params.put("shop_id", Integer.parseInt(shopId));
        params.put("timestamp", timestamp);
        params.put("access_token", accessToken);

        // Generate signature
        String baseString = partnerId + path + timestamp;
        String signature = generateShopeeSignature(baseString, getAppSecret());
        params.put("sign", signature);

        // Build URL
        String queryString = params.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + encodeValue(entry.getValue()))
                .collect(Collectors.joining("&"));

        return SHOPEE_BASE_URL + API_VERSION + path + "?" + queryString;
    }

    private String generateShopeeSignature(String message, String secret) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);

            byte[] hash = sha256Hmac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash).toLowerCase();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Shopee signature", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    private boolean isSuccessResponse(Map<String, Object> response) {
        return response != null &&
               response.containsKey("error") &&
               String.valueOf(response.get("error")).equals("");
    }

    private String extractPartnerId() {
        // Extract from API credentials JSON
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> credentials = objectMapper.readValue(integration.getApiCredentials(), Map.class);
            return String.valueOf(credentials.get("partner_id"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract partner ID from credentials", e);
        }
    }

    private String getAppSecret() {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> credentials = objectMapper.readValue(integration.getApiCredentials(), Map.class);
            return String.valueOf(credentials.get("app_secret"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract app secret from credentials", e);
        }
    }

    // Helper methods for data extraction
    private String extractCategoryName(Map<String, Object> item) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> categories = (List<Map<String, Object>>) item.get("category_list");
        if (categories != null && !categories.isEmpty()) {
            return String.valueOf(categories.get(0).get("display_category_name"));
        }
        return "";
    }

    private BigDecimal extractPrice(Map<String, Object> item) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> priceInfo = (List<Map<String, Object>>) item.get("price_info");
        if (priceInfo != null && !priceInfo.isEmpty()) {
            Number price = (Number) priceInfo.get(0).get("current_price");
            return price != null ? BigDecimal.valueOf(price.doubleValue()) : BigDecimal.ZERO;
        }
        return BigDecimal.ZERO;
    }

    private Integer extractStock(Map<String, Object> item) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> stockInfo = (List<Map<String, Object>>) item.get("stock_info");
        if (stockInfo != null && !stockInfo.isEmpty()) {
            Number stock = (Number) stockInfo.get(0).get("normal_stock");
            return stock != null ? stock.intValue() : 0;
        }
        return 0;
    }

    private List<String> extractImageUrls(Map<String, Object> item) {
        @SuppressWarnings("unchecked")
        List<String> images = (List<String>) item.get("image");
        return images != null ? images : new ArrayList<>();
    }

    private Map<String, Object> extractAttributes(Map<String, Object> item) {
        Map<String, Object> attributes = new HashMap<>();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> attributeList = (List<Map<String, Object>>) item.get("attribute_list");
        if (attributeList != null) {
            for (Map<String, Object> attr : attributeList) {
                String name = String.valueOf(attr.get("attribute_name"));
                @SuppressWarnings("unchecked")
                List<String> values = (List<String>) attr.get("attribute_value_list");
                attributes.put(name, values);
            }
        }

        return attributes;
    }

    private String extractCustomerName(Map<String, Object> order) {
        @SuppressWarnings("unchecked")
        Map<String, Object> recipient = (Map<String, Object>) order.get("recipient_address");
        return recipient != null ? String.valueOf(recipient.get("name")) : "";
    }

    private BigDecimal extractTotalAmount(Map<String, Object> order) {
        Number totalAmount = (Number) order.get("total_amount");
        return totalAmount != null ? BigDecimal.valueOf(totalAmount.doubleValue()) : BigDecimal.ZERO;
    }

    private List<PlatformOrderData.PlatformOrderItem> extractOrderItems(Map<String, Object> order) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) order.get("item_list");

        if (items == null) {
            return new ArrayList<>();
        }

        return items.stream()
                .map(item -> PlatformOrderData.PlatformOrderItem.builder()
                        .productId(String.valueOf(item.get("item_id")))
                        .sku(String.valueOf(item.get("item_sku")))
                        .name(String.valueOf(item.get("item_name")))
                        .quantity(((Number) item.get("model_quantity_purchased")).intValue())
                        .unitPrice(BigDecimal.valueOf(((Number) item.get("model_discounted_price")).doubleValue()))
                        .attributes(item)
                        .build())
                .collect(Collectors.toList());
    }

    private String encodeValue(Object value) {
        if (value instanceof List) {
            try {
                return objectMapper.writeValueAsString(value);
            } catch (Exception e) {
                return String.valueOf(value);
            }
        }
        return String.valueOf(value);
    }
}