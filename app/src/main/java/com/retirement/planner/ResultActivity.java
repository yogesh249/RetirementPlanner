package com.retirement.planner;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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

        // Personal
        String dob          = intent.getStringExtra("dob");
        int retirementAge   = intent.getIntExtra("retirementAge", 60);
        double targetCorpus = intent.getDoubleExtra("targetCorpus", 0);

        // Corpus values
        double nps         = intent.getDoubleExtra("nps", 0);
        double ppf         = intent.getDoubleExtra("ppf", 0);
        double epf         = intent.getDoubleExtra("epf", 0);
        double mf          = intent.getDoubleExtra("mf", 0);
        double stocks      = intent.getDoubleExtra("stocks", 0);
        double savingsBank = intent.getDoubleExtra("savingsBank", 0);
        double crypto      = intent.getDoubleExtra("crypto", 0);
        double hufBank     = intent.getDoubleExtra("hufBank", 0);
        double hufStocks   = intent.getDoubleExtra("hufStocks", 0);
        double hufMF       = intent.getDoubleExtra("hufMF", 0);

        // Growth rates (user-entered %, convert to decimal)
        double rNPS         = intent.getDoubleExtra("rateNPS", 10)         / 100.0;
        double rPPF         = intent.getDoubleExtra("ratePPF", 8)          / 100.0;
        double rEPF         = intent.getDoubleExtra("rateEPF", 8)          / 100.0;
        double rMF          = intent.getDoubleExtra("rateMF", 12)          / 100.0;
        double rStocks      = intent.getDoubleExtra("rateStocks", 12)      / 100.0;
        double rSavingsBank = intent.getDoubleExtra("rateSavingsBank", 4)  / 100.0;
        double rCrypto      = intent.getDoubleExtra("rateCrypto", 20)      / 100.0;
        double rHUFBank     = intent.getDoubleExtra("rateHUFBank", 4)      / 100.0;
        double rHUFStocks   = intent.getDoubleExtra("rateHUFStocks", 12)   / 100.0;
        double rHUFMF       = intent.getDoubleExtra("rateHUFMF", 12)       / 100.0;

        int years = calculateYearsToRetirement(dob, retirementAge);

        // Project each corpus
        double projNPS         = fv(nps,         rNPS,         years);
        double projPPF         = fv(ppf,         rPPF,         years);
        double projEPF         = fv(epf,         rEPF,         years);
        double projMF          = fv(mf,          rMF,          years);
        double projStocks      = fv(stocks,      rStocks,      years);
        double projSavingsBank = fv(savingsBank, rSavingsBank, years);
        double projCrypto      = fv(crypto,      rCrypto,      years);
        double projHUFBank     = fv(hufBank,     rHUFBank,     years);
        double projHUFStocks   = fv(hufStocks,   rHUFStocks,   years);
        double projHUFMF       = fv(hufMF,       rHUFMF,       years);

        double totalProjected = projNPS + projPPF + projEPF + projMF + projStocks
                + projSavingsBank + projCrypto + projHUFBank + projHUFStocks + projHUFMF;

        double gap = targetCorpus - totalProjected;

        // Monthly SIP at 12% equity (blended average of MF/Stocks rates for SIP assumption)
        double sipRate = (rMF + rStocks) / 2.0;
        double monthlyRate = sipRate / 12.0;
        int months = years * 12;
        double monthlySIP = 0;
        if (gap > 0 && months > 0 && monthlyRate > 0) {
            monthlySIP = gap * monthlyRate / (Math.pow(1 + monthlyRate, months) - 1);
        }

        // Equivalent debt SIP using blended PPF/EPF rate
        double debtRate = (rPPF + rEPF) / 2.0;
        double debtMonthlyRate = debtRate / 12.0;
        double debtSIP = 0;
        if (gap > 0 && months > 0 && debtMonthlyRate > 0) {
            debtSIP = gap * debtMonthlyRate / (Math.pow(1 + debtMonthlyRate, months) - 1);
        }

        double currentTotal = nps + ppf + epf + mf + stocks + savingsBank + crypto
                + hufBank + hufStocks + hufMF;

        displayResults(years, retirementAge, targetCorpus, currentTotal, totalProjected,
                gap, monthlySIP, debtSIP, sipRate * 100,
                nps, ppf, epf, mf, stocks, savingsBank, crypto, hufBank, hufStocks, hufMF,
                projNPS, projPPF, projEPF, projMF, projStocks, projSavingsBank, projCrypto,
                projHUFBank, projHUFStocks, projHUFMF,
                intent.getDoubleExtra("rateNPS", 10),
                intent.getDoubleExtra("ratePPF", 8),
                intent.getDoubleExtra("rateEPF", 8),
                intent.getDoubleExtra("rateMF", 12),
                intent.getDoubleExtra("rateStocks", 12),
                intent.getDoubleExtra("rateSavingsBank", 4),
                intent.getDoubleExtra("rateCrypto", 20),
                intent.getDoubleExtra("rateHUFBank", 4),
                intent.getDoubleExtra("rateHUFStocks", 12),
                intent.getDoubleExtra("rateHUFMF", 12));

        findViewById(R.id.btnRecalculate).setOnClickListener(v -> finish());
    }

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

    private void displayResults(int years, int retirementAge, double target,
                                 double currentTotal, double projected,
                                 double gap, double monthlySIP, double debtSIP, double sipRatePct,
                                 double nps, double ppf, double epf, double mf, double stocks,
                                 double savingsBank, double crypto, double hufBank, double hufStocks, double hufMF,
                                 double projNPS, double projPPF, double projEPF, double projMF, double projStocks,
                                 double projSavingsBank, double projCrypto,
                                 double projHUFBank, double projHUFStocks, double projHUFMF,
                                 double rNPS, double rPPF, double rEPF, double rMF, double rStocks,
                                 double rSavingsBank, double rCrypto, double rHUFBank, double rHUFStocks, double rHUFMF) {

        ((TextView) findViewById(R.id.tvHeaderSubtitle))
                .setText(years + " years to retire at age " + retirementAge);
        ((TextView) findViewById(R.id.tvYearsLeft)).setText(years + " years");
        ((TextView) findViewById(R.id.tvTargetAdjusted)).setText(formatCurrency(target));
        ((TextView) findViewById(R.id.tvCurrentPortfolio)).setText(formatCurrency(currentTotal));
        ((TextView) findViewById(R.id.tvProjectedValue)).setText(formatCurrency(projected));

        TextView tvSIP = findViewById(R.id.tvMonthlySIP);
        TextView tvGap = findViewById(R.id.tvGap);
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

        // SIP rate footnote
        TextView tvSIPNote = findViewById(R.id.tvSIPRateNote);
        if (tvSIPNote != null) {
            tvSIPNote.setText(String.format("(SIP assumed at blended %.1f%% equity rate)", sipRatePct));
        }

        // Breakdown table - inflate real rows
        LinearLayout tableBody = findViewById(R.id.tableBody);
        tableBody.removeAllViews();
        addTableRow(tableBody, "NPS",        rNPS,         nps,         projNPS,        false);
        addTableRow(tableBody, "PPF",        rPPF,         ppf,         projPPF,        false);
        addTableRow(tableBody, "EPF",        rEPF,         epf,         projEPF,        false);
        addTableRow(tableBody, "Mut. Funds", rMF,          mf,          projMF,         false);
        addTableRow(tableBody, "Stocks",     rStocks,      stocks,      projStocks,     false);
        addTableRow(tableBody, "Savings Bk", rSavingsBank, savingsBank, projSavingsBank,false);
        addTableRow(tableBody, "Crypto",     rCrypto,      crypto,      projCrypto,     false);
        addTableRow(tableBody, "HUF Bank",   rHUFBank,     hufBank,     projHUFBank,    false);
        addTableRow(tableBody, "HUF Stocks", rHUFStocks,   hufStocks,   projHUFStocks,  false);
        addTableRow(tableBody, "HUF MF",     rHUFMF,       hufMF,       projHUFMF,      false);
        // Divider
        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
        divider.setBackgroundColor(Color.parseColor("#D1D5DB"));
        tableBody.addView(divider);
        addTableRow(tableBody, "TOTAL", -1,
                nps+ppf+epf+mf+stocks+savingsBank+crypto+hufBank+hufStocks+hufMF,
                projNPS+projPPF+projEPF+projMF+projStocks+projSavingsBank+projCrypto+projHUFBank+projHUFStocks+projHUFMF,
                true);
    }

    private void addTableRow(LinearLayout parent, String asset, double ratePct,
                              double current, double projected, boolean isTotalRow) {
        View row = LayoutInflater.from(this).inflate(R.layout.table_row_breakdown, parent, false);

        TextView tvAsset    = row.findViewById(R.id.tvRowAsset);
        TextView tvRate     = row.findViewById(R.id.tvRowRate);
        TextView tvNow      = row.findViewById(R.id.tvRowNow);
        TextView tvAtRetire = row.findViewById(R.id.tvRowAtRetire);

        tvAsset.setText(asset);
        tvRate.setText(ratePct >= 0 ? String.format("%.0f%%", ratePct) : "");
        tvNow.setText(formatShort(current));
        tvAtRetire.setText(formatShort(projected));

        if (isTotalRow) {
            tvAsset.setTypeface(null, Typeface.BOLD);
            tvNow.setTextColor(Color.parseColor("#111827"));
            tvAtRetire.setTextColor(Color.parseColor("#1E40AF"));
            row.setBackgroundColor(Color.parseColor("#EFF6FF"));
        } else {
            // Alternate row shading
            int index = parent.getChildCount();
            if (index % 2 == 0) {
                row.setBackgroundColor(Color.parseColor("#F9FAFB"));
            }
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
