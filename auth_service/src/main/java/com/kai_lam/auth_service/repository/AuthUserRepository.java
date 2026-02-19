package com.kai_lam.auth_service.repository;

import com.kai_lam.auth_service.model.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AuthUserRepository extends JpaRepository<AuthUser, UUID> {
    Optional<AuthUser> findByLoginIgnoreCase(String login);

    Optional<AuthUser> findByEmailIgnoreCase(String email);

    Optional<AuthUser> findByLoginIgnoreCaseOrEmailIgnoreCase(String login, String email);

    boolean existsByLoginIgnoreCase(String login);

    boolean existsByEmailIgnoreCase(String email);
}
