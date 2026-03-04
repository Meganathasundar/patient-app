package com.patientapp.health.ui.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.patientapp.health.data.DailyForm
import com.patientapp.health.data.DailyFormRepository
import com.patientapp.health.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class PatientUiState(
    val formHistory: List<DailyForm> = emptyList(),
    val submitSuccess: Boolean = false,
    val submitError: String? = null,
    val isSubmitting: Boolean = false
)

class PatientViewModel(
    private val patientId: String,
    private val doctorId: String,
    private val dailyFormRepository: DailyFormRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PatientUiState())
    val uiState: StateFlow<PatientUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                userRepository.updateFcmToken(patientId, token)
            } catch (_: Exception) { /* ignore */ }
        }
        dailyFormRepository.getFormsByPatient(patientId)
            .onEach { forms ->
                _uiState.value = _uiState.value.copy(formHistory = forms)
            }
            .launchIn(viewModelScope)
    }

    fun submitForm(
        temperature: Double,
        symptoms: String,
        painLevel: Int,
        hasOtherSymptoms: Boolean,
        otherSymptomsDescription: String,
        tookMedicine: Boolean,
        medicineDescription: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSubmitting = true,
                submitError = null,
                submitSuccess = false
            )
            dailyFormRepository.submitForm(
                patientId, doctorId, temperature, symptoms, painLevel,
                hasOtherSymptoms, otherSymptomsDescription,
                tookMedicine, medicineDescription
            )
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        submitSuccess = true
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        submitError = it.message ?: "Failed to submit"
                    )
                }
        }
    }

    fun clearSubmitState() {
        _uiState.value = _uiState.value.copy(submitSuccess = false, submitError = null)
    }
}
