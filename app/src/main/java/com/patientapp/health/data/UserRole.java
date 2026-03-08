package com.patientapp.health.data;

public enum UserRole {
    DOCTOR,
    PATIENT;

    public static UserRole fromString(String value) {
        if (value == null) return null;
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
