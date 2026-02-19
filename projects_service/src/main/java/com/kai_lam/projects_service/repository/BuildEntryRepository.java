package com.kai_lam.projects_service.repository;

import com.kai_lam.projects_service.model.BuildEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BuildEntryRepository extends JpaRepository<BuildEntry, UUID> {
    List<BuildEntry> findAllByInfoProjectIdInfoProject(UUID projectId);
}
