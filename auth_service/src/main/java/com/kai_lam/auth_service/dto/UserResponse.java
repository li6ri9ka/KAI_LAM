package com.kai_lam.auth_service.dto;

import com.kai_lam.auth_service.kai_enum.AccountEnum;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String login,
        String email,
        AccountEnum status,
        String role,
        Instant createdAt,
        Instant lastLoginAt
) {
}
