package com.kai_lam.projects_service.repository;

import com.kai_lam.projects_service.model.InfoProject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InfoProjectRepository extends JpaRepository<InfoProject, UUID> {
    List<InfoProject> findAllByTeamId(UUID teamId);
}
