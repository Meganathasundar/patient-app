package com.patientapp.health.data

enum class UserRole {
    DOCTOR,
    PATIENT;

    companion object {
        fun fromString(value: String?): UserRole? = when (value?.uppercase()) {
            "DOCTOR" -> DOCTOR
            "PATIENT" -> PATIENT
            else -> null
        }
    }
}
