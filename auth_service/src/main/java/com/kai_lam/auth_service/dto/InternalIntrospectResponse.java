package com.kai_lam.auth_service.dto;

import com.kai_lam.auth_service.kai_enum.AccountEnum;

import java.time.Instant;
import java.util.UUID;

public record InternalIntrospectResponse(
        boolean active,
        UUID sub,
        String role,
        Instant exp,
        AccountEnum status
) {
}
