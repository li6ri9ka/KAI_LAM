package com.kai_lam.auth_service.dto;

import java.util.UUID;

public record RoleUpdateResponse(UUID userId, String role) {
}
