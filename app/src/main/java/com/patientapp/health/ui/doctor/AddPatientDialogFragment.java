package com.patientapp.health.ui.doctor;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.patientapp.health.R;
import com.patientapp.health.databinding.DialogAddPatientBinding;

public class AddPatientDialogFragment extends DialogFragment {

    private DialogAddPatientBinding binding;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogAddPatientBinding.inflate(LayoutInflater.from(requireContext()));

        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.add_patient)
                .setView(binding.getRoot())
                .setPositiveButton(R.string.add, null)
                .setNegativeButton(R.string.cancel, (d, w) -> dismiss())
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();
        androidx.appcompat.app.AlertDialog dialog = (androidx.appcompat.app.AlertDialog) getDialog();
        if (dialog == null) return;

        dialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String phone = binding.etPhone.getText().toString().trim();
            String displayName = binding.etDisplayName.getText().toString().trim();
            if (displayName.isEmpty()) displayName = phone;

            DoctorHomeFragment parent = (DoctorHomeFragment) getParentFragment();
            if (parent != null && parent.getDoctorViewModel() != null) {
                parent.getDoctorViewModel().addPatient(phone, displayName);
            }
        });

        DoctorHomeFragment parent = (DoctorHomeFragment) getParentFragment();
        if (parent != null && parent.getDoctorViewModel() != null) {
            parent.getDoctorViewModel().getUiState().observe(this, state -> {
                if (state.getAddPatientError() != null) {
                    binding.tvError.setVisibility(View.VISIBLE);
                    binding.tvError.setText(state.getAddPatientError());
                    binding.tvSuccess.setVisibility(View.GONE);
                } else if (state.isAddPatientSuccess()) {
                    binding.tvError.setVisibility(View.GONE);
                    binding.tvSuccess.setVisibility(View.VISIBLE);
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (isAdded()) {
                            parent.getDoctorViewModel().clearAddPatientState();
                            dismiss();
                        }
                    }, 2000);
                } else {
                    binding.tvError.setVisibility(View.GONE);
                    binding.tvSuccess.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
