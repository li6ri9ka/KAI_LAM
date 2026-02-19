package com.kai_lam.projects_service.service;

import com.kai_lam.projects_service.client.UserDirectoryClient;
import com.kai_lam.projects_service.dto.*;
import com.kai_lam.projects_service.exception.ConflictException;
import com.kai_lam.projects_service.exception.NotFoundException;
import com.kai_lam.projects_service.kafka.DomainEventPublisher;
import com.kai_lam.projects_service.kafka.KafkaTopicsProperties;
import com.kai_lam.projects_service.model.*;
import com.kai_lam.projects_service.repository.*;
import com.kai_lam.projects_service.security.AuthPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class ProjectService {
    private final InfoProjectRepository infoProjectRepository;
    private final UserProjectRepository userProjectRepository;
    private final RepositoryEntryRepository repositoryEntryRepository;
    private final BuildEntryRepository buildEntryRepository;
    private final FileBuildRepository fileBuildRepository;
    private final MeetingTranscribRepository meetingTranscribRepository;
    private final FileMeetingRepository fileMeetingRepository;
    private final SecretKeyEntryRepository secretKeyEntryRepository;
    private final KeyProjectRepository keyProjectRepository;
    private final DomainEventPublisher eventPublisher;
    private final KafkaTopicsProperties kafkaTopics;
    private final UserDirectoryClient userDirectoryClient;

    public ProjectService(InfoProjectRepository infoProjectRepository,
                          UserProjectRepository userProjectRepository,
                          RepositoryEntryRepository repositoryEntryRepository,
                          BuildEntryRepository buildEntryRepository,
                          FileBuildRepository fileBuildRepository,
                          MeetingTranscribRepository meetingTranscribRepository,
                          FileMeetingRepository fileMeetingRepository,
                          SecretKeyEntryRepository secretKeyEntryRepository,
                          KeyProjectRepository keyProjectRepository,
                          DomainEventPublisher eventPublisher,
                          KafkaTopicsProperties kafkaTopics,
                          UserDirectoryClient userDirectoryClient) {
        this.infoProjectRepository = infoProjectRepository;
        this.userProjectRepository = userProjectRepository;
        this.repositoryEntryRepository = repositoryEntryRepository;
        this.buildEntryRepository = buildEntryRepository;
        this.fileBuildRepository = fileBuildRepository;
        this.meetingTranscribRepository = meetingTranscribRepository;
        this.fileMeetingRepository = fileMeetingRepository;
        this.secretKeyEntryRepository = secretKeyEntryRepository;
        this.keyProjectRepository = keyProjectRepository;
        this.eventPublisher = eventPublisher;
        this.kafkaTopics = kafkaTopics;
        this.userDirectoryClient = userDirectoryClient;
    }

    @Transactional
    public InfoProjectResponse createProject(AuthPrincipal principal, InfoProjectRequest request) {
        InfoProject project = new InfoProject();
        project.setTeamId(request.teamId());
        project.setNameProject(request.nameProject().trim());
        project.setProjectDescription(request.projectDescription());
        project.setGithubLinkProject(request.githubLinkProject());

        InfoProject saved = infoProjectRepository.save(project);
        publishEvent("PROJECT_CREATED", principal.getAuthUserId(), saved.getIdInfoProject(), Map.of("teamId", saved.getTeamId().toString()));
        return toInfoProjectResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<InfoProjectResponse> listProjects(AuthPrincipal principal, String authorizationHeader, UUID teamId) {
        List<InfoProject> projects;
        if (isAdmin(principal)) {
            projects = teamId == null ? infoProjectRepository.findAll() : infoProjectRepository.findAllByTeamId(teamId);
        } else {
            List<UUID> userTeamIds = userDirectoryClient.getUserTeamIds(principal.getAuthUserId(), authorizationHeader);
            if (userTeamIds.isEmpty()) {
                return List.of();
            }

            Set<UUID> projectIds = userProjectRepository.findAllByUserTeamIdIn(userTeamIds)
                    .stream()
                    .map(userProject -> userProject.getInfoProject().getIdInfoProject())
                    .collect(LinkedHashSet::new, Set::add, Set::addAll);

            if (projectIds.isEmpty()) {
                return List.of();
            }

            projects = infoProjectRepository.findAllById(projectIds);
            if (teamId != null) {
                projects = projects.stream()
                        .filter(project -> teamId.equals(project.getTeamId()))
                        .toList();
            }
        }

        return projects.stream().map(this::toInfoProjectResponse).toList();
    }

    @Transactional(readOnly = true)
    public InfoProjectResponse getProject(UUID projectId) {
        return toInfoProjectResponse(findProject(projectId));
    }

    @Transactional
    public InfoProjectResponse updateProject(AuthPrincipal principal, UUID projectId, InfoProjectRequest request) {
        InfoProject project = findProject(projectId);
        project.setTeamId(request.teamId());
        project.setNameProject(request.nameProject().trim());
        project.setProjectDescription(request.projectDescription());
        project.setGithubLinkProject(request.githubLinkProject());

        InfoProject saved = infoProjectRepository.save(project);
        publishEvent("PROJECT_UPDATED", principal.getAuthUserId(), saved.getIdInfoProject(), Map.of());
        return toInfoProjectResponse(saved);
    }

    @Transactional
    public void deleteProject(AuthPrincipal principal, UUID projectId) {
        InfoProject project = findProject(projectId);
        infoProjectRepository.delete(project);
        publishEvent("PROJECT_DELETED", principal.getAuthUserId(), projectId, Map.of());
    }

    @Transactional
    public UserProjectResponse addUserProject(AuthPrincipal principal, UUID projectId, UserProjectRequest request) {
        InfoProject project = findProject(projectId);
        if (userProjectRepository.findByInfoProjectIdInfoProjectAndUserTeamId(projectId, request.userTeamId()).isPresent()) {
            throw new ConflictException("User team is already linked to this project");
        }

        UserProject userProject = new UserProject();
        userProject.setInfoProject(project);
        userProject.setUserTeamId(request.userTeamId());
        userProject.setProjectRole(request.projectRole().trim());

        UserProject saved = userProjectRepository.save(userProject);
        publishEvent("PROJECT_MEMBER_ADDED", principal.getAuthUserId(), projectId, Map.of("userTeamId", saved.getUserTeamId().toString()));
        return toUserProjectResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<UserProjectResponse> listUserProjects(UUID projectId) {
        return userProjectRepository.findAllByInfoProjectIdInfoProject(projectId)
                .stream().map(this::toUserProjectResponse).toList();
    }

    @Transactional
    public void removeUserProject(AuthPrincipal principal, UUID userProjectId) {
        UserProject userProject = userProjectRepository.findById(userProjectId)
                .orElseThrow(() -> new NotFoundException("User project link not found"));
        UUID projectId = userProject.getInfoProject().getIdInfoProject();
        userProjectRepository.delete(userProject);
        publishEvent("PROJECT_MEMBER_REMOVED", principal.getAuthUserId(), projectId, Map.of("idUserProject", userProjectId.toString()));
    }

    @Transactional
    public RepositoryResponse addRepository(AuthPrincipal principal, UUID projectId, RepositoryRequest request) {
        InfoProject project = findProject(projectId);
        RepositoryEntry entry = new RepositoryEntry();
        entry.setInfoProject(project);
        entry.setProvider(request.provider());
        entry.setRepoUrl(request.repoUrl());
        entry.setReadmeMd(request.readmeMd());

        RepositoryEntry saved = repositoryEntryRepository.save(entry);
        publishEvent("PROJECT_REPOSITORY_CREATED", principal.getAuthUserId(), projectId, Map.of("idRepository", saved.getIdRepository().toString()));
        return toRepositoryResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<RepositoryResponse> listRepositories(UUID projectId) {
        return repositoryEntryRepository.findAllByInfoProjectIdInfoProject(projectId)
                .stream().map(this::toRepositoryResponse).toList();
    }

    @Transactional
    public RepositoryResponse updateRepository(AuthPrincipal principal, UUID repositoryId, RepositoryRequest request) {
        RepositoryEntry entry = repositoryEntryRepository.findById(repositoryId)
                .orElseThrow(() -> new NotFoundException("Repository entry not found"));
        entry.setProvider(request.provider());
        entry.setRepoUrl(request.repoUrl());
        entry.setReadmeMd(request.readmeMd());

        RepositoryEntry saved = repositoryEntryRepository.save(entry);
        publishEvent("PROJECT_REPOSITORY_UPDATED", principal.getAuthUserId(), saved.getInfoProject().getIdInfoProject(), Map.of("idRepository", saved.getIdRepository().toString()));
        return toRepositoryResponse(saved);
    }

    @Transactional
    public void deleteRepository(AuthPrincipal principal, UUID repositoryId) {
        RepositoryEntry entry = repositoryEntryRepository.findById(repositoryId)
                .orElseThrow(() -> new NotFoundException("Repository entry not found"));
        UUID projectId = entry.getInfoProject().getIdInfoProject();
        repositoryEntryRepository.delete(entry);
        publishEvent("PROJECT_REPOSITORY_DELETED", principal.getAuthUserId(), projectId, Map.of("idRepository", repositoryId.toString()));
    }

    @Transactional
    public BuildResponse addBuild(AuthPrincipal principal, UUID projectId, BuildRequest request) {
        InfoProject project = findProject(projectId);

        BuildEntry build = new BuildEntry();
        build.setInfoProject(project);
        build.setNameBuild(request.nameBuild().trim());
        build.setReleaseVersion(request.releaseVersion());

        BuildEntry saved = buildEntryRepository.save(build);
        publishEvent("PROJECT_BUILD_CREATED", principal.getAuthUserId(), projectId, Map.of("idBuild", saved.getIdBuild().toString()));
        return toBuildResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<BuildResponse> listBuilds(UUID projectId) {
        return buildEntryRepository.findAllByInfoProjectIdInfoProject(projectId)
                .stream().map(this::toBuildResponse).toList();
    }

    @Transactional
    public FileBuildResponse addFileBuild(AuthPrincipal principal, UUID buildId, FileBuildRequest request) {
        BuildEntry build = buildEntryRepository.findById(buildId)
                .orElseThrow(() -> new NotFoundException("Build not found"));

        FileBuild fileBuild = new FileBuild();
        fileBuild.setBuild(build);
        fileBuild.setLinkFileBuildS3(request.linkFileBuildS3().trim());

        FileBuild saved = fileBuildRepository.save(fileBuild);
        publishEvent("PROJECT_BUILD_FILE_CREATED", principal.getAuthUserId(), build.getInfoProject().getIdInfoProject(), Map.of("idFileBuild", saved.getIdFileBuild().toString()));
        return toFileBuildResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<FileBuildResponse> listFileBuilds(UUID buildId) {
        return fileBuildRepository.findAllByBuildIdBuild(buildId).stream().map(this::toFileBuildResponse).toList();
    }

    @Transactional
    public MeetingTranscribResponse addMeetingTranscrib(AuthPrincipal principal, UUID projectId, MeetingTranscribRequest request) {
        InfoProject project = findProject(projectId);

        MeetingTranscrib meeting = new MeetingTranscrib();
        meeting.setInfoProject(project);
        meeting.setFileTranscribMeetS3(request.fileTranscribMeetS3());
        meeting.setShortDescriptionMeet(request.shortDescriptionMeet());

        MeetingTranscrib saved = meetingTranscribRepository.save(meeting);
        publishEvent("PROJECT_MEETING_TRANSCRIB_CREATED", principal.getAuthUserId(), projectId, Map.of("idMeetingTranscrib", saved.getIdMeetingTranscrib().toString()));
        return toMeetingTranscribResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<MeetingTranscribResponse> listMeetingTranscribs(UUID projectId) {
        return meetingTranscribRepository.findAllByInfoProjectIdInfoProject(projectId)
                .stream().map(this::toMeetingTranscribResponse).toList();
    }

    @Transactional
    public FileMeetingResponse addFileMeeting(AuthPrincipal principal, UUID meetingTranscribId, FileMeetingRequest request) {
        MeetingTranscrib meeting = meetingTranscribRepository.findById(meetingTranscribId)
                .orElseThrow(() -> new NotFoundException("Meeting transcrib not found"));

        FileMeeting fileMeeting = new FileMeeting();
        fileMeeting.setMeetingTranscrib(meeting);
        fileMeeting.setFileMeetS3(request.fileMeetS3().trim());

        FileMeeting saved = fileMeetingRepository.save(fileMeeting);
        publishEvent("PROJECT_MEETING_FILE_CREATED", principal.getAuthUserId(), meeting.getInfoProject().getIdInfoProject(), Map.of("idFileMeeting", saved.getIdFileMeeting().toString()));
        return toFileMeetingResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<FileMeetingResponse> listFileMeetings(UUID meetingTranscribId) {
        return fileMeetingRepository.findAllByMeetingTranscribIdMeetingTranscrib(meetingTranscribId)
                .stream().map(this::toFileMeetingResponse).toList();
    }

    @Transactional
    public SecretKeyResponse createSecretKey(AuthPrincipal principal, SecretKeyRequest request) {
        SecretKeyEntry entry = new SecretKeyEntry();
        entry.setNameSecretKey(request.nameSecretKey().trim());
        entry.setEncryptedValue(request.encryptedValue());

        SecretKeyEntry saved = secretKeyEntryRepository.save(entry);
        publishEvent("PROJECT_SECRET_KEY_CREATED", principal.getAuthUserId(), saved.getIdSecretKey(), Map.of());
        return toSecretKeyResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<SecretKeyResponse> listSecretKeys() {
        return secretKeyEntryRepository.findAll().stream().map(this::toSecretKeyResponse).toList();
    }

    @Transactional
    public KeyProjectResponse linkSecretKeyToProject(AuthPrincipal principal, UUID projectId, UUID secretKeyId) {
        InfoProject project = findProject(projectId);
        SecretKeyEntry secretKey = secretKeyEntryRepository.findById(secretKeyId)
                .orElseThrow(() -> new NotFoundException("Secret key not found"));

        if (keyProjectRepository.findByInfoProjectIdInfoProjectAndSecretKeyIdSecretKey(projectId, secretKeyId).isPresent()) {
            throw new ConflictException("Secret key already linked to this project");
        }

        KeyProject keyProject = new KeyProject();
        keyProject.setInfoProject(project);
        keyProject.setSecretKey(secretKey);

        KeyProject saved = keyProjectRepository.save(keyProject);
        publishEvent("PROJECT_SECRET_KEY_LINKED", principal.getAuthUserId(), projectId, Map.of("idKeyProject", saved.getIdKeyProject().toString()));
        return toKeyProjectResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<KeyProjectResponse> listProjectKeys(UUID projectId) {
        return keyProjectRepository.findAllByInfoProjectIdInfoProject(projectId)
                .stream().map(this::toKeyProjectResponse).toList();
    }

    @Transactional
    public void unlinkProjectKey(AuthPrincipal principal, UUID keyProjectId) {
        KeyProject keyProject = keyProjectRepository.findById(keyProjectId)
                .orElseThrow(() -> new NotFoundException("Project key link not found"));
        UUID projectId = keyProject.getInfoProject().getIdInfoProject();
        keyProjectRepository.delete(keyProject);
        publishEvent("PROJECT_SECRET_KEY_UNLINKED", principal.getAuthUserId(), projectId, Map.of("idKeyProject", keyProjectId.toString()));
    }

    private InfoProject findProject(UUID projectId) {
        return infoProjectRepository.findById(projectId).orElseThrow(() -> new NotFoundException("Project not found"));
    }

    private boolean isAdmin(AuthPrincipal principal) {
        if (principal == null || principal.getRole() == null) {
            return false;
        }
        return "ADMIN".equalsIgnoreCase(principal.getRole().trim());
    }

    private void publishEvent(String eventType, UUID actorId, UUID scopeId, Map<String, Object> extra) {
        Map<String, Object> payload = new HashMap<>(extra);
        payload.put("scopeId", scopeId.toString());

        eventPublisher.publish(
                kafkaTopics.projectEvents(),
                scopeId.toString(),
                eventType,
                actorId,
                payload
        );
    }

    private InfoProjectResponse toInfoProjectResponse(InfoProject project) {
        return new InfoProjectResponse(
                project.getIdInfoProject(),
                project.getTeamId(),
                project.getNameProject(),
                project.getProjectDescription(),
                project.getGithubLinkProject()
        );
    }

    private UserProjectResponse toUserProjectResponse(UserProject userProject) {
        return new UserProjectResponse(
                userProject.getIdUserProject(),
                userProject.getInfoProject().getIdInfoProject(),
                userProject.getUserTeamId(),
                userProject.getProjectRole()
        );
    }

    private RepositoryResponse toRepositoryResponse(RepositoryEntry entry) {
        return new RepositoryResponse(
                entry.getIdRepository(),
                entry.getInfoProject().getIdInfoProject(),
                entry.getProvider(),
                entry.getRepoUrl(),
                entry.getReadmeMd()
        );
    }

    private BuildResponse toBuildResponse(BuildEntry build) {
        return new BuildResponse(
                build.getIdBuild(),
                build.getNameBuild(),
                build.getReleaseVersion(),
                build.getInfoProject().getIdInfoProject()
        );
    }

    private FileBuildResponse toFileBuildResponse(FileBuild fileBuild) {
        return new FileBuildResponse(
                fileBuild.getIdFileBuild(),
                fileBuild.getBuild().getIdBuild(),
                fileBuild.getLinkFileBuildS3()
        );
    }

    private MeetingTranscribResponse toMeetingTranscribResponse(MeetingTranscrib meeting) {
        return new MeetingTranscribResponse(
                meeting.getIdMeetingTranscrib(),
                meeting.getFileTranscribMeetS3(),
                meeting.getShortDescriptionMeet(),
                meeting.getInfoProject().getIdInfoProject()
        );
    }

    private FileMeetingResponse toFileMeetingResponse(FileMeeting fileMeeting) {
        return new FileMeetingResponse(
                fileMeeting.getIdFileMeeting(),
                fileMeeting.getMeetingTranscrib().getIdMeetingTranscrib(),
                fileMeeting.getFileMeetS3()
        );
    }

    private SecretKeyResponse toSecretKeyResponse(SecretKeyEntry secretKeyEntry) {
        return new SecretKeyResponse(
                secretKeyEntry.getIdSecretKey(),
                secretKeyEntry.getNameSecretKey(),
                secretKeyEntry.getEncryptedValue()
        );
    }

    private KeyProjectResponse toKeyProjectResponse(KeyProject keyProject) {
        return new KeyProjectResponse(
                keyProject.getIdKeyProject(),
                keyProject.getInfoProject().getIdInfoProject(),
                keyProject.getSecretKey().getIdSecretKey(),
                keyProject.getSecretKey().getNameSecretKey()
        );
    }
}
