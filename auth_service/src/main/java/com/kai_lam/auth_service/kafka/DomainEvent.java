package com.kai_lam.auth_service.kafka;

import java.util.Map;
import java.util.UUID;

public record DomainEvent(
        String eventType,
        UUID actorId,
        String occurredAt,
        Map<String, Object> payload
) {
}
