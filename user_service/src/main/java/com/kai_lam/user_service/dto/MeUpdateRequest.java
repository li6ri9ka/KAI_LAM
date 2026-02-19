package com.kai_lam.user_service.dto;

import jakarta.validation.constraints.Size;

import java.util.UUID;

public record MeUpdateRequest(
        @Size(min = 2, max = 120, message = "nameUser length must be between 2 and 120")
        String nameUser,

        @Size(max = 120, message = "midleNameUser length must be <= 120")
        String midleNameUser,

        @Size(max = 255, message = "emailUser length must be <= 255")
        String emailUser,

        @Size(max = 64, message = "phoneUser length must be <= 64")
        String phoneUser,

        @Size(max = 128, message = "telegramUser length must be <= 128")
        String telegramUser,

        @Size(max = 120, message = "nameSpecialty length must be <= 120")
        String nameSpecialty,

        UUID nameSpecUserId
) {
}
