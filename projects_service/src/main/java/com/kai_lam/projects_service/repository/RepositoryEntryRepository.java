package com.kai_lam.projects_service.repository;

import com.kai_lam.projects_service.model.RepositoryEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RepositoryEntryRepository extends JpaRepository<RepositoryEntry, UUID> {
    List<RepositoryEntry> findAllByInfoProjectIdInfoProject(UUID projectId);
}
