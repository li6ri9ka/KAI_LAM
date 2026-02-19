package com.kai_lam.projects_service.repository;

import com.kai_lam.projects_service.model.MeetingTranscrib;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MeetingTranscribRepository extends JpaRepository<MeetingTranscrib, UUID> {
    List<MeetingTranscrib> findAllByInfoProjectIdInfoProject(UUID projectId);
}
