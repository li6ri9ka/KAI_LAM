package com.kai_lam.dashboards_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kai_lam.dashboards_service.dto.GanttDashboardResponse;
import com.kai_lam.dashboards_service.dto.KanbanDashboardResponse;
import com.kai_lam.dashboards_service.dto.UserDailyViewResponse;
import com.kai_lam.dashboards_service.model.UserDailyView;
import com.kai_lam.dashboards_service.repository.UserDailyViewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class UserDailyViewService {
    private final UserDailyViewRepository repository;
    private final ObjectMapper objectMapper;

    public UserDailyViewService(UserDailyViewRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public UserDailyViewResponse getDaily(UUID userId, LocalDate date) {
        return repository.findByUserIdAndDate(userId, date)
                .map(this::toResponse)
                .orElseGet(() -> new UserDailyViewResponse(userId, date, List.of(), List.of(), Instant.now()));
    }

    @Transactional
    public UserDailyViewResponse upsertDaily(UUID userId, LocalDate date, List<UUID> todayTaskIds, List<UUID> overdueTaskIds) {
        UserDailyView view = getOrCreate(userId, date);
        view.setTodayTaskIds(toJson(unique(todayTaskIds)));
        view.setOverdueTaskIds(toJson(unique(overdueTaskIds)));
        return toResponse(repository.save(view));
    }

    @Transactional(readOnly = true)
    public KanbanDashboardResponse getKanban(UUID userId, LocalDate date) {
        UserDailyViewResponse daily = getDaily(userId, date);
        return new KanbanDashboardResponse(
                userId,
                date,
                daily.todayTaskIds(),
                daily.overdueTaskIds(),
                List.of(),
                List.of()
        );
    }

    @Transactional(readOnly = true)
    public GanttDashboardResponse getGantt(UUID userId, LocalDate from, LocalDate to) {
        List<UserDailyView> rows = repository.findAllByUserIdAndDateBetweenOrderByDateAsc(userId, from, to);
        List<GanttDashboardResponse.GanttDayBucket> days = new ArrayList<>();

        for (UserDailyView row : rows) {
            days.add(new GanttDashboardResponse.GanttDayBucket(
                    row.getDate(),
                    parseTaskIds(row.getTodayTaskIds()),
                    parseTaskIds(row.getOverdueTaskIds())
            ));
        }

        return new GanttDashboardResponse(userId, from, to, days);
    }

    @Transactional
    public void addTask(UUID userId, LocalDate date, UUID taskId, boolean overdue) {
        UserDailyView view = getOrCreate(userId, date);
        Set<UUID> today = new LinkedHashSet<>(parseTaskIds(view.getTodayTaskIds()));
        Set<UUID> overdueSet = new LinkedHashSet<>(parseTaskIds(view.getOverdueTaskIds()));

        today.add(taskId);
        if (overdue) {
            overdueSet.add(taskId);
        }

        view.setTodayTaskIds(toJson(today));
        view.setOverdueTaskIds(toJson(overdueSet));
        repository.save(view);
    }

    @Transactional
    public void removeTaskFromAllViews(UUID userId, UUID taskId) {
        List<UserDailyView> rows = repository.findAllByUserIdOrderByDateAsc(userId);
        for (UserDailyView row : rows) {
            Set<UUID> today = new LinkedHashSet<>(parseTaskIds(row.getTodayTaskIds()));
            Set<UUID> overdue = new LinkedHashSet<>(parseTaskIds(row.getOverdueTaskIds()));

            boolean changed = today.remove(taskId) | overdue.remove(taskId);
            if (changed) {
                row.setTodayTaskIds(toJson(today));
                row.setOverdueTaskIds(toJson(overdue));
                repository.save(row);
            }
        }
    }

    private UserDailyView getOrCreate(UUID userId, LocalDate date) {
        return repository.findByUserIdAndDate(userId, date).orElseGet(() -> {
            UserDailyView view = new UserDailyView();
            view.setUserId(userId);
            view.setDate(date);
            view.setTodayTaskIds("[]");
            view.setOverdueTaskIds("[]");
            return repository.save(view);
        });
    }

    private UserDailyViewResponse toResponse(UserDailyView view) {
        return new UserDailyViewResponse(
                view.getUserId(),
                view.getDate(),
                parseTaskIds(view.getTodayTaskIds()),
                parseTaskIds(view.getOverdueTaskIds()),
                view.getUpdatedAt() == null ? Instant.now() : view.getUpdatedAt()
        );
    }

    private List<UUID> parseTaskIds(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            List<String> raw = objectMapper.readValue(json, new TypeReference<>() {});
            return raw.stream().map(this::safeUuid).filter(v -> v != null).toList();
        } catch (Exception ex) {
            return List.of();
        }
    }

    private String toJson(Set<UUID> ids) {
        List<String> values = ids.stream().map(UUID::toString).toList();
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException ex) {
            return "[]";
        }
    }

    private UUID safeUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (Exception ex) {
            return null;
        }
    }

    private Set<UUID> unique(List<UUID> values) {
        if (values == null) {
            return Set.of();
        }
        return new LinkedHashSet<>(values.stream().filter(v -> v != null).toList());
    }
}
