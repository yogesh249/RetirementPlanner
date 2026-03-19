package com.retirement.planner;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.NumberFormat;
import java.util.Locale;

public class NetWorthHistoryActivity extends AppCompatActivity {

    public static final String PREFS_HISTORY = "NetWorthHistory";
    public static final String KEY_HISTORY   = "history_json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_networth_history);

        loadHistory();

        findViewById(R.id.btnDeleteAllHistory).setOnClickListener(v -> confirmDeleteAll());
    }

    private void loadHistory() {
        LinearLayout container = findViewById(R.id.containerHistory);
        TextView tvEmpty       = findViewById(R.id.tvEmptyHistory);
        TextView tvCount       = findViewById(R.id.tvSnapshotCount);
        TextView tvFirstDate   = findViewById(R.id.tvFirstDate);
        TextView tvOverall     = findViewById(R.id.tvOverallChange);

        container.removeAllViews();

        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_HISTORY, MODE_PRIVATE);
            String json = prefs.getString(KEY_HISTORY, "[]");
            JSONArray arr = new JSONArray(json);

            if (arr.length() == 0) {
                tvEmpty.setVisibility(View.VISIBLE);
                container.setVisibility(View.GONE);
                tvCount.setText("0");
                tvFirstDate.setText("—");
                tvOverall.setText("—");
                return;
            }

            tvEmpty.setVisibility(View.GONE);
            container.setVisibility(View.VISIBLE);
            tvCount.setText(String.valueOf(arr.length()));
            tvFirstDate.setText(arr.getJSONObject(0).getString("date").split(",")[0]);

            // Overall change from first to last
            double firstNW = arr.getJSONObject(0).getDouble("netWorth");
            double lastNW  = arr.getJSONObject(arr.length() - 1).getDouble("netWorth");
            double change  = lastNW - firstNW;
            String changeStr = (change >= 0 ? "+" : "") + formatShort(change);
            tvOverall.setText(changeStr);
            tvOverall.setTextColor(change >= 0 ?
                    Color.parseColor("#059669") : Color.parseColor("#DC2626"));

            // Render items newest first
            for (int i = arr.length() - 1; i >= 0; i--) {
                JSONObject entry = arr.getJSONObject(i);
                double netWorth    = entry.getDouble("netWorth");
                double investments = entry.getDouble("investments");
                double properties  = entry.getDouble("properties");
                double liabilities = entry.getDouble("liabilities");
                String date        = entry.getString("date");

                // Calculate change vs previous snapshot
                double prevNW = i > 0 ? arr.getJSONObject(i - 1).getDouble("netWorth") : netWorth;
                double diff   = netWorth - prevNW;
                boolean isFirst = (i == 0);

                View itemView = LayoutInflater.from(this)
                        .inflate(R.layout.item_networth_history, container, false);

                ((TextView) itemView.findViewById(R.id.tvHistoryDate)).setText(date);
                ((TextView) itemView.findViewById(R.id.tvHistoryNetWorth)).setText(formatCurrency(netWorth));
                ((TextView) itemView.findViewById(R.id.tvHistoryNetWorth))
                        .setTextColor(netWorth >= 0 ?
                                Color.parseColor("#059669") : Color.parseColor("#DC2626"));
                ((TextView) itemView.findViewById(R.id.tvHistoryInvestments)).setText(formatShort(investments));
                ((TextView) itemView.findViewById(R.id.tvHistoryProperties)).setText(formatShort(properties));
                ((TextView) itemView.findViewById(R.id.tvHistoryLiabilities)).setText(formatShort(liabilities));

                TextView tvTrend = itemView.findViewById(R.id.tvHistoryTrend);
                if (isFirst) {
                    tvTrend.setText("FIRST");
                    tvTrend.setBackgroundColor(Color.parseColor("#6B7280"));
                } else if (diff > 0) {
                    tvTrend.setText("▲ +" + formatShort(diff));
                    tvTrend.setBackgroundColor(Color.parseColor("#059669"));
                } else if (diff < 0) {
                    tvTrend.setText("▼ " + formatShort(diff));
                    tvTrend.setBackgroundColor(Color.parseColor("#DC2626"));
                } else {
                    tvTrend.setText("— No change");
                    tvTrend.setBackgroundColor(Color.parseColor("#6B7280"));
                }

                container.addView(itemView);
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error loading history", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDeleteAll() {
        new AlertDialog.Builder(this)
            .setTitle("Delete All History")
            .setMessage("This will permanently delete all net worth snapshots. Are you sure?")
            .setPositiveButton("Delete All", (d, w) -> {
                getSharedPreferences(PREFS_HISTORY, MODE_PRIVATE).edit()
                        .remove(KEY_HISTORY).apply();
                loadHistory();
                Toast.makeText(this, "History deleted", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    // ── Static helper — called from PortfolioFragment to save a snapshot ─────

    public static void saveSnapshot(android.content.Context ctx,
                                     double netWorth, double investments,
                                     double properties, double liabilities) {
        try {
            SharedPreferences prefs = ctx.getSharedPreferences(PREFS_HISTORY, MODE_PRIVATE);
            String existing = prefs.getString(KEY_HISTORY, "[]");
            JSONArray arr = new JSONArray(existing);

            JSONObject entry = new JSONObject();
            String date = new java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a",
                    java.util.Locale.getDefault()).format(new java.util.Date());
            entry.put("date",        date);
            entry.put("netWorth",    netWorth);
            entry.put("investments", investments);
            entry.put("properties",  properties);
            entry.put("liabilities", liabilities);

            arr.put(entry);
            prefs.edit().putString(KEY_HISTORY, arr.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Static helper — returns full history JSON string for backup ───────────

    public static String getHistoryJson(android.content.Context ctx) {
        return ctx.getSharedPreferences(PREFS_HISTORY, MODE_PRIVATE)
                .getString(KEY_HISTORY, "[]");
    }

    public static void restoreHistoryJson(android.content.Context ctx, String json) {
        ctx.getSharedPreferences(PREFS_HISTORY, MODE_PRIVATE)
                .edit().putString(KEY_HISTORY, json).apply();
    }

    private String formatCurrency(double val) {
        if (val >= 10_000_000)  return String.format("₹%.2f Cr", val / 10_000_000);
        if (val <= -10_000_000) return String.format("-₹%.2f Cr", Math.abs(val) / 10_000_000);
        if (val >= 100_000)     return String.format("₹%.2f L", val / 100_000);
        if (val <= -100_000)    return String.format("-₹%.2f L", Math.abs(val) / 100_000);
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("en", "IN"));
        return (val < 0 ? "-₹" : "₹") + nf.format((long) Math.abs(val));
    }

    private String formatShort(double val) {
        boolean neg = val < 0;
        double abs = Math.abs(val);
        String s;
        if (abs >= 10_000_000)      s = String.format("%.1fCr", abs / 10_000_000);
        else if (abs >= 100_000)    s = String.format("%.1fL",  abs / 100_000);
        else if (abs >= 1_000)      s = String.format("%.1fK",  abs / 1_000);
        else                        s = String.format("%.0f",   abs);
        return (neg ? "-₹" : "₹") + s;
    }
}
