package com.kai_lam.projects_service.kafka;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "false")
public class NoopDomainEventPublisher implements DomainEventPublisher {
    @Override
    public void publish(String topic, String key, String eventType, UUID actorId, Map<String, Object> payload) {
        // no-op for local/test environments without Kafka
    }
}
