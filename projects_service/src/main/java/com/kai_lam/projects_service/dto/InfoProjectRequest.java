package com.kai_lam.projects_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record InfoProjectRequest(
        @NotNull(message = "teamId is required")
        UUID teamId,

        @NotBlank(message = "nameProject is required")
        @Size(max = 200, message = "nameProject length must be <= 200")
        String nameProject,

        @Size(max = 2000, message = "projectDescription length must be <= 2000")
        String projectDescription,

        @Size(max = 500, message = "githubLinkProject length must be <= 500")
        String githubLinkProject
) {
}
