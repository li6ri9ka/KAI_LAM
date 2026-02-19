package com.kai_lam.user_service.dto;

import java.util.UUID;

public record NameSpecialtyResponse(
        UUID idNameSpecialty,
        String nameSpecialty
) {
}
