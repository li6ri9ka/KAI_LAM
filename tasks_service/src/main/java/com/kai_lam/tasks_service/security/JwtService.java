package com.kai_lam.tasks_service.security;

import com.kai_lam.tasks_service.config.SecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
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

    public Optional<TokenClaims> parse(String token) {
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
            String login = claims.get("login", String.class);
            String role = claims.get("role", String.class);

            return Optional.of(new TokenClaims(userId, login == null ? userId.toString() : login, role == null ? "USER" : role));
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public record TokenClaims(UUID authUserId, String login, String role) {
    }
}
