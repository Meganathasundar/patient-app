package com.patientapp.health.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.Snackbar;
import com.patientapp.health.MainActivity;
import com.patientapp.health.R;
import com.patientapp.health.data.UserRole;
import com.patientapp.health.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private AuthViewModel authViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity activity = (MainActivity) requireActivity();
        authViewModel = new ViewModelProvider(requireActivity(),
                activity.getAuthViewModelFactory()).get(AuthViewModel.class);

        binding.btnSignIn.setOnClickListener(v -> {
            String phone = binding.etPhone.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            if (!phone.isEmpty() && !password.isEmpty()) {
                authViewModel.signIn(phone, password);
            }
        });

        binding.btnRegisterLink.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_login_to_register));

        authViewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            binding.btnSignIn.setEnabled(!state.isLoading());
            binding.etPhone.setEnabled(!state.isLoading());
            binding.etPassword.setEnabled(!state.isLoading());
            binding.btnSignIn.setText(state.isLoading() ? R.string.signing_in : R.string.sign_in);

            if (state.getError() != null) {
                Snackbar.make(view, state.getError(), Snackbar.LENGTH_LONG).show();
                authViewModel.clearError();
            }

            if (state.isLoggedIn() && state.getCurrentUser() != null) {
                if (state.getCurrentUser().getRole() == UserRole.DOCTOR) {
                    Navigation.findNavController(view).navigate(R.id.action_login_to_doctorHome);
                } else if (state.getCurrentUser().getRole() == UserRole.PATIENT) {
                    Navigation.findNavController(view).navigate(R.id.action_login_to_patientHome);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
