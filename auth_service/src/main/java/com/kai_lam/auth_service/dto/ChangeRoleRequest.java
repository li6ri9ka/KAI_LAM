package com.kai_lam.auth_service.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangeRoleRequest(
        @NotBlank(message = "role is required")
        String role
) {
}
