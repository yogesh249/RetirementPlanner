package com.retirement.planner;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
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
    private static final String KEY_NPS = "nps", KEY_PPF = "ppf", KEY_EPF = "epf";
    private static final String KEY_MF = "mf", KEY_STOCKS = "stocks";
    private static final String KEY_SAVINGS_BANK = "savings_bank", KEY_CRYPTO = "crypto";
    private static final String KEY_HUF_BANK = "huf_bank", KEY_HUF_STOCKS = "huf_stocks", KEY_HUF_MF = "huf_mf";
    private static final String KEY_RATE_NPS = "rate_nps", KEY_RATE_PPF = "rate_ppf", KEY_RATE_EPF = "rate_epf";
    private static final String KEY_RATE_MF = "rate_mf", KEY_RATE_STOCKS = "rate_stocks";
    private static final String KEY_RATE_SAVINGS = "rate_savings_bank", KEY_RATE_CRYPTO = "rate_crypto";
    private static final String KEY_RATE_HUF_BANK = "rate_huf_bank", KEY_RATE_HUF_STOCKS = "rate_huf_stocks", KEY_RATE_HUF_MF = "rate_huf_mf";

    private EditText etDOB, etRetirementAge, etTargetCorpus;
    private EditText etNPS, etPPF, etEPF, etMF, etStocks, etSavingsBank, etCrypto;
    private EditText etHUFBank, etHUFStocks, etHUFMF;
    private EditText etRateNPS, etRatePPF, etRateEPF, etRateMF, etRateStocks;
    private EditText etRateSavingsBank, etRateCrypto, etRateHUFBank, etRateHUFStocks, etRateHUFMF;
    private LinearLayout bannerLastSaved;
    private TextView tvLastSaved;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_portfolio, container, false);
        initViews(v);
        setAssetLabels(v);
        loadSavedData();
        setupDatePicker();
        v.findViewById(R.id.btnCalculate).setOnClickListener(btn -> {
            if (validateInputs()) { saveData(); launchResult(); }
        });
        v.findViewById(R.id.btnClearData).setOnClickListener(btn -> clearData());
        return v;
    }

    private void initViews(View v) {
        etDOB = v.findViewById(R.id.etDOB);
        etRetirementAge = v.findViewById(R.id.etRetirementAge);
        etTargetCorpus  = v.findViewById(R.id.etTargetCorpus);
        bannerLastSaved = v.findViewById(R.id.bannerLastSaved);
        tvLastSaved     = v.findViewById(R.id.tvLastSaved);

        View rowNPS = v.findViewById(R.id.rowNPS), rowPPF = v.findViewById(R.id.rowPPF);
        View rowEPF = v.findViewById(R.id.rowEPF), rowMF  = v.findViewById(R.id.rowMF);
        View rowStocks = v.findViewById(R.id.rowStocks), rowSavingsBank = v.findViewById(R.id.rowSavingsBank);
        View rowCrypto = v.findViewById(R.id.rowCrypto);
        View rowHUFBank = v.findViewById(R.id.rowHUFBank), rowHUFStocks = v.findViewById(R.id.rowHUFStocks);
        View rowHUFMF   = v.findViewById(R.id.rowHUFMF);

        etNPS = rowNPS.findViewById(R.id.etCorpus); etPPF = rowPPF.findViewById(R.id.etCorpus);
        etEPF = rowEPF.findViewById(R.id.etCorpus); etMF  = rowMF.findViewById(R.id.etCorpus);
        etStocks = rowStocks.findViewById(R.id.etCorpus); etSavingsBank = rowSavingsBank.findViewById(R.id.etCorpus);
        etCrypto = rowCrypto.findViewById(R.id.etCorpus); etHUFBank = rowHUFBank.findViewById(R.id.etCorpus);
        etHUFStocks = rowHUFStocks.findViewById(R.id.etCorpus); etHUFMF = rowHUFMF.findViewById(R.id.etCorpus);

        etRateNPS = rowNPS.findViewById(R.id.etRate); etRatePPF = rowPPF.findViewById(R.id.etRate);
        etRateEPF = rowEPF.findViewById(R.id.etRate); etRateMF  = rowMF.findViewById(R.id.etRate);
        etRateStocks = rowStocks.findViewById(R.id.etRate); etRateSavingsBank = rowSavingsBank.findViewById(R.id.etRate);
        etRateCrypto = rowCrypto.findViewById(R.id.etRate); etRateHUFBank = rowHUFBank.findViewById(R.id.etRate);
        etRateHUFStocks = rowHUFStocks.findViewById(R.id.etRate); etRateHUFMF = rowHUFMF.findViewById(R.id.etRate);
    }

    private void setAssetLabels(View v) {
        setLabel(v, R.id.rowNPS, "NPS"); setLabel(v, R.id.rowPPF, "PPF");
        setLabel(v, R.id.rowEPF, "EPF"); setLabel(v, R.id.rowMF, "Mutual\nFunds");
        setLabel(v, R.id.rowStocks, "Stocks"); setLabel(v, R.id.rowSavingsBank, "Savings\nBank");
        setLabel(v, R.id.rowCrypto, "Crypto/\nBitcoin"); setLabel(v, R.id.rowHUFBank, "HUF\nBank");
        setLabel(v, R.id.rowHUFStocks, "HUF\nStocks"); setLabel(v, R.id.rowHUFMF, "HUF MF");
    }

    private void setLabel(View root, int rowId, String label) {
        View row = root.findViewById(rowId);
        if (row != null) ((TextView) row.findViewById(R.id.tvAssetLabel)).setText(label);
    }

    private void setupDatePicker() {
        etDOB.setFocusable(false); etDOB.setClickable(true);
        etDOB.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (view, y, m, d) ->
                etDOB.setText(String.format("%02d/%02d/%04d", d, m + 1, y)),
                cal.get(Calendar.YEAR) - 30, cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)) {{
                getDatePicker().setMaxDate(System.currentTimeMillis());
            }}.show();
        });
    }

    private void saveData() {
        SharedPreferences.Editor e = requireContext().getSharedPreferences(PREFS_NAME, 0).edit();
        e.putString(KEY_SAVED_DATE, new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(new Date()));
        e.putString(KEY_DOB, etDOB.getText().toString().trim());
        e.putString(KEY_RETIREMENT_AGE, etRetirementAge.getText().toString().trim());
        e.putString(KEY_TARGET, etTargetCorpus.getText().toString().trim());
        e.putString(KEY_NPS, etNPS.getText().toString().trim()); e.putString(KEY_PPF, etPPF.getText().toString().trim());
        e.putString(KEY_EPF, etEPF.getText().toString().trim()); e.putString(KEY_MF, etMF.getText().toString().trim());
        e.putString(KEY_STOCKS, etStocks.getText().toString().trim()); e.putString(KEY_SAVINGS_BANK, etSavingsBank.getText().toString().trim());
        e.putString(KEY_CRYPTO, etCrypto.getText().toString().trim()); e.putString(KEY_HUF_BANK, etHUFBank.getText().toString().trim());
        e.putString(KEY_HUF_STOCKS, etHUFStocks.getText().toString().trim()); e.putString(KEY_HUF_MF, etHUFMF.getText().toString().trim());
        e.putString(KEY_RATE_NPS, etRateNPS.getText().toString().trim()); e.putString(KEY_RATE_PPF, etRatePPF.getText().toString().trim());
        e.putString(KEY_RATE_EPF, etRateEPF.getText().toString().trim()); e.putString(KEY_RATE_MF, etRateMF.getText().toString().trim());
        e.putString(KEY_RATE_STOCKS, etRateStocks.getText().toString().trim()); e.putString(KEY_RATE_SAVINGS, etRateSavingsBank.getText().toString().trim());
        e.putString(KEY_RATE_CRYPTO, etRateCrypto.getText().toString().trim()); e.putString(KEY_RATE_HUF_BANK, etRateHUFBank.getText().toString().trim());
        e.putString(KEY_RATE_HUF_STOCKS, etRateHUFStocks.getText().toString().trim()); e.putString(KEY_RATE_HUF_MF, etRateHUFMF.getText().toString().trim());
        e.apply();
    }

    private void loadSavedData() {
        SharedPreferences p = requireContext().getSharedPreferences(PREFS_NAME, 0);
        String savedDate = p.getString(KEY_SAVED_DATE, null);
        if (savedDate == null) { prefillDefaultRates(); return; }
        bannerLastSaved.setVisibility(View.VISIBLE);
        tvLastSaved.setText("Data last saved on " + savedDate);
        etDOB.setText(p.getString(KEY_DOB, "")); etRetirementAge.setText(p.getString(KEY_RETIREMENT_AGE, "60"));
        etTargetCorpus.setText(p.getString(KEY_TARGET, ""));
        etNPS.setText(p.getString(KEY_NPS, "")); etPPF.setText(p.getString(KEY_PPF, ""));
        etEPF.setText(p.getString(KEY_EPF, "")); etMF.setText(p.getString(KEY_MF, ""));
        etStocks.setText(p.getString(KEY_STOCKS, "")); etSavingsBank.setText(p.getString(KEY_SAVINGS_BANK, ""));
        etCrypto.setText(p.getString(KEY_CRYPTO, "")); etHUFBank.setText(p.getString(KEY_HUF_BANK, ""));
        etHUFStocks.setText(p.getString(KEY_HUF_STOCKS, "")); etHUFMF.setText(p.getString(KEY_HUF_MF, ""));
        etRateNPS.setText(p.getString(KEY_RATE_NPS, "10")); etRatePPF.setText(p.getString(KEY_RATE_PPF, "8"));
        etRateEPF.setText(p.getString(KEY_RATE_EPF, "8")); etRateMF.setText(p.getString(KEY_RATE_MF, "12"));
        etRateStocks.setText(p.getString(KEY_RATE_STOCKS, "12")); etRateSavingsBank.setText(p.getString(KEY_RATE_SAVINGS, "4"));
        etRateCrypto.setText(p.getString(KEY_RATE_CRYPTO, "20")); etRateHUFBank.setText(p.getString(KEY_RATE_HUF_BANK, "4"));
        etRateHUFStocks.setText(p.getString(KEY_RATE_HUF_STOCKS, "12")); etRateHUFMF.setText(p.getString(KEY_RATE_HUF_MF, "12"));
    }

    private void prefillDefaultRates() {
        etRateNPS.setText("10"); etRatePPF.setText("8"); etRateEPF.setText("8");
        etRateMF.setText("12"); etRateStocks.setText("12"); etRateSavingsBank.setText("4");
        etRateCrypto.setText("20"); etRateHUFBank.setText("4"); etRateHUFStocks.setText("12"); etRateHUFMF.setText("12");
    }

    private void clearData() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Clear Saved Data")
            .setMessage("This will erase all saved values. Are you sure?")
            .setPositiveButton("Clear", (d, w) -> {
                requireContext().getSharedPreferences(PREFS_NAME, 0).edit().clear().apply();
                etDOB.setText(""); etRetirementAge.setText(""); etTargetCorpus.setText("");
                etNPS.setText(""); etPPF.setText(""); etEPF.setText(""); etMF.setText("");
                etStocks.setText(""); etSavingsBank.setText(""); etCrypto.setText("");
                etHUFBank.setText(""); etHUFStocks.setText(""); etHUFMF.setText("");
                prefillDefaultRates();
                bannerLastSaved.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Saved data cleared", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null).show();
    }

    private boolean validateInputs() {
        if (etDOB.getText().toString().trim().isEmpty()) {
            Toast.makeText(requireContext(), "Please enter your date of birth", Toast.LENGTH_SHORT).show(); return false;
        }
        if (etTargetCorpus.getText().toString().trim().isEmpty()) {
            Toast.makeText(requireContext(), "Please enter your target corpus", Toast.LENGTH_SHORT).show(); return false;
        }
        try {
            String[] parts = etDOB.getText().toString().trim().split("/");
            Calendar dobCal = Calendar.getInstance();
            dobCal.set(Integer.parseInt(parts[2]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[0]));
            Calendar now = Calendar.getInstance();
            int age = now.get(Calendar.YEAR) - dobCal.get(Calendar.YEAR);
            if (now.get(Calendar.DAY_OF_YEAR) < dobCal.get(Calendar.DAY_OF_YEAR)) age--;
            if (age >= getInt(etRetirementAge, 60)) {
                Toast.makeText(requireContext(), "You have already reached retirement age!", Toast.LENGTH_SHORT).show(); return false;
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Invalid date. Use DD/MM/YYYY", Toast.LENGTH_SHORT).show(); return false;
        }
        return true;
    }

    private void launchResult() {
        Intent i = new Intent(requireContext(), ResultActivity.class);
        i.putExtra("dob", etDOB.getText().toString().trim());
        i.putExtra("retirementAge", getInt(etRetirementAge, 60));
        i.putExtra("targetCorpus",  getDbl(etTargetCorpus, 0));
        i.putExtra("nps", getDbl(etNPS,0)); i.putExtra("ppf", getDbl(etPPF,0));
        i.putExtra("epf", getDbl(etEPF,0)); i.putExtra("mf",  getDbl(etMF,0));
        i.putExtra("stocks", getDbl(etStocks,0)); i.putExtra("savingsBank", getDbl(etSavingsBank,0));
        i.putExtra("crypto", getDbl(etCrypto,0)); i.putExtra("hufBank", getDbl(etHUFBank,0));
        i.putExtra("hufStocks", getDbl(etHUFStocks,0)); i.putExtra("hufMF", getDbl(etHUFMF,0));
        i.putExtra("rateNPS", getDbl(etRateNPS,10)); i.putExtra("ratePPF", getDbl(etRatePPF,8));
        i.putExtra("rateEPF", getDbl(etRateEPF,8)); i.putExtra("rateMF", getDbl(etRateMF,12));
        i.putExtra("rateStocks", getDbl(etRateStocks,12)); i.putExtra("rateSavingsBank", getDbl(etRateSavingsBank,4));
        i.putExtra("rateCrypto", getDbl(etRateCrypto,20)); i.putExtra("rateHUFBank", getDbl(etRateHUFBank,4));
        i.putExtra("rateHUFStocks", getDbl(etRateHUFStocks,12)); i.putExtra("rateHUFMF", getDbl(etRateHUFMF,12));
        startActivity(i);
    }

    private int getInt(EditText et, int def) {
        try { String s = et.getText().toString().trim(); return s.isEmpty() ? def : Integer.parseInt(s); } catch (Exception e) { return def; }
    }
    private double getDbl(EditText et, double def) {
        try { String s = et.getText().toString().trim(); return s.isEmpty() ? def : Double.parseDouble(s); } catch (Exception e) { return def; }
    }
}
