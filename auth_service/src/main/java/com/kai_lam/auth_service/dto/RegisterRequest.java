package com.kai_lam.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "login is required")
        @Size(min = 3, max = 50, message = "login length must be between 3 and 50")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "login can contain only letters, digits, dot, underscore and hyphen")
        String login,

        @NotBlank(message = "email is required")
        @Email(message = "email must be valid")
        @Size(max = 100, message = "email length must be <= 100")
        String email,

        @NotBlank(message = "password is required")
        @Size(min = 8, max = 128, message = "password length must be between 8 and 128")
        String password
) {
}
