package com.kai_lam.dashboards_service.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record UserDailyViewResponse(
        UUID userId,
        LocalDate date,
        List<UUID> todayTaskIds,
        List<UUID> overdueTaskIds,
        Instant updatedAt
) {
}
