package com.kai_lam.searchandfiltered_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SearchDocumentUpsertRequest(
        @NotNull(message = "docId is required")
        UUID docId,

        @NotBlank(message = "docType is required")
        String docType,

        UUID teamId,

        UUID projectId,

        String title,

        String body
) {
}
