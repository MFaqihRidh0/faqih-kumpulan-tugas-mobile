package com.its.tugas4sqlite.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.its.tugas4sqlite.R;
import com.its.tugas4sqlite.model.Task;
import com.its.tugas4sqlite.util.CurrencyUtils;
import com.its.tugas4sqlite.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface OnTaskActionListener {
        void onTaskClick(Task task);
        void onTaskEdit(Task task);
        void onTaskDelete(Task task);
        void onTaskStatusChanged(Task task, boolean isCompleted);
    }

    private final Context context;
    private List<Task> taskList = new ArrayList<>();
    private final OnTaskActionListener listener;

    public TaskAdapter(Context context, OnTaskActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setTasks(List<Task> tasks) {
        this.taskList = tasks != null ? tasks : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.bind(task, listener, context);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView cardTask;
        final TextView tvPriorityBadge;
        final TextView tvCategoryBadge;
        final ImageButton btnMoreOptions;
        final MaterialCheckBox cbCompleted;
        final TextView tvTaskTitle;
        final TextView tvTaskDescription;
        final TextView tvDueDate;
        final TextView tvBudget;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cardTask = itemView.findViewById(R.id.cardTask);
            tvPriorityBadge = itemView.findViewById(R.id.tvPriorityBadge);
            tvCategoryBadge = itemView.findViewById(R.id.tvCategoryBadge);
            btnMoreOptions = itemView.findViewById(R.id.btnMoreOptions);
            cbCompleted = itemView.findViewById(R.id.cbCompleted);
            tvTaskTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvTaskDescription = itemView.findViewById(R.id.tvTaskDescription);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
            tvBudget = itemView.findViewById(R.id.tvBudget);
        }

        public void bind(Task task, OnTaskActionListener listener, Context context) {
            tvTaskTitle.setText(task.getTitle());

            // Deskripsi (sembunyikan jika kosong)
            if (task.getDescription() != null && !task.getDescription().trim().isEmpty()) {
                tvTaskDescription.setVisibility(View.VISIBLE);
                tvTaskDescription.setText(task.getDescription());
            } else {
                tvTaskDescription.setVisibility(View.GONE);
            }

            // Kategori
            tvCategoryBadge.setText(task.getCategory());

            // Prioritas Styling
            String priority = task.getPriority() != null ? task.getPriority() : "Sedang";
            tvPriorityBadge.setText(priority);
            if ("Tinggi".equalsIgnoreCase(priority)) {
                tvPriorityBadge.setBackgroundResource(R.drawable.bg_chip_high);
                tvPriorityBadge.setTextColor(ContextCompat.getColor(context, R.color.priority_high));
            } else if ("Rendah".equalsIgnoreCase(priority)) {
                tvPriorityBadge.setBackgroundResource(R.drawable.bg_chip_low);
                tvPriorityBadge.setTextColor(ContextCompat.getColor(context, R.color.priority_low));
            } else {
                tvPriorityBadge.setBackgroundResource(R.drawable.bg_chip_medium);
                tvPriorityBadge.setTextColor(ContextCompat.getColor(context, R.color.priority_medium));
            }

            // Status Selesai / Belum
            cbCompleted.setOnCheckedChangeListener(null);
            cbCompleted.setChecked(task.isCompleted());

            if (task.isCompleted()) {
                tvTaskTitle.setPaintFlags(tvTaskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvTaskTitle.setTextColor(ContextCompat.getColor(context, R.color.text_muted));
                cardTask.setAlpha(0.75f);
            } else {
                tvTaskTitle.setPaintFlags(tvTaskTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tvTaskTitle.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
                cardTask.setAlpha(1.0f);
            }

            cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onTaskStatusChanged(task, isChecked);
                }
            });

            // Tenggat Waktu
            String formattedDate = DateUtils.formatDisplayDate(task.getDueDate());
            boolean overdue = !task.isCompleted() && DateUtils.isOverdue(task.getDueDate());
            if (overdue) {
                tvDueDate.setText(formattedDate + " (Terlambat)");
                tvDueDate.setTextColor(ContextCompat.getColor(context, R.color.priority_high));
            } else {
                tvDueDate.setText(formattedDate);
                tvDueDate.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
            }

            // Anggaran (REAL)
            tvBudget.setText(CurrencyUtils.formatRupiah(task.getBudget()));

            // Klik Item -> Detail
            cardTask.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskClick(task);
                }
            });

            // Menu Pop-up (Edit / Hapus / Detail)
            btnMoreOptions.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(context, btnMoreOptions);
                popup.getMenu().add(0, 1, 0, "Lihat Detail");
                popup.getMenu().add(0, 2, 1, "Edit Tugas");
                popup.getMenu().add(0, 3, 2, "Hapus Tugas");

                popup.setOnMenuItemClickListener(item -> {
                    if (listener == null) return false;
                    int id = item.getItemId();
                    if (id == 1) {
                        listener.onTaskClick(task);
                        return true;
                    } else if (id == 2) {
                        listener.onTaskEdit(task);
                        return true;
                    } else if (id == 3) {
                        listener.onTaskDelete(task);
                        return true;
                    }
                    return false;
                });
                popup.show();
            });
        }
    }
}
