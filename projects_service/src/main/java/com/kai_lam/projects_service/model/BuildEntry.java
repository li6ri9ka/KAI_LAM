package com.kai_lam.projects_service.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "build")
public class BuildEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_build", nullable = false, updatable = false)
    private UUID idBuild;

    @Column(name = "name_build", nullable = false, length = 200)
    private String nameBuild;

    @Column(name = "release_version", length = 100)
    private String releaseVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "info_project_id", nullable = false)
    private InfoProject infoProject;

    public UUID getIdBuild() {
        return idBuild;
    }

    public String getNameBuild() {
        return nameBuild;
    }

    public void setNameBuild(String nameBuild) {
        this.nameBuild = nameBuild;
    }

    public String getReleaseVersion() {
        return releaseVersion;
    }

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    public InfoProject getInfoProject() {
        return infoProject;
    }

    public void setInfoProject(InfoProject infoProject) {
        this.infoProject = infoProject;
    }
}
