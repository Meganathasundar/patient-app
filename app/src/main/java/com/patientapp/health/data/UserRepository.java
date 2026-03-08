package com.patientapp.health.data;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    private final FirebaseFirestore firestore;

    public UserRepository() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    public ListenerRegistration observePatientsByDoctor(String doctorId,
                                                        MutableLiveData<List<User>> liveData) {
        return firestore.collection(FirestoreConstants.USERS)
                .whereEqualTo(FirestoreConstants.ROLE, UserRole.PATIENT.name())
                .whereEqualTo(FirestoreConstants.DOCTOR_ID, doctorId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null) {
                        liveData.setValue(new ArrayList<>());
                        return;
                    }
                    List<User> users = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        User user = DocumentParser.toUser(doc);
                        if (user != null) users.add(user);
                    }
                    liveData.setValue(users);
                });
    }

    public void updateFcmToken(String userId, String token) {
        firestore.collection(FirestoreConstants.USERS).document(userId)
                .update(FirestoreConstants.FCM_TOKEN, token);
    }
}
