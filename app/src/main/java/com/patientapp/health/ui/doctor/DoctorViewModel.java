package com.patientapp.health.ui.doctor;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.ListenerRegistration;
import com.patientapp.health.data.AuthRepository;
import com.patientapp.health.data.Callback;
import com.patientapp.health.data.DailyForm;
import com.patientapp.health.data.DailyFormRepository;
import com.patientapp.health.data.User;
import com.patientapp.health.data.UserRepository;

import java.util.ArrayList;
import java.util.List;

public class DoctorViewModel extends ViewModel {

    private final String doctorId;
    private final AuthRepository authRepository;
    private final MutableLiveData<DoctorUiState> uiState = new MutableLiveData<>(new DoctorUiState());
    private final MutableLiveData<List<User>> patients = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<DailyForm>> allForms = new MutableLiveData<>(new ArrayList<>());

    private ListenerRegistration patientsRegistration;
    private ListenerRegistration formsRegistration;

    public DoctorViewModel(String doctorId, AuthRepository authRepository,
                           UserRepository userRepository, DailyFormRepository dailyFormRepository) {
        this.doctorId = doctorId;
        this.authRepository = authRepository;

        patientsRegistration = userRepository.observePatientsByDoctor(doctorId, patients);
        formsRegistration = dailyFormRepository.observeFormsByDoctor(doctorId, allForms);

        patients.observeForever(this::updateState);
        allForms.observeForever(this::updateState);
    }

    private void updateState(Object ignored) {
        List<User> p = patients.getValue();
        List<DailyForm> f = allForms.getValue();
        DoctorUiState current = getCurrentState();
        uiState.setValue(current.withData(
                p != null ? p : new ArrayList<>(),
                f != null ? f : new ArrayList<>()));
    }

    public LiveData<DoctorUiState> getUiState() {
        return uiState;
    }

    private DoctorUiState getCurrentState() {
        DoctorUiState state = uiState.getValue();
        return state != null ? state : new DoctorUiState();
    }

    public void addPatient(String phone, String displayName) {
        uiState.setValue(getCurrentState().withAddResult(null, false));
        authRepository.createPatientAccount(phone, displayName, doctorId, new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                uiState.setValue(getCurrentState().withAddResult(null, true));
            }

            @Override
            public void onError(Exception e) {
                uiState.setValue(getCurrentState().withAddResult(
                        e.getMessage() != null ? e.getMessage() : "Failed to add patient", false));
            }
        });
    }

    public List<DailyForm> formsForPatient(String patientId) {
        List<DailyForm> all = allForms.getValue();
        if (all == null) return new ArrayList<>();
        List<DailyForm> result = new ArrayList<>();
        for (DailyForm form : all) {
            if (patientId.equals(form.getPatientId())) {
                result.add(form);
            }
        }
        return result;
    }

    public void clearAddPatientState() {
        uiState.setValue(getCurrentState().withAddResult(null, false));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (patientsRegistration != null) patientsRegistration.remove();
        if (formsRegistration != null) formsRegistration.remove();
        patients.removeObserver(this::updateState);
        allForms.removeObserver(this::updateState);
    }
}
