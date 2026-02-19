package com.kai_lam.projects_service.dto;

import java.util.UUID;

public record FileBuildResponse(
        UUID idFileBuild,
        UUID buildId,
        String linkFileBuildS3
) {
}
