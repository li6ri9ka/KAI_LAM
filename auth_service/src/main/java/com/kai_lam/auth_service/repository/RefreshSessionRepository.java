package com.kai_lam.auth_service.repository;

import com.kai_lam.auth_service.model.RefreshSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshSessionRepository extends JpaRepository<RefreshSession, UUID> {
    Optional<RefreshSession> findByRefreshTokenHash(String refreshTokenHash);
}
