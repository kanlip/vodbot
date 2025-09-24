package com.example.demo.platform.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductMatcher {

    public Optional<Product> findBestMatch(Product candidate, List<Product> existingProducts, List<ProductMatchingRule> rules) {
        if (existingProducts.isEmpty()) {
            return Optional.empty();
        }

        // Sort rules by priority (lower number = higher priority)
        List<ProductMatchingRule> activeRules = rules.stream()
                .filter(ProductMatchingRule::isActive)
                .sorted(Comparator.comparingInt(ProductMatchingRule::getPriority))
                .collect(Collectors.toList());

        if (activeRules.isEmpty()) {
            log.warn("No active matching rules found");
            return Optional.empty();
        }

        Product bestMatch = null;
        BigDecimal bestScore = BigDecimal.ZERO;

        for (Product existing : existingProducts) {
            BigDecimal totalScore = calculateMatchScore(candidate, existing, activeRules);

            if (totalScore.compareTo(bestScore) > 0) {
                bestScore = totalScore;
                bestMatch = existing;
            }
        }

        // Check if best match exceeds minimum threshold from any rule
        BigDecimal minThreshold = activeRules.stream()
                .map(ProductMatchingRule::getSimilarityThreshold)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.valueOf(0.8));

        if (bestScore.compareTo(minThreshold) >= 0) {
            log.info("Found product match: {} -> {} (score: {})",
                    candidate.getMasterSku(), bestMatch.getMasterSku(), bestScore);
            return Optional.of(bestMatch);
        }

        log.debug("No suitable match found for product: {} (best score: {})",
                candidate.getMasterSku(), bestScore);
        return Optional.empty();
    }

    private BigDecimal calculateMatchScore(Product candidate, Product existing, List<ProductMatchingRule> rules) {
        BigDecimal totalScore = BigDecimal.ZERO;
        int ruleCount = 0;

        for (ProductMatchingRule rule : rules) {
            BigDecimal ruleScore = calculateRuleScore(candidate, existing, rule);
            if (ruleScore.compareTo(BigDecimal.ZERO) > 0) {
                totalScore = totalScore.add(ruleScore);
                ruleCount++;
            }
        }

        return ruleCount > 0 ? totalScore.divide(BigDecimal.valueOf(ruleCount), 4, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;
    }

    private BigDecimal calculateRuleScore(Product candidate, Product existing, ProductMatchingRule rule) {
        List<BigDecimal> fieldScores = new ArrayList<>();

        for (String field : rule.getMatchFields()) {
            BigDecimal fieldScore = calculateFieldScore(candidate, existing, field);
            fieldScores.add(fieldScore);
        }

        if (fieldScores.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Average all field scores for this rule
        BigDecimal sum = fieldScores.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(BigDecimal.valueOf(fieldScores.size()), 4, BigDecimal.ROUND_HALF_UP);
    }

    private BigDecimal calculateFieldScore(Product candidate, Product existing, String fieldPath) {
        Object candidateValue = getFieldValue(candidate, fieldPath);
        Object existingValue = getFieldValue(existing, fieldPath);

        if (candidateValue == null || existingValue == null) {
            return BigDecimal.ZERO;
        }

        return calculateSimilarity(candidateValue.toString(), existingValue.toString());
    }

    private Object getFieldValue(Product product, String fieldPath) {
        String[] parts = fieldPath.split("\\.");

        switch (parts[0].toLowerCase()) {
            case "name":
            case "productname":
                return product.getProductName();
            case "sku":
            case "mastersku":
                return product.getMasterSku();
            case "description":
                return product.getDescription();
            case "category":
                return product.getCategory();
            case "brand":
                return product.getBrand();
            case "attributes":
                if (parts.length > 1 && product.getAttributes() != null) {
                    return product.getAttributes().get(parts[1]);
                }
                break;
        }
        return null;
    }

    private BigDecimal calculateSimilarity(String str1, String str2) {
        if (str1 == null || str2 == null) {
            return BigDecimal.ZERO;
        }

        str1 = str1.toLowerCase().trim();
        str2 = str2.toLowerCase().trim();

        if (str1.equals(str2)) {
            return BigDecimal.ONE;
        }

        // Use Levenshtein distance for similarity calculation
        int distance = levenshteinDistance(str1, str2);
        int maxLength = Math.max(str1.length(), str2.length());

        if (maxLength == 0) {
            return BigDecimal.ONE;
        }

        double similarity = 1.0 - (double) distance / maxLength;
        return BigDecimal.valueOf(Math.max(0.0, similarity));
    }

    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[s1.length()][s2.length()];
    }
}