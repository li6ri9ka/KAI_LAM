package com.kai_lam.dashboards_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kai_lam.dashboards_service.kafka.DomainEventMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "app.dashboard.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class DashboardProjectionService {
    private final UserDailyViewService userDailyViewService;
    private final ObjectMapper objectMapper;

    public DashboardProjectionService(UserDailyViewService userDailyViewService, ObjectMapper objectMapper) {
        this.userDailyViewService = userDailyViewService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(id = "dashboard-projection", topics = "#{'${app.dashboard.kafka.topics}'.split(',')}")
    public void onEvent(String message) {
        try {
            DomainEventMessage event = objectMapper.readValue(message, DomainEventMessage.class);
            handle(event);
        } catch (Exception ignored) {
            // ignore malformed messages and continue consuming
        }
    }

    private void handle(DomainEventMessage event) {
        String type = event.eventType() == null ? "" : event.eventType().trim().toUpperCase();
        Map<String, Object> payload = event.payload() == null ? Map.of() : event.payload();

        UUID userId = getUuid(payload, "userId", "idUser", "authUserId");
        if (userId == null) {
            userId = event.actorId();
        }
        UUID taskId = getUuid(payload, "idTask", "taskId", "scopeId");

        if (userId == null || taskId == null) {
            return;
        }

        LocalDate dueDate = parseDate(payload.get("dueDate"));
        LocalDate today = LocalDate.now();
        boolean overdue = dueDate != null && dueDate.isBefore(today);
        LocalDate targetDate = dueDate == null ? today : dueDate;

        switch (type) {
            case "TASK_CLAIMED" -> userDailyViewService.addTask(userId, targetDate, taskId, overdue);
            case "TASK_RELEASED", "TASK_DELETED" -> userDailyViewService.removeTaskFromAllViews(userId, taskId);
            case "TASK_UPDATED" -> {
                String status = getString(payload, "status");
                if ("DONE".equalsIgnoreCase(status)) {
                    userDailyViewService.removeTaskFromAllViews(userId, taskId);
                }
            }
            default -> {
                // ignore unsupported event types
            }
        }
    }

    private UUID getUuid(Map<String, Object> payload, String... keys) {
        for (String key : keys) {
            Object raw = payload.get(key);
            if (raw == null) {
                continue;
            }
            try {
                return UUID.fromString(String.valueOf(raw).trim());
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private String getString(Map<String, Object> payload, String... keys) {
        for (String key : keys) {
            Object raw = payload.get(key);
            if (raw != null && !String.valueOf(raw).isBlank()) {
                return String.valueOf(raw).trim();
            }
        }
        return "";
    }

    private LocalDate parseDate(Object raw) {
        if (raw == null) {
            return null;
        }
        try {
            String value = String.valueOf(raw).trim();
            return value.isBlank() ? null : LocalDate.parse(value);
        } catch (Exception ex) {
            return null;
        }
    }
}
