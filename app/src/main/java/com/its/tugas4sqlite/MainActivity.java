package com.its.tugas4sqlite;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.its.tugas4sqlite.adapter.TaskAdapter;
import com.its.tugas4sqlite.database.DatabaseHelper;
import com.its.tugas4sqlite.model.Task;
import com.its.tugas4sqlite.ui.PersistenceInfoDialog;
import com.its.tugas4sqlite.ui.TaskDetailDialog;
import com.its.tugas4sqlite.ui.TaskFormDialog;
import com.its.tugas4sqlite.util.CurrencyUtils;

import java.util.List;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskActionListener {

    private DatabaseHelper dbHelper;
    private TaskAdapter taskAdapter;

    // Header Statistics
    private TextView tvStatTotal, tvStatCompleted, tvStatPending, tvStatBudget;
    private MaterialButton btnPersistenceInfo;
    private ImageButton btnTopMenu;

    // Search & Filter
    private EditText etSearch;
    private ImageButton btnClearSearch;
    private ChipGroup chipGroupFilter;
    private TextView tvSectionTitle, tvSortLabel;

    // RecyclerView & Empty State
    private RecyclerView rvTasks;
    private LinearLayout layoutEmptyState;
    private MaterialButton btnEmptyLoadSample;
    private ExtendedFloatingActionButton fabAddTask;

    // State Variables
    private String currentSortBy = "newest";
    private String currentCategoryFilter = "Semua";
    private Integer currentStatusFilter = null; // null = all, 0 = pending, 1 = completed
    private boolean isPriorityFilter = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = DatabaseHelper.getInstance(this);

        // Jika basis data masih kosong saat aplikasi pertama kali dibuka,
        // muat data sampel secara otomatis agar penguji/asisten langsung melihat fungsionalitas
        if (dbHelper.getStatistics().totalTasks == 0) {
            dbHelper.insertSampleData();
        }

        initViews();
        setupRecyclerView();
        setupSearch();
        setupFilterChips();
        setupButtons();

        loadData();
    }

    private void initViews() {
        tvStatTotal = findViewById(R.id.tvStatTotal);
        tvStatCompleted = findViewById(R.id.tvStatCompleted);
        tvStatPending = findViewById(R.id.tvStatPending);
        tvStatBudget = findViewById(R.id.tvStatBudget);
        btnPersistenceInfo = findViewById(R.id.btnPersistenceInfo);
        btnTopMenu = findViewById(R.id.btnTopMenu);

        etSearch = findViewById(R.id.etSearch);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        chipGroupFilter = findViewById(R.id.chipGroupFilter);
        tvSectionTitle = findViewById(R.id.tvSectionTitle);
        tvSortLabel = findViewById(R.id.tvSortLabel);

        rvTasks = findViewById(R.id.rvTasks);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        btnEmptyLoadSample = findViewById(R.id.btnEmptyLoadSample);
        fabAddTask = findViewById(R.id.fabAddTask);
    }

    private void setupRecyclerView() {
        taskAdapter = new TaskAdapter(this, this);
        rvTasks.setLayoutManager(new LinearLayoutManager(this));
        rvTasks.setAdapter(taskAdapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();
                btnClearSearch.setVisibility(keyword.isEmpty() ? View.GONE : View.VISIBLE);
                if (keyword.isEmpty()) {
                    loadData();
                } else {
                    searchTasks(keyword);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnClearSearch.setOnClickListener(v -> etSearch.setText(""));
    }

    private void setupFilterChips() {
        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);

            if (checkedId == R.id.chipAll) {
                currentCategoryFilter = "Semua";
                currentStatusFilter = null;
                isPriorityFilter = false;
            } else if (checkedId == R.id.chipPending) {
                currentCategoryFilter = "Semua";
                currentStatusFilter = 0;
                isPriorityFilter = false;
            } else if (checkedId == R.id.chipCompleted) {
                currentCategoryFilter = "Semua";
                currentStatusFilter = 1;
                isPriorityFilter = false;
            } else if (checkedId == R.id.chipHighPriority) {
                currentCategoryFilter = "Semua";
                currentStatusFilter = null;
                isPriorityFilter = true;
            } else if (checkedId == R.id.chipCollege) {
                currentCategoryFilter = "Kuliah";
                currentStatusFilter = null;
                isPriorityFilter = false;
            } else if (checkedId == R.id.chipProject) {
                currentCategoryFilter = "Proyek";
                currentStatusFilter = null;
                isPriorityFilter = false;
            }
            loadData();
        });
    }

    private void setupButtons() {
        // Tombol Uji Persistensi
        btnPersistenceInfo.setOnClickListener(v -> {
            PersistenceInfoDialog dialog = new PersistenceInfoDialog(this);
            dialog.show();
        });

        // Tombol Menu Opsi Atas
        btnTopMenu.setOnClickListener(v -> showOptionsMenu(v));

        // FAB Tambah Tugas
        fabAddTask.setOnClickListener(v -> {
            TaskFormDialog dialog = new TaskFormDialog(this, null, (task, isEdit) -> {
                long newId = dbHelper.insertTask(task);
                if (newId != -1) {
                    showSnackbar("Tugas berhasil ditambahkan ke SQLite!");
                    loadData();
                } else {
                    Toast.makeText(this, "Gagal menyimpan tugas ke SQLite!", Toast.LENGTH_SHORT).show();
                }
            });
            dialog.show();
        });

        // Tombol Muat Sampel dari Empty State
        btnEmptyLoadSample.setOnClickListener(v -> {
            dbHelper.insertSampleData();
            showSnackbar("5 data sampel berhasil disisipkan!");
            loadData();
        });
    }

    private void showOptionsMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.main_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_persistence) {
                new PersistenceInfoDialog(this).show();
                return true;
            } else if (id == R.id.sort_newest) {
                currentSortBy = "newest";
                tvSortLabel.setText("Urutan: Terbaru");
                loadData();
                return true;
            } else if (id == R.id.sort_deadline) {
                currentSortBy = "deadline";
                tvSortLabel.setText("Urutan: Deadline Terdekat");
                loadData();
                return true;
            } else if (id == R.id.sort_priority) {
                currentSortBy = "priority";
                tvSortLabel.setText("Urutan: Prioritas");
                loadData();
                return true;
            } else if (id == R.id.sort_budget) {
                currentSortBy = "budget";
                tvSortLabel.setText("Urutan: Anggaran Terbesar");
                loadData();
                return true;
            } else if (id == R.id.action_generate_samples) {
                dbHelper.insertSampleData();
                showSnackbar("5 data sampel baru berhasil ditambahkan!");
                loadData();
                return true;
            } else if (id == R.id.action_delete_all) {
                confirmDeleteAll();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void confirmDeleteAll() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Hapus Semua Data?")
                .setMessage("Tindakan ini akan mengosongkan seluruh tabel tasks di database SQLite. Apakah Anda yakin?")
                .setPositiveButton("Hapus Semua", (dialog, which) -> {
                    int rows = dbHelper.deleteAllTasks();
                    showSnackbar("Database berhasil dikosongkan (" + rows + " baris terhapus)");
                    loadData();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    /**
     * Memuat data tugas dari database SQLite dan memperbarui komponen UI
     */
    public void loadData() {
        List<Task> tasks;
        if (isPriorityFilter) {
            // Filter prioritas tinggi
            tasks = dbHelper.getAllTasks(currentSortBy, null, null);
            tasks.removeIf(t -> !"Tinggi".equalsIgnoreCase(t.getPriority()));
        } else {
            tasks = dbHelper.getAllTasks(currentSortBy, currentCategoryFilter, currentStatusFilter);
        }

        taskAdapter.setTasks(tasks);

        // Update Section Header Count
        tvSectionTitle.setText("Daftar Tugas (" + tasks.size() + ")");

        // Empty state visibility
        if (tasks.isEmpty()) {
            rvTasks.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvTasks.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }

        // Update Header Statistics (SQLite Aggregate Query)
        DatabaseHelper.TaskStatistics stats = dbHelper.getStatistics();
        tvStatTotal.setText(String.valueOf(stats.totalTasks));
        tvStatCompleted.setText(String.valueOf(stats.completedTasks));
        tvStatPending.setText(String.valueOf(stats.pendingTasks));
        tvStatBudget.setText(CurrencyUtils.formatRupiah(stats.totalBudget));
    }

    /**
     * Pencarian realtime di SQLite dengan parameterized query LIKE
     */
    private void searchTasks(String keyword) {
        List<Task> results = dbHelper.searchTasks(keyword);
        taskAdapter.setTasks(results);
        tvSectionTitle.setText("Hasil Pencarian (" + results.size() + ")");

        if (results.isEmpty()) {
            rvTasks.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
            TextView tvEmptyTitle = findViewById(R.id.tvEmptyTitle);
            TextView tvEmptyDesc = findViewById(R.id.tvEmptyDesc);
            tvEmptyTitle.setText(R.string.search_empty_title);
            tvEmptyDesc.setText(R.string.search_empty_desc);
            btnEmptyLoadSample.setVisibility(View.GONE);
        } else {
            rvTasks.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
            btnEmptyLoadSample.setVisibility(View.VISIBLE);
        }
    }

    // ==========================================
    // Callbacks dari TaskAdapter
    // ==========================================
    @Override
    public void onTaskClick(Task task) {
        TaskDetailDialog dialog = new TaskDetailDialog(this, task, new TaskDetailDialog.OnDetailActionListener() {
            @Override
            public void onEdit(Task taskToEdit) {
                onTaskEdit(taskToEdit);
            }

            @Override
            public void onDelete(Task taskToDelete) {
                onTaskDelete(taskToDelete);
            }
        });
        dialog.show();
    }

    @Override
    public void onTaskEdit(Task task) {
        TaskFormDialog dialog = new TaskFormDialog(this, task, (updatedTask, isEdit) -> {
            int rowsUpdated = dbHelper.updateTask(updatedTask);
            if (rowsUpdated > 0) {
                showSnackbar("Perubahan tugas berhasil disimpan ke SQLite!");
                loadData();
            } else {
                Toast.makeText(this, "Gagal mengupdate data!", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }

    @Override
    public void onTaskDelete(Task task) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Hapus Tugas?")
                .setMessage("Apakah Anda yakin ingin menghapus \"" + task.getTitle() + "\" dari SQLite?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    int rows = dbHelper.deleteTask(task.getId());
                    if (rows > 0) {
                        showSnackbar("Tugas berhasil dihapus dari SQLite");
                        loadData();
                    } else {
                        Toast.makeText(this, "Gagal menghapus tugas!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    @Override
    public void onTaskStatusChanged(Task task, boolean isCompleted) {
        dbHelper.updateTaskStatus(task.getId(), isCompleted);
        task.setCompleted(isCompleted);
        loadData();
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }
}
