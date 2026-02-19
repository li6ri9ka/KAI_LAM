package com.kai_lam.tasks_service.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class ConflictException extends ApiException {
    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message, Map.of());
    }
}
