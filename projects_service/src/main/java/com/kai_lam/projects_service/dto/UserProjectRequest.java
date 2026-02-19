package com.kai_lam.projects_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UserProjectRequest(
        @NotNull(message = "userTeamId is required")
        UUID userTeamId,

        @NotBlank(message = "projectRole is required")
        @Size(max = 100, message = "projectRole length must be <= 100")
        String projectRole
) {
}
