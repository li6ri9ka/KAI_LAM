package com.kai_lam.auth_service.service;

import com.kai_lam.auth_service.dto.*;
import com.kai_lam.auth_service.exception.BadRequestException;
import com.kai_lam.auth_service.exception.ConflictException;
import com.kai_lam.auth_service.exception.NotFoundException;
import com.kai_lam.auth_service.exception.UnauthorizedException;
import com.kai_lam.auth_service.kafka.DomainEventPublisher;
import com.kai_lam.auth_service.kafka.KafkaTopicsProperties;
import com.kai_lam.auth_service.kai_enum.AccountEnum;
import com.kai_lam.auth_service.kai_enum.RoleEnum;
import com.kai_lam.auth_service.model.AuthUser;
import com.kai_lam.auth_service.model.RefreshSession;
import com.kai_lam.auth_service.model.RoleUser;
import com.kai_lam.auth_service.repository.AuthUserRepository;
import com.kai_lam.auth_service.repository.RefreshSessionRepository;
import com.kai_lam.auth_service.repository.RoleUserRepository;
import com.kai_lam.auth_service.security.AuthUserPrincipal;
import com.kai_lam.auth_service.security.JwtService;
import com.kai_lam.auth_service.security.TokenHashService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {
    private final AuthUserRepository authUserRepository;
    private final RoleUserRepository roleUserRepository;
    private final RefreshSessionRepository refreshSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenHashService tokenHashService;
    private final DomainEventPublisher eventPublisher;
    private final KafkaTopicsProperties kafkaTopics;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(AuthUserRepository authUserRepository,
                       RoleUserRepository roleUserRepository,
                       RefreshSessionRepository refreshSessionRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       TokenHashService tokenHashService,
                       DomainEventPublisher eventPublisher,
                       KafkaTopicsProperties kafkaTopics) {
        this.authUserRepository = authUserRepository;
        this.roleUserRepository = roleUserRepository;
        this.refreshSessionRepository = refreshSessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tokenHashService = tokenHashService;
        this.eventPublisher = eventPublisher;
        this.kafkaTopics = kafkaTopics;
    }

    @Transactional
    public AuthSessionResult register(RegisterRequest request, long refreshTokenDays) {
        String login = request.login().trim();
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (authUserRepository.existsByLoginIgnoreCase(login)) {
            throw new ConflictException("User with this login already exists", Map.of("login", "already in use"));
        }
        if (authUserRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("User with this email already exists", Map.of("email", "already in use"));
        }

        RoleUser userRole = roleUserRepository.findByNameRoleIgnoreCase(RoleEnum.USER.name())
                .orElseThrow(() -> new NotFoundException("Role USER is not configured"));

        AuthUser authUser = new AuthUser();
        authUser.setLogin(login);
        authUser.setEmail(email);
        authUser.setPasswordHash(passwordEncoder.encode(request.password()));
        authUser.setStatus(AccountEnum.ACTIVE);
        authUser.setRole(userRole);

        AuthUser saved = authUserRepository.save(authUser);
        publishAuthEvent("AUTH_USER_REGISTERED", saved.getId(), saved, Map.of("source", "register"));
        return issueTokens(saved, refreshTokenDays, "REGISTER");
    }

    @Transactional
    public AuthSessionResult login(LoginRequest request, long refreshTokenDays) {
        String loginOrEmail = request.loginOrEmail().trim();
        AuthUser user = authUserRepository
                .findByLoginIgnoreCaseOrEmailIgnoreCase(loginOrEmail, loginOrEmail)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (user.getStatus() != AccountEnum.ACTIVE) {
            throw new UnauthorizedException("User account is not active");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        user.setLastLoginAt(Instant.now());
        authUserRepository.save(user);

        publishAuthEvent("AUTH_LOGIN_SUCCESS", user.getId(), user, Map.of("source", "login"));
        return issueTokens(user, refreshTokenDays, "LOGIN");
    }

    @Transactional
    public AuthSessionResult refresh(String rawRefreshToken, long refreshTokenDays) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new UnauthorizedException("Refresh token is required");
        }

        String tokenHash = tokenHashService.sha256(rawRefreshToken);
        RefreshSession session = refreshSessionRepository.findByRefreshTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Refresh token is invalid"));

        if (session.getRevokedAt() != null) {
            throw new UnauthorizedException("Refresh token has been revoked");
        }
        if (session.getExpiresAt().isBefore(Instant.now())) {
            session.setRevokedAt(Instant.now());
            refreshSessionRepository.save(session);
            throw new UnauthorizedException("Refresh token has expired");
        }

        AuthUser user = session.getAuthUser();
        if (user.getStatus() != AccountEnum.ACTIVE) {
            throw new UnauthorizedException("User account is not active");
        }

        session.setRevokedAt(Instant.now());
        refreshSessionRepository.save(session);

        publishAuthEvent("AUTH_REFRESH_SUCCESS", user.getId(), user, Map.of("source", "refresh"));
        return issueTokens(user, refreshTokenDays, "REFRESH");
    }

    @Transactional
    public MessageResponse logoutCurrentSession(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return new MessageResponse("Logged out");
        }

        String tokenHash = tokenHashService.sha256(rawRefreshToken);
        refreshSessionRepository.findByRefreshTokenHash(tokenHash).ifPresent(session -> {
            if (session.getRevokedAt() == null) {
                session.setRevokedAt(Instant.now());
                refreshSessionRepository.save(session);
                AuthUser user = session.getAuthUser();
                publishAuthEvent("AUTH_LOGOUT", user.getId(), user, Map.of("source", "logout"));
            }
        });

        return new MessageResponse("Logged out");
    }

    @Transactional(readOnly = true)
    public UserResponse me(AuthUserPrincipal principal) {
        AuthUser user = authUserRepository.findById(principal.getId())
                .orElseThrow(() -> new NotFoundException("User not found"));
        return toUserResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        AuthUser user = authUserRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return toUserResponse(user);
    }

    @Transactional(readOnly = true)
    public InternalIntrospectResponse introspect(String token) {
        if (token == null || token.isBlank()) {
            return new InternalIntrospectResponse(false, null, null, null, null);
        }

        return jwtService.parseClaims(token)
                .map(claims -> {
                    AuthUser user = authUserRepository.findById(claims.userId()).orElse(null);
                    if (user == null) {
                        return new InternalIntrospectResponse(false, claims.userId(), claims.role(), claims.expiresAt(), null);
                    }

                    boolean active = user.getStatus() == AccountEnum.ACTIVE;
                    return new InternalIntrospectResponse(
                            active,
                            user.getId(),
                            user.getRole().getNameRole(),
                            claims.expiresAt(),
                            user.getStatus()
                    );
                })
                .orElseGet(() -> new InternalIntrospectResponse(false, null, null, null, null));
    }

    @Transactional
    public RoleUpdateResponse changeRole(UUID userId, ChangeRoleRequest request) {
        RoleEnum targetRole;
        try {
            targetRole = RoleEnum.valueOf(request.role().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid role value", Map.of("role", "allowed values: USER, ADMIN"));
        }

        AuthUser user = authUserRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        RoleUser role = roleUserRepository.findByNameRoleIgnoreCase(targetRole.name())
                .orElseThrow(() -> new NotFoundException("Role is not configured"));

        user.setRole(role);
        AuthUser saved = authUserRepository.save(user);
        publishAuthEvent("AUTH_ROLE_CHANGED", saved.getId(), saved, Map.of("newRole", role.getNameRole()));

        return new RoleUpdateResponse(saved.getId(), role.getNameRole());
    }

    private AuthSessionResult issueTokens(AuthUser user, long refreshTokenDays, String reason) {
        JwtService.TokenPayload payload = jwtService.generateAccessToken(user);

        String rawRefreshToken = generateRawRefreshToken();
        RefreshSession session = new RefreshSession();
        session.setAuthUser(user);
        session.setRefreshTokenHash(tokenHashService.sha256(rawRefreshToken));
        session.setExpiresAt(Instant.now().plus(refreshTokenDays, ChronoUnit.DAYS));
        refreshSessionRepository.save(session);

        publishAuthEvent("AUTH_TOKENS_ISSUED", user.getId(), user, Map.of("reason", reason));

        AuthResponse authResponse = new AuthResponse(
                payload.token(),
                payload.expiresAt(),
                toUserResponse(user)
        );

        return new AuthSessionResult(authResponse, rawRefreshToken);
    }

    private String generateRawRefreshToken() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private UserResponse toUserResponse(AuthUser user) {
        return new UserResponse(
                user.getId(),
                user.getLogin(),
                user.getEmail(),
                user.getStatus(),
                user.getRole().getNameRole(),
                user.getCreatedAt(),
                user.getLastLoginAt()
        );
    }

    private void publishAuthEvent(String eventType, UUID actorId, AuthUser user, Map<String, Object> extra) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("idUser", user.getId().toString());
        payload.put("login", user.getLogin());
        payload.put("email", user.getEmail());
        payload.put("status", user.getStatus().name());
        payload.put("role", user.getRole().getNameRole());
        payload.putAll(extra);

        eventPublisher.publish(
                kafkaTopics.authEvents(),
                user.getId().toString(),
                eventType,
                actorId,
                payload
        );
    }

    public record AuthSessionResult(AuthResponse response, String refreshToken) {
    }
}
