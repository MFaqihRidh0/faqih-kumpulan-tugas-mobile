package com.its.tugas4sqlite.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.its.tugas4sqlite.R;
import com.its.tugas4sqlite.model.Task;
import com.its.tugas4sqlite.util.CurrencyUtils;
import com.its.tugas4sqlite.util.DateUtils;

public class TaskDetailDialog extends Dialog {

    public interface OnDetailActionListener {
        void onEdit(Task task);
        void onDelete(Task task);
    }

    private final Task task;
    private final OnDetailActionListener listener;

    public TaskDetailDialog(@NonNull Context context, @NonNull Task task, @NonNull OnDetailActionListener listener) {
        super(context);
        this.task = task;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_task_detail);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getWindow().setLayout(
                    (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.92),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        bindViews();
    }

    private void bindViews() {
        TextView tvDetailPriority = findViewById(R.id.tvDetailPriority);
        TextView tvDetailCategory = findViewById(R.id.tvDetailCategory);
        TextView tvDetailStatus = findViewById(R.id.tvDetailStatus);
        TextView tvDetailTitle = findViewById(R.id.tvDetailTitle);
        TextView tvDetailDescription = findViewById(R.id.tvDetailDescription);
        TextView tvDetailDueDate = findViewById(R.id.tvDetailDueDate);
        TextView tvDetailBudget = findViewById(R.id.tvDetailBudget);
        TextView tvDetailId = findViewById(R.id.tvDetailId);
        TextView tvDetailCreatedAt = findViewById(R.id.tvDetailCreatedAt);

        MaterialButton btnDetailDelete = findViewById(R.id.btnDetailDelete);
        MaterialButton btnDetailEdit = findViewById(R.id.btnDetailEdit);
        MaterialButton btnDetailClose = findViewById(R.id.btnDetailClose);

        tvDetailTitle.setText(task.getTitle());
        if (task.getDescription() != null && !task.getDescription().trim().isEmpty()) {
            tvDetailDescription.setText(task.getDescription());
        } else {
            tvDetailDescription.setText("(Tidak ada deskripsi tambahan)");
        }

        tvDetailCategory.setText(task.getCategory());

        // Prioritas
        String priority = task.getPriority() != null ? task.getPriority() : "Sedang";
        tvDetailPriority.setText(priority);
        if ("Tinggi".equalsIgnoreCase(priority)) {
            tvDetailPriority.setBackgroundResource(R.drawable.bg_chip_high);
            tvDetailPriority.setTextColor(ContextCompat.getColor(getContext(), R.color.priority_high));
        } else if ("Rendah".equalsIgnoreCase(priority)) {
            tvDetailPriority.setBackgroundResource(R.drawable.bg_chip_low);
            tvDetailPriority.setTextColor(ContextCompat.getColor(getContext(), R.color.priority_low));
        } else {
            tvDetailPriority.setBackgroundResource(R.drawable.bg_chip_medium);
            tvDetailPriority.setTextColor(ContextCompat.getColor(getContext(), R.color.priority_medium));
        }

        // Status
        if (task.isCompleted()) {
            tvDetailStatus.setText("Selesai");
            tvDetailStatus.setBackgroundResource(R.drawable.bg_chip_category);
            tvDetailStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.status_completed));
        } else {
            tvDetailStatus.setText("Belum Selesai");
            tvDetailStatus.setBackgroundResource(R.drawable.bg_chip_medium);
            tvDetailStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.priority_medium));
        }

        // Due date & Overdue
        String formattedDate = DateUtils.formatDisplayDate(task.getDueDate());
        if (!task.isCompleted() && DateUtils.isOverdue(task.getDueDate())) {
            tvDetailDueDate.setText(formattedDate + " (Terlambat)");
            tvDetailDueDate.setTextColor(ContextCompat.getColor(getContext(), R.color.priority_high));
        } else {
            tvDetailDueDate.setText(formattedDate);
        }

        tvDetailBudget.setText(CurrencyUtils.formatRupiah(task.getBudget()));
        tvDetailId.setText(String.valueOf(task.getId()));
        tvDetailCreatedAt.setText(task.getCreatedAt() != null ? task.getCreatedAt() : "-");

        btnDetailDelete.setOnClickListener(v -> {
            dismiss();
            listener.onDelete(task);
        });

        btnDetailEdit.setOnClickListener(v -> {
            dismiss();
            listener.onEdit(task);
        });

        btnDetailClose.setOnClickListener(v -> dismiss());
    }
}
