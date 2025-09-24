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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class LazadaApiClient implements PlatformApiClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final PlatformIntegration integration;

    public LazadaApiClient(RestTemplate restTemplate, ObjectMapper objectMapper, PlatformIntegration integration) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.integration = integration;
    }

    private static final String LAZADA_BASE_URL = "https://api.lazada.co.th/rest";
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss+07:00");


    @Override
    public List<PlatformProductData> fetchAllProducts() {
        log.info("Fetching all products from Lazada for seller: {}", integration.getSellerId() != null ? integration.getSellerId() : "<not set>");

        try {
            String path = "/products/get";
            Map<String, String> params = new HashMap<>();
            params.put("filter", "all");
            params.put("offset", "0");
            params.put("limit", "100");

            String url = buildSignedUrl(path, params);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = response.getBody();

            if (!isSuccessResponse(responseBody)) {
                throw new RuntimeException("Failed to fetch products from Lazada: " + responseBody);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> products = (List<Map<String, Object>>) data.get("products");

            if (products == null || products.isEmpty()) {
                log.info("No products found in Lazada shop: {}", integration.getSellerId() != null ? integration.getSellerId() : "<not set>");
                return new ArrayList<>();
            }

            return products.stream()
                    .map(this::mapToProductData)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error fetching products from Lazada: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch products from Lazada", e);
        }
    }

    @Override
    public PlatformProductData fetchProductById(String productId) {
        log.info("Fetching product {} from Lazada", productId);

        try {
            String path = "/product/item/get";
            Map<String, String> params = new HashMap<>();
            params.put("item_id", productId);

            String url = buildSignedUrl(path, params);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = response.getBody();

            if (!isSuccessResponse(responseBody)) {
                throw new RuntimeException("Failed to fetch product from Lazada: " + responseBody);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) responseBody.get("data");

            return mapToProductData(data);

        } catch (Exception e) {
            log.error("Error fetching product {} from Lazada: {}", productId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch product from Lazada", e);
        }
    }

    @Override
    public List<PlatformOrderData> fetchOrders(String since) {
        log.info("Fetching orders from Lazada since: {}", since);

        try {
            String path = "/orders/get";
            Map<String, String> params = new HashMap<>();
            params.put("status", "ready_to_ship");
            params.put("created_after", formatTimestamp(Long.parseLong(since)));
            params.put("created_before", formatTimestamp(Instant.now().getEpochSecond()));
            params.put("offset", "0");
            params.put("limit", "100");

            String url = buildSignedUrl(path, params);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = response.getBody();

            if (!isSuccessResponse(responseBody)) {
                throw new RuntimeException("Failed to fetch orders from Lazada: " + responseBody);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> orders = (List<Map<String, Object>>) data.get("orders");

            return orders.stream()
                    .map(this::mapToOrderData)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error fetching orders from Lazada: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch orders from Lazada", e);
        }
    }

    @Override
    public PlatformOrderData fetchOrderById(String orderId) {
        log.info("Fetching order {} from Lazada", orderId);

        try {
            String path = "/order/get";
            Map<String, String> params = new HashMap<>();
            params.put("order_id", orderId);

            String url = buildSignedUrl(path, params);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = response.getBody();

            if (!isSuccessResponse(responseBody)) {
                throw new RuntimeException("Failed to fetch order from Lazada: " + responseBody);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) responseBody.get("data");

            return mapToOrderData(data);

        } catch (Exception e) {
            log.error("Error fetching order {} from Lazada: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch order from Lazada", e);
        }
    }

    @Override
    public boolean validateWebhookSignature(String payload, String signature, String secret) {
        try {
            String expectedSignature = generateLazadaSignature(payload, secret);
            return expectedSignature.equals(signature);
        } catch (Exception e) {
            log.error("Error validating Lazada webhook signature: {}", e.getMessage());
            return false;
        }
    }

    private PlatformProductData mapToProductData(Map<String, Object> product) {
        return PlatformProductData.builder()
                .id(String.valueOf(product.get("item_id")))
                .sku(String.valueOf(product.get("seller_sku")))
                .name(extractAttributeValue(product, "name"))
                .description(extractAttributeValue(product, "short_description"))
                .category(extractCategoryName(product))
                .brand(extractAttributeValue(product, "brand"))
                .price(extractPrice(product))
                .currency("THB") // Lazada Thailand
                .stockQuantity(extractStock(product))
                .status(String.valueOf(product.get("status")))
                .imageUrls(extractImageUrls(product))
                .attributes(extractAttributes(product))
                .rawData(product)
                .build();
    }

    private PlatformOrderData mapToOrderData(Map<String, Object> order) {
        return PlatformOrderData.builder()
                .id(String.valueOf(order.get("order_id")))
                .sellerId(integration.getSellerId() != null ? integration.getSellerId() : "unknown")
                .status(String.valueOf(order.get("statuses")))
                .orderDate(parseTimestamp(String.valueOf(order.get("created_at"))))
                .customerName(extractCustomerName(order))
                .customerPhone(extractAddressBillingValue(order, "phone"))
                .customerEmail(String.valueOf(order.getOrDefault("customer_email", "")))
                .shippingAddress(extractShippingAddress(order))
                .totalAmount(extractTotalAmount(order))
                .currency("THB")
                .items(extractOrderItems(order))
                .rawData(order)
                .build();
    }

    private String buildSignedUrl(String path, Map<String, String> params) {
        long timestamp = System.currentTimeMillis();
        String accessToken = integration.getAccessToken();
        String appKey = extractAppKey();

        // Add required parameters
        params.put("app_key", appKey);
        params.put("access_token", accessToken);
        params.put("timestamp", String.valueOf(timestamp));
        params.put("sign_method", "sha256");

        // Sort parameters for signature generation
        List<String> sortedKeys = params.keySet().stream()
                .sorted()
                .collect(Collectors.toList());

        // Build parameter string for signature
        StringBuilder paramString = new StringBuilder();
        paramString.append(path);

        for (String key : sortedKeys) {
            paramString.append(key).append(params.get(key));
        }

        // Generate signature
        String appSecret = getAppSecret();
        String signature = generateLazadaSignature(paramString.toString(), appSecret);
        params.put("sign", signature);

        // Build final URL
        String queryString = params.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + urlEncode(entry.getValue()))
                .collect(Collectors.joining("&"));

        return LAZADA_BASE_URL + path + "?" + queryString;
    }

    private String generateLazadaSignature(String message, String secret) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);

            byte[] hash = sha256Hmac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash).toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Lazada signature", e);
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
               response.containsKey("code") &&
               "0".equals(String.valueOf(response.get("code")));
    }

    private String extractAppKey() {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> credentials = objectMapper.readValue(integration.getApiCredentials(), Map.class);
            return String.valueOf(credentials.get("app_key"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract app key from credentials", e);
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
    private String extractCategoryName(Map<String, Object> product) {
        Number categoryId = (Number) product.get("primary_category");
        return categoryId != null ? "Category_" + categoryId : "";
    }

    private BigDecimal extractPrice(Map<String, Object> product) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> skus = (List<Map<String, Object>>) product.get("skus");
        if (skus != null && !skus.isEmpty()) {
            Number price = (Number) skus.get(0).get("price");
            return price != null ? BigDecimal.valueOf(price.doubleValue()) : BigDecimal.ZERO;
        }
        return BigDecimal.ZERO;
    }

    private Integer extractStock(Map<String, Object> product) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> skus = (List<Map<String, Object>>) product.get("skus");
        if (skus != null && !skus.isEmpty()) {
            Number stock = (Number) skus.get(0).get("quantity");
            return stock != null ? stock.intValue() : 0;
        }
        return 0;
    }

    private List<String> extractImageUrls(Map<String, Object> product) {
        @SuppressWarnings("unchecked")
        List<String> images = (List<String>) product.get("images");
        return images != null ? images : new ArrayList<>();
    }

    private Map<String, Object> extractAttributes(Map<String, Object> product) {
        @SuppressWarnings("unchecked")
        Map<String, Object> attributes = (Map<String, Object>) product.get("attributes");
        return attributes != null ? attributes : new HashMap<>();
    }

    private String extractCustomerName(Map<String, Object> order) {
        @SuppressWarnings("unchecked")
        Map<String, Object> shippingAddress = (Map<String, Object>) order.get("address_shipping");
        if (shippingAddress != null) {
            return String.valueOf(shippingAddress.get("first_name")) + " " +
                   String.valueOf(shippingAddress.get("last_name"));
        }
        return "";
    }

    private Map<String, Object> extractShippingAddress(Map<String, Object> order) {
        @SuppressWarnings("unchecked")
        Map<String, Object> shippingAddress = (Map<String, Object>) order.get("address_shipping");
        return shippingAddress != null ? shippingAddress : new HashMap<>();
    }

    private BigDecimal extractTotalAmount(Map<String, Object> order) {
        Number price = (Number) order.get("price");
        return price != null ? BigDecimal.valueOf(price.doubleValue()) : BigDecimal.ZERO;
    }

    private List<PlatformOrderData.PlatformOrderItem> extractOrderItems(Map<String, Object> order) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) order.get("order_items");

        if (items == null) {
            return new ArrayList<>();
        }

        return items.stream()
                .map(item -> PlatformOrderData.PlatformOrderItem.builder()
                        .productId(String.valueOf(item.get("product_id")))
                        .sku(String.valueOf(item.get("sku")))
                        .name(String.valueOf(item.get("name")))
                        .quantity(((Number) item.get("quantity")).intValue())
                        .unitPrice(BigDecimal.valueOf(((Number) item.get("item_price")).doubleValue()))
                        .attributes(item)
                        .build())
                .collect(Collectors.toList());
    }

    private String formatTimestamp(long epochSeconds) {
        return Instant.ofEpochSecond(epochSeconds)
                .atZone(ZoneOffset.of("+07:00"))
                .format(ISO_FORMATTER);
    }

    private Instant parseTimestamp(String timestamp) {
        try {
            return Instant.from(ISO_FORMATTER.parse(timestamp));
        } catch (Exception e) {
            log.warn("Failed to parse timestamp: {}", timestamp);
            return Instant.now();
        }
    }

    private String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }

    private String extractAttributeValue(Map<String, Object> product, String attributeName) {
        @SuppressWarnings("unchecked")
        Map<String, Object> attributes = (Map<String, Object>) product.getOrDefault("attributes", new HashMap<>());
        return String.valueOf(attributes.getOrDefault(attributeName, ""));
    }

    private String extractAddressBillingValue(Map<String, Object> order, String fieldName) {
        @SuppressWarnings("unchecked")
        Map<String, Object> addressBilling = (Map<String, Object>) order.getOrDefault("address_billing", new HashMap<>());
        return String.valueOf(addressBilling.getOrDefault(fieldName, ""));
    }
}