package com.kai_lam.dashboards_service.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public abstract class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final Map<String, String> details;

    protected ApiException(HttpStatus status, String message, Map<String, String> details) {
        super(message);
        this.status = status;
        this.details = details;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Map<String, String> getDetails() {
        return details;
    }
}
