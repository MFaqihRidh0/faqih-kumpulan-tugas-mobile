package com.its.tugas4sqlite.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.its.tugas4sqlite.model.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * DatabaseHelper mengelola pembuatan database, upgrade skema, serta
 * eksekusi operasi CRUD (Create, Read, Update, Delete) dan query agregasi SQLite.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static DatabaseHelper instance;

    // Singleton pattern untuk mengelola koneksi database secara efisien dan konsisten
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    public DatabaseHelper(@Nullable Context context) {
        super(context, DatabaseContract.DATABASE_NAME, null, DatabaseContract.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate: Membuat skema tabel SQLite...");
        db.execSQL(DatabaseContract.TaskEntry.SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "onUpgrade: Memperbarui database dari versi " + oldVersion + " ke " + newVersion);
        db.execSQL(DatabaseContract.TaskEntry.SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    // ==========================================
    // 1. CREATE (Insert Data)
    // ==========================================
    /**
     * Menyimpan data tugas baru ke SQLite menggunakan ContentValues
     * @param task objek Task yang akan disimpan
     * @return id baris yang baru disisipkan, atau -1 jika gagal
     */
    public long insertTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseContract.TaskEntry.COLUMN_TITLE, task.getTitle());
        values.put(DatabaseContract.TaskEntry.COLUMN_DESCRIPTION, task.getDescription());
        values.put(DatabaseContract.TaskEntry.COLUMN_CATEGORY, task.getCategory());
        values.put(DatabaseContract.TaskEntry.COLUMN_PRIORITY, task.getPriority());
        values.put(DatabaseContract.TaskEntry.COLUMN_DUE_DATE, task.getDueDate());
        values.put(DatabaseContract.TaskEntry.COLUMN_BUDGET, task.getBudget());
        values.put(DatabaseContract.TaskEntry.COLUMN_IS_COMPLETED, task.isCompleted() ? 1 : 0);

        String createdAt = task.getCreatedAt();
        if (createdAt == null || createdAt.trim().isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            createdAt = sdf.format(new Date());
        }
        values.put(DatabaseContract.TaskEntry.COLUMN_CREATED_AT, createdAt);

        long newRowId = db.insert(DatabaseContract.TaskEntry.TABLE_NAME, null, values);
        Log.d(TAG, "insertTask: Berhasil menambahkan task dengan ID = " + newRowId);
        return newRowId;
    }

    // ==========================================
    // 2. READ (Ambil Data dengan Filter & Sort)
    // ==========================================
    /**
     * Mengambil daftar tugas dengan filter opsional dan pengurutan
     */
    public List<Task> getAllTasks(String sortBy, @Nullable String filterCategory, @Nullable Integer filterStatus) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        StringBuilder selectionBuilder = new StringBuilder();
        List<String> selectionArgsList = new ArrayList<>();

        if (filterCategory != null && !filterCategory.equalsIgnoreCase("Semua")) {
            selectionBuilder.append(DatabaseContract.TaskEntry.COLUMN_CATEGORY).append(" = ?");
            selectionArgsList.add(filterCategory);
        }

        if (filterStatus != null) {
            if (selectionBuilder.length() > 0) {
                selectionBuilder.append(" AND ");
            }
            selectionBuilder.append(DatabaseContract.TaskEntry.COLUMN_IS_COMPLETED).append(" = ?");
            selectionArgsList.add(String.valueOf(filterStatus));
        }

        String selection = selectionBuilder.length() > 0 ? selectionBuilder.toString() : null;
        String[] selectionArgs = selectionArgsList.isEmpty() ? null : selectionArgsList.toArray(new String[0]);

        String orderBy;
        if ("deadline".equalsIgnoreCase(sortBy)) {
            orderBy = DatabaseContract.TaskEntry.COLUMN_DUE_DATE + " ASC";
        } else if ("priority".equalsIgnoreCase(sortBy)) {
            // Prioritas: Tinggi -> Sedang -> Rendah
            orderBy = "CASE " + DatabaseContract.TaskEntry.COLUMN_PRIORITY +
                    " WHEN 'Tinggi' THEN 1 WHEN 'Sedang' THEN 2 WHEN 'Rendah' THEN 3 ELSE 4 END ASC";
        } else if ("budget".equalsIgnoreCase(sortBy)) {
            orderBy = DatabaseContract.TaskEntry.COLUMN_BUDGET + " DESC";
        } else {
            // Default: ID descending (terbaru)
            orderBy = DatabaseContract.TaskEntry._ID + " DESC";
        }

        Cursor cursor = db.query(
                DatabaseContract.TaskEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                orderBy
        );

        if (cursor != null) {
            try {
                int idIndex = cursor.getColumnIndexOrThrow(DatabaseContract.TaskEntry._ID);
                int titleIndex = cursor.getColumnIndexOrThrow(DatabaseContract.TaskEntry.COLUMN_TITLE);
                int descIndex = cursor.getColumnIndexOrThrow(DatabaseContract.TaskEntry.COLUMN_DESCRIPTION);
                int catIndex = cursor.getColumnIndexOrThrow(DatabaseContract.TaskEntry.COLUMN_CATEGORY);
                int prioIndex = cursor.getColumnIndexOrThrow(DatabaseContract.TaskEntry.COLUMN_PRIORITY);
                int dueIndex = cursor.getColumnIndexOrThrow(DatabaseContract.TaskEntry.COLUMN_DUE_DATE);
                int budgetIndex = cursor.getColumnIndexOrThrow(DatabaseContract.TaskEntry.COLUMN_BUDGET);
                int compIndex = cursor.getColumnIndexOrThrow(DatabaseContract.TaskEntry.COLUMN_IS_COMPLETED);
                int createdIndex = cursor.getColumnIndexOrThrow(DatabaseContract.TaskEntry.COLUMN_CREATED_AT);

                while (cursor.moveToNext()) {
                    Task task = new Task(
                            cursor.getLong(idIndex),
                            cursor.getString(titleIndex),
                            cursor.getString(descIndex),
                            cursor.getString(catIndex),
                            cursor.getString(prioIndex),
                            cursor.getString(dueIndex),
                            cursor.getDouble(budgetIndex),
                            cursor.getInt(compIndex) == 1,
                            cursor.getString(createdIndex)
                    );
                    taskList.add(task);
                }
            } finally {
                cursor.close();
            }
        }
        return taskList;
    }

    /**
     * Mencari tugas berdasarkan kata kunci pada Judul atau Deskripsi (Parameterized Query)
     */
    public List<Task> searchTasks(String keyword) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = DatabaseContract.TaskEntry.COLUMN_TITLE + " LIKE ? OR " +
                DatabaseContract.TaskEntry.COLUMN_DESCRIPTION + " LIKE ? OR " +
                DatabaseContract.TaskEntry.COLUMN_CATEGORY + " LIKE ?";
        String wildKeyword = "%" + keyword + "%";
        String[] selectionArgs = new String[]{wildKeyword, wildKeyword, wildKeyword};

        Cursor cursor = db.query(
                DatabaseContract.TaskEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                DatabaseContract.TaskEntry._ID + " DESC"
        );

        if (cursor != null) {
            try {
                int idIndex = cursor.getColumnIndexOrThrow(DatabaseContract.TaskEntry._ID);
                int titleIndex = cursor.getColumnIndexOrThrow(DatabaseContract.TaskEntry.COLUMN_TITLE);
                int descIndex = cursor.getColumnIndexOrThrow(DatabaseContract.TaskEntry.COLUMN_DESCRIPTION);
                int catIndex = cursor.getColumnIndexOrThrow(DatabaseContract.TaskEntry.COLUMN_CATEGORY);
                int prioIndex = cursor.getColumnIndexOrThrow(DatabaseContract.TaskEntry.COLUMN_PRIORITY);
                int dueIndex = cursor.getColumnIndexOrThrow(DatabaseContract.TaskEntry.COLUMN_DUE_DATE);
                int budgetIndex = cursor.getColumnIndexOrThrow(DatabaseContract.TaskEntry.COLUMN_BUDGET);
                int compIndex = cursor.getColumnIndexOrThrow(DatabaseContract.TaskEntry.COLUMN_IS_COMPLETED);
                int createdIndex = cursor.getColumnIndexOrThrow(DatabaseContract.TaskEntry.COLUMN_CREATED_AT);

                while (cursor.moveToNext()) {
                    Task task = new Task(
                            cursor.getLong(idIndex),
                            cursor.getString(titleIndex),
                            cursor.getString(descIndex),
                            cursor.getString(catIndex),
                            cursor.getString(prioIndex),
                            cursor.getString(dueIndex),
                            cursor.getDouble(budgetIndex),
                            cursor.getInt(compIndex) == 1,
                            cursor.getString(createdIndex)
                    );
                    taskList.add(task);
                }
            } finally {
                cursor.close();
            }
        }
        return taskList;
    }

    /**
     * Mengambil satu tugas berdasarkan ID
     */
    public Task getTaskById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = DatabaseContract.TaskEntry._ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(id)};

        Cursor cursor = db.query(
                DatabaseContract.TaskEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        Task task = null;
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    task = new Task(
                            cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.TaskEntry._ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.TaskEntry.COLUMN_TITLE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.TaskEntry.COLUMN_DESCRIPTION)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.TaskEntry.COLUMN_CATEGORY)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.TaskEntry.COLUMN_PRIORITY)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.TaskEntry.COLUMN_DUE_DATE)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.TaskEntry.COLUMN_BUDGET)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.TaskEntry.COLUMN_IS_COMPLETED)) == 1,
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.TaskEntry.COLUMN_CREATED_AT))
                    );
                }
            } finally {
                cursor.close();
            }
        }
        return task;
    }

    // ==========================================
    // 3. UPDATE (Perbarui Data)
    // ==========================================
    /**
     * Memperbarui seluruh data tugas
     * @return jumlah baris yang berhasil diupdate
     */
    public int updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseContract.TaskEntry.COLUMN_TITLE, task.getTitle());
        values.put(DatabaseContract.TaskEntry.COLUMN_DESCRIPTION, task.getDescription());
        values.put(DatabaseContract.TaskEntry.COLUMN_CATEGORY, task.getCategory());
        values.put(DatabaseContract.TaskEntry.COLUMN_PRIORITY, task.getPriority());
        values.put(DatabaseContract.TaskEntry.COLUMN_DUE_DATE, task.getDueDate());
        values.put(DatabaseContract.TaskEntry.COLUMN_BUDGET, task.getBudget());
        values.put(DatabaseContract.TaskEntry.COLUMN_IS_COMPLETED, task.isCompleted() ? 1 : 0);

        String selection = DatabaseContract.TaskEntry._ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(task.getId())};

        int count = db.update(
                DatabaseContract.TaskEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );
        Log.d(TAG, "updateTask: Berhasil update " + count + " baris untuk ID = " + task.getId());
        return count;
    }

    /**
     * Memperbarui status selesai/belum selesai saja (Toggle)
     */
    public int updateTaskStatus(long id, boolean isCompleted) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.TaskEntry.COLUMN_IS_COMPLETED, isCompleted ? 1 : 0);

        String selection = DatabaseContract.TaskEntry._ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(id)};

        return db.update(DatabaseContract.TaskEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    // ==========================================
    // 4. DELETE (Hapus Data)
    // ==========================================
    /**
     * Menghapus tugas berdasarkan ID
     * @return jumlah baris yang berhasil dihapus
     */
    public int deleteTask(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = DatabaseContract.TaskEntry._ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(id)};

        int count = db.delete(DatabaseContract.TaskEntry.TABLE_NAME, selection, selectionArgs);
        Log.d(TAG, "deleteTask: Berhasil menghapus task ID = " + id + " (baris terhapus: " + count + ")");
        return count;
    }

    /**
     * Menghapus semua tugas (Reset data)
     */
    public int deleteAllTasks() {
        SQLiteDatabase db = this.getWritableDatabase();
        int count = db.delete(DatabaseContract.TaskEntry.TABLE_NAME, null, null);
        Log.d(TAG, "deleteAllTasks: Menghapus seluruh data (total baris: " + count + ")");
        return count;
    }

    // ==========================================
    // 5. QUERY AGREGASI & STATISTIK (SQLite Aggregate Functions)
    // ==========================================
    public static class TaskStatistics {
        public final int totalTasks;
        public final int completedTasks;
        public final int pendingTasks;
        public final double totalBudget;

        public TaskStatistics(int totalTasks, int completedTasks, int pendingTasks, double totalBudget) {
            this.totalTasks = totalTasks;
            this.completedTasks = completedTasks;
            this.pendingTasks = pendingTasks;
            this.totalBudget = totalBudget;
        }
    }

    /**
     * Menghitung statistik menggunakan query agregasi SQLite (COUNT, SUM, CASE WHEN)
     */
    public TaskStatistics getStatistics() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " +
                "COUNT(*) AS total, " +
                "SUM(CASE WHEN " + DatabaseContract.TaskEntry.COLUMN_IS_COMPLETED + " = 1 THEN 1 ELSE 0 END) AS completed, " +
                "SUM(CASE WHEN " + DatabaseContract.TaskEntry.COLUMN_IS_COMPLETED + " = 0 THEN 1 ELSE 0 END) AS pending, " +
                "SUM(" + DatabaseContract.TaskEntry.COLUMN_BUDGET + ") AS total_budget " +
                "FROM " + DatabaseContract.TaskEntry.TABLE_NAME;

        Cursor cursor = db.rawQuery(query, null);
        int total = 0;
        int completed = 0;
        int pending = 0;
        double totalBudget = 0.0;

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    total = cursor.getInt(cursor.getColumnIndexOrThrow("total"));
                    completed = cursor.getInt(cursor.getColumnIndexOrThrow("completed"));
                    pending = cursor.getInt(cursor.getColumnIndexOrThrow("pending"));
                    totalBudget = cursor.getDouble(cursor.getColumnIndexOrThrow("total_budget"));
                }
            } finally {
                cursor.close();
            }
        }
        return new TaskStatistics(total, completed, pending, totalBudget);
    }

    // ==========================================
    // 6. UJI INTEGRITAS PERSISTENSI SQLITE (PRAGMA integrity_check)
    // ==========================================
    /**
     * Memeriksa integritas fisik file SQLite database menggunakan PRAGMA integrity_check.
     * Mengembalikan pesan status dari engine SQLite ("ok" jika valid dan tidak corrupt).
     */
    public String checkDatabaseIntegrity() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("PRAGMA integrity_check;", null);
        StringBuilder result = new StringBuilder();
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    result.append(cursor.getString(0)).append("\n");
                }
            } finally {
                cursor.close();
            }
        }
        return result.toString().trim();
    }

    // ==========================================
    // 7. TRANSACTION MANAGEMENT (Batch Insert Sample Data)
    // ==========================================
    /**
     * Memasukkan data sampel awal dengan SQLite Transaction untuk efisiensi dan keamanan atomis.
     */
    public void insertSampleData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String now = sdf.format(new Date());

            Task[] samples = new Task[]{
                    new Task("Praktikum Pemrograman Mobile - Modul 4",
                            "Mengimplementasikan CRUD SQLite dan Uji Persistensi Data pada Android.",
                            "Kuliah", "Tinggi", "2026-10-05", 25000.0, true, now),
                    new Task("Final Project Website E-Commerce",
                            "Mengerjakan backend API dan integrasi payment gateway Midtrans.",
                            "Proyek", "Tinggi", "2026-10-12", 150000.0, false, now),
                    new Task("Rapat Koordinasi Himpunan Mahasiswa",
                            "Membahas proposal anggaran kegiatan tahunan di Gedung Robotika ITS.",
                            "Organisasi", "Sedang", "2026-09-30", 35000.0, false, now),
                    new Task("Belanja Buku Referensi Algoritma",
                            "Membeli buku karangan Thomas H. Cormen di toko buku.",
                            "Pribadi", "Rendah", "2026-10-15", 220000.0, true, now),
                    new Task("Desain UI/UX Prototipe Mobile App",
                            "Membuat wireframe dan mockup high-fidelity di Figma sesuai guideline Material 3.",
                            "Proyek", "Sedang", "2026-10-08", 50000.0, false, now)
            };

            for (Task t : samples) {
                ContentValues values = new ContentValues();
                values.put(DatabaseContract.TaskEntry.COLUMN_TITLE, t.getTitle());
                values.put(DatabaseContract.TaskEntry.COLUMN_DESCRIPTION, t.getDescription());
                values.put(DatabaseContract.TaskEntry.COLUMN_CATEGORY, t.getCategory());
                values.put(DatabaseContract.TaskEntry.COLUMN_PRIORITY, t.getPriority());
                values.put(DatabaseContract.TaskEntry.COLUMN_DUE_DATE, t.getDueDate());
                values.put(DatabaseContract.TaskEntry.COLUMN_BUDGET, t.getBudget());
                values.put(DatabaseContract.TaskEntry.COLUMN_IS_COMPLETED, t.isCompleted() ? 1 : 0);
                values.put(DatabaseContract.TaskEntry.COLUMN_CREATED_AT, t.getCreatedAt());

                db.insert(DatabaseContract.TaskEntry.TABLE_NAME, null, values);
            }

            db.setTransactionSuccessful();
            Log.d(TAG, "insertSampleData: Berhasil menambahkan data sampel dengan SQLite Transaction.");
        } finally {
            db.endTransaction();
        }
    }
}
