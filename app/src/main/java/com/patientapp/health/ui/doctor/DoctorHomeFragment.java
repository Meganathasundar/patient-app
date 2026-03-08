package com.patientapp.health.ui.doctor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.patientapp.health.MainActivity;
import com.patientapp.health.R;
import com.patientapp.health.data.User;
import com.patientapp.health.databinding.FragmentDoctorHomeBinding;
import com.patientapp.health.ui.auth.AuthViewModel;

public class DoctorHomeFragment extends Fragment {

    private FragmentDoctorHomeBinding binding;
    private DoctorViewModel doctorViewModel;
    private AuthViewModel authViewModel;
    private PatientAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDoctorHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity activity = (MainActivity) requireActivity();
        authViewModel = new ViewModelProvider(requireActivity(),
                activity.getAuthViewModelFactory()).get(AuthViewModel.class);

        authViewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            if (state.getCurrentUser() != null) {
                initDoctor(view, activity, state.getCurrentUser().getId());
            }
        });

        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_sign_out) {
                authViewModel.signOut();
                Navigation.findNavController(view).navigate(R.id.action_doctorHome_to_login);
                return true;
            }
            return false;
        });

        binding.fabAddPatient.setOnClickListener(v -> {
            AddPatientDialogFragment dialog = new AddPatientDialogFragment();
            dialog.show(getChildFragmentManager(), "add_patient");
        });

        adapter = new PatientAdapter(patient -> navigateToPatientHistory(view, patient));
        binding.recyclerPatients.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerPatients.setAdapter(adapter);
    }

    private boolean doctorInitialized = false;

    private void initDoctor(View view, MainActivity activity, String doctorId) {
        if (doctorInitialized) return;
        doctorInitialized = true;

        doctorViewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            @SuppressWarnings("unchecked")
            public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new DoctorViewModel(doctorId, activity.getAuthRepository(),
                        activity.getUserRepository(), activity.getDailyFormRepository());
            }
        }).get(DoctorViewModel.class);

        doctorViewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            boolean empty = state.getPatients().isEmpty();
            binding.emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.recyclerPatients.setVisibility(empty ? View.GONE : View.VISIBLE);
            adapter.submitList(state.getPatients());
        });
    }

    private void navigateToPatientHistory(View view, User patient) {
        Bundle args = new Bundle();
        args.putString("patientId", patient.getId());
        String name = patient.getDisplayName() != null
                ? patient.getDisplayName() : patient.getIdentifier();
        args.putString("patientName", name);
        Navigation.findNavController(view).navigate(R.id.action_doctorHome_to_formHistory, args);
    }

    public DoctorViewModel getDoctorViewModel() {
        return doctorViewModel;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
