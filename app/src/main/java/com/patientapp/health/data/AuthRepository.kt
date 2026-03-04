package com.patientapp.health.data

import android.app.Activity
import android.content.Context
import com.google.firebase.FirebaseApp
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

sealed class PhoneVerificationResult {
    data class CodeSent(val verificationId: String) : PhoneVerificationResult()
    data class VerificationCompleted(val credential: PhoneAuthCredential) : PhoneVerificationResult()
}

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

    /** Register a new doctor with phone + password. */
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

    /** Sign up with SMS code and write Firestore profile. */
    suspend fun signUpWithPhoneCode(verificationId: String, code: String, role: UserRole, displayName: String?): Result<FirebaseUser> {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        return signInWithPhoneCredentialAndCreateProfile(credential, role, displayName)
    }

    /** Sign up with instant verification credential and write Firestore profile. */
    suspend fun signUpWithPhoneCredential(credential: PhoneAuthCredential, role: UserRole, displayName: String?): Result<FirebaseUser> {
        return signInWithPhoneCredentialAndCreateProfile(credential, role, displayName)
    }

    /** Sign in with phone code (for existing users). */
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

    private suspend fun signInWithPhoneCredentialAndCreateProfile(
        credential: PhoneAuthCredential,
        role: UserRole,
        displayName: String?
    ): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: return Result.failure(Exception("User is null"))
            val phone = user.phoneNumber ?: return Result.failure(Exception("Phone number is null"))
            val firestoreUser = hashMapOf(
                FirestoreConstants.PHONE to phone,
                FirestoreConstants.ROLE to role.name,
                FirestoreConstants.DISPLAY_NAME to (displayName ?: phone),
                FirestoreConstants.DOCTOR_ID to null
            )
            firestore.collection(FirestoreConstants.USERS).document(user.uid).set(firestoreUser).await()
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
