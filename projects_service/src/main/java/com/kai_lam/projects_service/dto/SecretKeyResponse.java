package com.kai_lam.projects_service.dto;

import java.util.UUID;

public record SecretKeyResponse(
        UUID idSecretKey,
        String nameSecretKey,
        String encryptedValue
) {
}
