package com.kai_lam.auth_service.repository;

import com.kai_lam.auth_service.model.RoleUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleUserRepository extends JpaRepository<RoleUser, UUID> {
    Optional<RoleUser> findByNameRoleIgnoreCase(String nameRole);
}
