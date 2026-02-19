package com.kai_lam.searchandfiltered_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kai_lam.searchandfiltered_service.kafka.DomainEventMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "app.search.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class SearchIndexerService {
    private final SearchService searchService;
    private final ObjectMapper objectMapper;
    public SearchIndexerService(SearchService searchService, ObjectMapper objectMapper) {
        this.searchService = searchService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(id = "search-indexer", topics = "#{'${app.search.kafka.topics}'.split(',')}")
    public void onEvent(String message) {
        try {
            DomainEventMessage event = objectMapper.readValue(message, DomainEventMessage.class);
            handle(event);
        } catch (Exception ignored) {
            // ignore malformed messages, do not stop consumer
        }
    }

    private void handle(DomainEventMessage event) {
        String type = event.eventType() == null ? "" : event.eventType().trim().toUpperCase();
        Map<String, Object> payload = event.payload() == null ? Map.of() : event.payload();

        switch (type) {
            case "USER_CREATED", "USER_UPDATED" ->
                    searchService.upsertFromEvent(
                            "USER",
                            getString(payload, "idUser", "scopeId"),
                            null,
                            null,
                            getString(payload, "nameUser", "login", "name"),
                            joinBody(List.of(
                                    getString(payload, "midleNameUser"),
                                    getString(payload, "nameSpecialty"),
                                    getString(payload, "email")
                            )),
                            event.occurredAt()
                    );
            case "USER_DELETED" -> searchService.deleteFromEvent("USER", getString(payload, "idUser", "scopeId"));

            case "TEAM_CREATED" ->
                    searchService.upsertFromEvent(
                            "TEAM",
                            getString(payload, "idTeam", "scopeId"),
                            toUuid(getString(payload, "idTeam", "scopeId")),
                            null,
                            getString(payload, "nameTeam", "name"),
                            "",
                            event.occurredAt()
                    );

            case "TASK_CREATED", "TASK_UPDATED", "TASK_CLAIMED", "TASK_RELEASED" ->
                    searchService.upsertFromEvent(
                            "TASK",
                            getString(payload, "idTask", "scopeId"),
                            null,
                            toUuid(getString(payload, "infoProjectId", "projectId", "scopeId")),
                            getString(payload, "nameTask", "title"),
                            joinBody(List.of(
                                    getString(payload, "status"),
                                    getString(payload, "dueDate"),
                                    getString(payload, "requiredSpecialtyId")
                            )),
                            event.occurredAt()
                    );
            case "TASK_DELETED" -> searchService.deleteFromEvent("TASK", getString(payload, "idTask", "scopeId"));

            case "PROJECT_CREATED", "PROJECT_UPDATED" ->
                    searchService.upsertFromEvent(
                            "PROJECT",
                            getString(payload, "idInfoProject", "scopeId"),
                            toUuid(getString(payload, "teamId")),
                            toUuid(getString(payload, "idInfoProject", "scopeId")),
                            getString(payload, "nameProject", "title", "scopeId"),
                            getString(payload, "projectDescription", "body"),
                            event.occurredAt()
                    );
            case "PROJECT_DELETED" -> searchService.deleteFromEvent("PROJECT", getString(payload, "idInfoProject", "scopeId"));

            default -> {
                // ignore unsupported events
            }
        }
    }

    private String getString(Map<String, Object> payload, String... keys) {
        for (String key : keys) {
            Object value = payload.get(key);
            if (value != null && !String.valueOf(value).isBlank()) {
                return String.valueOf(value).trim();
            }
        }
        return "";
    }

    private UUID toUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value.trim());
        } catch (Exception ex) {
            return null;
        }
    }

    private String joinBody(List<String> parts) {
        return parts.stream().filter(v -> v != null && !v.isBlank()).reduce((a, b) -> a + " | " + b).orElse("");
    }
}
