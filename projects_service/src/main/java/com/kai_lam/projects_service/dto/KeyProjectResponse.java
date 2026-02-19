package com.kai_lam.projects_service.dto;

import java.util.UUID;

public record KeyProjectResponse(
        UUID idKeyProject,
        UUID infoProjectId,
        UUID secretKeyId,
        String nameSecretKey
) {
}
