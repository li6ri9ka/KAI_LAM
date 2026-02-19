package com.kai_lam.projects_service.dto;

import java.util.UUID;

public record RepositoryResponse(
        UUID idRepository,
        UUID infoProjectId,
        String provider,
        String repoUrl,
        String readmeMd
) {
}
