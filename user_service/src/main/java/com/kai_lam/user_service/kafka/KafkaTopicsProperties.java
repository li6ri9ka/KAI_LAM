package com.kai_lam.user_service.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka.topics")
public record KafkaTopicsProperties(
        String userEvents,
        String directoryEvents
) {
}
