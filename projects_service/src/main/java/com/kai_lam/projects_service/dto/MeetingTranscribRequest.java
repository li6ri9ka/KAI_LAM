package com.kai_lam.projects_service.dto;

import jakarta.validation.constraints.Size;

public record MeetingTranscribRequest(
        @Size(max = 500, message = "fileTranscribMeetS3 length must be <= 500")
        String fileTranscribMeetS3,

        @Size(max = 1000, message = "shortDescriptionMeet length must be <= 1000")
        String shortDescriptionMeet
) {
}
