package com.kai_lam.tasks_service.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_task")
public class UserTask {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_user_task", nullable = false, updatable = false)
    private UUID idUserTask;

    @Column(name = "user_team_id", nullable = false)
    private UUID userTeamId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt;

    @Column(name = "released_at")
    private Instant releasedAt;

    @Column(name = "active", nullable = false)
    private boolean active;

    @PrePersist
    void prePersist() {
        if (assignedAt == null) {
            assignedAt = Instant.now();
        }
        active = true;
    }

    public UUID getIdUserTask() {
        return idUserTask;
    }

    public UUID getUserTeamId() {
        return userTeamId;
    }

    public void setUserTeamId(UUID userTeamId) {
        this.userTeamId = userTeamId;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public Instant getReleasedAt() {
        return releasedAt;
    }

    public void setReleasedAt(Instant releasedAt) {
        this.releasedAt = releasedAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
