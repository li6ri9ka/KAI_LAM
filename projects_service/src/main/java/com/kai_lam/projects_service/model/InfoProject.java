package com.kai_lam.projects_service.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "info_project")
public class InfoProject {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_info_project", nullable = false, updatable = false)
    private UUID idInfoProject;

    @Column(name = "team_id", nullable = false)
    private UUID teamId;

    @Column(name = "name_project", nullable = false, length = 200)
    private String nameProject;

    @Column(name = "project_description", length = 2000)
    private String projectDescription;

    @Column(name = "github_link_project", length = 500)
    private String githubLinkProject;

    public UUID getIdInfoProject() {
        return idInfoProject;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public void setTeamId(UUID teamId) {
        this.teamId = teamId;
    }

    public String getNameProject() {
        return nameProject;
    }

    public void setNameProject(String nameProject) {
        this.nameProject = nameProject;
    }

    public String getProjectDescription() {
        return projectDescription;
    }

    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }

    public String getGithubLinkProject() {
        return githubLinkProject;
    }

    public void setGithubLinkProject(String githubLinkProject) {
        this.githubLinkProject = githubLinkProject;
    }
}
