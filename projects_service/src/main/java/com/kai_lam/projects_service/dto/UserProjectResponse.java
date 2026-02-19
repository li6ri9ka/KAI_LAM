package com.kai_lam.projects_service.dto;

import java.util.UUID;

public record UserProjectResponse(
        UUID idUserProject,
        UUID infoProjectId,
        UUID userTeamId,
        String projectRole
) {
}
