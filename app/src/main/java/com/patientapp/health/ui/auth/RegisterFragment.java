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
import com.patientapp.health.databinding.FragmentRegisterBinding;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private AuthViewModel authViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity activity = (MainActivity) requireActivity();
        authViewModel = new ViewModelProvider(requireActivity(),
                activity.getAuthViewModelFactory()).get(AuthViewModel.class);

        binding.btnRegister.setOnClickListener(v -> {
            String phone = binding.etPhone.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            String displayName = binding.etDisplayName.getText().toString().trim();
            if (!phone.isEmpty() && !password.isEmpty()) {
                authViewModel.signUp(phone, password, displayName.isEmpty() ? null : displayName);
            }
        });

        binding.btnLoginLink.setOnClickListener(v ->
                Navigation.findNavController(view).popBackStack());

        authViewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            binding.btnRegister.setEnabled(!state.isLoading());
            binding.etPhone.setEnabled(!state.isLoading());
            binding.etPassword.setEnabled(!state.isLoading());
            binding.etDisplayName.setEnabled(!state.isLoading());
            binding.btnRegister.setText(state.isLoading() ? R.string.creating : R.string.register);

            if (state.getError() != null) {
                Snackbar.make(view, state.getError(), Snackbar.LENGTH_LONG).show();
                authViewModel.clearError();
            }

            if (state.isLoggedIn() && state.getCurrentUser() != null) {
                if (state.getCurrentUser().getRole() == UserRole.DOCTOR) {
                    Navigation.findNavController(view).navigate(R.id.action_login_to_doctorHome);
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
