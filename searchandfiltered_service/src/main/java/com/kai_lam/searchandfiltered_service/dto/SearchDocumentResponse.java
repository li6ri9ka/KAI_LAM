package com.kai_lam.searchandfiltered_service.dto;

import java.time.Instant;
import java.util.UUID;

public record SearchDocumentResponse(
        UUID docId,
        String docType,
        UUID teamId,
        UUID projectId,
        String title,
        String body,
        Instant updatedAt
) {
}
