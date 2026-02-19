package com.kai_lam.auth_service.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaDomainEventPublisher implements DomainEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(KafkaDomainEventPublisher.class);

    private final KafkaTemplate<Object, Object> kafkaTemplate;

    public KafkaDomainEventPublisher(KafkaTemplate<Object, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(String topic, String key, String eventType, UUID actorId, Map<String, Object> payload) {
        DomainEvent event = new DomainEvent(eventType, actorId, Instant.now().toString(), payload);

        try {
            kafkaTemplate.send(topic, key, event);
        } catch (Exception ex) {
            log.error("Failed to publish domain event. topic={}, key={}, eventType={}", topic, key, eventType, ex);
        }
    }
}
