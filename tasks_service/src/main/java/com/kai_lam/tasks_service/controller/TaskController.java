package com.kai_lam.tasks_service.controller;

import com.kai_lam.tasks_service.dto.*;
import com.kai_lam.tasks_service.model.TaskStatus;
import com.kai_lam.tasks_service.security.AuthPrincipal;
import com.kai_lam.tasks_service.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(@AuthenticationPrincipal AuthPrincipal principal,
                                   @Valid @RequestBody TaskCreateRequest request) {
        return taskService.createTask(principal, request);
    }

    @GetMapping
    public Page<TaskResponse> listTasks(@RequestParam(required = false) UUID infoProjectId,
                                        @RequestParam(required = false) TaskStatus status,
                                        @RequestParam(required = false) UUID requiredSpecialtyId,
                                        @RequestParam(required = false) LocalDate dueDateFrom,
                                        @RequestParam(required = false) LocalDate dueDateTo,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size,
                                        @RequestParam(defaultValue = "createTask") String sortBy,
                                        @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return taskService.listTasks(infoProjectId, status, requiredSpecialtyId, dueDateFrom, dueDateTo, pageable);
    }

    @GetMapping("/{taskId}")
    public TaskResponse getTask(@PathVariable UUID taskId) {
        return taskService.getTask(taskId);
    }

    @PatchMapping("/{taskId}")
    public TaskResponse updateTask(@AuthenticationPrincipal AuthPrincipal principal,
                                   @PathVariable UUID taskId,
                                   @Valid @RequestBody TaskUpdateRequest request) {
        return taskService.updateTask(principal, taskId, request);
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@AuthenticationPrincipal AuthPrincipal principal,
                           @PathVariable UUID taskId) {
        taskService.deleteTask(principal, taskId);
    }

    @PostMapping("/{taskId}/claim")
    public TaskResponse claimTask(@AuthenticationPrincipal AuthPrincipal principal,
                                  @PathVariable UUID taskId,
                                  @Valid @RequestBody TaskClaimRequest request) {
        return taskService.claimTask(principal, taskId, request);
    }

    @PostMapping("/{taskId}/release")
    public TaskResponse releaseTask(@AuthenticationPrincipal AuthPrincipal principal,
                                    @PathVariable UUID taskId,
                                    @Valid @RequestBody TaskReleaseRequest request) {
        return taskService.releaseTask(principal, taskId, request);
    }

    @GetMapping("/assignments/active")
    public List<UserTaskResponse> listActiveAssignments(@RequestParam UUID userTeamId) {
        return taskService.listActiveAssignments(userTeamId);
    }
}
