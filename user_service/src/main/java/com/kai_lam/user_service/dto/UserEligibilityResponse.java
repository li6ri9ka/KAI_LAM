package com.kai_lam.user_service.dto;

import java.util.List;
import java.util.UUID;

public record UserEligibilityResponse(
        UUID userId,
        UUID nameSpecUserId,
        String nameSpecialty,
        List<UUID> teamIds,
        boolean availableForTask
) {
}
