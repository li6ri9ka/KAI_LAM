package com.kai_lam.auth_service.security;

import com.kai_lam.auth_service.config.SecurityProperties;
import com.kai_lam.auth_service.model.AuthUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Component
public class JwtService {
    private final SecurityProperties securityProperties;
    private final SecretKey signingKey;

    public JwtService(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
        this.signingKey = Keys.hmacShaKeyFor(securityProperties.jwt().secret().getBytes(StandardCharsets.UTF_8));
    }

    public TokenPayload generateAccessToken(AuthUser user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(securityProperties.jwt().accessTokenMinutes(), ChronoUnit.MINUTES);

        String token = Jwts.builder()
                .issuer(securityProperties.jwt().issuer())
                .subject(user.getId().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .claim("login", user.getLogin())
                .claim("role", user.getRole().getNameRole())
                .signWith(signingKey)
                .compact();

        return new TokenPayload(token, expiresAt);
    }

    public Optional<UUID> parseUserId(String token) {
        return parseClaims(token).map(AccessClaims::userId);
    }

    public Optional<AccessClaims> parseClaims(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (!securityProperties.jwt().issuer().equals(claims.getIssuer())) {
                return Optional.empty();
            }

            UUID userId = UUID.fromString(claims.getSubject());
            String role = claims.get("role", String.class);
            Instant expiresAt = claims.getExpiration().toInstant();

            return Optional.of(new AccessClaims(userId, role, expiresAt));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public record TokenPayload(String token, Instant expiresAt) {
    }

    public record AccessClaims(UUID userId, String role, Instant expiresAt) {
    }
}
