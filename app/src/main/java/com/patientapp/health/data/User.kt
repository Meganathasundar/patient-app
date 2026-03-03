package com.patientapp.health.data

data class User(
    val id: String,
    val email: String,
    val role: UserRole,
    val displayName: String? = null,
    val doctorId: String? = null,
    val fcmToken: String? = null
)
