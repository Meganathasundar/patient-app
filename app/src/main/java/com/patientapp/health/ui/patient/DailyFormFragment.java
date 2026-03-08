package com.patientapp.health.ui.patient;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.patientapp.health.MainActivity;
import com.patientapp.health.R;
import com.patientapp.health.databinding.FragmentDailyFormBinding;
import com.patientapp.health.ui.auth.AuthViewModel;

public class DailyFormFragment extends Fragment {

    private static final int[] VAS_COLORS = {
            0xFF4CAF50, 0xFF66BB6A, 0xFF8BC34A, 0xFFCDDC39, 0xFFFFEB3B,
            0xFFFFC107, 0xFFFF9800, 0xFFFF7043, 0xFFEF5350, 0xFFE53935, 0xFFC62828
    };
    private static final String[] VAS_LABELS = {
            "No pain", "", "Mild", "", "Moderate discomfort", "",
            "Severe pain", "", "Very severe pain", "", "Most pain possible"
    };

    private FragmentDailyFormBinding binding;
    private AuthViewModel authViewModel;
    private PatientViewModel patientViewModel;
    private int selectedPainLevel = -1;
    private View[] vasRows;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDailyFormBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity activity = (MainActivity) requireActivity();
        authViewModel = new ViewModelProvider(requireActivity(),
                activity.getAuthViewModelFactory()).get(AuthViewModel.class);

        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_sign_out) {
                authViewModel.signOut();
                Navigation.findNavController(view).navigate(R.id.action_dailyForm_to_login);
                return true;
            }
            return false;
        });

        buildVasRows();
        setupRadioGroups();

        binding.btnStartSurvey.setOnClickListener(v -> {
            binding.landingSection.setVisibility(View.GONE);
            binding.surveySection.setVisibility(View.VISIBLE);
        });

        binding.btnCancel.setOnClickListener(v -> {
            binding.surveySection.setVisibility(View.GONE);
            binding.landingSection.setVisibility(View.VISIBLE);
            binding.cardValidationError.setVisibility(View.GONE);
        });

        binding.btnSubmit.setOnClickListener(v -> submitForm());

        authViewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            if (state.getCurrentUser() != null && state.getCurrentUser().getDoctorId() != null) {
                initPatientViewModel(activity, state.getCurrentUser().getId(),
                        state.getCurrentUser().getDoctorId());
            }
        });
    }

    private boolean patientInitialized = false;

    private void initPatientViewModel(MainActivity activity, String patientId, String doctorId) {
        if (patientInitialized) return;
        patientInitialized = true;

        patientViewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            @SuppressWarnings("unchecked")
            public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new PatientViewModel(patientId, doctorId,
                        activity.getDailyFormRepository(), activity.getUserRepository());
            }
        }).get(PatientViewModel.class);

        patientViewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            binding.btnSubmit.setEnabled(!state.isSubmitting());
            binding.btnCancel.setEnabled(!state.isSubmitting());
            binding.btnSubmit.setText(state.isSubmitting() ? R.string.submitting : R.string.submit);

            if (state.isSubmitSuccess()) {
                binding.cardSuccess.setVisibility(View.VISIBLE);
                binding.cardError.setVisibility(View.GONE);
                binding.surveySection.setVisibility(View.GONE);
                binding.landingSection.setVisibility(View.GONE);
            }

            if (state.getSubmitError() != null) {
                binding.cardError.setVisibility(View.VISIBLE);
                binding.tvError.setText(state.getSubmitError());
            } else {
                binding.cardError.setVisibility(View.GONE);
            }
        });

        binding.btnDismiss.setOnClickListener(v -> {
            patientViewModel.clearSubmitState();
            binding.cardSuccess.setVisibility(View.GONE);
            binding.surveySection.setVisibility(View.GONE);
            binding.landingSection.setVisibility(View.VISIBLE);
            resetForm();
        });
    }

    private void buildVasRows() {
        vasRows = new View[11];
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (int i = 0; i <= 10; i++) {
            View row = inflater.inflate(R.layout.item_vas_row, binding.vasContainer, false);

            TextView tvLevel = row.findViewById(R.id.tv_level);
            VasFaceView faceIcon = row.findViewById(R.id.face_icon);
            TextView tvLabel = row.findViewById(R.id.tv_label);

            tvLevel.setText(String.valueOf(i));
            tvLevel.setTextColor(VAS_COLORS[i]);
            faceIcon.setLevel(i, VAS_COLORS[i]);
            tvLabel.setText(VAS_LABELS[i]);

            final int level = i;
            row.setOnClickListener(v -> selectPainLevel(level));

            binding.vasContainer.addView(row);
            vasRows[i] = row;
        }
    }

    private void selectPainLevel(int level) {
        selectedPainLevel = level;
        binding.cardValidationError.setVisibility(View.GONE);

        for (int i = 0; i <= 10; i++) {
            View row = vasRows[i];
            ImageView check = row.findViewById(R.id.iv_check);
            boolean selected = (i == level);
            check.setVisibility(selected ? View.VISIBLE : View.GONE);

            if (selected) {
                GradientDrawable bg = new GradientDrawable();
                bg.setColor((VAS_COLORS[i] & 0x00FFFFFF) | 0x26000000);
                bg.setStroke(4, VAS_COLORS[i]);
                bg.setCornerRadius(16f);
                row.setBackground(bg);
            } else {
                row.setBackground(null);
            }
        }
    }

    private void setupRadioGroups() {
        binding.rgOtherSymptoms.setOnCheckedChangeListener((group, checkedId) -> {
            binding.tilOtherSymptoms.setVisibility(
                    checkedId == R.id.rb_other_yes ? View.VISIBLE : View.GONE);
        });

        binding.rgMedicine.setOnCheckedChangeListener((group, checkedId) -> {
            binding.tilMedicine.setVisibility(
                    checkedId == R.id.rb_medicine_yes ? View.VISIBLE : View.GONE);
        });
    }

    private void submitForm() {
        if (selectedPainLevel < 0) {
            binding.cardValidationError.setVisibility(View.VISIBLE);
            binding.tvValidationError.setText(R.string.select_pain_level);
            return;
        }

        if (patientViewModel == null) return;

        boolean hasOther = binding.rbOtherYes.isChecked();
        String otherDesc = hasOther ? binding.etOtherSymptoms.getText().toString() : "";
        boolean tookMed = binding.rbMedicineYes.isChecked();
        String medDesc = tookMed ? binding.etMedicine.getText().toString() : "";

        patientViewModel.submitForm(0.0, "", selectedPainLevel,
                hasOther, otherDesc, tookMed, medDesc);
    }

    private void resetForm() {
        selectedPainLevel = -1;
        for (int i = 0; i <= 10; i++) {
            vasRows[i].setBackground(null);
            vasRows[i].findViewById(R.id.iv_check).setVisibility(View.GONE);
        }
        binding.rgOtherSymptoms.clearCheck();
        binding.rgMedicine.clearCheck();
        binding.etOtherSymptoms.setText("");
        binding.etMedicine.setText("");
        binding.tilOtherSymptoms.setVisibility(View.GONE);
        binding.tilMedicine.setVisibility(View.GONE);
        binding.cardValidationError.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
