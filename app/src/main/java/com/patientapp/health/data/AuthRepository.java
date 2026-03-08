package com.patientapp.health.data;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AuthRepository {

    private final Context appContext;
    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;

    public AuthRepository(Context appContext) {
        this.appContext = appContext.getApplicationContext();
        this.auth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public String getCurrentUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    private String phoneToEmail(String phone) {
        String sanitized = phone.replaceAll("[^0-9]", "");
        return sanitized + "@phone.patientapp.com";
    }

    public LiveData<FirebaseUser> authStateLiveData() {
        MutableLiveData<FirebaseUser> liveData = new MutableLiveData<>();
        auth.addAuthStateListener(firebaseAuth -> liveData.setValue(firebaseAuth.getCurrentUser()));
        return liveData;
    }

    public void signUp(String phone, String password, String displayName, Callback<FirebaseUser> callback) {
        String syntheticEmail = phoneToEmail(phone);
        auth.createUserWithEmailAndPassword(syntheticEmail, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user == null) {
                        callback.onError(new Exception("User is null"));
                        return;
                    }
                    Map<String, Object> userData = new HashMap<>();
                    userData.put(FirestoreConstants.PHONE, phone);
                    userData.put(FirestoreConstants.ROLE, UserRole.DOCTOR.name());
                    userData.put(FirestoreConstants.DISPLAY_NAME, displayName != null ? displayName : phone);
                    userData.put(FirestoreConstants.DOCTOR_ID, null);

                    firestore.collection(FirestoreConstants.USERS).document(user.getUid())
                            .set(userData)
                            .addOnSuccessListener(aVoid -> callback.onSuccess(user))
                            .addOnFailureListener(callback::onError);
                })
                .addOnFailureListener(callback::onError);
    }

    public void signIn(String phone, String password, Callback<FirebaseUser> callback) {
        String syntheticEmail = phoneToEmail(phone);
        auth.signInWithEmailAndPassword(syntheticEmail, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user != null) {
                        callback.onSuccess(user);
                    } else {
                        callback.onError(new Exception("User is null"));
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    public void createPatientAccount(String phone, String displayName, String doctorId,
                                     Callback<Void> callback) {
        FirebaseApp secondaryApp;
        try {
            secondaryApp = FirebaseApp.getInstance("patientCreator");
        } catch (IllegalStateException e) {
            secondaryApp = FirebaseApp.initializeApp(
                    appContext,
                    FirebaseApp.getInstance().getOptions(),
                    "patientCreator");
        }

        FirebaseAuth secondaryAuth = FirebaseAuth.getInstance(secondaryApp);
        String syntheticEmail = phoneToEmail(phone);

        secondaryAuth.createUserWithEmailAndPassword(syntheticEmail, phone)
                .addOnSuccessListener(result -> {
                    FirebaseUser patientUser = result.getUser();
                    if (patientUser == null) {
                        callback.onError(new Exception("Failed to create patient account"));
                        return;
                    }
                    String patientUid = patientUser.getUid();
                    secondaryAuth.signOut();

                    Map<String, Object> userData = new HashMap<>();
                    userData.put(FirestoreConstants.PHONE, phone);
                    userData.put(FirestoreConstants.ROLE, UserRole.PATIENT.name());
                    userData.put(FirestoreConstants.DISPLAY_NAME, displayName);
                    userData.put(FirestoreConstants.DOCTOR_ID, doctorId);

                    firestore.collection(FirestoreConstants.USERS).document(patientUid)
                            .set(userData)
                            .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                            .addOnFailureListener(callback::onError);
                })
                .addOnFailureListener(callback::onError);
    }

    public void signOut() {
        auth.signOut();
    }

    public void getUserProfile(String uid, Callback<User> callback) {
        firestore.collection(FirestoreConstants.USERS).document(uid).get()
                .addOnSuccessListener(doc -> {
                    User user = DocumentParser.toUser(doc);
                    callback.onSuccess(user);
                })
                .addOnFailureListener(callback::onError);
    }
}
