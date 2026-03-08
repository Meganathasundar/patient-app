package com.patientapp.health.ui.doctor;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.patientapp.health.data.User;
import com.patientapp.health.databinding.ItemPatientBinding;

public class PatientAdapter extends ListAdapter<User, PatientAdapter.ViewHolder> {

    public interface OnPatientClickListener {
        void onPatientClick(User patient);
    }

    private final OnPatientClickListener listener;

    public PatientAdapter(OnPatientClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPatientBinding binding = ItemPatientBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemPatientBinding binding;

        ViewHolder(ItemPatientBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(User patient) {
            String name = patient.getDisplayName() != null
                    ? patient.getDisplayName() : patient.getIdentifier();
            binding.tvName.setText(name);
            binding.tvPhone.setText(patient.getIdentifier());
            itemView.setOnClickListener(v -> listener.onPatientClick(patient));
        }
    }

    private static final DiffUtil.ItemCallback<User> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<User>() {
                @Override
                public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
                    return oldItem.getId().equals(newItem.getId())
                            && String.valueOf(oldItem.getDisplayName())
                            .equals(String.valueOf(newItem.getDisplayName()));
                }
            };
}
