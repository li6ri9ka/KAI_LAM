package com.kai_lam.dashboards_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.dashboard.kafka")
public record DashboardKafkaProperties(
        boolean enabled,
        List<String> topics
) {
}
