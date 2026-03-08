package com.retirement.planner;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // SharedPreferences file name
    private static final String PREFS_NAME = "RetirementPlannerData";

    // Keys for all fields
    private static final String KEY_SAVED_DATE    = "saved_date";
    private static final String KEY_DOB            = "dob";
    private static final String KEY_RETIREMENT_AGE = "retirement_age";
    private static final String KEY_TARGET         = "target_corpus";
    private static final String KEY_NPS            = "nps";
    private static final String KEY_PPF            = "ppf";
    private static final String KEY_EPF            = "epf";
    private static final String KEY_MF             = "mf";
    private static final String KEY_STOCKS         = "stocks";
    private static final String KEY_SAVINGS_BANK   = "savings_bank";
    private static final String KEY_CRYPTO         = "crypto";
    private static final String KEY_HUF_BANK       = "huf_bank";
    private static final String KEY_HUF_STOCKS     = "huf_stocks";
    private static final String KEY_HUF_MF         = "huf_mf";
    private static final String KEY_RATE_NPS        = "rate_nps";
    private static final String KEY_RATE_PPF        = "rate_ppf";
    private static final String KEY_RATE_EPF        = "rate_epf";
    private static final String KEY_RATE_MF         = "rate_mf";
    private static final String KEY_RATE_STOCKS     = "rate_stocks";
    private static final String KEY_RATE_SAVINGS    = "rate_savings_bank";
    private static final String KEY_RATE_CRYPTO     = "rate_crypto";
    private static final String KEY_RATE_HUF_BANK   = "rate_huf_bank";
    private static final String KEY_RATE_HUF_STOCKS = "rate_huf_stocks";
    private static final String KEY_RATE_HUF_MF     = "rate_huf_mf";

    private EditText etDOB, etRetirementAge, etTargetCorpus;
    private EditText etNPS, etPPF, etEPF, etMF, etStocks, etSavingsBank, etCrypto;
    private EditText etHUFBank, etHUFStocks, etHUFMF;
    private EditText etRateNPS, etRatePPF, etRateEPF, etRateMF, etRateStocks;
    private EditText etRateSavingsBank, etRateCrypto;
    private EditText etRateHUFBank, etRateHUFStocks, etRateHUFMF;
    private Button btnCalculate, btnAbout;
    private LinearLayout bannerLastSaved;
    private TextView tvLastSaved;
    private Button btnClearData;

    // Default rates
    private static final String DEFAULT_NPS          = "10";
    private static final String DEFAULT_PPF          = "8";
    private static final String DEFAULT_EPF          = "8";
    private static final String DEFAULT_MF           = "12";
    private static final String DEFAULT_STOCKS       = "12";
    private static final String DEFAULT_SAVINGS_BANK = "4";
    private static final String DEFAULT_CRYPTO       = "20";
    private static final String DEFAULT_HUF_BANK     = "4";
    private static final String DEFAULT_HUF_STOCKS   = "12";
    private static final String DEFAULT_HUF_MF       = "12";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        setAssetLabels();
        loadSavedData();   // Load saved data (or defaults if nothing saved)
        setupDatePicker();
        setupButtons();
    }

    private void initViews() {
        etDOB           = findViewById(R.id.etDOB);
        etRetirementAge = findViewById(R.id.etRetirementAge);
        etTargetCorpus  = findViewById(R.id.etTargetCorpus);
        bannerLastSaved = findViewById(R.id.bannerLastSaved);
        tvLastSaved     = findViewById(R.id.tvLastSaved);
        btnClearData    = findViewById(R.id.btnClearData);

        View rowNPS         = findViewById(R.id.rowNPS);
        View rowPPF         = findViewById(R.id.rowPPF);
        View rowEPF         = findViewById(R.id.rowEPF);
        View rowMF          = findViewById(R.id.rowMF);
        View rowStocks      = findViewById(R.id.rowStocks);
        View rowSavingsBank = findViewById(R.id.rowSavingsBank);
        View rowCrypto      = findViewById(R.id.rowCrypto);
        View rowHUFBank     = findViewById(R.id.rowHUFBank);
        View rowHUFStocks   = findViewById(R.id.rowHUFStocks);
        View rowHUFMF       = findViewById(R.id.rowHUFMF);

        etNPS         = rowNPS.findViewById(R.id.etCorpus);
        etPPF         = rowPPF.findViewById(R.id.etCorpus);
        etEPF         = rowEPF.findViewById(R.id.etCorpus);
        etMF          = rowMF.findViewById(R.id.etCorpus);
        etStocks      = rowStocks.findViewById(R.id.etCorpus);
        etSavingsBank = rowSavingsBank.findViewById(R.id.etCorpus);
        etCrypto      = rowCrypto.findViewById(R.id.etCorpus);
        etHUFBank     = rowHUFBank.findViewById(R.id.etCorpus);
        etHUFStocks   = rowHUFStocks.findViewById(R.id.etCorpus);
        etHUFMF       = rowHUFMF.findViewById(R.id.etCorpus);

        etRateNPS         = rowNPS.findViewById(R.id.etRate);
        etRatePPF         = rowPPF.findViewById(R.id.etRate);
        etRateEPF         = rowEPF.findViewById(R.id.etRate);
        etRateMF          = rowMF.findViewById(R.id.etRate);
        etRateStocks      = rowStocks.findViewById(R.id.etRate);
        etRateSavingsBank = rowSavingsBank.findViewById(R.id.etRate);
        etRateCrypto      = rowCrypto.findViewById(R.id.etRate);
        etRateHUFBank     = rowHUFBank.findViewById(R.id.etRate);
        etRateHUFStocks   = rowHUFStocks.findViewById(R.id.etRate);
        etRateHUFMF       = rowHUFMF.findViewById(R.id.etRate);

        btnCalculate = findViewById(R.id.btnCalculate);
        btnAbout     = findViewById(R.id.btnAbout);
    }

    private void setAssetLabels() {
        setLabel(R.id.rowNPS,         "NPS");
        setLabel(R.id.rowPPF,         "PPF");
        setLabel(R.id.rowEPF,         "EPF");
        setLabel(R.id.rowMF,          "Mutual\nFunds");
        setLabel(R.id.rowStocks,      "Stocks");
        setLabel(R.id.rowSavingsBank, "Savings\nBank");
        setLabel(R.id.rowCrypto,      "Crypto/\nBitcoin");
        setLabel(R.id.rowHUFBank,     "HUF\nBank");
        setLabel(R.id.rowHUFStocks,   "HUF\nStocks");
        setLabel(R.id.rowHUFMF,       "HUF MF");
    }

    private void setLabel(int rowId, String label) {
        View row = findViewById(rowId);
        if (row != null) ((TextView) row.findViewById(R.id.tvAssetLabel)).setText(label);
    }

    // ─── SAVE ────────────────────────────────────────────────────────────────

    private void saveData() {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();

        // Save timestamp
        String now = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                .format(new Date());
        editor.putString(KEY_SAVED_DATE, now);

        // Personal
        editor.putString(KEY_DOB,            etDOB.getText().toString().trim());
        editor.putString(KEY_RETIREMENT_AGE, etRetirementAge.getText().toString().trim());
        editor.putString(KEY_TARGET,         etTargetCorpus.getText().toString().trim());

        // Corpus
        editor.putString(KEY_NPS,          etNPS.getText().toString().trim());
        editor.putString(KEY_PPF,          etPPF.getText().toString().trim());
        editor.putString(KEY_EPF,          etEPF.getText().toString().trim());
        editor.putString(KEY_MF,           etMF.getText().toString().trim());
        editor.putString(KEY_STOCKS,       etStocks.getText().toString().trim());
        editor.putString(KEY_SAVINGS_BANK, etSavingsBank.getText().toString().trim());
        editor.putString(KEY_CRYPTO,       etCrypto.getText().toString().trim());
        editor.putString(KEY_HUF_BANK,     etHUFBank.getText().toString().trim());
        editor.putString(KEY_HUF_STOCKS,   etHUFStocks.getText().toString().trim());
        editor.putString(KEY_HUF_MF,       etHUFMF.getText().toString().trim());

        // Rates
        editor.putString(KEY_RATE_NPS,        etRateNPS.getText().toString().trim());
        editor.putString(KEY_RATE_PPF,        etRatePPF.getText().toString().trim());
        editor.putString(KEY_RATE_EPF,        etRateEPF.getText().toString().trim());
        editor.putString(KEY_RATE_MF,         etRateMF.getText().toString().trim());
        editor.putString(KEY_RATE_STOCKS,     etRateStocks.getText().toString().trim());
        editor.putString(KEY_RATE_SAVINGS,    etRateSavingsBank.getText().toString().trim());
        editor.putString(KEY_RATE_CRYPTO,     etRateCrypto.getText().toString().trim());
        editor.putString(KEY_RATE_HUF_BANK,   etRateHUFBank.getText().toString().trim());
        editor.putString(KEY_RATE_HUF_STOCKS, etRateHUFStocks.getText().toString().trim());
        editor.putString(KEY_RATE_HUF_MF,     etRateHUFMF.getText().toString().trim());

        editor.apply();
    }

    // ─── LOAD ────────────────────────────────────────────────────────────────

    private void loadSavedData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedDate = prefs.getString(KEY_SAVED_DATE, null);

        if (savedDate == null) {
            // No saved data — just fill default rates
            prefillDefaultRates();
            return;
        }

        // Show the banner with saved date
        bannerLastSaved.setVisibility(View.VISIBLE);
        tvLastSaved.setText("Data last saved on " + savedDate);

        // Personal
        setText(etDOB,           prefs.getString(KEY_DOB, ""));
        setText(etRetirementAge, prefs.getString(KEY_RETIREMENT_AGE, "60"));
        setText(etTargetCorpus,  prefs.getString(KEY_TARGET, ""));

        // Corpus
        setText(etNPS,         prefs.getString(KEY_NPS, ""));
        setText(etPPF,         prefs.getString(KEY_PPF, ""));
        setText(etEPF,         prefs.getString(KEY_EPF, ""));
        setText(etMF,          prefs.getString(KEY_MF, ""));
        setText(etStocks,      prefs.getString(KEY_STOCKS, ""));
        setText(etSavingsBank, prefs.getString(KEY_SAVINGS_BANK, ""));
        setText(etCrypto,      prefs.getString(KEY_CRYPTO, ""));
        setText(etHUFBank,     prefs.getString(KEY_HUF_BANK, ""));
        setText(etHUFStocks,   prefs.getString(KEY_HUF_STOCKS, ""));
        setText(etHUFMF,       prefs.getString(KEY_HUF_MF, ""));

        // Rates
        setText(etRateNPS,         prefs.getString(KEY_RATE_NPS,        DEFAULT_NPS));
        setText(etRatePPF,         prefs.getString(KEY_RATE_PPF,        DEFAULT_PPF));
        setText(etRateEPF,         prefs.getString(KEY_RATE_EPF,        DEFAULT_EPF));
        setText(etRateMF,          prefs.getString(KEY_RATE_MF,         DEFAULT_MF));
        setText(etRateStocks,      prefs.getString(KEY_RATE_STOCKS,     DEFAULT_STOCKS));
        setText(etRateSavingsBank, prefs.getString(KEY_RATE_SAVINGS,    DEFAULT_SAVINGS_BANK));
        setText(etRateCrypto,      prefs.getString(KEY_RATE_CRYPTO,     DEFAULT_CRYPTO));
        setText(etRateHUFBank,     prefs.getString(KEY_RATE_HUF_BANK,   DEFAULT_HUF_BANK));
        setText(etRateHUFStocks,   prefs.getString(KEY_RATE_HUF_STOCKS, DEFAULT_HUF_STOCKS));
        setText(etRateHUFMF,       prefs.getString(KEY_RATE_HUF_MF,     DEFAULT_HUF_MF));
    }

    private void prefillDefaultRates() {
        setText(etRateNPS,         DEFAULT_NPS);
        setText(etRatePPF,         DEFAULT_PPF);
        setText(etRateEPF,         DEFAULT_EPF);
        setText(etRateMF,          DEFAULT_MF);
        setText(etRateStocks,      DEFAULT_STOCKS);
        setText(etRateSavingsBank, DEFAULT_SAVINGS_BANK);
        setText(etRateCrypto,      DEFAULT_CRYPTO);
        setText(etRateHUFBank,     DEFAULT_HUF_BANK);
        setText(etRateHUFStocks,   DEFAULT_HUF_STOCKS);
        setText(etRateHUFMF,       DEFAULT_HUF_MF);
    }

    private void clearData() {
        new AlertDialog.Builder(this)
            .setTitle("Clear Saved Data")
            .setMessage("This will erase all saved values. Are you sure?")
            .setPositiveButton("Clear", (dialog, which) -> {
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().clear().apply();
                // Clear all fields
                etDOB.setText("");
                etRetirementAge.setText("");
                etTargetCorpus.setText("");
                etNPS.setText(""); etPPF.setText(""); etEPF.setText("");
                etMF.setText(""); etStocks.setText(""); etSavingsBank.setText("");
                etCrypto.setText(""); etHUFBank.setText("");
                etHUFStocks.setText(""); etHUFMF.setText("");
                prefillDefaultRates();
                bannerLastSaved.setVisibility(View.GONE);
                Toast.makeText(this, "Saved data cleared", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    // ─── SETUP ───────────────────────────────────────────────────────────────

    private void setupDatePicker() {
        etDOB.setFocusable(false);
        etDOB.setClickable(true);
        etDOB.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            DatePickerDialog dpd = new DatePickerDialog(this, (view, y, m, d) ->
                etDOB.setText(String.format("%02d/%02d/%04d", d, m + 1, y)),
                cal.get(Calendar.YEAR) - 30, cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            dpd.getDatePicker().setMaxDate(System.currentTimeMillis());
            dpd.show();
        });
    }

    private void setupButtons() {
        btnCalculate.setOnClickListener(v -> {
            if (validateInputs()) {
                saveData();   // Auto-save every time Calculate is tapped
                launchResult();
            }
        });
        btnAbout.setOnClickListener(v ->
            startActivity(new Intent(this, AboutActivity.class)));
        btnClearData.setOnClickListener(v -> clearData());
    }

    // ─── VALIDATE ────────────────────────────────────────────────────────────

    private boolean validateInputs() {
        if (etDOB.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter your date of birth", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (etTargetCorpus.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter your target corpus", Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            String[] parts = etDOB.getText().toString().trim().split("/");
            Calendar dobCal = Calendar.getInstance();
            dobCal.set(Integer.parseInt(parts[2]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[0]));
            Calendar now = Calendar.getInstance();
            int age = now.get(Calendar.YEAR) - dobCal.get(Calendar.YEAR);
            if (now.get(Calendar.DAY_OF_YEAR) < dobCal.get(Calendar.DAY_OF_YEAR)) age--;
            if (age >= getIntValue(etRetirementAge, 60)) {
                Toast.makeText(this, "You have already reached retirement age!", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Invalid date. Use DD/MM/YYYY", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // ─── LAUNCH RESULT ───────────────────────────────────────────────────────

    private void launchResult() {
        Intent i = new Intent(this, ResultActivity.class);
        i.putExtra("dob",           etDOB.getText().toString().trim());
        i.putExtra("retirementAge", getIntValue(etRetirementAge, 60));
        i.putExtra("targetCorpus",  getDoubleValue(etTargetCorpus, 0));
        i.putExtra("nps",           getDoubleValue(etNPS, 0));
        i.putExtra("ppf",           getDoubleValue(etPPF, 0));
        i.putExtra("epf",           getDoubleValue(etEPF, 0));
        i.putExtra("mf",            getDoubleValue(etMF, 0));
        i.putExtra("stocks",        getDoubleValue(etStocks, 0));
        i.putExtra("savingsBank",   getDoubleValue(etSavingsBank, 0));
        i.putExtra("crypto",        getDoubleValue(etCrypto, 0));
        i.putExtra("hufBank",       getDoubleValue(etHUFBank, 0));
        i.putExtra("hufStocks",     getDoubleValue(etHUFStocks, 0));
        i.putExtra("hufMF",         getDoubleValue(etHUFMF, 0));
        i.putExtra("rateNPS",         getDoubleValue(etRateNPS, 10));
        i.putExtra("ratePPF",         getDoubleValue(etRatePPF, 8));
        i.putExtra("rateEPF",         getDoubleValue(etRateEPF, 8));
        i.putExtra("rateMF",          getDoubleValue(etRateMF, 12));
        i.putExtra("rateStocks",      getDoubleValue(etRateStocks, 12));
        i.putExtra("rateSavingsBank", getDoubleValue(etRateSavingsBank, 4));
        i.putExtra("rateCrypto",      getDoubleValue(etRateCrypto, 20));
        i.putExtra("rateHUFBank",     getDoubleValue(etRateHUFBank, 4));
        i.putExtra("rateHUFStocks",   getDoubleValue(etRateHUFStocks, 12));
        i.putExtra("rateHUFMF",       getDoubleValue(etRateHUFMF, 12));
        startActivity(i);
    }

    // ─── HELPERS ─────────────────────────────────────────────────────────────

    private void setText(EditText et, String value) {
        et.setText(value);
    }

    private int getIntValue(EditText et, int def) {
        try { String s = et.getText().toString().trim(); return s.isEmpty() ? def : Integer.parseInt(s); }
        catch (Exception e) { return def; }
    }

    private double getDoubleValue(EditText et, double def) {
        try { String s = et.getText().toString().trim(); return s.isEmpty() ? def : Double.parseDouble(s); }
        catch (Exception e) { return def; }
    }
}
