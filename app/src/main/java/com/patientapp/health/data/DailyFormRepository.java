package com.patientapp.health.data;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DailyFormRepository {

    private final FirebaseFirestore firestore;

    public DailyFormRepository() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    public void submitForm(String patientId, String doctorId, double temperature,
                           String symptoms, int painLevel, boolean hasOtherSymptoms,
                           String otherSymptomsDescription, boolean tookMedicine,
                           String medicineDescription, Callback<Void> callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("patientId", patientId);
        data.put("doctorId", doctorId);
        data.put("bodyTemperature", temperature);
        data.put("symptomsDescription", symptoms);
        data.put("painLevel", painLevel);
        data.put("hasOtherSymptoms", hasOtherSymptoms);
        data.put("otherSymptomsDescription", otherSymptomsDescription);
        data.put("tookMedicine", tookMedicine);
        data.put("medicineDescription", medicineDescription);
        data.put("submittedAt", Timestamp.now());

        firestore.collection(DailyForm.COLLECTION).add(data)
                .addOnSuccessListener(ref -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    public ListenerRegistration observeFormsByPatient(String patientId,
                                                      MutableLiveData<List<DailyForm>> liveData) {
        return firestore.collection(DailyForm.COLLECTION)
                .whereEqualTo(DailyForm.FIELD_PATIENT_ID, patientId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null) {
                        liveData.setValue(new ArrayList<>());
                        return;
                    }
                    List<DailyForm> forms = parseForms(snapshot.getDocuments());
                    liveData.setValue(forms);
                });
    }

    public ListenerRegistration observeFormsByDoctor(String doctorId,
                                                     MutableLiveData<List<DailyForm>> liveData) {
        return firestore.collection(DailyForm.COLLECTION)
                .whereEqualTo(DailyForm.FIELD_DOCTOR_ID, doctorId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null) {
                        liveData.setValue(new ArrayList<>());
                        return;
                    }
                    List<DailyForm> forms = parseForms(snapshot.getDocuments());
                    liveData.setValue(forms);
                });
    }

    private List<DailyForm> parseForms(List<DocumentSnapshot> documents) {
        List<DailyForm> forms = new ArrayList<>();
        for (DocumentSnapshot doc : documents) {
            DailyForm form = DocumentParser.toDailyForm(doc);
            if (form != null) forms.add(form);
        }
        Collections.sort(forms, (a, b) -> {
            if (a.getSubmittedAt() == null && b.getSubmittedAt() == null) return 0;
            if (a.getSubmittedAt() == null) return 1;
            if (b.getSubmittedAt() == null) return -1;
            return b.getSubmittedAt().compareTo(a.getSubmittedAt());
        });
        return forms;
    }
}
