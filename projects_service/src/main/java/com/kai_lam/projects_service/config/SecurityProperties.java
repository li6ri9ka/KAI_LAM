package com.kai_lam.projects_service.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(Jwt jwt) {

    public record Jwt(
            @NotBlank String issuer,
            @NotBlank String secret
    ) {
    }
}
