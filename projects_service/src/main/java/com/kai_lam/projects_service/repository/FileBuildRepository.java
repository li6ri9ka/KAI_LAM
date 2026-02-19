package com.kai_lam.projects_service.repository;

import com.kai_lam.projects_service.model.FileBuild;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FileBuildRepository extends JpaRepository<FileBuild, UUID> {
    List<FileBuild> findAllByBuildIdBuild(UUID buildId);
}
