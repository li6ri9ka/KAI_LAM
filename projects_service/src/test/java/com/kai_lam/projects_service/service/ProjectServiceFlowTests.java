package com.kai_lam.projects_service.service;

import com.kai_lam.projects_service.client.UserDirectoryClient;
import com.kai_lam.projects_service.dto.*;
import com.kai_lam.projects_service.repository.*;
import com.kai_lam.projects_service.security.AuthPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProjectServiceFlowTests {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private InfoProjectRepository infoProjectRepository;

    @Autowired
    private UserProjectRepository userProjectRepository;

    @Autowired
    private RepositoryEntryRepository repositoryEntryRepository;

    @Autowired
    private BuildEntryRepository buildEntryRepository;

    @Autowired
    private FileBuildRepository fileBuildRepository;

    @Autowired
    private MeetingTranscribRepository meetingTranscribRepository;

    @Autowired
    private FileMeetingRepository fileMeetingRepository;

    @Autowired
    private SecretKeyEntryRepository secretKeyEntryRepository;

    @Autowired
    private KeyProjectRepository keyProjectRepository;

    @MockBean
    private UserDirectoryClient userDirectoryClient;

    private AuthPrincipal principal;

    @BeforeEach
    void setup() {
        principal = new AuthPrincipal(UUID.randomUUID(), "admin", "ADMIN");
    }

    @Test
    void tc013_teamLeadAddsMemberToProject() {
        InfoProjectResponse project = createProject();

        UserProjectResponse userProject = projectService.addUserProject(principal, project.idInfoProject(),
                new UserProjectRequest(UUID.randomUUID(), "TEAM_LEAD"));

        assertThat(userProject.idUserProject()).isNotNull();
        assertThat(projectService.listUserProjects(project.idInfoProject())).hasSize(1);
    }

    @Test
    void tc014_secretKeysAreStoredAndLinkedToProject() {
        InfoProjectResponse project = createProject();

        SecretKeyResponse key = projectService.createSecretKey(principal,
                new SecretKeyRequest("S3_ACCESS_KEY", "enc:v1:ABCDEF"));
        projectService.linkSecretKeyToProject(principal, project.idInfoProject(), key.idSecretKey());

        assertThat(secretKeyEntryRepository.findById(key.idSecretKey())).isPresent();
        assertThat(secretKeyEntryRepository.findById(key.idSecretKey()).orElseThrow().getEncryptedValue())
                .isEqualTo("enc:v1:ABCDEF");
        assertThat(projectService.listProjectKeys(project.idInfoProject())).hasSize(1);
    }

    @Test
    void tc015_deletingProjectCascadesRelatedData() {
        InfoProjectResponse project = createProject();
        UUID projectId = project.idInfoProject();

        UserProjectResponse userProject = projectService.addUserProject(principal, projectId,
                new UserProjectRequest(UUID.randomUUID(), "USER"));
        RepositoryResponse repo = projectService.addRepository(principal, projectId,
                new RepositoryRequest("github", "https://example/repo", "# readme"));
        BuildResponse build = projectService.addBuild(principal, projectId,
                new BuildRequest("v1 build", "1.0.0"));
        FileBuildResponse buildFile = projectService.addFileBuild(principal, build.idBuild(),
                new FileBuildRequest("s3://build/file.zip"));
        MeetingTranscribResponse meeting = projectService.addMeetingTranscrib(principal, projectId,
                new MeetingTranscribRequest("s3://meeting/transcrib.txt", "daily sync"));
        FileMeetingResponse meetingFile = projectService.addFileMeeting(principal, meeting.idMeetingTranscrib(),
                new FileMeetingRequest("s3://meeting/file.mp3"));
        SecretKeyResponse key = projectService.createSecretKey(principal,
                new SecretKeyRequest("TOKEN", "encrypted-token"));
        KeyProjectResponse keyProject = projectService.linkSecretKeyToProject(principal, projectId, key.idSecretKey());

        projectService.deleteProject(principal, projectId);

        assertThat(infoProjectRepository.findById(projectId)).isEmpty();
        assertThat(userProjectRepository.findById(userProject.idUserProject())).isEmpty();
        assertThat(repositoryEntryRepository.findById(repo.idRepository())).isEmpty();
        assertThat(buildEntryRepository.findById(build.idBuild())).isEmpty();
        assertThat(fileBuildRepository.findById(buildFile.idFileBuild())).isEmpty();
        assertThat(meetingTranscribRepository.findById(meeting.idMeetingTranscrib())).isEmpty();
        assertThat(fileMeetingRepository.findById(meetingFile.idFileMeeting())).isEmpty();
        assertThat(keyProjectRepository.findById(keyProject.idKeyProject())).isEmpty();
        assertThat(secretKeyEntryRepository.findById(key.idSecretKey())).isPresent();
    }

    @Test
    void tc023_teamLeadCanRemoveSelfFromProject() {
        InfoProjectResponse project = createProject();

        UserProjectResponse member = projectService.addUserProject(principal, project.idInfoProject(),
                new UserProjectRequest(UUID.randomUUID(), "TEAM_LEAD"));
        projectService.removeUserProject(principal, member.idUserProject());

        assertThat(projectService.listUserProjects(project.idInfoProject())).isEmpty();
    }

    private InfoProjectResponse createProject() {
        return projectService.createProject(principal, new InfoProjectRequest(
                UUID.randomUUID(),
                "Core Platform",
                "Project description",
                "https://github.com/test/repo"
        ));
    }
}
