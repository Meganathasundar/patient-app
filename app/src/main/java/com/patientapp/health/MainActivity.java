package com.patientapp.health;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.patientapp.health.data.AuthRepository;
import com.patientapp.health.data.DailyFormRepository;
import com.patientapp.health.data.UserRepository;
import com.patientapp.health.ui.auth.AuthViewModel;

public class MainActivity extends AppCompatActivity {

    private AuthRepository authRepository;
    private UserRepository userRepository;
    private DailyFormRepository dailyFormRepository;
    private ViewModelProvider.Factory authViewModelFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        authRepository = new AuthRepository(getApplicationContext());
        userRepository = new UserRepository();
        dailyFormRepository = new DailyFormRepository();

        authViewModelFactory = new ViewModelProvider.Factory() {
            @NonNull
            @Override
            @SuppressWarnings("unchecked")
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new AuthViewModel(authRepository);
            }
        };

        setContentView(R.layout.activity_main);
    }

    public AuthRepository getAuthRepository() {
        return authRepository;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public DailyFormRepository getDailyFormRepository() {
        return dailyFormRepository;
    }

    public ViewModelProvider.Factory getAuthViewModelFactory() {
        return authViewModelFactory;
    }
}
