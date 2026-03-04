package com.patientapp.health.data

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(private val appContext: Context) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val currentUserId: String?
        get() = auth.currentUser?.uid

    private fun phoneToEmail(phone: String): String {
        val sanitized = phone.replace(Regex("[^0-9]"), "")
        return "$sanitized@phone.patientapp.com"
    }

    fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signUp(phone: String, password: String, displayName: String?): Result<FirebaseUser> {
        return try {
            val syntheticEmail = phoneToEmail(phone)
            val result = auth.createUserWithEmailAndPassword(syntheticEmail, password).await()
            val user = result.user ?: return Result.failure(Exception("User is null"))
            val userData = hashMapOf(
                FirestoreConstants.PHONE to phone,
                FirestoreConstants.ROLE to UserRole.DOCTOR.name,
                FirestoreConstants.DISPLAY_NAME to (displayName ?: phone),
                FirestoreConstants.DOCTOR_ID to null
            )
            firestore.collection(FirestoreConstants.USERS).document(user.uid).set(userData).await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(phone: String, password: String): Result<FirebaseUser> {
        return try {
            val syntheticEmail = phoneToEmail(phone)
            val result = auth.signInWithEmailAndPassword(syntheticEmail, password).await()
            val user = result.user ?: return Result.failure(Exception("User is null"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Creates a patient account without affecting the current doctor's session.
     * Uses a secondary FirebaseApp instance so the doctor stays signed in.
     * The patient's password is set to their phone number.
     */
    suspend fun createPatientAccount(
        phone: String,
        displayName: String,
        doctorId: String
    ): Result<Unit> {
        return try {
            val secondaryApp = try {
                FirebaseApp.getInstance("patientCreator")
            } catch (_: IllegalStateException) {
                FirebaseApp.initializeApp(
                    appContext,
                    FirebaseApp.getInstance().options,
                    "patientCreator"
                )
            }
            val secondaryAuth = FirebaseAuth.getInstance(secondaryApp)
            val syntheticEmail = phoneToEmail(phone)
            val result = secondaryAuth.createUserWithEmailAndPassword(syntheticEmail, phone).await()
            val patientUid = result.user?.uid
                ?: return Result.failure(Exception("Failed to create patient account"))
            secondaryAuth.signOut()

            val userData = hashMapOf(
                FirestoreConstants.PHONE to phone,
                FirestoreConstants.ROLE to UserRole.PATIENT.name,
                FirestoreConstants.DISPLAY_NAME to displayName,
                FirestoreConstants.DOCTOR_ID to doctorId
            )
            firestore.collection(FirestoreConstants.USERS).document(patientUid).set(userData).await()
            Result.success(Unit)
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
