package com.kai_lam.auth_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(Jwt jwt, Cookie cookie, Internal internal) {

    public record Jwt(String issuer, long accessTokenMinutes, long refreshTokenDays, String secret) {
    }

    public record Cookie(String refreshName, boolean refreshSecure) {
    }

    public record Internal(String apiKey) {
    }
}
