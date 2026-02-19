package com.kai_lam.projects_service.repository;

import com.kai_lam.projects_service.model.UserProject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserProjectRepository extends JpaRepository<UserProject, UUID> {
    List<UserProject> findAllByInfoProjectIdInfoProject(UUID projectId);
    List<UserProject> findAllByUserTeamIdIn(List<UUID> userTeamIds);

    Optional<UserProject> findByInfoProjectIdInfoProjectAndUserTeamId(UUID projectId, UUID userTeamId);
}
