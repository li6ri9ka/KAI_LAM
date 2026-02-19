package com.kai_lam.user_service.dto;

import java.util.UUID;

public record UserTeamMembershipResponse(
        UUID idUserTeam,
        UUID userId,
        UUID teamId,
        String nameTeam
) {
}
