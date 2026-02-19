package com.kai_lam.projects_service.kafka;

import java.util.Map;
import java.util.UUID;

public record DomainEvent(
        String eventType,
        UUID actorId,
        String occurredAt,
        Map<String, Object> payload
) {
}
