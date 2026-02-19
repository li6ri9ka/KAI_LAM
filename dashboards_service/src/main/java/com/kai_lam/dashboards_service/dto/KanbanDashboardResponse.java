package com.kai_lam.dashboards_service.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record KanbanDashboardResponse(
        UUID userId,
        LocalDate date,
        List<UUID> backlogTaskIds,
        List<UUID> overdueTaskIds,
        List<UUID> inProgressTaskIds,
        List<UUID> doneTaskIds
) {
}
