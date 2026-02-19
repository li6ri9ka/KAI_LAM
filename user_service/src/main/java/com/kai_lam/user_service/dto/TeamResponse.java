package com.kai_lam.user_service.dto;

import java.util.UUID;

public record TeamResponse(
        UUID idTeam,
        String nameTeam
) {
}
