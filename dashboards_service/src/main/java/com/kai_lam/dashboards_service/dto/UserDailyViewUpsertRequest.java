package com.kai_lam.dashboards_service.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record UserDailyViewUpsertRequest(
        @NotNull List<UUID> todayTaskIds,
        @NotNull List<UUID> overdueTaskIds
) {
}
