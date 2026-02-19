package com.kai_lam.user_service.dto;

import java.util.List;
import java.util.UUID;

public record UserResponse(
        UUID idUser,
        String loginUser,
        String nameUser,
        String midleNameUser,
        String emailUser,
        String phoneUser,
        String telegramUser,
        UUID nameSpecUserId,
        String nameSpecialty,
        List<TeamResponse> teams
) {
}
