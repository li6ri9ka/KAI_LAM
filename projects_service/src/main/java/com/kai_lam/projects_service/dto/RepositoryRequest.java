package com.kai_lam.projects_service.dto;

import jakarta.validation.constraints.Size;

public record RepositoryRequest(
        @Size(max = 100, message = "provider length must be <= 100")
        String provider,

        @Size(max = 500, message = "repoUrl length must be <= 500")
        String repoUrl,

        String readmeMd
) {
}
