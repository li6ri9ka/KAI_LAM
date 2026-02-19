package com.kai_lam.projects_service.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "file_build")
public class FileBuild {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_file_build", nullable = false, updatable = false)
    private UUID idFileBuild;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "build_id", nullable = false)
    private BuildEntry build;

    @Column(name = "link_file_build_s3", nullable = false, length = 500)
    private String linkFileBuildS3;

    public UUID getIdFileBuild() {
        return idFileBuild;
    }

    public BuildEntry getBuild() {
        return build;
    }

    public void setBuild(BuildEntry build) {
        this.build = build;
    }

    public String getLinkFileBuildS3() {
        return linkFileBuildS3;
    }

    public void setLinkFileBuildS3(String linkFileBuildS3) {
        this.linkFileBuildS3 = linkFileBuildS3;
    }
}
