package com.kai_lam.tasks_service.dto;

import java.time.Instant;
import java.util.UUID;

public record UserTaskResponse(
        UUID idUserTask,
        UUID userTeamId,
        UUID taskId,
        Instant assignedAt,
        Instant releasedAt,
        boolean active
) {
}
