package com.kai_lam.projects_service.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class BadRequestException extends ApiException {
    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message, Map.of());
    }
}
