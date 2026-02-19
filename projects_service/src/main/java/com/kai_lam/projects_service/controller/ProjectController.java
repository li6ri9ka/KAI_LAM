package com.kai_lam.projects_service.controller;

import com.kai_lam.projects_service.dto.*;
import com.kai_lam.projects_service.security.AuthPrincipal;
import com.kai_lam.projects_service.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/projects")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InfoProjectResponse createProject(@AuthenticationPrincipal AuthPrincipal principal,
                                             @Valid @RequestBody InfoProjectRequest request) {
        return projectService.createProject(principal, request);
    }

    @GetMapping
    public List<InfoProjectResponse> listProjects(@AuthenticationPrincipal AuthPrincipal principal,
                                                  @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
                                                  @RequestParam(required = false) UUID teamId) {
        return projectService.listProjects(principal, authorizationHeader, teamId);
    }

    @GetMapping("/{projectId}")
    public InfoProjectResponse getProject(@PathVariable UUID projectId) {
        return projectService.getProject(projectId);
    }

    @PutMapping("/{projectId}")
    public InfoProjectResponse updateProject(@AuthenticationPrincipal AuthPrincipal principal,
                                             @PathVariable UUID projectId,
                                             @Valid @RequestBody InfoProjectRequest request) {
        return projectService.updateProject(principal, projectId, request);
    }

    @DeleteMapping("/{projectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(@AuthenticationPrincipal AuthPrincipal principal,
                              @PathVariable UUID projectId) {
        projectService.deleteProject(principal, projectId);
    }

    @PostMapping("/{projectId}/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserProjectResponse addUserProject(@AuthenticationPrincipal AuthPrincipal principal,
                                              @PathVariable UUID projectId,
                                              @Valid @RequestBody UserProjectRequest request) {
        return projectService.addUserProject(principal, projectId, request);
    }

    @GetMapping("/{projectId}/users")
    public List<UserProjectResponse> listUserProjects(@PathVariable UUID projectId) {
        return projectService.listUserProjects(projectId);
    }

    @DeleteMapping("/users/{userProjectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeUserProject(@AuthenticationPrincipal AuthPrincipal principal,
                                  @PathVariable UUID userProjectId) {
        projectService.removeUserProject(principal, userProjectId);
    }

    @PostMapping("/{projectId}/repositories")
    @ResponseStatus(HttpStatus.CREATED)
    public RepositoryResponse addRepository(@AuthenticationPrincipal AuthPrincipal principal,
                                            @PathVariable UUID projectId,
                                            @Valid @RequestBody RepositoryRequest request) {
        return projectService.addRepository(principal, projectId, request);
    }

    @GetMapping("/{projectId}/repositories")
    public List<RepositoryResponse> listRepositories(@PathVariable UUID projectId) {
        return projectService.listRepositories(projectId);
    }

    @PutMapping("/repositories/{repositoryId}")
    public RepositoryResponse updateRepository(@AuthenticationPrincipal AuthPrincipal principal,
                                               @PathVariable UUID repositoryId,
                                               @Valid @RequestBody RepositoryRequest request) {
        return projectService.updateRepository(principal, repositoryId, request);
    }

    @DeleteMapping("/repositories/{repositoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRepository(@AuthenticationPrincipal AuthPrincipal principal,
                                 @PathVariable UUID repositoryId) {
        projectService.deleteRepository(principal, repositoryId);
    }

    @PostMapping("/{projectId}/builds")
    @ResponseStatus(HttpStatus.CREATED)
    public BuildResponse addBuild(@AuthenticationPrincipal AuthPrincipal principal,
                                  @PathVariable UUID projectId,
                                  @Valid @RequestBody BuildRequest request) {
        return projectService.addBuild(principal, projectId, request);
    }

    @GetMapping("/{projectId}/builds")
    public List<BuildResponse> listBuilds(@PathVariable UUID projectId) {
        return projectService.listBuilds(projectId);
    }

    @PostMapping("/builds/{buildId}/files")
    @ResponseStatus(HttpStatus.CREATED)
    public FileBuildResponse addFileBuild(@AuthenticationPrincipal AuthPrincipal principal,
                                          @PathVariable UUID buildId,
                                          @Valid @RequestBody FileBuildRequest request) {
        return projectService.addFileBuild(principal, buildId, request);
    }

    @GetMapping("/builds/{buildId}/files")
    public List<FileBuildResponse> listFileBuilds(@PathVariable UUID buildId) {
        return projectService.listFileBuilds(buildId);
    }

    @PostMapping("/{projectId}/meetings/transcribs")
    @ResponseStatus(HttpStatus.CREATED)
    public MeetingTranscribResponse addMeetingTranscrib(@AuthenticationPrincipal AuthPrincipal principal,
                                                        @PathVariable UUID projectId,
                                                        @Valid @RequestBody MeetingTranscribRequest request) {
        return projectService.addMeetingTranscrib(principal, projectId, request);
    }

    @GetMapping("/{projectId}/meetings/transcribs")
    public List<MeetingTranscribResponse> listMeetingTranscribs(@PathVariable UUID projectId) {
        return projectService.listMeetingTranscribs(projectId);
    }

    @PostMapping("/meetings/transcribs/{meetingTranscribId}/files")
    @ResponseStatus(HttpStatus.CREATED)
    public FileMeetingResponse addFileMeeting(@AuthenticationPrincipal AuthPrincipal principal,
                                              @PathVariable UUID meetingTranscribId,
                                              @Valid @RequestBody FileMeetingRequest request) {
        return projectService.addFileMeeting(principal, meetingTranscribId, request);
    }

    @GetMapping("/meetings/transcribs/{meetingTranscribId}/files")
    public List<FileMeetingResponse> listFileMeetings(@PathVariable UUID meetingTranscribId) {
        return projectService.listFileMeetings(meetingTranscribId);
    }

    @PostMapping("/secret-keys")
    @ResponseStatus(HttpStatus.CREATED)
    public SecretKeyResponse createSecretKey(@AuthenticationPrincipal AuthPrincipal principal,
                                             @Valid @RequestBody SecretKeyRequest request) {
        return projectService.createSecretKey(principal, request);
    }

    @GetMapping("/secret-keys")
    public List<SecretKeyResponse> listSecretKeys() {
        return projectService.listSecretKeys();
    }

    @PostMapping("/{projectId}/secret-keys/{secretKeyId}")
    @ResponseStatus(HttpStatus.CREATED)
    public KeyProjectResponse linkSecretKey(@AuthenticationPrincipal AuthPrincipal principal,
                                            @PathVariable UUID projectId,
                                            @PathVariable UUID secretKeyId) {
        return projectService.linkSecretKeyToProject(principal, projectId, secretKeyId);
    }

    @GetMapping("/{projectId}/secret-keys")
    public List<KeyProjectResponse> listProjectKeys(@PathVariable UUID projectId) {
        return projectService.listProjectKeys(projectId);
    }

    @DeleteMapping("/secret-keys/link/{keyProjectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlinkProjectKey(@AuthenticationPrincipal AuthPrincipal principal,
                                 @PathVariable UUID keyProjectId) {
        projectService.unlinkProjectKey(principal, keyProjectId);
    }
}
