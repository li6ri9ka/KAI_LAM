package com.kai_lam.tasks_service.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "task")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_task", nullable = false, updatable = false)
    private UUID idTask;

    @Column(name = "info_project_id", nullable = false)
    private UUID infoProjectId;

    @Column(name = "name_task", nullable = false, length = 200)
    private String nameTask;

    @Column(name = "description_task", columnDefinition = "TEXT")
    private String descriptionTask;

    @Column(name = "create_task", nullable = false, updatable = false)
    private Instant createTask;

    @Column(name = "estimation")
    private Double estimation;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private TaskStatus status;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "required_specialty_id")
    private UUID requiredSpecialtyId;

    @PrePersist
    void prePersist() {
        if (createTask == null) {
            createTask = Instant.now();
        }
        if (status == null) {
            status = TaskStatus.OPEN;
        }
    }

    public UUID getIdTask() {
        return idTask;
    }

    public UUID getInfoProjectId() {
        return infoProjectId;
    }

    public void setInfoProjectId(UUID infoProjectId) {
        this.infoProjectId = infoProjectId;
    }

    public String getNameTask() {
        return nameTask;
    }

    public void setNameTask(String nameTask) {
        this.nameTask = nameTask;
    }

    public String getDescriptionTask() {
        return descriptionTask;
    }

    public void setDescriptionTask(String descriptionTask) {
        this.descriptionTask = descriptionTask;
    }

    public Instant getCreateTask() {
        return createTask;
    }

    public Double getEstimation() {
        return estimation;
    }

    public void setEstimation(Double estimation) {
        this.estimation = estimation;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public UUID getRequiredSpecialtyId() {
        return requiredSpecialtyId;
    }

    public void setRequiredSpecialtyId(UUID requiredSpecialtyId) {
        this.requiredSpecialtyId = requiredSpecialtyId;
    }
}
