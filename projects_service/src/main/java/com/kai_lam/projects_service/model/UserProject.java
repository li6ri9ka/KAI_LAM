package com.kai_lam.projects_service.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "user_project", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_project", columnNames = {"info_project_id", "user_team_id"})
})
public class UserProject {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_user_project", nullable = false, updatable = false)
    private UUID idUserProject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "info_project_id", nullable = false)
    private InfoProject infoProject;

    @Column(name = "user_team_id", nullable = false)
    private UUID userTeamId;

    @Column(name = "project_role", nullable = false, length = 100)
    private String projectRole;

    public UUID getIdUserProject() {
        return idUserProject;
    }

    public InfoProject getInfoProject() {
        return infoProject;
    }

    public void setInfoProject(InfoProject infoProject) {
        this.infoProject = infoProject;
    }

    public UUID getUserTeamId() {
        return userTeamId;
    }

    public void setUserTeamId(UUID userTeamId) {
        this.userTeamId = userTeamId;
    }

    public String getProjectRole() {
        return projectRole;
    }

    public void setProjectRole(String projectRole) {
        this.projectRole = projectRole;
    }
}
