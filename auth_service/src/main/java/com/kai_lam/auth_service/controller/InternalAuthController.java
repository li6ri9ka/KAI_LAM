package com.kai_lam.auth_service.controller;

import com.kai_lam.auth_service.config.SecurityProperties;
import com.kai_lam.auth_service.dto.InternalIntrospectRequest;
import com.kai_lam.auth_service.dto.InternalIntrospectResponse;
import com.kai_lam.auth_service.dto.UserResponse;
import com.kai_lam.auth_service.exception.UnauthorizedException;
import com.kai_lam.auth_service.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/internal/auth")
public class InternalAuthController {
    private final AuthService authService;
    private final SecurityProperties securityProperties;

    public InternalAuthController(AuthService authService, SecurityProperties securityProperties) {
        this.authService = authService;
        this.securityProperties = securityProperties;
    }

    @PostMapping("/introspect")
    public InternalIntrospectResponse introspect(@RequestHeader("X-Internal-Api-Key") String internalApiKey,
                                                 @Valid @RequestBody InternalIntrospectRequest request) {
        validateInternalApiKey(internalApiKey);
        return authService.introspect(request.token());
    }

    @GetMapping("/users/{userId}")
    public UserResponse getUserById(@RequestHeader("X-Internal-Api-Key") String internalApiKey,
                                    @PathVariable UUID userId) {
        validateInternalApiKey(internalApiKey);
        return authService.getUserById(userId);
    }

    private void validateInternalApiKey(String internalApiKey) {
        String configured = securityProperties.internal().apiKey();
        if (configured == null || configured.isBlank() || !configured.equals(internalApiKey)) {
            throw new UnauthorizedException("Invalid internal api key");
        }
    }
}
