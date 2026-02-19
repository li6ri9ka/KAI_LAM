package com.kai_lam.projects_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FileMeetingRequest(
        @NotBlank(message = "fileMeetS3 is required")
        @Size(max = 500, message = "fileMeetS3 length must be <= 500")
        String fileMeetS3
) {
}
