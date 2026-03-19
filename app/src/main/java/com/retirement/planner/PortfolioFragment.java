package com.retirement.planner;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PortfolioFragment extends Fragment {

    private static final String PREFS_NAME     = "RetirementPlannerData";
    private static final String KEY_SAVED_DATE = "saved_date";
    private static final String KEY_DOB            = "dob";
    private static final String KEY_RETIREMENT_AGE = "retirement_age";
    private static final String KEY_TARGET         = "target_corpus";
    private static final String KEY_RATE_HUF_BANK   = "rate_huf_bank";
    private static final String KEY_RATE_HUF_STOCKS = "rate_huf_stocks";
    private static final String KEY_RATE_HUF_MF     = "rate_huf_mf";

    private EditText etDOB, etRetirementAge, etTargetCorpus;

    // HUF fixed rows
    private EditText etHUFBank, etHUFStocks, etHUFMF;
    private EditText etRateHUFBank, etRateHUFStocks, etRateHUFMF;

    private LinearLayout bannerLastSaved;
    private TextView tvLastSaved;

    // Dynamic containers
    private LinearLayout containerAssets, containerDebt, containerProperties;
    private TextView tvInvestmentsTotal, tvDebtTotal, tvPropertyTotal;
    private TextView tvNWInvestments, tvNWProperties, tvNWLiabilities, tvNetWorth, tvNetWorthNote;

    // Default individual asset rows: name, corpus, rate
    private static final String[][] DEFAULT_ASSETS = {
        {"NPS",          "10000", "10"},
        {"PPF",          "10000", "8"},
        {"EPF",          "10000", "8"},
        {"Mutual Funds", "10000", "12"},
        {"Stocks",       "10000", "12"},
        {"Savings Bank", "10000", "4"},
        {"Crypto/Bitcoin","10000","20"},
    };

    private static final String[][] DEFAULT_DEBT = {
        {"Home Loan",    "0"},
        {"Car Loan",     "0"},
        {"Credit Card 1","0"},
        {"Credit Card 2","0"},
    };

    private static final String[][] DEFAULT_PROPERTIES = {
        {"Property 1", "0"},
        {"Property 2", "0"},
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_portfolio, container, false);
        initViews(v);
        setHUFLabels(v);
        loadSavedData();
        setupDatePicker();

        v.findViewById(R.id.btnCalculate).setOnClickListener(btn -> {
            if (validateInputs()) { saveData(); launchResult(); }
        });
        v.findViewById(R.id.btnClearData).setOnClickListener(btn -> clearData());
        v.findViewById(R.id.btnAddAsset).setOnClickListener(btn -> addAssetRow("", "", "", true));
        v.findViewById(R.id.btnAddDebt).setOnClickListener(btn -> addDebtRow("", "0", true));
        v.findViewById(R.id.btnAddProperty).setOnClickListener(btn -> addPropertyRow("", "0", true));
        v.findViewById(R.id.btnSaveSnapshot).setOnClickListener(btn -> saveSnapshot());
        v.findViewById(R.id.btnViewHistory).setOnClickListener(btn ->
            startActivity(new Intent(requireContext(), NetWorthHistoryActivity.class)));
        v.findViewById(R.id.btnBackup).setOnClickListener(btn ->
            startActivity(new Intent(requireContext(), BackupRestoreActivity.class)));
        return v;
    }

    private void initViews(View v) {
        etDOB           = v.findViewById(R.id.etDOB);
        etRetirementAge = v.findViewById(R.id.etRetirementAge);
        etTargetCorpus  = v.findViewById(R.id.etTargetCorpus);
        bannerLastSaved = v.findViewById(R.id.bannerLastSaved);
        tvLastSaved     = v.findViewById(R.id.tvLastSaved);

        // HUF fixed rows
        View rowHUFBank   = v.findViewById(R.id.rowHUFBank);
        View rowHUFStocks = v.findViewById(R.id.rowHUFStocks);
        View rowHUFMF     = v.findViewById(R.id.rowHUFMF);
        etHUFBank     = rowHUFBank.findViewById(R.id.etCorpus);
        etHUFStocks   = rowHUFStocks.findViewById(R.id.etCorpus);
        etHUFMF       = rowHUFMF.findViewById(R.id.etCorpus);
        etRateHUFBank   = rowHUFBank.findViewById(R.id.etRate);
        etRateHUFStocks = rowHUFStocks.findViewById(R.id.etRate);
        etRateHUFMF     = rowHUFMF.findViewById(R.id.etRate);

        containerAssets     = v.findViewById(R.id.containerAssets);
        containerDebt       = v.findViewById(R.id.containerProperties) != null ?
                              v.findViewById(R.id.containerDebt) : v.findViewById(R.id.containerDebt);
        containerDebt       = v.findViewById(R.id.containerDebt);
        containerProperties = v.findViewById(R.id.containerProperties);
        tvInvestmentsTotal  = v.findViewById(R.id.tvInvestmentsTotal);
        tvDebtTotal         = v.findViewById(R.id.tvDebtTotal);
        tvPropertyTotal     = v.findViewById(R.id.tvPropertyTotal);
        tvNWInvestments     = v.findViewById(R.id.tvNWInvestments);
        tvNWProperties      = v.findViewById(R.id.tvNWProperties);
        tvNWLiabilities     = v.findViewById(R.id.tvNWLiabilities);
        tvNetWorth          = v.findViewById(R.id.tvNetWorth);
        tvNetWorthNote      = v.findViewById(R.id.tvNetWorthNote);
    }

    private void setHUFLabels(View v) {
        setLabel(v, R.id.rowHUFBank,   "HUF\nBank");
        setLabel(v, R.id.rowHUFStocks, "HUF\nStocks");
        setLabel(v, R.id.rowHUFMF,     "HUF MF");
    }

    private void setLabel(View root, int rowId, String label) {
        View row = root.findViewById(rowId);
        if (row != null) ((TextView) row.findViewById(R.id.tvAssetLabel)).setText(label);
    }

    // ── DYNAMIC ASSET ROWS ────────────────────────────────────────────────────

    private void addAssetRow(String name, String corpus, String rate, boolean isNew) {
        View row = LayoutInflater.from(requireContext())
                .inflate(R.layout.portfolio_asset_row, containerAssets, false);

        EditText etName   = row.findViewById(R.id.etAssetName);
        EditText etCorpus = row.findViewById(R.id.etAssetCorpus);
        EditText etRate   = row.findViewById(R.id.etAssetRate);

        etName.setText(name);

        // Apply default value helper to corpus and rate
        DefaultValueHelper.attach(etCorpus, corpus.isEmpty() ? "10000" : corpus);
        DefaultValueHelper.attach(etRate,   rate.isEmpty()   ? "0"     : rate);

        // If loading saved data, set actual value in BLACK — not gray default
        if (!corpus.isEmpty()) {
            etCorpus.setText(corpus);
            etCorpus.setTextColor(android.graphics.Color.parseColor("#111827"));
        }
        if (!rate.isEmpty()) {
            etRate.setText(rate);
            etRate.setTextColor(android.graphics.Color.parseColor("#111827"));
        }

        TextWatcher tw = simpleWatcher();
        etName.addTextChangedListener(tw);
        etCorpus.addTextChangedListener(tw);
        etRate.addTextChangedListener(tw);

        row.findViewById(R.id.btnDeleteAsset).setOnClickListener(v -> {
            containerAssets.removeView(row);
            saveData();
            updateNetWorth();
        });

        containerAssets.addView(row);
        if (isNew) updateNetWorth();
    }

    // ── DEBT ROWS ─────────────────────────────────────────────────────────────

    private void addDebtRow(String name, String amount, boolean isNew) {
        View row = LayoutInflater.from(requireContext())
                .inflate(R.layout.debt_row, containerDebt, false);
        EditText etName   = row.findViewById(R.id.etDebtName);
        EditText etAmount = row.findViewById(R.id.etDebtAmount);

        etName.setText(name);

        etAmount.addTextChangedListener(new TextWatcher() {
            boolean editing = false;
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            public void onTextChanged(CharSequence s, int st, int b, int c) {}
            public void afterTextChanged(Editable s) {
                if (editing) return;
                editing = true;
                String text = s.toString().replace("-", "").trim();
                if (!text.isEmpty() && !text.equals(".")) {
                    etAmount.setText("-" + text);
                    etAmount.setSelection(etAmount.getText().length());
                }
                editing = false;
                saveData(); updateNetWorth();
            }
        });

        if (!amount.equals("0") && !amount.isEmpty()) {
            String val = amount.startsWith("-") ? amount : "-" + amount;
            etAmount.setText(val);
        }

        etName.addTextChangedListener(simpleWatcher());

        row.findViewById(R.id.btnDeleteDebt).setOnClickListener(v -> {
            containerDebt.removeView(row);
            saveData(); updateNetWorth();
        });

        containerDebt.addView(row);
        if (isNew) updateNetWorth();
    }

    // ── PROPERTY ROWS ─────────────────────────────────────────────────────────

    private void addPropertyRow(String name, String value, boolean isNew) {
        View row = LayoutInflater.from(requireContext())
                .inflate(R.layout.property_row, containerProperties, false);
        EditText etName  = row.findViewById(R.id.etPropertyName);
        EditText etValue = row.findViewById(R.id.etPropertyValue);

        etName.setText(name);
        etValue.setText(value.equals("0") ? "" : value);

        TextWatcher tw = simpleWatcher();
        etName.addTextChangedListener(tw);
        etValue.addTextChangedListener(tw);

        row.findViewById(R.id.btnDeleteProperty).setOnClickListener(v -> {
            containerProperties.removeView(row);
            saveData(); updateNetWorth();
        });

        containerProperties.addView(row);
        if (isNew) updateNetWorth();
    }

    // ── SNAPSHOT ──────────────────────────────────────────────────────────────

    private void saveSnapshot() {
        double investments = sumAssets();
        double hufTotal = getDbl(etHUFBank,10000) + getDbl(etHUFStocks,10000) + getDbl(etHUFMF,10000);
        double totalDebt = sumDebt();
        double totalProperty = sumProperties();
        double netWorth = investments + hufTotal + totalProperty + totalDebt;

        saveData();
        NetWorthHistoryActivity.saveSnapshot(requireContext(), netWorth,
                investments + hufTotal, totalProperty, totalDebt);
        Toast.makeText(requireContext(),
                "✅ Snapshot saved! (" + formatCurrency(netWorth) + ")",
                Toast.LENGTH_LONG).show();
    }

    // ── NET WORTH ─────────────────────────────────────────────────────────────

    private void updateNetWorth() {
        double investments   = sumAssets();
        double hufTotal      = getDbl(etHUFBank,10000) + getDbl(etHUFStocks,10000) + getDbl(etHUFMF,10000);
        double totalInvest   = investments + hufTotal;
        double totalDebt     = sumDebt();
        double totalProperty = sumProperties();
        double netWorth      = totalInvest + totalProperty + totalDebt;

        tvInvestmentsTotal.setText(formatCurrency(totalInvest));
        tvDebtTotal.setText(formatCurrency(totalDebt));
        tvPropertyTotal.setText(formatCurrency(totalProperty));
        tvNWInvestments.setText(formatCurrency(totalInvest));
        tvNWProperties.setText(formatCurrency(totalProperty));
        tvNWLiabilities.setText(formatCurrency(totalDebt));
        tvNetWorth.setText(formatCurrency(netWorth));
        tvNetWorth.setTextColor(netWorth >= 0 ?
                Color.parseColor("#4ADE80") : Color.parseColor("#FCA5A5"));
        tvNetWorthNote.setText(String.format("Investments %s + Properties %s − Liabilities %s",
                formatShort(totalInvest), formatShort(totalProperty), formatShort(Math.abs(totalDebt))));
    }

    private double sumAssets() {
        double total = 0;
        for (int i = 0; i < containerAssets.getChildCount(); i++) {
            View row = containerAssets.getChildAt(i);
            total += DefaultValueHelper.getDouble(row.findViewById(R.id.etAssetCorpus), 10000);
        }
        return total;
    }

    private double sumDebt() {
        double total = 0;
        for (int i = 0; i < containerDebt.getChildCount(); i++) {
            try {
                String s = ((EditText) containerDebt.getChildAt(i)
                        .findViewById(R.id.etDebtAmount)).getText().toString().trim();
                if (!s.isEmpty()) total += Double.parseDouble(s);
            } catch (Exception ignored) {}
        }
        return total;
    }

    private double sumProperties() {
        double total = 0;
        for (int i = 0; i < containerProperties.getChildCount(); i++) {
            try {
                String s = ((EditText) containerProperties.getChildAt(i)
                        .findViewById(R.id.etPropertyValue)).getText().toString().trim();
                if (!s.isEmpty()) total += Double.parseDouble(s);
            } catch (Exception ignored) {}
        }
        return total;
    }

    // ── SAVE / LOAD ───────────────────────────────────────────────────────────

    private void saveData() {
        SharedPreferences.Editor e = requireContext()
                .getSharedPreferences(PREFS_NAME, 0).edit();
        e.putString(KEY_SAVED_DATE, new SimpleDateFormat(
                "dd MMM yyyy, hh:mm a", Locale.getDefault()).format(new Date()));
        e.putString(KEY_DOB, etDOB.getText().toString().trim());
        e.putString(KEY_RETIREMENT_AGE, DefaultValueHelper.getString(etRetirementAge));
        e.putString(KEY_TARGET,         DefaultValueHelper.getString(etTargetCorpus));

        // HUF fixed rows
        e.putString("huf_bank",   DefaultValueHelper.getString(etHUFBank));
        e.putString("huf_stocks", DefaultValueHelper.getString(etHUFStocks));
        e.putString("huf_mf",     DefaultValueHelper.getString(etHUFMF));
        e.putString(KEY_RATE_HUF_BANK,   DefaultValueHelper.getString(etRateHUFBank));
        e.putString(KEY_RATE_HUF_STOCKS, DefaultValueHelper.getString(etRateHUFStocks));
        e.putString(KEY_RATE_HUF_MF,     DefaultValueHelper.getString(etRateHUFMF));

        // Dynamic assets — always save actual displayed text (even if gray default)
        int ac = containerAssets.getChildCount();
        e.putInt("asset_count", ac);
        for (int i = 0; i < ac; i++) {
            View row = containerAssets.getChildAt(i);
            EditText etC = row.findViewById(R.id.etAssetCorpus);
            EditText etR = row.findViewById(R.id.etAssetRate);
            e.putString("asset_name_" + i,   ((EditText) row.findViewById(R.id.etAssetName)).getText().toString());
            e.putString("asset_corpus_" + i, etC.getText().toString().trim());
            e.putString("asset_rate_" + i,   etR.getText().toString().trim());
        }

        // Debt rows
        int dc = containerDebt.getChildCount();
        e.putInt("debt_count", dc);
        for (int i = 0; i < dc; i++) {
            View row = containerDebt.getChildAt(i);
            e.putString("debt_name_" + i,   ((EditText) row.findViewById(R.id.etDebtName)).getText().toString());
            e.putString("debt_amount_" + i, ((EditText) row.findViewById(R.id.etDebtAmount)).getText().toString());
        }

        // Property rows
        int pc = containerProperties.getChildCount();
        e.putInt("prop_count", pc);
        for (int i = 0; i < pc; i++) {
            View row = containerProperties.getChildAt(i);
            e.putString("prop_name_" + i,  ((EditText) row.findViewById(R.id.etPropertyName)).getText().toString());
            e.putString("prop_value_" + i, ((EditText) row.findViewById(R.id.etPropertyValue)).getText().toString());
        }
        e.apply();
    }

    private void loadSavedData() {
        SharedPreferences p = requireContext().getSharedPreferences(PREFS_NAME, 0);
        String savedDate = p.getString(KEY_SAVED_DATE, null);

        if (savedDate == null) {
            attachHUFDefaults();
            loadDefaultRows();
            return;
        }

        bannerLastSaved.setVisibility(View.VISIBLE);
        tvLastSaved.setText("Data last saved on " + savedDate);

        etDOB.setText(p.getString(KEY_DOB, ""));
        setUserText(etRetirementAge,  p.getString(KEY_RETIREMENT_AGE, ""));
        setUserText(etTargetCorpus,   p.getString(KEY_TARGET, ""));
        setUserText(etHUFBank,        p.getString("huf_bank", ""));
        setUserText(etHUFStocks,      p.getString("huf_stocks", ""));
        setUserText(etHUFMF,          p.getString("huf_mf", ""));
        setUserText(etRateHUFBank,    p.getString(KEY_RATE_HUF_BANK, ""));
        setUserText(etRateHUFStocks,  p.getString(KEY_RATE_HUF_STOCKS, ""));
        setUserText(etRateHUFMF,      p.getString(KEY_RATE_HUF_MF, ""));
        attachHUFDefaults();

        // Load dynamic assets — migrate gracefully from old fixed-row version
        int ac = p.getInt("asset_count", -1);
        if (ac == -1) {
            // Old install: no dynamic assets saved — load defaults for assets only
            for (String[] a : DEFAULT_ASSETS) addAssetRow(a[0], a[1], a[2], false);
        } else {
            for (int i = 0; i < ac; i++)
                addAssetRow(p.getString("asset_name_" + i, ""),
                        p.getString("asset_corpus_" + i, ""),
                        p.getString("asset_rate_" + i, ""), false);
        }
        // Always load debt and properties independently
        int dc = p.getInt("debt_count", -1);
        if (dc == -1) {
            for (String[] d : DEFAULT_DEBT) addDebtRow(d[0], d[1], false);
        } else {
            for (int i = 0; i < dc; i++)
                addDebtRow(p.getString("debt_name_" + i, ""),
                        p.getString("debt_amount_" + i, "0"), false);
        }
        // Load properties independently
        int pc = p.getInt("prop_count", -1);
        if (pc == -1) {
            for (String[] pr : DEFAULT_PROPERTIES) addPropertyRow(pr[0], pr[1], false);
        } else {
            for (int i = 0; i < pc; i++)
                addPropertyRow(p.getString("prop_name_" + i, ""),
                        p.getString("prop_value_" + i, "0"), false);
        }
        updateNetWorth();
    }

    private void loadDefaultRows() {
        for (String[] a : DEFAULT_ASSETS)     addAssetRow(a[0], a[1], a[2], false);
        for (String[] d : DEFAULT_DEBT)       addDebtRow(d[0], d[1], false);
        for (String[] p : DEFAULT_PROPERTIES) addPropertyRow(p[0], p[1], false);
        updateNetWorth();
    }

    private void attachHUFDefaults() {
        DefaultValueHelper.attach(etRetirementAge, "60");
        DefaultValueHelper.attach(etTargetCorpus,  "50000000");
        DefaultValueHelper.attach(etHUFBank,     "10000");
        DefaultValueHelper.attach(etHUFStocks,   "10000");
        DefaultValueHelper.attach(etHUFMF,       "10000");
        DefaultValueHelper.attach(etRateHUFBank,   "4");
        DefaultValueHelper.attach(etRateHUFStocks, "12");
        DefaultValueHelper.attach(etRateHUFMF,     "12");
    }

    private void clearData() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Clear Saved Data")
            .setMessage("This will erase all saved values. Are you sure?")
            .setPositiveButton("Clear", (d, w) -> {
                requireContext().getSharedPreferences(PREFS_NAME, 0).edit().clear().apply();
                etDOB.setText(""); etRetirementAge.setText(""); etTargetCorpus.setText("");
                etHUFBank.setText(""); etHUFStocks.setText(""); etHUFMF.setText("");
                containerAssets.removeAllViews();
                containerDebt.removeAllViews();
                containerProperties.removeAllViews();
                attachHUFDefaults();
                loadDefaultRows();
                bannerLastSaved.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Saved data cleared", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null).show();
    }

    private void setupDatePicker() {
        etDOB.setFocusable(false); etDOB.setClickable(true);
        etDOB.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (view, y, m, d) ->
                etDOB.setText(String.format("%02d/%02d/%04d", d, m + 1, y)),
                cal.get(Calendar.YEAR) - 30, cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)) {{
                getDatePicker().setMaxDate(System.currentTimeMillis());
            }}.show();
        });
    }

    // ── LAUNCH RESULT ─────────────────────────────────────────────────────────

    private boolean validateInputs() {
        if (etDOB.getText().toString().trim().isEmpty()) {
            Toast.makeText(requireContext(), "Please enter your date of birth", Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            String[] parts = etDOB.getText().toString().trim().split("/");
            Calendar dobCal = Calendar.getInstance();
            dobCal.set(Integer.parseInt(parts[2]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[0]));
            Calendar now = Calendar.getInstance();
            int age = now.get(Calendar.YEAR) - dobCal.get(Calendar.YEAR);
            if (now.get(Calendar.DAY_OF_YEAR) < dobCal.get(Calendar.DAY_OF_YEAR)) age--;
            if (age >= DefaultValueHelper.getInt(etRetirementAge, 60)) {
                Toast.makeText(requireContext(), "You have already reached retirement age!", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Invalid date. Use DD/MM/YYYY", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void launchResult() {
        Intent i = new Intent(requireContext(), ResultActivity.class);
        i.putExtra("dob",           etDOB.getText().toString().trim());
        i.putExtra("retirementAge", DefaultValueHelper.getInt(etRetirementAge, 60));
        i.putExtra("targetCorpus",  DefaultValueHelper.getDouble(etTargetCorpus, 50000000));

        // --- Pass every dynamic asset row exactly as-is, no bucketing ---
        int count = containerAssets.getChildCount();
        // Add HUF rows at the end as extra entries
        int totalCount = count + 3;
        String[] names   = new String[totalCount];
        double[] corpora = new double[totalCount];
        double[] rates   = new double[totalCount];

        for (int idx = 0; idx < count; idx++) {
            View row = containerAssets.getChildAt(idx);
            names[idx]   = ((android.widget.EditText) row.findViewById(R.id.etAssetName)).getText().toString().trim();
            corpora[idx] = DefaultValueHelper.getDouble(row.findViewById(R.id.etAssetCorpus), 10000);
            rates[idx]   = DefaultValueHelper.getDouble(row.findViewById(R.id.etAssetRate), 0);
        }
        // Append fixed HUF rows
        names[count]     = "HUF Bank";
        corpora[count]   = DefaultValueHelper.getDouble(etHUFBank, 10000);
        rates[count]     = DefaultValueHelper.getDouble(etRateHUFBank, 4);
        names[count+1]   = "HUF Stocks";
        corpora[count+1] = DefaultValueHelper.getDouble(etHUFStocks, 10000);
        rates[count+1]   = DefaultValueHelper.getDouble(etRateHUFStocks, 12);
        names[count+2]   = "HUF MF";
        corpora[count+2] = DefaultValueHelper.getDouble(etHUFMF, 10000);
        rates[count+2]   = DefaultValueHelper.getDouble(etRateHUFMF, 12);

        i.putExtra("assetCount",   totalCount);
        i.putExtra("assetNames",   names);
        i.putExtra("assetCorpora", corpora);
        i.putExtra("assetRates",   rates);

        startActivity(i);
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    /** Sets text in black — marks it as user-entered, not gray default */
    private void setUserText(EditText et, String value) {
        if (value != null && !value.isEmpty()) {
            et.setText(value);
            et.setTextColor(android.graphics.Color.parseColor("#111827"));
        }
    }

    private TextWatcher simpleWatcher() {
        return new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            public void onTextChanged(CharSequence s, int st, int b, int c) {}
            public void afterTextChanged(Editable s) { saveData(); updateNetWorth(); }
        };
    }

    private double getDbl(EditText et, double def) {
        return DefaultValueHelper.getDouble(et, def);
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
        double abs = Math.abs(val);
        String s;
        if (abs >= 10_000_000)   s = String.format("%.1fCr", abs / 10_000_000);
        else if (abs >= 100_000) s = String.format("%.1fL",  abs / 100_000);
        else if (abs >= 1_000)   s = String.format("%.1fK",  abs / 1_000);
        else                     s = String.format("%.0f",   abs);
        return (val < 0 ? "-₹" : "₹") + s;
    }
}
