package com.retirement.planner;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent intent = getIntent();

        String dob          = intent.getStringExtra("dob");
        int retirementAge   = intent.getIntExtra("retirementAge", 60);
        double targetCorpus = intent.getDoubleExtra("targetCorpus", 0);

        // ── Read flat asset arrays passed from PortfolioFragment ──────────────
        int assetCount     = intent.getIntExtra("assetCount", 0);
        String[] names     = intent.getStringArrayExtra("assetNames");
        double[] corpora   = intent.getDoubleArrayExtra("assetCorpora");
        double[] rates     = intent.getDoubleArrayExtra("assetRates");

        // Safeguard
        if (names == null || corpora == null || rates == null || assetCount == 0) {
            ((TextView) findViewById(R.id.tvHeaderSubtitle)).setText("No asset data found");
            return;
        }

        int years = calculateYearsToRetirement(dob, retirementAge);

        // ── Project each asset individually ───────────────────────────────────
        double[] projected = new double[assetCount];
        double totalCurrent   = 0;
        double totalProjected = 0;

        for (int i = 0; i < assetCount; i++) {
            double rate = rates[i] / 100.0;
            projected[i]   = fv(corpora[i], rate, years);
            totalCurrent   += corpora[i];
            totalProjected += projected[i];
        }

        double gap = targetCorpus - totalProjected;

        // ── SIP rate: always 12% equity — that's what the SIP is invested into ──
        double sipAnnualRate  = 0.12;
        double monthlyRate    = sipAnnualRate / 12.0;
        int months = years * 12;
        double monthlySIP = 0;
        if (gap > 0 && months > 0) {
            monthlySIP = gap * monthlyRate / (Math.pow(1 + monthlyRate, months) - 1);
        }

        // ── Debt SIP alternative: fixed 8% (PPF/EPF equivalent) ─────────────
        double debtMonthlyRate = 0.08 / 12.0;
        double debtSIP = 0;
        if (gap > 0 && months > 0) {
            debtSIP = gap * debtMonthlyRate / (Math.pow(1 + debtMonthlyRate, months) - 1);
        }
        double avgRate = sipAnnualRate; // used only for footnote display

        // ── Display ───────────────────────────────────────────────────────────
        ((TextView) findViewById(R.id.tvHeaderSubtitle))
                .setText(years + " years to retire at age " + retirementAge);
        ((TextView) findViewById(R.id.tvYearsLeft)).setText(years + " years");
        ((TextView) findViewById(R.id.tvTargetAdjusted)).setText(formatCurrency(targetCorpus));
        ((TextView) findViewById(R.id.tvCurrentPortfolio)).setText(formatCurrency(totalCurrent));
        ((TextView) findViewById(R.id.tvProjectedValue)).setText(formatCurrency(totalProjected));

        TextView tvSIP    = findViewById(R.id.tvMonthlySIP);
        TextView tvGap    = findViewById(R.id.tvGap);
        TextView tvDebtSIP = findViewById(R.id.tvSIPDebt);

        if (gap <= 0) {
            tvSIP.setText("₹0");
            tvGap.setText("No gap! You're on track 🎉");
            tvDebtSIP.setText("₹0 / month");
        } else {
            tvSIP.setText(formatCurrency(monthlySIP));
            tvGap.setText(formatCurrency(gap));
            tvDebtSIP.setText(formatCurrency(debtSIP) + " / month");
        }

        TextView tvSIPNote = findViewById(R.id.tvSIPRateNote);
        if (tvSIPNote != null) {
            tvSIPNote.setText(String.format(
                    "Monthly SIP invested in equity at 12%% p.a."));
        }

        // ── Breakdown table — one row per asset, exactly as entered ──────────
        LinearLayout tableBody = findViewById(R.id.tableBody);
        tableBody.removeAllViews();

        for (int i = 0; i < assetCount; i++) {
            addTableRow(tableBody, names[i], rates[i], corpora[i], projected[i], false, i);
        }

        // Divider
        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
        divider.setBackgroundColor(Color.parseColor("#D1D5DB"));
        tableBody.addView(divider);

        addTableRow(tableBody, "TOTAL", -1, totalCurrent, totalProjected, true, -1);

        findViewById(R.id.btnRecalculate).setOnClickListener(v -> finish());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private int calculateYearsToRetirement(String dob, int retirementAge) {
        try {
            String[] p = dob.split("/");
            Calendar dobCal = Calendar.getInstance();
            dobCal.set(Integer.parseInt(p[2]), Integer.parseInt(p[1]) - 1, Integer.parseInt(p[0]));
            Calendar now = Calendar.getInstance();
            int age = now.get(Calendar.YEAR) - dobCal.get(Calendar.YEAR);
            if (now.get(Calendar.DAY_OF_YEAR) < dobCal.get(Calendar.DAY_OF_YEAR)) age--;
            return Math.max(0, retirementAge - age);
        } catch (Exception e) { return 20; }
    }

    private double fv(double pv, double annualRate, int years) {
        if (pv == 0) return 0;
        return pv * Math.pow(1 + annualRate, years);
    }

    private void addTableRow(LinearLayout parent, String asset, double ratePct,
                              double current, double projected, boolean isTotalRow, int index) {
        View row = LayoutInflater.from(this).inflate(R.layout.table_row_breakdown, parent, false);

        TextView tvAsset    = row.findViewById(R.id.tvRowAsset);
        TextView tvRate     = row.findViewById(R.id.tvRowRate);
        TextView tvNow      = row.findViewById(R.id.tvRowNow);
        TextView tvAtRetire = row.findViewById(R.id.tvRowAtRetire);

        tvAsset.setText(asset.isEmpty() ? "(unnamed)" : asset);
        tvRate.setText(ratePct >= 0 ? String.format("%.0f%%", ratePct) : "");
        tvNow.setText(formatShort(current));
        tvAtRetire.setText(formatShort(projected));

        if (isTotalRow) {
            tvAsset.setTypeface(null, Typeface.BOLD);
            tvNow.setTextColor(Color.parseColor("#111827"));
            tvAtRetire.setTextColor(Color.parseColor("#1E40AF"));
            row.setBackgroundColor(Color.parseColor("#EFF6FF"));
        } else if (index >= 0 && index % 2 == 0) {
            row.setBackgroundColor(Color.parseColor("#F9FAFB"));
        }

        parent.addView(row);
    }

    private String formatShort(double val) {
        if (val == 0) return "₹0";
        if (val >= 10_000_000) return String.format("₹%.1fCr", val / 10_000_000);
        if (val >= 100_000)    return String.format("₹%.1fL",  val / 100_000);
        if (val >= 1_000)      return String.format("₹%.1fK",  val / 1_000);
        return String.format("₹%.0f", val);
    }

    private String formatCurrency(double val) {
        if (val >= 10_000_000) return String.format("₹%.2f Cr", val / 10_000_000);
        if (val >= 100_000)    return String.format("₹%.2f L",  val / 100_000);
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("en", "IN"));
        return "₹" + nf.format((long) val);
    }
}
