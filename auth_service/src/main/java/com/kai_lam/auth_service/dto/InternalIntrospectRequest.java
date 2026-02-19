package com.kai_lam.auth_service.dto;

import jakarta.validation.constraints.NotBlank;

public record InternalIntrospectRequest(
        @NotBlank(message = "token is required")
        String token
) {
}
