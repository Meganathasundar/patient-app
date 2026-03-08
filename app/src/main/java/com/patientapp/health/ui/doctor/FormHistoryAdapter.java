package com.patientapp.health.ui.doctor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.patientapp.health.R;
import com.patientapp.health.data.DailyForm;
import com.patientapp.health.databinding.ItemFormHistoryBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FormHistoryAdapter extends ListAdapter<DailyForm, FormHistoryAdapter.ViewHolder> {

    public FormHistoryAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFormHistoryBinding binding = ItemFormHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemFormHistoryBinding binding;

        ViewHolder(ItemFormHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(DailyForm form) {
            String dateStr = "\u2014";
            if (form.getSubmittedAt() != null) {
                Date date = form.getSubmittedAt().toDate();
                dateStr = new SimpleDateFormat("MMM d, yyyy 'at' HH:mm", Locale.getDefault())
                        .format(date);
            }
            binding.tvDate.setText(dateStr);
            binding.tvPainLevel.setText(
                    itemView.getContext().getString(R.string.pain_level_format, form.getPainLevel()));

            if (!form.getSymptomsDescription().isEmpty()) {
                binding.tvSymptoms.setVisibility(View.VISIBLE);
                binding.tvSymptoms.setText(
                        itemView.getContext().getString(R.string.symptoms_format,
                                form.getSymptomsDescription()));
            } else {
                binding.tvSymptoms.setVisibility(View.GONE);
            }

            binding.tvOtherSymptoms.setText(form.isHasOtherSymptoms()
                    ? R.string.other_symptoms_yes : R.string.other_symptoms_no);

            if (form.isHasOtherSymptoms() && !form.getOtherSymptomsDescription().isEmpty()) {
                binding.tvOtherSymptomsDetail.setVisibility(View.VISIBLE);
                binding.tvOtherSymptomsDetail.setText(
                        itemView.getContext().getString(R.string.details_format,
                                form.getOtherSymptomsDescription()));
            } else {
                binding.tvOtherSymptomsDetail.setVisibility(View.GONE);
            }

            binding.tvTookMedicine.setText(form.isTookMedicine()
                    ? R.string.took_medicine_yes : R.string.took_medicine_no);

            if (form.isTookMedicine() && !form.getMedicineDescription().isEmpty()) {
                binding.tvMedicineDetail.setVisibility(View.VISIBLE);
                binding.tvMedicineDetail.setText(
                        itemView.getContext().getString(R.string.medicine_format,
                                form.getMedicineDescription()));
            } else {
                binding.tvMedicineDetail.setVisibility(View.GONE);
            }
        }
    }

    private static final DiffUtil.ItemCallback<DailyForm> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<DailyForm>() {
                @Override
                public boolean areItemsTheSame(@NonNull DailyForm oldItem, @NonNull DailyForm newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull DailyForm oldItem, @NonNull DailyForm newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }
            };
}
