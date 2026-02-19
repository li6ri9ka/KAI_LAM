package com.kai_lam.projects_service.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "meeting_transcrib")
public class MeetingTranscrib {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_meeting_transcrib", nullable = false, updatable = false)
    private UUID idMeetingTranscrib;

    @Column(name = "file_transcrib_meet_s3", length = 500)
    private String fileTranscribMeetS3;

    @Column(name = "short_description_meet", length = 1000)
    private String shortDescriptionMeet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "info_project_id", nullable = false)
    private InfoProject infoProject;

    public UUID getIdMeetingTranscrib() {
        return idMeetingTranscrib;
    }

    public String getFileTranscribMeetS3() {
        return fileTranscribMeetS3;
    }

    public void setFileTranscribMeetS3(String fileTranscribMeetS3) {
        this.fileTranscribMeetS3 = fileTranscribMeetS3;
    }

    public String getShortDescriptionMeet() {
        return shortDescriptionMeet;
    }

    public void setShortDescriptionMeet(String shortDescriptionMeet) {
        this.shortDescriptionMeet = shortDescriptionMeet;
    }

    public InfoProject getInfoProject() {
        return infoProject;
    }

    public void setInfoProject(InfoProject infoProject) {
        this.infoProject = infoProject;
    }
}
