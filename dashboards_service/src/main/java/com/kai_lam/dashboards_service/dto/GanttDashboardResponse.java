package com.kai_lam.dashboards_service.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record GanttDashboardResponse(
        UUID userId,
        LocalDate from,
        LocalDate to,
        List<GanttDayBucket> days
) {
    public record GanttDayBucket(
            LocalDate date,
            List<UUID> todayTaskIds,
            List<UUID> overdueTaskIds
    ) {
    }
}
