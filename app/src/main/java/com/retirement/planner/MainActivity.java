package com.retirement.planner;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private EditText etDOB, etRetirementAge, etTargetCorpus;

    // Corpus fields
    private EditText etNPS, etPPF, etEPF, etMF, etStocks, etSavingsBank, etCrypto;
    private EditText etHUFBank, etHUFStocks, etHUFMF;

    // Rate fields
    private EditText etRateNPS, etRatePPF, etRateEPF, etRateMF, etRateStocks;
    private EditText etRateSavingsBank, etRateCrypto;
    private EditText etRateHUFBank, etRateHUFStocks, etRateHUFMF;

    private Button btnCalculate;
    private Button btnAbout;

    // Default rates
    private static final String DEFAULT_NPS    = "10";
    private static final String DEFAULT_PPF    = "8";
    private static final String DEFAULT_EPF    = "8";
    private static final String DEFAULT_MF     = "12";
    private static final String DEFAULT_STOCKS = "12";
    private static final String DEFAULT_SAVINGS_BANK = "4";
    private static final String DEFAULT_CRYPTO = "20";
    private static final String DEFAULT_HUF_BANK   = "4";
    private static final String DEFAULT_HUF_STOCKS = "12";
    private static final String DEFAULT_HUF_MF     = "12";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        setAssetLabels();
        prefillDefaultRates();
        setupDatePicker();
        setupCalculateButton();
    }

    private void initViews() {
        etDOB            = findViewById(R.id.etDOB);
        etRetirementAge  = findViewById(R.id.etRetirementAge);
        etTargetCorpus   = findViewById(R.id.etTargetCorpus);

        // Individual rows
        View rowNPS         = findViewById(R.id.rowNPS);
        View rowPPF         = findViewById(R.id.rowPPF);
        View rowEPF         = findViewById(R.id.rowEPF);
        View rowMF          = findViewById(R.id.rowMF);
        View rowStocks      = findViewById(R.id.rowStocks);
        View rowSavingsBank = findViewById(R.id.rowSavingsBank);
        View rowCrypto      = findViewById(R.id.rowCrypto);

        // HUF rows
        View rowHUFBank   = findViewById(R.id.rowHUFBank);
        View rowHUFStocks = findViewById(R.id.rowHUFStocks);
        View rowHUFMF     = findViewById(R.id.rowHUFMF);

        // Corpus inputs
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

        // Rate inputs
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
        btnAbout = findViewById(R.id.btnAbout);
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
        if (row != null) {
            ((TextView) row.findViewById(R.id.tvAssetLabel)).setText(label);
        }
    }

    private void prefillDefaultRates() {
        etRateNPS.setText(DEFAULT_NPS);
        etRatePPF.setText(DEFAULT_PPF);
        etRateEPF.setText(DEFAULT_EPF);
        etRateMF.setText(DEFAULT_MF);
        etRateStocks.setText(DEFAULT_STOCKS);
        etRateSavingsBank.setText(DEFAULT_SAVINGS_BANK);
        etRateCrypto.setText(DEFAULT_CRYPTO);
        etRateHUFBank.setText(DEFAULT_HUF_BANK);
        etRateHUFStocks.setText(DEFAULT_HUF_STOCKS);
        etRateHUFMF.setText(DEFAULT_HUF_MF);
    }

    private void setupDatePicker() {
        etDOB.setFocusable(false);
        etDOB.setClickable(true);
        etDOB.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            DatePickerDialog dpd = new DatePickerDialog(this, (view, y, m, d) -> {
                etDOB.setText(String.format("%02d/%02d/%04d", d, m + 1, y));
            }, cal.get(Calendar.YEAR) - 30, cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            dpd.getDatePicker().setMaxDate(System.currentTimeMillis());
            dpd.show();
        });
    }

    private void setupCalculateButton() {
        btnCalculate.setOnClickListener(v -> {
            if (validateInputs()) launchResult();
        });
        btnAbout.setOnClickListener(v ->
            startActivity(new Intent(this, AboutActivity.class)));
    }

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

    private void launchResult() {
        Intent i = new Intent(this, ResultActivity.class);

        i.putExtra("dob",           etDOB.getText().toString().trim());
        i.putExtra("retirementAge", getIntValue(etRetirementAge, 60));
        i.putExtra("targetCorpus",  getDoubleValue(etTargetCorpus, 0));

        // Corpus values
        i.putExtra("nps",         getDoubleValue(etNPS, 0));
        i.putExtra("ppf",         getDoubleValue(etPPF, 0));
        i.putExtra("epf",         getDoubleValue(etEPF, 0));
        i.putExtra("mf",          getDoubleValue(etMF, 0));
        i.putExtra("stocks",      getDoubleValue(etStocks, 0));
        i.putExtra("savingsBank", getDoubleValue(etSavingsBank, 0));
        i.putExtra("crypto",      getDoubleValue(etCrypto, 0));
        i.putExtra("hufBank",     getDoubleValue(etHUFBank, 0));
        i.putExtra("hufStocks",   getDoubleValue(etHUFStocks, 0));
        i.putExtra("hufMF",       getDoubleValue(etHUFMF, 0));

        // Growth rates (as percentages, e.g. 12.0 for 12%)
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

    private int getIntValue(EditText et, int def) {
        try { String s = et.getText().toString().trim(); return s.isEmpty() ? def : Integer.parseInt(s); }
        catch (Exception e) { return def; }
    }

    private double getDoubleValue(EditText et, double def) {
        try { String s = et.getText().toString().trim(); return s.isEmpty() ? def : Double.parseDouble(s); }
        catch (Exception e) { return def; }
    }
}
