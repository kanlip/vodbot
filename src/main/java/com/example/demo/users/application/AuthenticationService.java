package com.example.demo.users.application;

import com.example.demo.users.adapter.in.web.AuthResponse;
import com.example.demo.users.adapter.in.web.LoginRequest;
import com.example.demo.users.adapter.in.web.SignupRequest;
import com.example.demo.users.domain.Organization;
import com.example.demo.users.domain.User;
import com.example.demo.users.port.out.OrganizationRepository;
import com.example.demo.users.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthenticationService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse signup(SignupRequest request) {
        log.info("User signup attempt for email: {}", request.getEmail());

        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("User with email " + request.getEmail() + " already exists");
        }

        // Create organization first
        Organization organization = Organization.builder()
                .id(UUID.randomUUID())
                .name(request.getOrganizationName() != null ?
                      request.getOrganizationName() :
                      request.getDisplayName() + "'s Organization")
                .plan(Organization.Plan.FREE)
                .status("ACTIVE")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Organization savedOrg = organizationRepository.save(organization);
        log.info("Created organization: {} for user: {}", savedOrg.getName(), request.getEmail());

        // Create user
        User user = User.builder()
                .id(UUID.randomUUID())
                .orgId(savedOrg.getId())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .displayName(request.getDisplayName())
                .roles(List.of("USER"))
                .isSupervisor(false)
                .status("ACTIVE")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        User savedUser = userRepository.save(user);
        log.info("Created user: {} in organization: {}", savedUser.getEmail(), savedOrg.getName());

        // Generate tokens
        String accessToken = jwtService.generateToken(
                savedUser.getId().toString(),
                savedUser.getEmail(),
                savedUser.getOrgId()
        );
        String refreshToken = jwtService.generateRefreshToken(
                savedUser.getId().toString(),
                savedUser.getEmail(),
                savedUser.getOrgId()
        );

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400000L) // 24 hours
                .user(AuthResponse.UserInfo.builder()
                        .id(savedUser.getId())
                        .email(savedUser.getEmail())
                        .displayName(savedUser.getDisplayName())
                        .orgId(savedUser.getOrgId())
                        .orgName(savedOrg.getName())
                        .build())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        log.info("User login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new IllegalArgumentException("User account is not active");
        }

        // Update last login
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        // Get organization info
        Organization organization = organizationRepository.findById(user.getOrgId())
                .orElseThrow(() -> new RuntimeException("User organization not found"));

        // Generate tokens
        String accessToken = jwtService.generateToken(
                user.getId().toString(),
                user.getEmail(),
                user.getOrgId()
        );
        String refreshToken = jwtService.generateRefreshToken(
                user.getId().toString(),
                user.getEmail(),
                user.getOrgId()
        );

        log.info("User successfully logged in: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400000L) // 24 hours
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .displayName(user.getDisplayName())
                        .orgId(user.getOrgId())
                        .orgName(organization.getName())
                        .build())
                .build();
    }

    public AuthResponse refreshToken(String refreshToken) {
        log.info("Token refresh attempt");

        try {
            String userId = jwtService.extractUserId(refreshToken);
            String email = jwtService.extractEmail(refreshToken);
            UUID orgId = jwtService.extractOrgId(refreshToken);

            if (jwtService.isTokenValid(refreshToken, userId)) {
                String newAccessToken = jwtService.generateToken(userId, email, orgId);
                String newRefreshToken = jwtService.generateRefreshToken(userId, email, orgId);

                User user = userRepository.findById(UUID.fromString(userId))
                        .orElseThrow(() -> new RuntimeException("User not found"));

                Organization organization = organizationRepository.findById(orgId)
                        .orElseThrow(() -> new RuntimeException("Organization not found"));

                return AuthResponse.builder()
                        .accessToken(newAccessToken)
                        .refreshToken(newRefreshToken)
                        .tokenType("Bearer")
                        .expiresIn(86400000L)
                        .user(AuthResponse.UserInfo.builder()
                                .id(user.getId())
                                .email(user.getEmail())
                                .displayName(user.getDisplayName())
                                .orgId(user.getOrgId())
                                .orgName(organization.getName())
                                .build())
                        .build();
            } else {
                throw new IllegalArgumentException("Invalid refresh token");
            }
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            throw new IllegalArgumentException("Invalid refresh token");
        }
    }
}