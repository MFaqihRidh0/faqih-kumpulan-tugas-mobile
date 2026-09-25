package com.its.tugas4sqlite.ui;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.its.tugas4sqlite.R;
import com.its.tugas4sqlite.model.Task;
import com.its.tugas4sqlite.util.DateUtils;

import java.util.Calendar;
import java.util.Locale;

public class TaskFormDialog extends Dialog {

    public interface OnTaskSaveListener {
        void onTaskSave(Task task, boolean isEdit);
    }

    private final Task taskToEdit;
    private final OnTaskSaveListener listener;

    private TextView tvFormTitle;
    private TextInputLayout tilTitle, tilCategory, tilPriority, tilDueDate, tilBudget;
    private TextInputEditText etTitle, etDescription, etDueDate, etBudget;
    private AutoCompleteTextView actvCategory, actvPriority;
    private MaterialButton btnSave, btnCancel;

    private String selectedDbDate = "";

    public TaskFormDialog(@NonNull Context context, @Nullable Task taskToEdit, @NonNull OnTaskSaveListener listener) {
        super(context);
        this.taskToEdit = taskToEdit;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_task_form);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getWindow().setLayout(
                    (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.92),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        initViews();
        setupDropdowns();
        setupDatePicker();
        populateDataIfEdit();
        setupButtons();
    }

    private void initViews() {
        tvFormTitle = findViewById(R.id.tvFormTitle);
        tilTitle = findViewById(R.id.tilTitle);
        tilCategory = findViewById(R.id.tilCategory);
        tilPriority = findViewById(R.id.tilPriority);
        tilDueDate = findViewById(R.id.tilDueDate);
        tilBudget = findViewById(R.id.tilBudget);

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etDueDate = findViewById(R.id.etDueDate);
        etBudget = findViewById(R.id.etBudget);

        actvCategory = findViewById(R.id.actvCategory);
        actvPriority = findViewById(R.id.actvPriority);

        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void setupDropdowns() {
        String[] categories = new String[]{"Kuliah", "Proyek", "Organisasi", "Pribadi", "Lainnya"};
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, categories);
        actvCategory.setAdapter(catAdapter);
        actvCategory.setText(categories[0], false);

        String[] priorities = new String[]{"Tinggi", "Sedang", "Rendah"};
        ArrayAdapter<String> prioAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, priorities);
        actvPriority.setAdapter(prioAdapter);
        actvPriority.setText(priorities[1], false); // Default: Sedang
    }

    private void setupDatePicker() {
        selectedDbDate = DateUtils.getTodayDbDate();
        etDueDate.setText(DateUtils.formatDisplayDate(selectedDbDate));

        etDueDate.setOnClickListener(v -> showDatePicker());
        tilDueDate.setEndIconOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (!selectedDbDate.isEmpty()) {
            try {
                String[] parts = selectedDbDate.split("-");
                if (parts.length == 3) {
                    calendar.set(Calendar.YEAR, Integer.parseInt(parts[0]));
                    calendar.set(Calendar.MONTH, Integer.parseInt(parts[1]) - 1);
                    calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[2]));
                }
            } catch (Exception ignored) {
            }
        }

        DatePickerDialog dialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDbDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    etDueDate.setText(DateUtils.formatDisplayDate(selectedDbDate));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void populateDataIfEdit() {
        if (taskToEdit != null) {
            tvFormTitle.setText(R.string.edit_task);
            etTitle.setText(taskToEdit.getTitle());
            etDescription.setText(taskToEdit.getDescription());
            actvCategory.setText(taskToEdit.getCategory(), false);
            actvPriority.setText(taskToEdit.getPriority(), false);
            selectedDbDate = taskToEdit.getDueDate();
            etDueDate.setText(DateUtils.formatDisplayDate(selectedDbDate));
            if (taskToEdit.getBudget() > 0) {
                etBudget.setText(String.format(Locale.US, "%.0f", taskToEdit.getBudget()));
            }
        } else {
            tvFormTitle.setText(R.string.add_task);
        }
    }

    private void setupButtons() {
        btnCancel.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
            String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
            String category = actvCategory.getText() != null ? actvCategory.getText().toString().trim() : "Kuliah";
            String priority = actvPriority.getText() != null ? actvPriority.getText().toString().trim() : "Sedang";
            String budgetStr = etBudget.getText() != null ? etBudget.getText().toString().trim() : "0";

            if (title.isEmpty()) {
                tilTitle.setError("Judul tugas tidak boleh kosong!");
                etTitle.requestFocus();
                return;
            } else {
                tilTitle.setError(null);
            }

            double budget = 0.0;
            if (!budgetStr.isEmpty()) {
                try {
                    budget = Double.parseDouble(budgetStr);
                } catch (NumberFormatException e) {
                    tilBudget.setError("Format angka anggaran tidak valid");
                    etBudget.requestFocus();
                    return;
                }
            }

            if (taskToEdit != null) {
                taskToEdit.setTitle(title);
                taskToEdit.setDescription(description);
                taskToEdit.setCategory(category);
                taskToEdit.setPriority(priority);
                taskToEdit.setDueDate(selectedDbDate);
                taskToEdit.setBudget(budget);
                listener.onTaskSave(taskToEdit, true);
            } else {
                Task newTask = new Task(title, description, category, priority, selectedDbDate, budget, false, null);
                listener.onTaskSave(newTask, false);
            }
            dismiss();
        });
    }
}
