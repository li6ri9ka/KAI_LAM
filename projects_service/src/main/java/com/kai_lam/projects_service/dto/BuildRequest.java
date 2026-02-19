package com.kai_lam.projects_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BuildRequest(
        @NotBlank(message = "nameBuild is required")
        @Size(max = 200, message = "nameBuild length must be <= 200")
        String nameBuild,

        @Size(max = 100, message = "releaseVersion length must be <= 100")
        String releaseVersion
) {
}
