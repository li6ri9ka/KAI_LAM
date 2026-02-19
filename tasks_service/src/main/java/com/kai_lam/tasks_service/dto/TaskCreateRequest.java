package com.kai_lam.tasks_service.dto;

import com.kai_lam.tasks_service.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record TaskCreateRequest(
        @NotNull(message = "infoProjectId is required")
        UUID infoProjectId,

        @NotBlank(message = "nameTask is required")
        @Size(max = 200, message = "nameTask length must be <= 200")
        String nameTask,

        String descriptionTask,

        Double estimation,

        TaskStatus status,

        LocalDate dueDate,

        UUID requiredSpecialtyId
) {
}
