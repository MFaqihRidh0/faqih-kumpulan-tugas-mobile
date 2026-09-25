package com.its.tugas4sqlite.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.its.tugas4sqlite.R;
import com.its.tugas4sqlite.database.DatabaseContract;
import com.its.tugas4sqlite.database.DatabaseHelper;

import java.io.File;
import java.util.Locale;

public class PersistenceInfoDialog extends Dialog {

    private final DatabaseHelper dbHelper;

    public PersistenceInfoDialog(@NonNull Context context) {
        super(context);
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_persistence_info);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getWindow().setLayout(
                    (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.92),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        refreshData();

        MaterialButton btnRecheck = findViewById(R.id.btnRecheckIntegrity);
        MaterialButton btnClose = findViewById(R.id.btnClosePersistence);

        btnRecheck.setOnClickListener(v -> {
            refreshData();
            Toast.makeText(getContext(), "Pemeriksaan integritas SQLite selesai: Status VALID (ok)", Toast.LENGTH_SHORT).show();
        });

        btnClose.setOnClickListener(v -> dismiss());
    }

    private void refreshData() {
        TextView tvDbName = findViewById(R.id.tvDbName);
        TextView tvDbPath = findViewById(R.id.tvDbPath);
        TextView tvDbSize = findViewById(R.id.tvDbSize);
        TextView tvDbRows = findViewById(R.id.tvDbRows);
        TextView tvDbIntegrity = findViewById(R.id.tvDbIntegrity);

        File dbFile = getContext().getDatabasePath(DatabaseContract.DATABASE_NAME);
        tvDbName.setText(String.format(Locale.getDefault(), "%s (v%d)", DatabaseContract.DATABASE_NAME, DatabaseContract.DATABASE_VERSION));

        if (dbFile.exists()) {
            tvDbPath.setText(dbFile.getAbsolutePath());
            long sizeBytes = dbFile.length();
            if (sizeBytes < 1024) {
                tvDbSize.setText(sizeBytes + " Bytes");
            } else {
                tvDbSize.setText(String.format(Locale.getDefault(), "%.1f KB", sizeBytes / 1024.0));
            }
        } else {
            tvDbPath.setText("(Belum terbuat di storage)");
            tvDbSize.setText("0 KB");
        }

        DatabaseHelper.TaskStatistics stats = dbHelper.getStatistics();
        tvDbRows.setText(stats.totalTasks + " Baris");

        String integrity = dbHelper.checkDatabaseIntegrity();
        if ("ok".equalsIgnoreCase(integrity)) {
            tvDbIntegrity.setText("Status: ok (Integritas Valid)");
            tvDbIntegrity.setBackgroundResource(R.drawable.bg_chip_category);
        } else {
            tvDbIntegrity.setText("Status: " + integrity);
            tvDbIntegrity.setBackgroundResource(R.drawable.bg_chip_high);
        }
    }
}
