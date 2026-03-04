package com.patientapp.health.data

data class User(
    val id: String,
    val email: String? = null,
    val phone: String? = null,
    val role: UserRole,
    val displayName: String? = null,
    val doctorId: String? = null,
    val fcmToken: String? = null
) {
    /** Primary identifier for display (email or phone). */
    val identifier: String get() = email ?: phone ?: ""
}
