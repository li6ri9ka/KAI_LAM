package com.kai_lam.auth_service.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class BadRequestException extends ApiException {
    public BadRequestException(String message, Map<String, String> details) {
        super(HttpStatus.BAD_REQUEST, message, details);
    }
}
