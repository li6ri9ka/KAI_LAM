package com.kai_lam.projects_service.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "key_project", uniqueConstraints = {
        @UniqueConstraint(name = "uk_key_project", columnNames = {"secret_key_id", "info_project_id"})
})
public class KeyProject {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_key_project", nullable = false, updatable = false)
    private UUID idKeyProject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "secret_key_id", nullable = false)
    private SecretKeyEntry secretKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "info_project_id", nullable = false)
    private InfoProject infoProject;

    public UUID getIdKeyProject() {
        return idKeyProject;
    }

    public SecretKeyEntry getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(SecretKeyEntry secretKey) {
        this.secretKey = secretKey;
    }

    public InfoProject getInfoProject() {
        return infoProject;
    }

    public void setInfoProject(InfoProject infoProject) {
        this.infoProject = infoProject;
    }
}
