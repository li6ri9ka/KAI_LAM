package com.kai_lam.projects_service.repository;

import com.kai_lam.projects_service.model.SecretKeyEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SecretKeyEntryRepository extends JpaRepository<SecretKeyEntry, UUID> {
}
