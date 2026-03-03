package com.patientapp.health.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Firestore repository for user/patient data.
 * - Add patient (pending invite) for a doctor.
 * - List patients by doctor (real-time).
 * - Update FCM token for a user.
 */
class UserRepository {

    private val firestore = FirebaseFirestore.getInstance()

    /** Doctor adds a patient by email. Patient must then sign up in the app with this email to claim the account. */
    suspend fun addPatient(doctorId: String, email: String, displayName: String): Result<Unit> {
        return try {
            val docId = email.replace(".", "_")
            val data = hashMapOf(
                FirestoreConstants.EMAIL to email,
                FirestoreConstants.DISPLAY_NAME to displayName,
                FirestoreConstants.DOCTOR_ID to doctorId
            )
            firestore.collection(FirestoreConstants.PENDING_PATIENTS).document(docId).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getPatientsByDoctor(doctorId: String): Flow<List<User>> = callbackFlow {
        val listener = firestore.collection(FirestoreConstants.USERS)
            .whereEqualTo(FirestoreConstants.ROLE, UserRole.PATIENT.name)
            .whereEqualTo(FirestoreConstants.DOCTOR_ID, doctorId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val users = snapshot?.documents?.mapNotNull { it.toUser() } ?: emptyList()
                trySend(users)
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateFcmToken(userId: String, token: String) {
        firestore.collection(FirestoreConstants.USERS).document(userId)
            .update(FirestoreConstants.FCM_TOKEN, token).await()
    }
}

private fun com.google.firebase.firestore.DocumentSnapshot.toUser(): User? {
    val id = id
    val email = getString(FirestoreConstants.EMAIL) ?: return null
    val role = UserRole.fromString(getString(FirestoreConstants.ROLE)) ?: return null
    return User(
        id = id,
        email = email,
        role = role,
        displayName = getString(FirestoreConstants.DISPLAY_NAME),
        doctorId = getString(FirestoreConstants.DOCTOR_ID),
        fcmToken = getString(FirestoreConstants.FCM_TOKEN)
    )
}
