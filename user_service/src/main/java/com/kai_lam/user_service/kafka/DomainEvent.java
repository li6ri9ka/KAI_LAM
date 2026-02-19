package com.kai_lam.user_service.kafka;

import java.util.Map;
import java.util.UUID;

public record DomainEvent(
        String eventType,
        UUID actorId,
        String occurredAt,
        Map<String, Object> payload
) {
}
