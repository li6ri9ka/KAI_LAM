package com.kai_lam.projects_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SecretKeyRequest(
        @NotBlank(message = "nameSecretKey is required")
        @Size(max = 200, message = "nameSecretKey length must be <= 200")
        String nameSecretKey,

        @NotBlank(message = "encryptedValue is required")
        String encryptedValue
) {
}
