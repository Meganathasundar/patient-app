package com.patientapp.health.ui.patient;

import com.patientapp.health.data.DailyForm;

import java.util.ArrayList;
import java.util.List;

public class PatientUiState {
    private final List<DailyForm> formHistory;
    private final boolean submitSuccess;
    private final String submitError;
    private final boolean isSubmitting;

    public PatientUiState() {
        this(new ArrayList<>(), false, null, false);
    }

    public PatientUiState(List<DailyForm> formHistory, boolean submitSuccess,
                          String submitError, boolean isSubmitting) {
        this.formHistory = formHistory;
        this.submitSuccess = submitSuccess;
        this.submitError = submitError;
        this.isSubmitting = isSubmitting;
    }

    public List<DailyForm> getFormHistory() { return formHistory; }
    public boolean isSubmitSuccess() { return submitSuccess; }
    public String getSubmitError() { return submitError; }
    public boolean isSubmitting() { return isSubmitting; }
}
