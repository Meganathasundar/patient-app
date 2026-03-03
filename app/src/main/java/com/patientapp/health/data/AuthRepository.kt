package com.patientapp.health.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository for Firebase Auth and user profile in Firestore.
 * - Auth: sign in, sign up, sign out, auth state.
 * - Firestore: read/write user profile in "users" and "pending_patients" for sign-up flow.
 */
class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val currentUserId: String?
        get() = auth.currentUser?.uid

    fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signUp(email: String, password: String, role: UserRole, displayName: String?): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Result.failure(Exception("User is null"))
            if (role == UserRole.PATIENT) {
                val pendingDoc = firestore.collection(FirestoreConstants.PENDING_PATIENTS)
                    .document(email.replace(".", "_")).get().await()
                if (!pendingDoc.exists()) {
                    auth.currentUser?.delete()?.await()
                    return Result.failure(Exception("No invite found for this email. Ask your doctor to add you first."))
                }
                val doctorId = pendingDoc.getString(FirestoreConstants.DOCTOR_ID) ?: ""
                val name = pendingDoc.getString(FirestoreConstants.DISPLAY_NAME) ?: email.substringBefore("@")
                val firestoreUser = hashMapOf(
                    FirestoreConstants.EMAIL to email,
                    FirestoreConstants.ROLE to UserRole.PATIENT.name,
                    FirestoreConstants.DISPLAY_NAME to name,
                    FirestoreConstants.DOCTOR_ID to doctorId
                )
                firestore.collection(FirestoreConstants.USERS).document(user.uid).set(firestoreUser).await()
                firestore.collection(FirestoreConstants.PENDING_PATIENTS).document(pendingDoc.id).delete().await()
            } else {
                val firestoreUser = hashMapOf(
                    FirestoreConstants.EMAIL to email,
                    FirestoreConstants.ROLE to role.name,
                    FirestoreConstants.DISPLAY_NAME to (displayName ?: email.substringBefore("@")),
                    FirestoreConstants.DOCTOR_ID to null
                )
                firestore.collection(FirestoreConstants.USERS).document(user.uid).set(firestoreUser).await()
            }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Result.failure(Exception("User is null"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun getUserProfile(uid: String): User? {
        return try {
            val doc = firestore.collection(FirestoreConstants.USERS).document(uid).get().await()
            doc.toUser()
        } catch (e: Exception) {
            null
        }
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
