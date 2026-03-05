package com.kai_lam.user_service.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserExpiredJwtTests {

    @Autowired
    private MockMvc mockMvc;

    @Value("${app.security.jwt.secret}")
    private String jwtSecret;

    @Value("${app.security.jwt.issuer}")
    private String jwtIssuer;

    @Test
    void tc005_expiredAccessTokenIsRejected() throws Exception {
        String expired = buildExpiredToken();

        mockMvc.perform(get("/me")
                        .header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized());
    }

    private String buildExpiredToken() {
        SecretKey signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        Instant issuedAt = Instant.now().minus(2, ChronoUnit.HOURS);
        Instant expiredAt = Instant.now().minus(1, ChronoUnit.HOURS);

        return Jwts.builder()
                .issuer(jwtIssuer)
                .subject(UUID.randomUUID().toString())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiredAt))
                .claim("login", "expired-user")
                .claim("role", "USER")
                .signWith(signingKey)
                .compact();
    }
}
