package com.kai_lam.searchandfiltered_service.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class NotFoundException extends ApiException {
    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message, Map.of());
    }
}
