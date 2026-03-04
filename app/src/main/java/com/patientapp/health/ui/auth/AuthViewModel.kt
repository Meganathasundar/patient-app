package com.patientapp.health.ui.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.PhoneAuthCredential
import com.patientapp.health.data.AuthRepository
import com.patientapp.health.data.PhoneVerificationResult
import com.patientapp.health.data.User
import com.patientapp.health.data.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoggedIn: Boolean = false,
    val currentUser: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    /** Set when SMS code was sent; UI shows code field and Register. */
    val phoneVerificationId: String? = null,
    /** Set when instant verification completed; UI shows Register without code. */
    val phoneInstantCredentialReady: Boolean = false
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var pendingPhoneCredential: PhoneAuthCredential? = null

    init {
        authRepository.authStateFlow()
            .onEach { firebaseUser ->
                if (firebaseUser != null) {
                    val profile = authRepository.getUserProfile(firebaseUser.uid)
                    _uiState.value = _uiState.value.copy(
                        isLoggedIn = true,
                        currentUser = profile
                    )
                } else {
                    _uiState.value = AuthUiState(isLoggedIn = false, currentUser = null)
                }
            }
            .catch { _uiState.value = _uiState.value.copy(error = it.message) }
            .launchIn(viewModelScope)
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            authRepository.signIn(email, password)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = it.message ?: "Sign in failed"
                    )
                }
        }
    }

    fun signUp(email: String, password: String, role: UserRole, displayName: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            authRepository.signUp(email, password, role, displayName)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = it.message ?: "Sign up failed"
                    )
                }
        }
    }

    fun startPhoneVerification(phoneNumber: String, activity: Activity) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, phoneVerificationId = null, phoneInstantCredentialReady = false)
            pendingPhoneCredential = null
            authRepository.startPhoneVerification(phoneNumber, activity)
                .onSuccess { result ->
                    when (result) {
                        is PhoneVerificationResult.CodeSent ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                phoneVerificationId = result.verificationId
                            )
                        is PhoneVerificationResult.VerificationCompleted -> {
                            pendingPhoneCredential = result.credential
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                phoneInstantCredentialReady = true
                            )
                        }
                    }
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = it.message ?: "Phone verification failed"
                    )
                }
        }
    }

    fun signUpWithPhoneCode(verificationId: String, code: String, role: UserRole, displayName: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            authRepository.signUpWithPhoneCode(verificationId, code, role, displayName)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, phoneVerificationId = null)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = it.message ?: "Sign up failed"
                    )
                }
        }
    }

    fun signUpWithPhoneCredential(role: UserRole, displayName: String?) {
        val credential = pendingPhoneCredential ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            authRepository.signUpWithPhoneCredential(credential, role, displayName)
                .onSuccess {
                    pendingPhoneCredential = null
                    _uiState.value = _uiState.value.copy(isLoading = false, phoneInstantCredentialReady = false)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = it.message ?: "Sign up failed"
                    )
                }
        }
    }

    fun signInWithPhoneCode(verificationId: String, code: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            authRepository.signInWithPhoneCode(verificationId, code)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, phoneVerificationId = null)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = it.message ?: "Sign in failed"
                    )
                }
        }
    }

    fun signInWithPhoneCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            authRepository.signInWithPhoneCredential(credential)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, phoneInstantCredentialReady = false)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = it.message ?: "Sign in failed"
                    )
                }
        }
    }

    /** Sign in using the stored phone credential (after instant verification on login). */
    fun signInWithStoredPhoneCredential() {
        pendingPhoneCredential?.let { signInWithPhoneCredential(it) }
    }

    fun clearPhoneVerificationState() {
        pendingPhoneCredential = null
        _uiState.value = _uiState.value.copy(phoneVerificationId = null, phoneInstantCredentialReady = false)
    }

    fun signOut() {
        authRepository.signOut()
        pendingPhoneCredential = null
        _uiState.value = AuthUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
