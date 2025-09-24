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
public class TikTokShopApiClient implements PlatformApiClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final PlatformIntegration integration;

    public TikTokShopApiClient(RestTemplate restTemplate, ObjectMapper objectMapper, PlatformIntegration integration) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.integration = integration;
    }

    private static final String TIKTOK_BASE_URL = "https://open-api.tiktokglobalshop.com";
    private static final String API_VERSION = "/api/products/v1";
    private static final String ORDER_API_VERSION = "/api/orders/v1";


    @Override
    public List<PlatformProductData> fetchAllProducts() {
        log.info("Fetching all products from TikTok Shop for shop: {}", integration.getShopId());

        try {
            String path = "/products/search";
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("page_info", Map.of(
                "page_number", 1,
                "page_size", 100
            ));

            String url = buildSignedUrl(path, new HashMap<>());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-tts-access-token", integration.getAccessToken());

            String requestJson = objectMapper.writeValueAsString(requestBody);
            HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = response.getBody();

            if (!isSuccessResponse(responseBody)) {
                throw new RuntimeException("Failed to fetch products from TikTok Shop: " + responseBody);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> products = (List<Map<String, Object>>) data.get("products");

            if (products == null || products.isEmpty()) {
                log.info("No products found in TikTok Shop: {}", integration.getShopId());
                return new ArrayList<>();
            }

            // Fetch detailed product information
            return fetchProductDetails(products);

        } catch (Exception e) {
            log.error("Error fetching products from TikTok Shop: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch products from TikTok Shop", e);
        }
    }

    @Override
    public PlatformProductData fetchProductById(String productId) {
        log.info("Fetching product {} from TikTok Shop", productId);

        try {
            String path = "/products/details";
            Map<String, String> params = new HashMap<>();
            params.put("product_id", productId);

            String url = buildSignedUrl(path, params);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-tts-access-token", integration.getAccessToken());

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = response.getBody();

            if (!isSuccessResponse(responseBody)) {
                throw new RuntimeException("Failed to fetch product from TikTok Shop: " + responseBody);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) responseBody.get("data");

            return mapToProductData(data);

        } catch (Exception e) {
            log.error("Error fetching product {} from TikTok Shop: {}", productId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch product from TikTok Shop", e);
        }
    }

    @Override
    public List<PlatformOrderData> fetchOrders(String since) {
        log.info("Fetching orders from TikTok Shop since: {}", since);

        try {
            String path = "/orders/search";
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("page_info", Map.of(
                "page_number", 1,
                "page_size", 100
            ));
            requestBody.put("order_status", "AWAITING_SHIPMENT");
            requestBody.put("create_time_from", Long.parseLong(since));
            requestBody.put("create_time_to", Instant.now().getEpochSecond());

            String url = buildSignedUrl(path, new HashMap<>());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-tts-access-token", integration.getAccessToken());

            String requestJson = objectMapper.writeValueAsString(requestBody);
            HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = response.getBody();

            if (!isSuccessResponse(responseBody)) {
                throw new RuntimeException("Failed to fetch orders from TikTok Shop: " + responseBody);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> orders = (List<Map<String, Object>>) data.get("orders");

            return orders.stream()
                    .map(this::mapToOrderData)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error fetching orders from TikTok Shop: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch orders from TikTok Shop", e);
        }
    }

    @Override
    public PlatformOrderData fetchOrderById(String orderId) {
        log.info("Fetching order {} from TikTok Shop", orderId);

        try {
            String path = "/orders/detail/query";
            Map<String, String> params = new HashMap<>();
            params.put("order_id", orderId);

            String url = buildSignedUrl(path, params);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-tts-access-token", integration.getAccessToken());

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = response.getBody();

            if (!isSuccessResponse(responseBody)) {
                throw new RuntimeException("Failed to fetch order from TikTok Shop: " + responseBody);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) responseBody.get("data");

            return mapToOrderData(data);

        } catch (Exception e) {
            log.error("Error fetching order {} from TikTok Shop: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch order from TikTok Shop", e);
        }
    }

    @Override
    public boolean validateWebhookSignature(String payload, String signature, String secret) {
        try {
            String expectedSignature = generateTikTokSignature(payload, secret);
            return expectedSignature.equals(signature);
        } catch (Exception e) {
            log.error("Error validating TikTok Shop webhook signature: {}", e.getMessage());
            return false;
        }
    }

    private List<PlatformProductData> fetchProductDetails(List<Map<String, Object>> products) {
        List<PlatformProductData> detailedProducts = new ArrayList<>();

        for (Map<String, Object> product : products) {
            String productId = String.valueOf(product.get("product_id"));
            try {
                PlatformProductData detailed = fetchProductById(productId);
                if (detailed != null) {
                    detailedProducts.add(detailed);
                }
            } catch (Exception e) {
                log.warn("Failed to fetch details for product {}: {}", productId, e.getMessage());
                // Add basic product info even if detailed fetch fails
                detailedProducts.add(mapToProductData(product));
            }
        }

        return detailedProducts;
    }

    private PlatformProductData mapToProductData(Map<String, Object> product) {
        return PlatformProductData.builder()
                .id(String.valueOf(product.get("product_id")))
                .sku(String.valueOf(product.get("seller_sku")))
                .name(String.valueOf(product.get("product_name")))
                .description(String.valueOf(product.getOrDefault("description", "")))
                .category(extractCategoryName(product))
                .brand(String.valueOf(product.getOrDefault("brand", "")))
                .price(extractPrice(product))
                .currency(extractCurrency(product))
                .stockQuantity(extractStock(product))
                .status(String.valueOf(product.get("product_status")))
                .imageUrls(extractImageUrls(product))
                .attributes(extractAttributes(product))
                .rawData(product)
                .build();
    }

    private PlatformOrderData mapToOrderData(Map<String, Object> order) {
        return PlatformOrderData.builder()
                .id(String.valueOf(order.get("order_id")))
                .sellerId(integration.getSellerId())
                .status(String.valueOf(order.get("order_status")))
                .orderDate(Instant.ofEpochSecond(((Number) order.get("create_time")).longValue()))
                .customerName(extractCustomerName(order))
                .shippingAddress(extractShippingAddress(order))
                .totalAmount(extractTotalAmount(order))
                .currency(extractOrderCurrency(order))
                .items(extractOrderItems(order))
                .specialInstructions(String.valueOf(order.getOrDefault("buyer_message", "")))
                .rawData(order)
                .build();
    }

    private String buildSignedUrl(String path, Map<String, String> params) {
        long timestamp = Instant.now().getEpochSecond();
        String appKey = extractAppKey();

        // Build base parameters
        Map<String, String> allParams = new HashMap<>(params);
        allParams.put("app_key", appKey);
        allParams.put("timestamp", String.valueOf(timestamp));

        // Sort parameters for signature generation
        List<String> sortedKeys = allParams.keySet().stream()
                .sorted()
                .collect(Collectors.toList());

        // Build parameter string for signature
        StringBuilder paramString = new StringBuilder();
        for (String key : sortedKeys) {
            paramString.append(key).append(allParams.get(key));
        }

        // Generate signature
        String appSecret = getAppSecret();
        String signature = generateTikTokSignature(path + paramString, appSecret);
        allParams.put("sign", signature);

        // Build final URL
        String queryString = allParams.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

        return TIKTOK_BASE_URL +
               (path.startsWith("/api/orders") ? ORDER_API_VERSION : API_VERSION) +
               path.replace("/api/products/v1", "").replace("/api/orders/v1", "") +
               "?" + queryString;
    }

    private String generateTikTokSignature(String message, String secret) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);

            byte[] hash = sha256Hmac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate TikTok Shop signature", e);
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
               Integer.valueOf(0).equals(response.get("code"));
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
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> categories = (List<Map<String, Object>>) product.get("category_chains");
        if (categories != null && !categories.isEmpty()) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> chain = (List<Map<String, Object>>) categories.get(0).get("category_chain");
            if (chain != null && !chain.isEmpty()) {
                return String.valueOf(chain.get(chain.size() - 1).get("local_display_name"));
            }
        }
        return "";
    }

    private BigDecimal extractPrice(Map<String, Object> product) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> skus = (List<Map<String, Object>>) product.get("skus");
        if (skus != null && !skus.isEmpty()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> price = (Map<String, Object>) skus.get(0).get("price");
            if (price != null) {
                String amount = String.valueOf(price.get("amount"));
                return new BigDecimal(amount);
            }
        }
        return BigDecimal.ZERO;
    }

    private String extractCurrency(Map<String, Object> product) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> skus = (List<Map<String, Object>>) product.get("skus");
        if (skus != null && !skus.isEmpty()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> price = (Map<String, Object>) skus.get(0).get("price");
            if (price != null) {
                return String.valueOf(price.get("currency"));
            }
        }
        return "USD";
    }

    private Integer extractStock(Map<String, Object> product) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> skus = (List<Map<String, Object>>) product.get("skus");
        if (skus != null && !skus.isEmpty()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> inventory = (Map<String, Object>) skus.get(0).get("stock_infos");
            if (inventory != null && inventory.containsKey("available_stock")) {
                Number stock = (Number) inventory.get("available_stock");
                return stock != null ? stock.intValue() : 0;
            }
        }
        return 0;
    }

    private List<String> extractImageUrls(Map<String, Object> product) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> images = (List<Map<String, Object>>) product.get("images");
        if (images != null) {
            return images.stream()
                    .map(img -> String.valueOf(img.get("url")))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private Map<String, Object> extractAttributes(Map<String, Object> product) {
        Map<String, Object> attributes = new HashMap<>();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> productAttributes = (List<Map<String, Object>>) product.get("product_attributes");
        if (productAttributes != null) {
            for (Map<String, Object> attr : productAttributes) {
                String name = String.valueOf(attr.get("attribute_name"));
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> values = (List<Map<String, Object>>) attr.get("attribute_values");
                if (values != null) {
                    List<String> valueList = values.stream()
                            .map(v -> String.valueOf(v.get("attribute_value")))
                            .collect(Collectors.toList());
                    attributes.put(name, valueList);
                }
            }
        }

        return attributes;
    }

    private String extractCustomerName(Map<String, Object> order) {
        @SuppressWarnings("unchecked")
        Map<String, Object> recipient = (Map<String, Object>) order.get("recipient_address");
        if (recipient != null) {
            return String.valueOf(recipient.get("name"));
        }
        return "";
    }

    private Map<String, Object> extractShippingAddress(Map<String, Object> order) {
        @SuppressWarnings("unchecked")
        Map<String, Object> recipient = (Map<String, Object>) order.get("recipient_address");
        return recipient != null ? recipient : new HashMap<>();
    }

    private BigDecimal extractTotalAmount(Map<String, Object> order) {
        @SuppressWarnings("unchecked")
        Map<String, Object> payment = (Map<String, Object>) order.get("payment_info");
        if (payment != null) {
            String amount = String.valueOf(payment.get("total_amount"));
            try {
                return new BigDecimal(amount);
            } catch (NumberFormatException e) {
                log.warn("Failed to parse total amount: {}", amount);
            }
        }
        return BigDecimal.ZERO;
    }

    private String extractOrderCurrency(Map<String, Object> order) {
        @SuppressWarnings("unchecked")
        Map<String, Object> payment = (Map<String, Object>) order.get("payment_info");
        if (payment != null) {
            return String.valueOf(payment.get("currency"));
        }
        return "USD";
    }

    private List<PlatformOrderData.PlatformOrderItem> extractOrderItems(Map<String, Object> order) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) order.get("order_line_items");

        if (items == null) {
            return new ArrayList<>();
        }

        return items.stream()
                .map(item -> PlatformOrderData.PlatformOrderItem.builder()
                        .productId(String.valueOf(item.get("product_id")))
                        .sku(String.valueOf(item.get("seller_sku")))
                        .name(String.valueOf(item.get("product_name")))
                        .quantity(((Number) item.get("quantity")).intValue())
                        .unitPrice(new BigDecimal(String.valueOf(item.get("display_price"))))
                        .attributes(item)
                        .build())
                .collect(Collectors.toList());
    }
}