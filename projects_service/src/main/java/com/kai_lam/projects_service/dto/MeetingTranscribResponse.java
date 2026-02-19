package com.kai_lam.projects_service.dto;

import java.util.UUID;

public record MeetingTranscribResponse(
        UUID idMeetingTranscrib,
        String fileTranscribMeetS3,
        String shortDescriptionMeet,
        UUID infoProjectId
) {
}
