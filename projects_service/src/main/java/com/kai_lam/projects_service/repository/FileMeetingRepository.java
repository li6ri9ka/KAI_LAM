package com.kai_lam.projects_service.repository;

import com.kai_lam.projects_service.model.FileMeeting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FileMeetingRepository extends JpaRepository<FileMeeting, UUID> {
    List<FileMeeting> findAllByMeetingTranscribIdMeetingTranscrib(UUID meetingTranscribId);
}
