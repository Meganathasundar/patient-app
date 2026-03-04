package com.patientapp.health.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val firestore = FirebaseFirestore.getInstance()

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
    val phone = getString(FirestoreConstants.PHONE) ?: return null
    val role = UserRole.fromString(getString(FirestoreConstants.ROLE)) ?: return null
    return User(
        id = id,
        phone = phone,
        role = role,
        displayName = getString(FirestoreConstants.DISPLAY_NAME),
        doctorId = getString(FirestoreConstants.DOCTOR_ID),
        fcmToken = getString(FirestoreConstants.FCM_TOKEN)
    )
}
