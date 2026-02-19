package com.kai_lam.user_service.repository;

import com.kai_lam.user_service.model.NameSpecialty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NameSpecialtyRepository extends JpaRepository<NameSpecialty, UUID> {
    Optional<NameSpecialty> findByNameSpecialtyIgnoreCase(String nameSpecialty);
}
