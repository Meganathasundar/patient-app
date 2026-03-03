package com.patientapp.health.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patientapp.health.data.AuthRepository
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
    val error: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

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

    fun signOut() {
        authRepository.signOut()
        _uiState.value = AuthUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
