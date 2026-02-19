package com.kai_lam.tasks_service.service;

import com.kai_lam.tasks_service.dto.*;
import com.kai_lam.tasks_service.exception.BadRequestException;
import com.kai_lam.tasks_service.exception.ConflictException;
import com.kai_lam.tasks_service.exception.NotFoundException;
import com.kai_lam.tasks_service.kafka.DomainEventPublisher;
import com.kai_lam.tasks_service.kafka.KafkaTopicsProperties;
import com.kai_lam.tasks_service.model.Task;
import com.kai_lam.tasks_service.model.TaskStatus;
import com.kai_lam.tasks_service.model.UserTask;
import com.kai_lam.tasks_service.repository.TaskRepository;
import com.kai_lam.tasks_service.repository.UserTaskRepository;
import com.kai_lam.tasks_service.security.AuthPrincipal;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TaskService {
    private static final long MAX_ACTIVE_TASKS_PER_USER_TEAM = 2;

    private final TaskRepository taskRepository;
    private final UserTaskRepository userTaskRepository;
    private final DomainEventPublisher eventPublisher;
    private final KafkaTopicsProperties kafkaTopics;

    public TaskService(TaskRepository taskRepository,
                       UserTaskRepository userTaskRepository,
                       DomainEventPublisher eventPublisher,
                       KafkaTopicsProperties kafkaTopics) {
        this.taskRepository = taskRepository;
        this.userTaskRepository = userTaskRepository;
        this.eventPublisher = eventPublisher;
        this.kafkaTopics = kafkaTopics;
    }

    @Transactional
    public TaskResponse createTask(AuthPrincipal principal, TaskCreateRequest request) {
        Task task = new Task();
        task.setInfoProjectId(request.infoProjectId());
        task.setNameTask(request.nameTask().trim());
        task.setDescriptionTask(request.descriptionTask());
        task.setEstimation(request.estimation());
        task.setStatus(request.status() == null ? TaskStatus.OPEN : request.status());
        task.setDueDate(request.dueDate());
        task.setRequiredSpecialtyId(request.requiredSpecialtyId());

        Task saved = taskRepository.save(task);
        publishTaskEvent("TASK_CREATED", principal.getAuthUserId(), saved, Map.of());

        return toTaskResponse(saved);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(UUID taskId) {
        return toTaskResponse(findTask(taskId));
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> listTasks(UUID infoProjectId,
                                        TaskStatus status,
                                        UUID requiredSpecialtyId,
                                        LocalDate dueDateFrom,
                                        LocalDate dueDateTo,
                                        Pageable pageable) {
        Specification<Task> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (infoProjectId != null) {
                predicates.add(cb.equal(root.get("infoProjectId"), infoProjectId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (requiredSpecialtyId != null) {
                predicates.add(cb.equal(root.get("requiredSpecialtyId"), requiredSpecialtyId));
            }
            if (dueDateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dueDate"), dueDateFrom));
            }
            if (dueDateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dueDate"), dueDateTo));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };

        return taskRepository.findAll(spec, pageable).map(this::toTaskResponse);
    }

    @Transactional
    public TaskResponse updateTask(AuthPrincipal principal, UUID taskId, TaskUpdateRequest request) {
        Task task = findTask(taskId);

        if (request.nameTask() != null && !request.nameTask().isBlank()) {
            task.setNameTask(request.nameTask().trim());
        }
        if (request.descriptionTask() != null) {
            task.setDescriptionTask(request.descriptionTask());
        }
        if (request.estimation() != null) {
            task.setEstimation(request.estimation());
        }
        if (request.status() != null) {
            task.setStatus(request.status());
        }
        if (request.dueDate() != null) {
            task.setDueDate(request.dueDate());
        }
        if (request.requiredSpecialtyId() != null) {
            task.setRequiredSpecialtyId(request.requiredSpecialtyId());
        }

        Task saved = taskRepository.save(task);
        publishTaskEvent("TASK_UPDATED", principal.getAuthUserId(), saved, Map.of());

        return toTaskResponse(saved);
    }

    @Transactional
    public void deleteTask(AuthPrincipal principal, UUID taskId) {
        Task task = findTask(taskId);
        taskRepository.delete(task);
        publishTaskEvent("TASK_DELETED", principal.getAuthUserId(), task, Map.of());
    }

    @Transactional
    public TaskResponse claimTask(AuthPrincipal principal, UUID taskId, TaskClaimRequest request) {
        Task task = findTask(taskId);

        if (task.getStatus() == TaskStatus.DONE) {
            throw new BadRequestException("DONE task cannot be claimed");
        }

        if (userTaskRepository.existsByTaskIdTaskAndActiveTrue(taskId)) {
            throw new ConflictException("Task is already claimed by another user");
        }

        long activeCount = userTaskRepository.countByUserTeamIdAndActiveTrue(request.userTeamId());
        if (activeCount >= MAX_ACTIVE_TASKS_PER_USER_TEAM) {
            throw new BadRequestException("User team already has 2 active tasks");
        }

        UserTask userTask = new UserTask();
        userTask.setTask(task);
        userTask.setUserTeamId(request.userTeamId());
        userTask.setActive(true);
        UserTask savedUserTask = userTaskRepository.save(userTask);

        task.setStatus(TaskStatus.IN_PROGRESS);
        Task savedTask = taskRepository.save(task);

        publishTaskEvent(
                "TASK_CLAIMED",
                principal.getAuthUserId(),
                savedTask,
                Map.of(
                        "userTeamId", request.userTeamId().toString(),
                        "idUserTask", savedUserTask.getIdUserTask().toString()
                )
        );

        return toTaskResponse(savedTask);
    }

    @Transactional
    public TaskResponse releaseTask(AuthPrincipal principal, UUID taskId, TaskReleaseRequest request) {
        Task task = findTask(taskId);

        UserTask userTask = userTaskRepository.findByTaskIdTaskAndUserTeamIdAndActiveTrue(taskId, request.userTeamId())
                .orElseThrow(() -> new NotFoundException("Active task assignment not found"));

        userTask.setActive(false);
        userTask.setReleasedAt(Instant.now());
        userTaskRepository.save(userTask);

        if (task.getStatus() == TaskStatus.IN_PROGRESS) {
            task.setStatus(TaskStatus.OPEN);
            taskRepository.save(task);
        }

        publishTaskEvent(
                "TASK_RELEASED",
                principal.getAuthUserId(),
                task,
                Map.of(
                        "userTeamId", request.userTeamId().toString(),
                        "idUserTask", userTask.getIdUserTask().toString()
                )
        );

        return toTaskResponse(task);
    }

    @Transactional(readOnly = true)
    public List<UserTaskResponse> listActiveAssignments(UUID userTeamId) {
        return userTaskRepository.findAllByUserTeamIdAndActiveTrue(userTeamId)
                .stream()
                .map(this::toUserTaskResponse)
                .toList();
    }

    private Task findTask(UUID taskId) {
        return taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException("Task not found"));
    }

    private TaskResponse toTaskResponse(Task task) {
        UserTaskResponse active = userTaskRepository.findByTaskIdTaskAndActiveTrue(task.getIdTask())
                .map(this::toUserTaskResponse)
                .orElse(null);

        return new TaskResponse(
                task.getIdTask(),
                task.getInfoProjectId(),
                task.getNameTask(),
                task.getDescriptionTask(),
                task.getCreateTask(),
                task.getEstimation(),
                task.getStatus(),
                task.getDueDate(),
                task.getRequiredSpecialtyId(),
                active
        );
    }

    private UserTaskResponse toUserTaskResponse(UserTask userTask) {
        return new UserTaskResponse(
                userTask.getIdUserTask(),
                userTask.getUserTeamId(),
                userTask.getTask().getIdTask(),
                userTask.getAssignedAt(),
                userTask.getReleasedAt(),
                userTask.isActive()
        );
    }

    private void publishTaskEvent(String eventType, UUID actorId, Task task, Map<String, Object> extra) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("idTask", task.getIdTask().toString());
        payload.put("infoProjectId", task.getInfoProjectId().toString());
        payload.put("nameTask", task.getNameTask());
        payload.put("status", task.getStatus().name());
        payload.put("dueDate", task.getDueDate() == null ? "" : task.getDueDate().toString());
        payload.put("requiredSpecialtyId", task.getRequiredSpecialtyId() == null ? "" : task.getRequiredSpecialtyId().toString());
        payload.putAll(extra);

        eventPublisher.publish(
                kafkaTopics.taskEvents(),
                task.getIdTask().toString(),
                eventType,
                actorId,
                payload
        );
    }
}
