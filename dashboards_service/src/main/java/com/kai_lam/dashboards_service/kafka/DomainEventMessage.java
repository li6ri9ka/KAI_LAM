package com.kai_lam.dashboards_service.kafka;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record DomainEventMessage(
        String eventType,
        UUID actorId,
        Instant occurredAt,
        Map<String, Object> payload
) {
}
