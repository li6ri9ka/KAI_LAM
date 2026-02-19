package com.kai_lam.auth_service.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "loginOrEmail is required")
        String loginOrEmail,

        @NotBlank(message = "password is required")
        String password
) {
}
