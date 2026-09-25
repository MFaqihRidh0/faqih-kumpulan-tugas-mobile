package com.its.tugas4sqlite.database;

import android.provider.BaseColumns;

/**
 * DatabaseContract menentukan skema tabel, nama kolom, serta query pembuatan
 * dan penghapusan tabel secara terstruktur dan formal sesuai standar Android SQLite.
 */
public final class DatabaseContract {

    // Konstruktor privat mencegah instansiasi kelas contract
    private DatabaseContract() {}

    public static final String DATABASE_NAME = "taskmaster.db";
    public static final int DATABASE_VERSION = 1;

    public static final class TaskEntry implements BaseColumns {
        public static final String TABLE_NAME = "tasks";

        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_CATEGORY = "category";
        public static final String COLUMN_PRIORITY = "priority";
        public static final String COLUMN_DUE_DATE = "due_date";
        public static final String COLUMN_BUDGET = "budget";
        public static final String COLUMN_IS_COMPLETED = "is_completed";
        public static final String COLUMN_CREATED_AT = "created_at";

        // Query CREATE TABLE yang valid dan aman
        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_TITLE + " TEXT NOT NULL, " +
                        COLUMN_DESCRIPTION + " TEXT, " +
                        COLUMN_CATEGORY + " TEXT NOT NULL, " +
                        COLUMN_PRIORITY + " TEXT NOT NULL, " +
                        COLUMN_DUE_DATE + " TEXT NOT NULL, " +
                        COLUMN_BUDGET + " REAL NOT NULL DEFAULT 0.0, " +
                        COLUMN_IS_COMPLETED + " INTEGER NOT NULL DEFAULT 0, " +
                        COLUMN_CREATED_AT + " TEXT NOT NULL" +
                        ");";

        // Query DROP TABLE
        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}
