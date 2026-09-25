package com.its.tugas4sqlite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.its.tugas4sqlite.database.DatabaseContract;
import com.its.tugas4sqlite.model.Task;
import com.its.tugas4sqlite.util.CurrencyUtils;
import com.its.tugas4sqlite.util.DateUtils;

import org.junit.Test;

/**
 * Unit Test memverifikasi kebenaran model data, contract skema SQL,
 * serta utility format mata uang dan tanggal.
 */
public class TaskModelAndContractTest {

    @Test
    public void testTaskModelInstantiationAndGetters() {
        Task task = new Task(1L, "Laporan Pemrograman Mobile", "Membuat CRUD SQLite",
                "Kuliah", "Tinggi", "2026-10-05", 25000.0, false, "2026-09-25 21:00:00");

        assertEquals(1L, task.getId());
        assertEquals("Laporan Pemrograman Mobile", task.getTitle());
        assertEquals("Membuat CRUD SQLite", task.getDescription());
        assertEquals("Kuliah", task.getCategory());
        assertEquals("Tinggi", task.getPriority());
        assertEquals("2026-10-05", task.getDueDate());
        assertEquals(25000.0, task.getBudget(), 0.001);
        assertFalse(task.isCompleted());
        assertEquals("2026-09-25 21:00:00", task.getCreatedAt());

        // Test status toggle
        task.setCompleted(true);
        assertTrue(task.isCompleted());
    }

    @Test
    public void testDatabaseContractSchemaQuery() {
        // Memastikan query CREATE TABLE SQLite memiliki struktur dan kolom yang valid
        String createSql = DatabaseContract.TaskEntry.SQL_CREATE_ENTRIES;

        assertNotNull(createSql);
        assertTrue(createSql.contains("CREATE TABLE tasks"));
        assertTrue(createSql.contains("_id INTEGER PRIMARY KEY AUTOINCREMENT"));
        assertTrue(createSql.contains("title TEXT NOT NULL"));
        assertTrue(createSql.contains("description TEXT"));
        assertTrue(createSql.contains("category TEXT NOT NULL"));
        assertTrue(createSql.contains("priority TEXT NOT NULL"));
        assertTrue(createSql.contains("due_date TEXT NOT NULL"));
        assertTrue(createSql.contains("budget REAL NOT NULL DEFAULT 0.0"));
        assertTrue(createSql.contains("is_completed INTEGER NOT NULL DEFAULT 0"));
        assertTrue(createSql.contains("created_at TEXT NOT NULL"));

        // Memastikan query DROP TABLE valid
        String deleteSql = DatabaseContract.TaskEntry.SQL_DELETE_ENTRIES;
        assertEquals("DROP TABLE IF EXISTS tasks", deleteSql);
    }

    @Test
    public void testCurrencyUtils() {
        String formatted = CurrencyUtils.formatRupiah(50000.0);
        assertNotNull(formatted);
        assertTrue(formatted.contains("50.000") || formatted.contains("50,000"));
    }

    @Test
    public void testDateUtilsFormatting() {
        String display = DateUtils.formatDisplayDate("2026-10-05");
        assertNotNull(display);
        assertTrue(display.contains("05") && display.contains("2026"));
    }
}
