package com.kai_lam.projects_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FileBuildRequest(
        @NotBlank(message = "linkFileBuildS3 is required")
        @Size(max = 500, message = "linkFileBuildS3 length must be <= 500")
        String linkFileBuildS3
) {
}
