package com.kai_lam.user_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateNameSpecialtyRequest(
        @NotBlank(message = "nameSpecialty is required")
        @Size(max = 120, message = "nameSpecialty length must be <= 120")
        String nameSpecialty
) {
}
