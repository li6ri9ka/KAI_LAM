package com.kai_lam.user_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTeamRequest(
        @NotBlank(message = "nameTeam is required")
        @Size(max = 120, message = "nameTeam length must be <= 120")
        String nameTeam
) {
}
