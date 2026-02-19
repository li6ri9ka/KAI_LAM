package com.kai_lam.tasks_service.dto;

import com.kai_lam.tasks_service.model.TaskStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TaskResponse(
        UUID idTask,
        UUID infoProjectId,
        String nameTask,
        String descriptionTask,
        Instant createTask,
        Double estimation,
        TaskStatus status,
        LocalDate dueDate,
        UUID requiredSpecialtyId,
        UserTaskResponse activeAssignment
) {
}
