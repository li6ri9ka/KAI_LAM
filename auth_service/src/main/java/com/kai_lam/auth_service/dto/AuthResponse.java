package com.kai_lam.auth_service.dto;

import java.time.Instant;

public record AuthResponse(
        String accessToken,
        Instant accessTokenExpiresAt,
        UserResponse user
) {
}
