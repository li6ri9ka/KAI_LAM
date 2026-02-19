package com.kai_lam.user_service.model;

public enum GlobalRole {
    USER,
    ADMIN;

    public static GlobalRole from(String value) {
        if (value == null) {
            return USER;
        }
        return GlobalRole.valueOf(value.trim().toUpperCase());
    }
}
