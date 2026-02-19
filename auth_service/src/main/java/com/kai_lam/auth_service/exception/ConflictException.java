package com.kai_lam.auth_service.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class ConflictException extends ApiException {
    public ConflictException(String message, Map<String, String> details) {
        super(HttpStatus.CONFLICT, message, details);
    }
}
