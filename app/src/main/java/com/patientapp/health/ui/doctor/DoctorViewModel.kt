package com.patientapp.health.ui.doctor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patientapp.health.data.DailyForm
import com.patientapp.health.data.DailyFormRepository
import com.patientapp.health.data.User
import com.patientapp.health.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class DoctorUiState(
    val patients: List<User> = emptyList(),
    val allForms: List<DailyForm> = emptyList(),
    val addPatientError: String? = null,
    val addPatientSuccess: Boolean = false
)

class DoctorViewModel(
    private val doctorId: String,
    private val userRepository: UserRepository,
    private val dailyFormRepository: DailyFormRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DoctorUiState())
    val uiState: StateFlow<DoctorUiState> = _uiState.asStateFlow()

    init {
        combine(
            userRepository.getPatientsByDoctor(doctorId),
            dailyFormRepository.getFormsByDoctor(doctorId)
        ) { patients, forms ->
            DoctorUiState(
                patients = patients,
                allForms = forms
            )
        }.onEach { state ->
            _uiState.value = _uiState.value.copy(
                patients = state.patients,
                allForms = state.allForms
            )
        }.launchIn(viewModelScope)
    }

    fun addPatient(email: String, displayName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(addPatientError = null, addPatientSuccess = false)
            userRepository.addPatient(doctorId, email, displayName)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(addPatientSuccess = true)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        addPatientError = it.message ?: "Failed to add patient"
                    )
                }
        }
    }

    fun formsForPatient(patientId: String): List<DailyForm> {
        return _uiState.value.allForms.filter { it.patientId == patientId }
    }

    fun clearAddPatientState() {
        _uiState.value = _uiState.value.copy(addPatientError = null, addPatientSuccess = false)
    }
}
