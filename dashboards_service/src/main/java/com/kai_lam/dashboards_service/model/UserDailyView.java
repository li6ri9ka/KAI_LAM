package com.kai_lam.dashboards_service.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "user_daily_view", uniqueConstraints = @UniqueConstraint(name = "uk_user_daily_view", columnNames = {"user_id", "date"}))
public class UserDailyView {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_user_daily_view", nullable = false, updatable = false)
    private UUID idUserDailyView;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "today_task_ids", nullable = false)
    private String todayTaskIds = "[]";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "overdue_task_ids", nullable = false)
    private String overdueTaskIds = "[]";

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }

    public UUID getIdUserDailyView() {
        return idUserDailyView;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getTodayTaskIds() {
        return todayTaskIds;
    }

    public void setTodayTaskIds(String todayTaskIds) {
        this.todayTaskIds = todayTaskIds;
    }

    public String getOverdueTaskIds() {
        return overdueTaskIds;
    }

    public void setOverdueTaskIds(String overdueTaskIds) {
        this.overdueTaskIds = overdueTaskIds;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
