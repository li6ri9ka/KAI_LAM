package com.kai_lam.tasks_service.service;

import com.kai_lam.tasks_service.dto.TaskClaimRequest;
import com.kai_lam.tasks_service.dto.TaskCreateRequest;
import com.kai_lam.tasks_service.dto.TaskUpdateRequest;
import com.kai_lam.tasks_service.kafka.DomainEventPublisher;
import com.kai_lam.tasks_service.kafka.KafkaTopicsProperties;
import com.kai_lam.tasks_service.model.TaskStatus;
import com.kai_lam.tasks_service.security.AuthPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TaskServiceFlowTests {

    @Autowired
    private TaskService taskService;

    @MockBean
    private DomainEventPublisher eventPublisher;

    @MockBean
    private KafkaTopicsProperties kafkaTopics;

    private AuthPrincipal principal;

    @BeforeEach
    void setup() {
        principal = new AuthPrincipal(UUID.randomUUID(), "team.lead", "ADMIN");
        when(kafkaTopics.taskEvents()).thenReturn("task.events");
    }

    @Test
    void tc008_createsTaskWithRequiredFields() {
        var created = taskService.createTask(principal, new TaskCreateRequest(
                UUID.randomUUID(),
                "Implement feature",
                "Details",
                3.5,
                null,
                LocalDate.now().plusDays(1),
                UUID.randomUUID()
        ));

        assertThat(created.idTask()).isNotNull();
        assertThat(created.nameTask()).isEqualTo("Implement feature");
        assertThat(created.status()).isEqualTo(TaskStatus.OPEN);
    }

    @Test
    void tc010_updatesStatusTodoInProgressDone() {
        var created = taskService.createTask(principal, new TaskCreateRequest(
                UUID.randomUUID(),
                "Status workflow",
                null,
                null,
                TaskStatus.OPEN,
                LocalDate.now().plusDays(2),
                null
        ));

        var inProgress = taskService.updateTask(principal, created.idTask(), new TaskUpdateRequest(
                null,
                null,
                null,
                TaskStatus.IN_PROGRESS,
                null,
                null
        ));
        var done = taskService.updateTask(principal, created.idTask(), new TaskUpdateRequest(
                null,
                null,
                null,
                TaskStatus.DONE,
                null,
                null
        ));

        assertThat(inProgress.status()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(done.status()).isEqualTo(TaskStatus.DONE);
    }

    @Test
    void tc020_publishesKafkaEventOnTaskCreate() {
        var created = taskService.createTask(principal, new TaskCreateRequest(
                UUID.randomUUID(),
                "Event task",
                "payload",
                null,
                TaskStatus.OPEN,
                null,
                null
        ));

        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(eventPublisher).publish(
                eq("task.events"),
                eq(created.idTask().toString()),
                eq("TASK_CREATED"),
                eq(principal.getAuthUserId()),
                payloadCaptor.capture()
        );

        Map<String, Object> payload = payloadCaptor.getValue();
        assertThat(payload.get("idTask")).isEqualTo(created.idTask().toString());
        assertThat(payload.get("nameTask")).isEqualTo("Event task");
    }

    @Test
    void tc024_allowsTaskWithPastDeadline() {
        LocalDate past = LocalDate.now().minusDays(5);

        var created = taskService.createTask(principal, new TaskCreateRequest(
                UUID.randomUUID(),
                "Past deadline",
                null,
                null,
                TaskStatus.OPEN,
                past,
                null
        ));

        assertThat(created.dueDate()).isEqualTo(past);
    }
}
