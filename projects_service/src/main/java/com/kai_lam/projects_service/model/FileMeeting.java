package com.kai_lam.projects_service.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "file_meeting")
public class FileMeeting {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_file_meeting", nullable = false, updatable = false)
    private UUID idFileMeeting;

    @Column(name = "file_meet_s3", nullable = false, length = 500)
    private String fileMeetS3;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_transcrib_id", nullable = false)
    private MeetingTranscrib meetingTranscrib;

    public UUID getIdFileMeeting() {
        return idFileMeeting;
    }

    public String getFileMeetS3() {
        return fileMeetS3;
    }

    public void setFileMeetS3(String fileMeetS3) {
        this.fileMeetS3 = fileMeetS3;
    }

    public MeetingTranscrib getMeetingTranscrib() {
        return meetingTranscrib;
    }

    public void setMeetingTranscrib(MeetingTranscrib meetingTranscrib) {
        this.meetingTranscrib = meetingTranscrib;
    }
}
