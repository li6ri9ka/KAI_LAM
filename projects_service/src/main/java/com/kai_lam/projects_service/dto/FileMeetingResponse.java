package com.kai_lam.projects_service.dto;

import java.util.UUID;

public record FileMeetingResponse(
        UUID idFileMeeting,
        UUID meetingTranscribId,
        String fileMeetS3
) {
}
