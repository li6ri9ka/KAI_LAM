package com.kai_lam.tasks_service.kafka;

import java.util.Map;
import java.util.UUID;

public interface DomainEventPublisher {
    void publish(String topic, String key, String eventType, UUID actorId, Map<String, Object> payload);
}
