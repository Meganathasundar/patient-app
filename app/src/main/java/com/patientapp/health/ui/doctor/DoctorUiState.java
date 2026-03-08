package com.patientapp.health.ui.doctor;

import com.patientapp.health.data.DailyForm;
import com.patientapp.health.data.User;

import java.util.ArrayList;
import java.util.List;

public class DoctorUiState {
    private final List<User> patients;
    private final List<DailyForm> allForms;
    private final String addPatientError;
    private final boolean addPatientSuccess;

    public DoctorUiState() {
        this(new ArrayList<>(), new ArrayList<>(), null, false);
    }

    public DoctorUiState(List<User> patients, List<DailyForm> allForms,
                         String addPatientError, boolean addPatientSuccess) {
        this.patients = patients;
        this.allForms = allForms;
        this.addPatientError = addPatientError;
        this.addPatientSuccess = addPatientSuccess;
    }

    public List<User> getPatients() { return patients; }
    public List<DailyForm> getAllForms() { return allForms; }
    public String getAddPatientError() { return addPatientError; }
    public boolean isAddPatientSuccess() { return addPatientSuccess; }

    public DoctorUiState withAddResult(String error, boolean success) {
        return new DoctorUiState(patients, allForms, error, success);
    }

    public DoctorUiState withData(List<User> patients, List<DailyForm> forms) {
        return new DoctorUiState(patients, forms, addPatientError, addPatientSuccess);
    }
}
