package com.patientapp.health.ui.auth;

import com.patientapp.health.data.User;

public class AuthUiState {
    private final boolean isLoggedIn;
    private final User currentUser;
    private final boolean isLoading;
    private final String error;

    public AuthUiState() {
        this(false, null, false, null);
    }

    public AuthUiState(boolean isLoggedIn, User currentUser, boolean isLoading, String error) {
        this.isLoggedIn = isLoggedIn;
        this.currentUser = currentUser;
        this.isLoading = isLoading;
        this.error = error;
    }

    public boolean isLoggedIn() { return isLoggedIn; }
    public User getCurrentUser() { return currentUser; }
    public boolean isLoading() { return isLoading; }
    public String getError() { return error; }

    public AuthUiState withLoading(boolean loading) {
        return new AuthUiState(isLoggedIn, currentUser, loading, error);
    }

    public AuthUiState withError(String error) {
        return new AuthUiState(isLoggedIn, currentUser, false, error);
    }

    public AuthUiState withUser(User user) {
        return new AuthUiState(user != null, user, false, null);
    }
}
