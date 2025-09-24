package com.example.demo.platform.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class BarcodeGenerator {

    private static final String VODBOT_PREFIX = "VB";
    private static final SecureRandom RANDOM = new SecureRandom();

    public String generateBarcode(Product product, Product.BarcodeType type) {
        return switch (type) {
            case CODE128 -> generateCode128Barcode(product);
            case CODE39 -> generateCode39Barcode(product);
            case EAN13 -> generateEAN13Barcode(product);
            case EAN8 -> generateEAN8Barcode(product);
            case UPC_A -> generateUPCABarcode(product);
            case UPC_E -> generateUPCEBarcode(product);
        };
    }

    private String generateCode128Barcode(Product product) {
        // CODE128 can encode any ASCII character
        // Format: VB + YYYYMMDD + 6-digit random number
        String datePart = DateTimeFormatter.ofPattern("yyyyMMdd").format(Instant.now().atZone(java.time.ZoneOffset.UTC));
        String randomPart = String.format("%06d", RANDOM.nextInt(1000000));
        return VODBOT_PREFIX + datePart + randomPart;
    }

    private String generateCode39Barcode(Product product) {
        // CODE39 supports A-Z, 0-9, and some special characters
        // Format: VB + 8-digit random number (uppercase)
        String randomPart = String.format("%08d", RANDOM.nextInt(100000000));
        return (VODBOT_PREFIX + randomPart).toUpperCase();
    }

    private String generateEAN13Barcode(Product product) {
        // EAN13 is exactly 13 digits (12 data digits + 1 check digit)
        // Format: Country(3) + Company(4-7) + Product(2-5) + Check(1)
        // Using 999 as country code (internal use), 1234 as company code
        StringBuilder barcode = new StringBuilder("9991234");

        // Add 5 random digits for product code
        for (int i = 0; i < 5; i++) {
            barcode.append(RANDOM.nextInt(10));
        }

        // Calculate and add check digit
        String checkDigit = calculateEAN13CheckDigit(barcode.toString());
        barcode.append(checkDigit);

        return barcode.toString();
    }

    private String generateEAN8Barcode(Product product) {
        // EAN8 is exactly 8 digits (7 data digits + 1 check digit)
        StringBuilder barcode = new StringBuilder("999");

        // Add 4 random digits
        for (int i = 0; i < 4; i++) {
            barcode.append(RANDOM.nextInt(10));
        }

        // Calculate and add check digit
        String checkDigit = calculateEAN8CheckDigit(barcode.toString());
        barcode.append(checkDigit);

        return barcode.toString();
    }

    private String generateUPCABarcode(Product product) {
        // UPC-A is exactly 12 digits
        // Format: System(1) + Manufacturer(5) + Product(5) + Check(1)
        StringBuilder barcode = new StringBuilder("0");  // System digit (0 for regular UPC)

        // Add manufacturer code (use fixed code for Vodbot)
        barcode.append("12345");

        // Add 5 random digits for product code
        for (int i = 0; i < 5; i++) {
            barcode.append(RANDOM.nextInt(10));
        }

        // Calculate and add check digit
        String checkDigit = calculateUPCCheckDigit(barcode.toString());
        barcode.append(checkDigit);

        return barcode.toString();
    }

    private String generateUPCEBarcode(Product product) {
        // UPC-E is compressed UPC-A, 8 digits total
        // For simplicity, generate as UPC-A then compress
        String upcA = generateUPCABarcode(product);
        return compressToUPCE(upcA);
    }

    private String calculateEAN13CheckDigit(String barcode) {
        int sum = 0;
        for (int i = 0; i < barcode.length(); i++) {
            int digit = Character.getNumericValue(barcode.charAt(i));
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int checkDigit = (10 - (sum % 10)) % 10;
        return String.valueOf(checkDigit);
    }

    private String calculateEAN8CheckDigit(String barcode) {
        int sum = 0;
        for (int i = 0; i < barcode.length(); i++) {
            int digit = Character.getNumericValue(barcode.charAt(i));
            sum += (i % 2 == 0) ? digit * 3 : digit;
        }
        int checkDigit = (10 - (sum % 10)) % 10;
        return String.valueOf(checkDigit);
    }

    private String calculateUPCCheckDigit(String barcode) {
        int sum = 0;
        for (int i = 0; i < barcode.length(); i++) {
            int digit = Character.getNumericValue(barcode.charAt(i));
            sum += (i % 2 == 0) ? digit * 3 : digit;
        }
        int checkDigit = (10 - (sum % 10)) % 10;
        return String.valueOf(checkDigit);
    }

    private String compressToUPCE(String upcA) {
        // Simplified UPC-E compression logic
        // In real implementation, this would follow specific UPC-E compression rules
        String manufacturer = upcA.substring(1, 6);
        String product = upcA.substring(6, 11);
        String check = upcA.substring(11, 12);

        // Simple compression: take first 3 of manufacturer, first 3 of product
        return "0" + manufacturer.substring(0, 3) + product.substring(0, 3) + check;
    }

    public String generateUniqueBarcode(Product product, Product.BarcodeType type, BarcodeValidator validator) {
        int attempts = 0;
        int maxAttempts = 100;

        while (attempts < maxAttempts) {
            String barcode = generateBarcode(product, type);

            if (validator.isUnique(barcode)) {
                log.info("Generated unique barcode {} for product {} after {} attempts",
                        barcode, product.getMasterSku(), attempts + 1);
                return barcode;
            }

            attempts++;
        }

        throw new RuntimeException("Unable to generate unique barcode after " + maxAttempts + " attempts");
    }

    // Interface for barcode uniqueness validation
    public interface BarcodeValidator {
        boolean isUnique(String barcode);
    }
}