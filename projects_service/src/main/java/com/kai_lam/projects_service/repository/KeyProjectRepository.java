package com.kai_lam.projects_service.repository;

import com.kai_lam.projects_service.model.KeyProject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KeyProjectRepository extends JpaRepository<KeyProject, UUID> {
    List<KeyProject> findAllByInfoProjectIdInfoProject(UUID projectId);

    Optional<KeyProject> findByInfoProjectIdInfoProjectAndSecretKeyIdSecretKey(UUID projectId, UUID secretKeyId);
}
