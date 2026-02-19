package com.kai_lam.auth_service.controller;

import com.kai_lam.auth_service.config.SecurityProperties;
import com.kai_lam.auth_service.dto.*;
import com.kai_lam.auth_service.security.AuthUserPrincipal;
import com.kai_lam.auth_service.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final SecurityProperties securityProperties;

    public AuthController(AuthService authService, SecurityProperties securityProperties) {
        this.authService = authService;
        this.securityProperties = securityProperties;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthService.AuthSessionResult result = authService.register(request, securityProperties.jwt().refreshTokenDays());
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(result.refreshToken()).toString())
                .body(result.response());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthService.AuthSessionResult result = authService.login(request, securityProperties.jwt().refreshTokenDays());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(result.refreshToken()).toString())
                .body(result.response());
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request) {
        String refreshToken = extractRefreshCookie(request);
        AuthService.AuthSessionResult result = authService.refresh(refreshToken, securityProperties.jwt().refreshTokenDays());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(result.refreshToken()).toString())
                .body(result.response());
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(HttpServletRequest request) {
        String refreshToken = extractRefreshCookie(request);
        MessageResponse response = authService.logoutCurrentSession(refreshToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString())
                .body(response);
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal AuthUserPrincipal principal) {
        return authService.me(principal);
    }

    @PatchMapping("/users/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public RoleUpdateResponse changeRole(@PathVariable UUID userId,
                                         @Valid @RequestBody ChangeRoleRequest request) {
        return authService.changeRole(userId, request);
    }

    private String extractRefreshCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        String cookieName = securityProperties.cookie().refreshName();
        for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private ResponseCookie buildRefreshCookie(String value) {
        return ResponseCookie.from(securityProperties.cookie().refreshName(), value)
                .httpOnly(true)
                .secure(securityProperties.cookie().refreshSecure())
                .path("/")
                .sameSite("Strict")
                .maxAge(Duration.ofDays(securityProperties.jwt().refreshTokenDays()))
                .build();
    }

    private ResponseCookie clearRefreshCookie() {
        return ResponseCookie.from(securityProperties.cookie().refreshName(), "")
                .httpOnly(true)
                .secure(securityProperties.cookie().refreshSecure())
                .path("/")
                .sameSite("Strict")
                .maxAge(Duration.ZERO)
                .build();
    }
}
