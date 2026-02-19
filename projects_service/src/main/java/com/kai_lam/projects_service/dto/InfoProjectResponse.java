package com.kai_lam.projects_service.dto;

import java.util.UUID;

public record InfoProjectResponse(
        UUID idInfoProject,
        UUID teamId,
        String nameProject,
        String projectDescription,
        String githubLinkProject
) {
}
