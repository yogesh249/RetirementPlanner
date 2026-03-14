package com.retirement.planner;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import java.text.NumberFormat;
import java.util.Locale;

public class ExpenseFragment extends Fragment {

    private static final String PREFS_EXPENSE = "ExpensePlannerData";

    // Assumption inputs
    private EditText etExpRetirementAge, etLifeExpectancy, etExpInflation, etPostRetirementReturn;

    // Containers
    private LinearLayout containerCurrentExpenses, containerPostExpenses;

    // Totals
    private TextView tvCurrentTotal, tvPostTotal;
    private TextView tvResultCurrentToday, tvResultPostToday, tvResultCombinedToday;
    private TextView tvInflationLabel, tvResultInflationAdjusted;
    private TextView tvMinCorpus, tvCorpusSubtitle, tvCorpusNote;

    // Default current expenses: name, default amount, checked by default
    private static final String[][] DEFAULT_CURRENT = {
        {"Groceries",         "15000", "true"},
        {"Vegetables & milk", "5000",  "true"},
        {"Electricity bill",  "3000",  "true"},
        {"Water / gas bill",  "1000",  "true"},
        {"Mobile / internet", "1500",  "true"},
        {"Home loan EMI",     "30000", "false"},
        {"Car loan EMI",      "10000", "false"},
        {"Child education",   "15000", "false"},
        {"Transport / fuel",  "5000",  "true"},
        {"Dining / outings",  "4000",  "true"},
        {"Insurance premium", "5000",  "true"},
        {"Clothing",          "2000",  "true"},
        {"Domestic help",     "3000",  "true"},
        {"OTT / subscriptions","1000", "true"},
        {"Miscellaneous",     "5000",  "true"},
    };

    // Default post-retirement expenses
    private static final String[][] DEFAULT_POST = {
        {"Medical expenses",   "10000"},
        {"Medicines / health", "5000"},
        {"Caretaker salary",   "0"},
        {"Travel / leisure",   "8000"},
        {"Grandchildren gifts","2000"},
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_expense, container, false);

        etExpRetirementAge    = v.findViewById(R.id.etExpRetirementAge);
        etLifeExpectancy      = v.findViewById(R.id.etLifeExpectancy);
        etExpInflation        = v.findViewById(R.id.etExpInflation);
        etPostRetirementReturn= v.findViewById(R.id.etPostRetirementReturn);
        containerCurrentExpenses = v.findViewById(R.id.containerCurrentExpenses);
        containerPostExpenses    = v.findViewById(R.id.containerPostExpenses);
        tvCurrentTotal        = v.findViewById(R.id.tvCurrentTotal);
        tvPostTotal           = v.findViewById(R.id.tvPostTotal);
        tvResultCurrentToday  = v.findViewById(R.id.tvResultCurrentToday);
        tvResultPostToday     = v.findViewById(R.id.tvResultPostToday);
        tvResultCombinedToday = v.findViewById(R.id.tvResultCombinedToday);
        tvInflationLabel      = v.findViewById(R.id.tvInflationLabel);
        tvResultInflationAdjusted = v.findViewById(R.id.tvResultInflationAdjusted);
        tvMinCorpus           = v.findViewById(R.id.tvMinCorpus);
        tvCorpusSubtitle      = v.findViewById(R.id.tvCorpusSubtitle);
        tvCorpusNote          = v.findViewById(R.id.tvCorpusNote);

        // Set defaults
        etExpRetirementAge.setText("60");
        etLifeExpectancy.setText("80");
        etExpInflation.setText("6");
        etPostRetirementReturn.setText("8");

        // Load saved or default expense rows
        loadOrInitExpenses();

        // Add expense buttons
        v.findViewById(R.id.btnAddCurrentExpense).setOnClickListener(btn -> addCurrentExpenseRow("", "0", true));
        v.findViewById(R.id.btnAddPostExpense).setOnClickListener(btn -> addPostExpenseRow("", "0"));

        // Recalculate when assumptions change
        TextWatcher assumptionWatcher = new SimpleTextWatcher(this::recalculate);
        etExpRetirementAge.addTextChangedListener(assumptionWatcher);
        etLifeExpectancy.addTextChangedListener(assumptionWatcher);
        etExpInflation.addTextChangedListener(assumptionWatcher);
        etPostRetirementReturn.addTextChangedListener(assumptionWatcher);

        recalculate();
        return v;
    }

    private void loadOrInitExpenses() {
        SharedPreferences p = requireContext().getSharedPreferences(PREFS_EXPENSE, 0);
        int currentCount = p.getInt("current_count", -1);

        if (currentCount == -1) {
            // First launch — load defaults
            for (String[] row : DEFAULT_CURRENT)
                addCurrentExpenseRow(row[0], row[1], Boolean.parseBoolean(row[2]));
            for (String[] row : DEFAULT_POST)
                addPostExpenseRow(row[0], row[1]);
        } else {
            // Load saved data
            for (int i = 0; i < currentCount; i++) {
                String name    = p.getString("c_name_" + i, "");
                String amount  = p.getString("c_amount_" + i, "0");
                boolean checked = p.getBoolean("c_checked_" + i, true);
                addCurrentExpenseRow(name, amount, checked);
            }
            int postCount = p.getInt("post_count", 0);
            for (int i = 0; i < postCount; i++) {
                String name   = p.getString("p_name_" + i, "");
                String amount = p.getString("p_amount_" + i, "0");
                addPostExpenseRow(name, amount);
            }
        }
    }

    private void saveExpenses() {
        SharedPreferences.Editor e = requireContext().getSharedPreferences(PREFS_EXPENSE, 0).edit();
        int currentCount = containerCurrentExpenses.getChildCount();
        e.putInt("current_count", currentCount);
        for (int i = 0; i < currentCount; i++) {
            View row = containerCurrentExpenses.getChildAt(i);
            e.putString("c_name_" + i, ((EditText) row.findViewById(R.id.etExpenseName)).getText().toString());
            e.putString("c_amount_" + i, ((EditText) row.findViewById(R.id.etExpenseAmount)).getText().toString());
            e.putBoolean("c_checked_" + i, ((CheckBox) row.findViewById(R.id.cbAtRetirement)).isChecked());
        }
        int postCount = containerPostExpenses.getChildCount();
        e.putInt("post_count", postCount);
        for (int i = 0; i < postCount; i++) {
            View row = containerPostExpenses.getChildAt(i);
            e.putString("p_name_" + i, ((EditText) row.findViewById(R.id.etPostExpenseName)).getText().toString());
            e.putString("p_amount_" + i, ((EditText) row.findViewById(R.id.etPostExpenseAmount)).getText().toString());
        }
        e.apply();
    }

    private void addCurrentExpenseRow(String name, String amount, boolean checked) {
        View row = LayoutInflater.from(requireContext()).inflate(R.layout.expense_row_current, containerCurrentExpenses, false);
        EditText etName   = row.findViewById(R.id.etExpenseName);
        EditText etAmount = row.findViewById(R.id.etExpenseAmount);
        CheckBox cb       = row.findViewById(R.id.cbAtRetirement);

        etName.setText(name);
        etAmount.setText(amount);
        cb.setChecked(checked);

        TextWatcher tw = new SimpleTextWatcher(() -> { saveExpenses(); recalculate(); });
        etName.addTextChangedListener(tw);
        etAmount.addTextChangedListener(tw);
        cb.setOnCheckedChangeListener((btn, isChecked) -> { saveExpenses(); recalculate(); });

        row.findViewById(R.id.btnDeleteExpense).setOnClickListener(v -> {
            containerCurrentExpenses.removeView(row);
            saveExpenses();
            recalculate();
        });

        containerCurrentExpenses.addView(row);
        recalculate();
    }

    private void addPostExpenseRow(String name, String amount) {
        View row = LayoutInflater.from(requireContext()).inflate(R.layout.expense_row_post, containerPostExpenses, false);
        EditText etName   = row.findViewById(R.id.etPostExpenseName);
        EditText etAmount = row.findViewById(R.id.etPostExpenseAmount);

        etName.setText(name);
        etAmount.setText(amount);

        TextWatcher tw = new SimpleTextWatcher(() -> { saveExpenses(); recalculate(); });
        etName.addTextChangedListener(tw);
        etAmount.addTextChangedListener(tw);

        row.findViewById(R.id.btnDeletePostExpense).setOnClickListener(v -> {
            containerPostExpenses.removeView(row);
            saveExpenses();
            recalculate();
        });

        containerPostExpenses.addView(row);
        recalculate();
    }

    private void recalculate() {
        // ── Read assumptions ──────────────────────────────────────────────────
        int retirementAge     = getInt(etExpRetirementAge, 60);
        int lifeExpectancy    = getInt(etLifeExpectancy, 80);
        double inflationRate  = getDbl(etExpInflation, 6) / 100.0;
        double postReturnRate = getDbl(etPostRetirementReturn, 8) / 100.0;

        // Years until retirement — use saved DOB if available, else default 17 years
        int yearsToRetirement = getYearsToRetirement(retirementAge);
        int retirementYears   = Math.max(0, lifeExpectancy - retirementAge); // years IN retirement

        // ── Sum current expenses ──────────────────────────────────────────────
        double currentContinuing = 0;
        double currentTotal      = 0;
        for (int i = 0; i < containerCurrentExpenses.getChildCount(); i++) {
            View row = containerCurrentExpenses.getChildAt(i);
            double amt = getDblView(row.findViewById(R.id.etExpenseAmount));
            boolean checked = ((CheckBox) row.findViewById(R.id.cbAtRetirement)).isChecked();
            currentTotal += amt;
            if (checked) currentContinuing += amt;
        }

        // ── Sum post expenses ─────────────────────────────────────────────────
        double postTotal = 0;
        for (int i = 0; i < containerPostExpenses.getChildCount(); i++) {
            View row = containerPostExpenses.getChildAt(i);
            postTotal += getDblView(row.findViewById(R.id.etPostExpenseAmount));
        }

        // ── Update running totals ─────────────────────────────────────────────
        tvCurrentTotal.setText(formatCurrency(currentContinuing) + " / mo");
        tvPostTotal.setText(formatCurrency(postTotal) + " / mo");

        // ── Combined today's monthly need at retirement ───────────────────────
        double combinedToday = currentContinuing + postTotal;

        // ── Inflation-adjust to retirement date ───────────────────────────────
        double inflationAdjusted = combinedToday * Math.pow(1 + inflationRate, yearsToRetirement);

        // ── Update result summary ─────────────────────────────────────────────
        tvResultCurrentToday.setText(formatCurrency(currentContinuing) + " / mo");
        tvResultPostToday.setText(formatCurrency(postTotal) + " / mo");
        tvResultCombinedToday.setText(formatCurrency(combinedToday) + " / mo");
        tvInflationLabel.setText(String.format("Inflation adjusted (%d yrs @ %.0f%%)", yearsToRetirement, inflationRate * 100));
        tvResultInflationAdjusted.setText(formatCurrency(inflationAdjusted) + " / mo");

        // ── Minimum corpus: PV of growing annuity ────────────────────────────
        // We need a corpus that, invested at postReturnRate, can pay increasing
        // monthly withdrawals (rising with inflation) for retirementYears years
        // and reach exactly 0 at life expectancy.
        //
        // Formula: PV = PMT_monthly * [1 - ((1+g)/(1+r))^n] / (r - g)
        // where r = monthly return, g = monthly inflation, n = total months
        //
        double minCorpus = 0;
        if (retirementYears > 0 && inflationAdjusted > 0) {
            double monthlyReturn    = postReturnRate / 12.0;
            double monthlyInflation = inflationRate / 12.0;
            int    totalMonths      = retirementYears * 12;

            if (Math.abs(monthlyReturn - monthlyInflation) < 0.0001) {
                // Edge case: return == inflation
                minCorpus = inflationAdjusted * totalMonths;
            } else {
                double ratio = (1 + monthlyInflation) / (1 + monthlyReturn);
                minCorpus = inflationAdjusted * (1 - Math.pow(ratio, totalMonths)) / (monthlyReturn - monthlyInflation);
            }
        }

        tvMinCorpus.setText(formatCrore(minCorpus));
        tvCorpusSubtitle.setText(String.format("to never run out of money (%d → %d yrs, %.0f%% return)",
                retirementAge, lifeExpectancy, postReturnRate * 100));
        tvCorpusNote.setText(String.format("Sustains %s/mo (inflation-adjusted) for %d years",
                formatCurrency(inflationAdjusted), retirementYears));
    }

    private int getYearsToRetirement(int retirementAge) {
        try {
            SharedPreferences p = requireContext().getSharedPreferences("RetirementPlannerData", 0);
            String dob = p.getString("dob", null);
            if (dob == null) return Math.max(0, retirementAge - 43); // fallback
            String[] parts = dob.split("/");
            java.util.Calendar dobCal = java.util.Calendar.getInstance();
            dobCal.set(Integer.parseInt(parts[2]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[0]));
            java.util.Calendar now = java.util.Calendar.getInstance();
            int age = now.get(java.util.Calendar.YEAR) - dobCal.get(java.util.Calendar.YEAR);
            if (now.get(java.util.Calendar.DAY_OF_YEAR) < dobCal.get(java.util.Calendar.DAY_OF_YEAR)) age--;
            return Math.max(1, retirementAge - age);
        } catch (Exception e) { return Math.max(1, retirementAge - 43); }
    }

    private String formatCurrency(double val) {
        if (val >= 10_000_000) return String.format("₹%.2f Cr", val / 10_000_000);
        if (val >= 100_000)    return String.format("₹%.2f L",  val / 100_000);
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("en", "IN"));
        return "₹" + nf.format((long) val);
    }

    private String formatCrore(double val) {
        if (val >= 10_000_000) return String.format("₹%.2f Cr", val / 10_000_000);
        if (val >= 100_000)    return String.format("₹%.2f L",  val / 100_000);
        return String.format("₹%.0f", val);
    }

    private int getInt(EditText et, int def) {
        try { String s = et.getText().toString().trim(); return s.isEmpty() ? def : Integer.parseInt(s); } catch (Exception e) { return def; }
    }
    private double getDbl(EditText et, double def) {
        try { String s = et.getText().toString().trim(); return s.isEmpty() ? def : Double.parseDouble(s); } catch (Exception e) { return def; }
    }
    private double getDblView(EditText et) {
        try { String s = et.getText().toString().trim(); return s.isEmpty() ? 0 : Double.parseDouble(s); } catch (Exception e) { return 0; }
    }

    // Simple TextWatcher that only cares about afterTextChanged
    interface Action { void run(); }
    static class SimpleTextWatcher implements TextWatcher {
        private final Action action;
        SimpleTextWatcher(Action a) { this.action = a; }
        public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
        public void onTextChanged(CharSequence s, int st, int b, int c) {}
        public void afterTextChanged(Editable s) { action.run(); }
    }
}
