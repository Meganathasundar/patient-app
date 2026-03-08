package com.patientapp.health.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.patientapp.health.data.AuthRepository;
import com.patientapp.health.data.Callback;
import com.patientapp.health.data.User;

public class AuthViewModel extends ViewModel {

    private final AuthRepository authRepository;
    private final MutableLiveData<AuthUiState> uiState = new MutableLiveData<>(new AuthUiState());
    private final MediatorLiveData<AuthUiState> mergedState = new MediatorLiveData<>();

    public AuthViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;

        mergedState.addSource(uiState, mergedState::setValue);

        LiveData<FirebaseUser> authState = authRepository.authStateLiveData();
        mergedState.addSource(authState, firebaseUser -> {
            if (firebaseUser != null) {
                authRepository.getUserProfile(firebaseUser.getUid(), new Callback<User>() {
                    @Override
                    public void onSuccess(User profile) {
                        uiState.setValue(new AuthUiState(true, profile, false, null));
                    }

                    @Override
                    public void onError(Exception e) {
                        uiState.setValue(getCurrentState().withError(e.getMessage()));
                    }
                });
            } else {
                uiState.setValue(new AuthUiState());
            }
        });
    }

    public LiveData<AuthUiState> getUiState() {
        return mergedState;
    }

    private AuthUiState getCurrentState() {
        AuthUiState state = uiState.getValue();
        return state != null ? state : new AuthUiState();
    }

    public void signIn(String phone, String password) {
        uiState.setValue(getCurrentState().withLoading(true));
        authRepository.signIn(phone, password, new Callback<FirebaseUser>() {
            @Override
            public void onSuccess(FirebaseUser result) {
                uiState.setValue(getCurrentState().withLoading(false));
            }

            @Override
            public void onError(Exception e) {
                uiState.setValue(getCurrentState().withError(
                        e.getMessage() != null ? e.getMessage() : "Sign in failed"));
            }
        });
    }

    public void signUp(String phone, String password, String displayName) {
        uiState.setValue(getCurrentState().withLoading(true));
        authRepository.signUp(phone, password, displayName, new Callback<FirebaseUser>() {
            @Override
            public void onSuccess(FirebaseUser result) {
                uiState.setValue(getCurrentState().withLoading(false));
            }

            @Override
            public void onError(Exception e) {
                uiState.setValue(getCurrentState().withError(
                        e.getMessage() != null ? e.getMessage() : "Sign up failed"));
            }
        });
    }

    public void signOut() {
        authRepository.signOut();
        uiState.setValue(new AuthUiState());
    }

    public void clearError() {
        AuthUiState current = getCurrentState();
        uiState.setValue(new AuthUiState(
                current.isLoggedIn(), current.getCurrentUser(), false, null));
    }
}
