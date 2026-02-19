package com.kai_lam.projects_service.dto;

import java.util.UUID;

public record BuildResponse(
        UUID idBuild,
        String nameBuild,
        String releaseVersion,
        UUID infoProjectId
) {
}
