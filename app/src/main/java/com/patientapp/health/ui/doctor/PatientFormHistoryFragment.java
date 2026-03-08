package com.patientapp.health.ui.doctor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.ListenerRegistration;
import com.patientapp.health.MainActivity;
import com.patientapp.health.data.DailyForm;
import com.patientapp.health.data.DailyFormRepository;
import com.patientapp.health.databinding.FragmentPatientFormHistoryBinding;

import java.util.ArrayList;
import java.util.List;

public class PatientFormHistoryFragment extends Fragment {

    private FragmentPatientFormHistoryBinding binding;
    private FormHistoryAdapter adapter;
    private ListenerRegistration formsRegistration;
    private final MutableLiveData<List<DailyForm>> forms = new MutableLiveData<>(new ArrayList<>());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPatientFormHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String patientId = getArguments() != null ? getArguments().getString("patientId") : "";
        String patientName = getArguments() != null ? getArguments().getString("patientName") : "";

        binding.toolbar.setTitle(patientName);
        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(view).popBackStack());

        adapter = new FormHistoryAdapter();
        binding.recyclerForms.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerForms.setAdapter(adapter);

        MainActivity activity = (MainActivity) requireActivity();
        DailyFormRepository repo = activity.getDailyFormRepository();
        formsRegistration = repo.observeFormsByPatient(patientId, forms);

        forms.observe(getViewLifecycleOwner(), formList -> {
            boolean empty = formList == null || formList.isEmpty();
            binding.tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.recyclerForms.setVisibility(empty ? View.GONE : View.VISIBLE);
            adapter.submitList(formList);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (formsRegistration != null) formsRegistration.remove();
        binding = null;
    }
}
