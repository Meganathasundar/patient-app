package com.patientapp.health.ui.patient;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.messaging.FirebaseMessaging;
import com.patientapp.health.data.Callback;
import com.patientapp.health.data.DailyForm;
import com.patientapp.health.data.DailyFormRepository;
import com.patientapp.health.data.UserRepository;

import java.util.ArrayList;
import java.util.List;

public class PatientViewModel extends ViewModel {

    private final String patientId;
    private final String doctorId;
    private final DailyFormRepository dailyFormRepository;
    private final MutableLiveData<PatientUiState> uiState = new MutableLiveData<>(new PatientUiState());
    private final MutableLiveData<List<DailyForm>> formHistory = new MutableLiveData<>(new ArrayList<>());
    private ListenerRegistration formsRegistration;

    public PatientViewModel(String patientId, String doctorId,
                            DailyFormRepository dailyFormRepository,
                            UserRepository userRepository) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.dailyFormRepository = dailyFormRepository;

        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> userRepository.updateFcmToken(patientId, token));

        formsRegistration = dailyFormRepository.observeFormsByPatient(patientId, formHistory);
        formHistory.observeForever(forms -> {
            PatientUiState current = getCurrentState();
            uiState.setValue(new PatientUiState(
                    forms != null ? forms : new ArrayList<>(),
                    current.isSubmitSuccess(),
                    current.getSubmitError(),
                    current.isSubmitting()));
        });
    }

    public LiveData<PatientUiState> getUiState() {
        return uiState;
    }

    private PatientUiState getCurrentState() {
        PatientUiState state = uiState.getValue();
        return state != null ? state : new PatientUiState();
    }

    public void submitForm(double temperature, String symptoms, int painLevel,
                           boolean hasOtherSymptoms, String otherSymptomsDescription,
                           boolean tookMedicine, String medicineDescription) {
        uiState.setValue(new PatientUiState(
                getCurrentState().getFormHistory(), false, null, true));

        dailyFormRepository.submitForm(patientId, doctorId, temperature, symptoms, painLevel,
                hasOtherSymptoms, otherSymptomsDescription, tookMedicine, medicineDescription,
                new Callback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        uiState.setValue(new PatientUiState(
                                getCurrentState().getFormHistory(), true, null, false));
                    }

                    @Override
                    public void onError(Exception e) {
                        String msg = e.getMessage() != null ? e.getMessage() : "Failed to submit";
                        uiState.setValue(new PatientUiState(
                                getCurrentState().getFormHistory(), false, msg, false));
                    }
                });
    }

    public void clearSubmitState() {
        PatientUiState current = getCurrentState();
        uiState.setValue(new PatientUiState(current.getFormHistory(), false, null, false));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (formsRegistration != null) formsRegistration.remove();
    }
}
