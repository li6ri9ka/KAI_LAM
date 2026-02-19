package com.kai_lam.searchandfiltered_service.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "search_documents")
public class SearchDocument {
    @Id
    @Column(name = "doc_id", nullable = false, updatable = false)
    private UUID docId;

    @Column(name = "doc_type", nullable = false, length = 50)
    private String docType;

    @Column(name = "team_id")
    private UUID teamId;

    @Column(name = "project_id")
    private UUID projectId;

    @Column(name = "title", columnDefinition = "TEXT")
    private String title;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UUID getDocId() {
        return docId;
    }

    public void setDocId(UUID docId) {
        this.docId = docId;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public void setTeamId(UUID teamId) {
        this.teamId = teamId;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
