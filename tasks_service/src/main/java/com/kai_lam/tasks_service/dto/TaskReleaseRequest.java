package com.kai_lam.tasks_service.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TaskReleaseRequest(
        @NotNull(message = "userTeamId is required")
        UUID userTeamId
) {
}
