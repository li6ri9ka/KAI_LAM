package com.kai_lam.searchandfiltered_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.search.kafka")
public record SearchKafkaProperties(
        boolean enabled,
        List<String> topics
) {
}
