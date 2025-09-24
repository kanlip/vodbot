package com.example.demo.users.application;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class JwtService {

    private final SecretKey secretKey;
    private final long jwtExpiration;
    private final long refreshExpiration;

    public JwtService(
            @Value("${security.jwt.secret-key:mySecretKey1234567890123456789012345678901234567890}") String secretKeyString,
            @Value("${security.jwt.expiration:86400000}") long jwtExpiration, // 24 hours
            @Value("${security.jwt.refresh-token.expiration:604800000}") long refreshExpiration // 7 days
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes());
        this.jwtExpiration = jwtExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    public String generateToken(String userId, String email, UUID orgId) {
        return generateToken(new HashMap<>(), userId, email, orgId);
    }

    public String generateToken(Map<String, Object> extraClaims, String userId, String email, UUID orgId) {
        return buildToken(extraClaims, userId, email, orgId, jwtExpiration);
    }

    public String generateRefreshToken(String userId, String email, UUID orgId) {
        return buildToken(new HashMap<>(), userId, email, orgId, refreshExpiration);
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            String userId,
            String email,
            UUID orgId,
            long expiration
    ) {
        Instant now = Instant.now();
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userId)
                .claim("email", email)
                .claim("orgId", orgId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiration, ChronoUnit.MILLIS)))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, String userId) {
        final String extractedUserId = extractUserId(token);
        return (extractedUserId.equals(userId)) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    public UUID extractOrgId(String token) {
        String orgIdString = extractClaim(token, claims -> claims.get("orgId", String.class));
        return UUID.fromString(orgIdString);
    }

    public <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.debug("JWT token is expired: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.error("JWT token is malformed: {}", e.getMessage());
            throw e;
        } catch (SecurityException e) {
            log.error("JWT signature validation failed: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("JWT token compact of handler are invalid: {}", e.getMessage());
            throw e;
        }
    }
}