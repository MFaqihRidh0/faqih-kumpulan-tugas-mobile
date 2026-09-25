package com.its.tugas4sqlite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.its.tugas4sqlite.database.DatabaseHelper;
import com.its.tugas4sqlite.model.Task;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * Instrumented Android Test untuk memvalidasi:
 * 1. Fungsionalitas CRUD SQLite (Create, Read, Update, Delete)
 * 2. Kebenaran Skema & Query Terparameterisasi
 * 3. Uji Persistensi Data & Integritas SQLite
 */
@RunWith(AndroidJUnit4.class)
public class DatabasePersistenceAndroidTest {

    private DatabaseHelper dbHelper;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        dbHelper = DatabaseHelper.getInstance(context);
        dbHelper.deleteAllTasks(); // Reset ke kondisi bersih sebelum pengujian
    }

    @After
    public void tearDown() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    @Test
    public void testSQLiteCRUDOperationsAndPersistence() {
        // 1. CREATE (Insert)
        Task sample = new Task(
                "Uji Coba SQLite Persistence",
                "Menguji penyimpanan permanen data pada SQLite Android.",
                "Kuliah",
                "Tinggi",
                "2026-10-10",
                75000.0,
                false,
                "2026-09-25 21:00:00"
        );
        long newId = dbHelper.insertTask(sample);
        assertTrue("ID hasil insert harus > 0", newId > 0);

        // 2. READ (Get By ID)
        Task retrieved = dbHelper.getTaskById(newId);
        assertNotNull("Data tugas harus berhasil diambil dari database", retrieved);
        assertEquals("Uji Coba SQLite Persistence", retrieved.getTitle());
        assertEquals("Kuliah", retrieved.getCategory());
        assertEquals("Tinggi", retrieved.getPriority());
        assertEquals(75000.0, retrieved.getBudget(), 0.001);

        // 3. UPDATE
        retrieved.setTitle("Uji Coba SQLite Persistence (Updated)");
        retrieved.setBudget(85000.0);
        int rowsUpdated = dbHelper.updateTask(retrieved);
        assertEquals("Harus mengupdate tepat 1 baris", 1, rowsUpdated);

        Task updatedTask = dbHelper.getTaskById(newId);
        assertEquals("Uji Coba SQLite Persistence (Updated)", updatedTask.getTitle());
        assertEquals(85000.0, updatedTask.getBudget(), 0.001);

        // 4. TOGGLE STATUS
        dbHelper.updateTaskStatus(newId, true);
        Task completedTask = dbHelper.getTaskById(newId);
        assertTrue("Status tugas harus menjadi completed (true)", completedTask.isCompleted());

        // 5. SEARCH QUERY (Parameterized LIKE)
        List<Task> searchResults = dbHelper.searchTasks("Persistence");
        assertEquals("Pencarian harus menemukan 1 tugas", 1, searchResults.size());

        // 6. SQL AGGREGATE & PERSISTENCE TEST
        DatabaseHelper.TaskStatistics stats = dbHelper.getStatistics();
        assertEquals(1, stats.totalTasks);
        assertEquals(1, stats.completedTasks);
        assertEquals(0, stats.pendingTasks);
        assertEquals(85000.0, stats.totalBudget, 0.001);

        // 7. PRAGMA integrity_check
        String integrity = dbHelper.checkDatabaseIntegrity();
        assertEquals("Integritas struktur file SQLite harus valid (ok)", "ok", integrity);

        // 8. DELETE
        int rowsDeleted = dbHelper.deleteTask(newId);
        assertEquals("Harus menghapus tepat 1 baris", 1, rowsDeleted);

        List<Task> afterDeleteList = dbHelper.getAllTasks("newest", null, null);
        assertEquals(0, afterDeleteList.size());
    }
}
