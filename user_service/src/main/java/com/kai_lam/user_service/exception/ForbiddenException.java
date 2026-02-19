package com.kai_lam.user_service.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class ForbiddenException extends ApiException {
    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, message, Map.of());
    }
}
