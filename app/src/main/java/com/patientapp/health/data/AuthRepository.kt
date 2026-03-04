package com.patientapp.health.data

import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Result of starting phone number verification (SMS or instant). */
sealed class PhoneVerificationResult {
    data class CodeSent(val verificationId: String) : PhoneVerificationResult()
    data class VerificationCompleted(val credential: PhoneAuthCredential) : PhoneVerificationResult()
}

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
                    .document(pendingDocIdForEmail(email)).get().await()
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

    /** Start phone verification (sends SMS or completes instantly). Requires Activity for reCAPTCHA. */
    suspend fun startPhoneVerification(phoneNumber: String, activity: Activity): Result<PhoneVerificationResult> =
        suspendCancellableCoroutine { cont ->
            val callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    if (cont.isActive) cont.resume(Result.success(PhoneVerificationResult.VerificationCompleted(credential)))
                }
                override fun onVerificationFailed(e: FirebaseException) {
                    if (cont.isActive) cont.resume(Result.failure(e))
                }
                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    if (cont.isActive) cont.resume(Result.success(PhoneVerificationResult.CodeSent(verificationId)))
                }
            }
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callback)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }

    /** Sign in/up with SMS code and write Firestore profile. */
    suspend fun signUpWithPhoneCode(verificationId: String, code: String, role: UserRole, displayName: String?): Result<FirebaseUser> {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        return signInWithPhoneCredentialAndCreateProfile(credential, role, displayName)
    }

    /** Sign in/up with instant verification credential and write Firestore profile. */
    suspend fun signUpWithPhoneCredential(credential: PhoneAuthCredential, role: UserRole, displayName: String?): Result<FirebaseUser> {
        return signInWithPhoneCredentialAndCreateProfile(credential, role, displayName)
    }

    /** Sign in with phone (for existing users); no Firestore profile creation. */
    suspend fun signInWithPhoneCode(verificationId: String, code: String): Result<FirebaseUser> {
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: return Result.failure(Exception("User is null"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithPhoneCredential(credential: PhoneAuthCredential): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: return Result.failure(Exception("User is null"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun signInWithPhoneCredentialAndCreateProfile(credential: PhoneAuthCredential, role: UserRole, displayName: String?): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: return Result.failure(Exception("User is null"))
            val phone = user.phoneNumber ?: return Result.failure(Exception("Phone number is null"))
            val pendingId = pendingDocIdForPhone(phone)
            if (role == UserRole.PATIENT) {
                val pendingDoc = firestore.collection(FirestoreConstants.PENDING_PATIENTS).document(pendingId).get().await()
                if (!pendingDoc.exists()) {
                    auth.currentUser?.delete()?.await()
                    return Result.failure(Exception("No invite found for this phone number. Ask your doctor to add you first."))
                }
                val doctorId = pendingDoc.getString(FirestoreConstants.DOCTOR_ID) ?: ""
                val name = pendingDoc.getString(FirestoreConstants.DISPLAY_NAME) ?: phone
                val firestoreUser = hashMapOf(
                    FirestoreConstants.PHONE to phone,
                    FirestoreConstants.ROLE to UserRole.PATIENT.name,
                    FirestoreConstants.DISPLAY_NAME to name,
                    FirestoreConstants.DOCTOR_ID to doctorId
                )
                firestore.collection(FirestoreConstants.USERS).document(user.uid).set(firestoreUser).await()
                firestore.collection(FirestoreConstants.PENDING_PATIENTS).document(pendingId).delete().await()
            } else {
                val firestoreUser = hashMapOf(
                    FirestoreConstants.PHONE to phone,
                    FirestoreConstants.ROLE to role.name,
                    FirestoreConstants.DISPLAY_NAME to (displayName ?: phone),
                    FirestoreConstants.DOCTOR_ID to null
                )
                firestore.collection(FirestoreConstants.USERS).document(user.uid).set(firestoreUser).await()
            }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun pendingDocIdForEmail(email: String) = email.replace(".", "_")
    private fun pendingDocIdForPhone(phone: String) = phone.filter { it.isDigit() }

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
    val email = getString(FirestoreConstants.EMAIL)
    val phone = getString(FirestoreConstants.PHONE)
    if (email == null && phone == null) return null
    val role = UserRole.fromString(getString(FirestoreConstants.ROLE)) ?: return null
    return User(
        id = id,
        email = email,
        phone = phone,
        role = role,
        displayName = getString(FirestoreConstants.DISPLAY_NAME),
        doctorId = getString(FirestoreConstants.DOCTOR_ID),
        fcmToken = getString(FirestoreConstants.FCM_TOKEN)
    )
}
