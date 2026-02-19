package com.kai_lam.tasks_service.repository;

import com.kai_lam.tasks_service.model.UserTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserTaskRepository extends JpaRepository<UserTask, UUID> {
    long countByUserTeamIdAndActiveTrue(UUID userTeamId);

    boolean existsByTaskIdTaskAndActiveTrue(UUID taskId);

    Optional<UserTask> findByTaskIdTaskAndUserTeamIdAndActiveTrue(UUID taskId, UUID userTeamId);

    Optional<UserTask> findByTaskIdTaskAndActiveTrue(UUID taskId);

    List<UserTask> findAllByUserTeamIdAndActiveTrue(UUID userTeamId);

    List<UserTask> findAllByTaskIdTask(UUID taskId);
}
