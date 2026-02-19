package com.kai_lam.projects_service.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "repository")
public class RepositoryEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_repository", nullable = false, updatable = false)
    private UUID idRepository;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "info_project_id", nullable = false)
    private InfoProject infoProject;

    @Column(name = "provider", length = 100)
    private String provider;

    @Column(name = "repo_url", length = 500)
    private String repoUrl;

    @Column(name = "readme_md", columnDefinition = "TEXT")
    private String readmeMd;

    public UUID getIdRepository() {
        return idRepository;
    }

    public InfoProject getInfoProject() {
        return infoProject;
    }

    public void setInfoProject(InfoProject infoProject) {
        this.infoProject = infoProject;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public String getReadmeMd() {
        return readmeMd;
    }

    public void setReadmeMd(String readmeMd) {
        this.readmeMd = readmeMd;
    }
}
