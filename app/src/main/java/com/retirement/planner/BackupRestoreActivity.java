package com.retirement.planner;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class BackupRestoreActivity extends AppCompatActivity {

    private static final String PREFS_BACKUP  = "BackupMeta";
    private static final String KEY_LAST_BACKUP = "last_backup_date";

    private ActivityResultLauncher<Intent> exportLauncher;
    private ActivityResultLauncher<Intent> importLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);

        TextView tvLastBackup = findViewById(R.id.tvLastBackupDate);
        String lastBackup = getSharedPreferences(PREFS_BACKUP, MODE_PRIVATE)
                .getString(KEY_LAST_BACKUP, null);
        tvLastBackup.setText(lastBackup != null ?
                "Last backup: " + lastBackup : "No backup created yet");

        // Register export launcher
        exportLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) performExport(uri);
                }
            });

        // Register import launcher
        importLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) confirmImport(uri);
                }
            });

        findViewById(R.id.btnExport).setOnClickListener(v -> launchExport());
        findViewById(R.id.btnImport).setOnClickListener(v -> launchImport());
    }

    // ── EXPORT ───────────────────────────────────────────────────────────────

    private void launchExport() {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(new Date());
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, "RetirementPlanner_Backup_" + timestamp + ".json");
        exportLauncher.launch(intent);
    }

    private void performExport(Uri uri) {
        try {
            JSONObject backup = buildBackupJson();
            OutputStream os = getContentResolver().openOutputStream(uri);
            if (os != null) {
                os.write(backup.toString(2).getBytes());
                os.close();
            }
            // Record backup date
            String now = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(new Date());
            getSharedPreferences(PREFS_BACKUP, MODE_PRIVATE).edit()
                    .putString(KEY_LAST_BACKUP, now).apply();
            ((TextView) findViewById(R.id.tvLastBackupDate)).setText("Last backup: " + now);
            Toast.makeText(this, "✅ Backup saved successfully!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ── IMPORT ───────────────────────────────────────────────────────────────

    private void launchImport() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        importLauncher.launch(intent);
    }

    private void confirmImport(Uri uri) {
        new AlertDialog.Builder(this)
            .setTitle("Restore Backup")
            .setMessage("This will replace ALL your current data with the backup file. This cannot be undone. Are you sure?")
            .setPositiveButton("Restore", (d, w) -> performImport(uri))
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void performImport(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            if (is == null) throw new Exception("Cannot open file");

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            is.close();

            JSONObject backup = new JSONObject(sb.toString());

            // Validate it's our backup format
            if (!backup.has("portfolioData") || !backup.has("version")) {
                Toast.makeText(this, "Invalid backup file format", Toast.LENGTH_LONG).show();
                return;
            }

            restorePortfolioData(backup.getJSONObject("portfolioData"));

            if (backup.has("historyData")) {
                NetWorthHistoryActivity.restoreHistoryJson(this, backup.getString("historyData"));
            }

            // Also restore expense data if present
            if (backup.has("expenseData")) {
                org.json.JSONObject expData = backup.getJSONObject("expenseData");
                android.content.SharedPreferences.Editor expEditor =
                        getSharedPreferences("ExpensePlannerData", MODE_PRIVATE).edit();
                expEditor.clear();
                java.util.Iterator<String> expKeys = expData.keys();
                while (expKeys.hasNext()) {
                    String key = expKeys.next();
                    Object val = expData.get(key);
                    if (val instanceof String)  expEditor.putString(key, (String) val);
                    else if (val instanceof Integer) expEditor.putInt(key, (Integer) val);
                    else if (val instanceof Boolean) expEditor.putBoolean(key, (Boolean) val);
                }
                expEditor.apply();
            }

            Toast.makeText(this, "✅ Data restored successfully!", Toast.LENGTH_SHORT).show();

            // Restart the app so all screens reload with restored data
            android.content.Intent intent = getPackageManager()
                    .getLaunchIntentForPackage(getPackageName());
            if (intent != null) {
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            finish();

        } catch (Exception e) {
            Toast.makeText(this, "Restore failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ── BUILD / RESTORE JSON ──────────────────────────────────────────────────

    private JSONObject buildBackupJson() throws Exception {
        JSONObject backup = new JSONObject();
        backup.put("version", 1);
        backup.put("appName", "RetirementPlanner");
        backup.put("exportDate", new SimpleDateFormat("dd MMM yyyy, hh:mm a",
                Locale.getDefault()).format(new Date()));

        // Portfolio SharedPreferences → JSON object
        SharedPreferences prefs = getSharedPreferences("RetirementPlannerData", MODE_PRIVATE);
        JSONObject portfolioData = new JSONObject();
        Map<String, ?> all = prefs.getAll();
        for (Map.Entry<String, ?> entry : all.entrySet()) {
            Object val = entry.getValue();
            if (val instanceof String)  portfolioData.put(entry.getKey(), (String) val);
            else if (val instanceof Integer) portfolioData.put(entry.getKey(), (int)(Integer) val);
            else if (val instanceof Boolean) portfolioData.put(entry.getKey(), (boolean)(Boolean) val);
            else if (val instanceof Long) portfolioData.put(entry.getKey(), (long)(Long) val);
        }
        backup.put("portfolioData", portfolioData);

        // Expense SharedPreferences
        SharedPreferences expPrefs = getSharedPreferences("ExpensePlannerData", MODE_PRIVATE);
        JSONObject expenseData = new JSONObject();
        for (Map.Entry<String, ?> entry : expPrefs.getAll().entrySet()) {
            Object val = entry.getValue();
            if (val instanceof String)  expenseData.put(entry.getKey(), (String) val);
            else if (val instanceof Integer) expenseData.put(entry.getKey(), (int)(Integer) val);
            else if (val instanceof Boolean) expenseData.put(entry.getKey(), (boolean)(Boolean) val);
        }
        backup.put("expenseData", expenseData);

        // Net worth history
        backup.put("historyData", NetWorthHistoryActivity.getHistoryJson(this));

        return backup;
    }

    private void restorePortfolioData(JSONObject data) throws Exception {
        SharedPreferences.Editor editor = getSharedPreferences("RetirementPlannerData", MODE_PRIVATE).edit();
        editor.clear();
        java.util.Iterator<String> keys = data.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object val = data.get(key);
            if (val instanceof String)  editor.putString(key, (String) val);
            else if (val instanceof Integer) editor.putInt(key, (Integer) val);
            else if (val instanceof Boolean) editor.putBoolean(key, (Boolean) val);
            else if (val instanceof Long) editor.putLong(key, (Long) val);
        }
        editor.apply();
    }
}
