package com.retirement.planner;

import android.graphics.Color;
import android.view.View;
import android.widget.EditText;

/**
 * Attaches default-value behaviour to an EditText:
 *  - Shows the default in light gray when the field is empty and unfocused
 *  - Clears the field when the user taps it
 *  - Restores the gray default if the user leaves without typing anything
 *  - getDouble() / getInt() always returns the default if the field is empty
 */
public class DefaultValueHelper {


    // REPLACE with these two helper methods:
    private static int colorDefault(EditText et) {
        return com.google.android.material.color.MaterialColors.getColor(et, android.R.attr.textColorHint);
    }
    private static int colorUser(EditText et) {
        return com.google.android.material.color.MaterialColors.getColor(et, android.R.attr.textColorPrimary);
    }
    /**
     * Attach to an EditText with a numeric string default.
     */
    public static void attach(EditText et, String defaultValue) {
        // Show default immediately if field is empty
        if (et.getText().toString().trim().isEmpty()) {
            showDefault(et, defaultValue);
        }

        et.setOnFocusChangeListener((v, hasFocus) -> {
            String current = et.getText().toString().trim();
            if (hasFocus) {
                // User tapped — clear the gray default so they can type freely
                if (current.equals(defaultValue) && et.getCurrentTextColor() == colorDefault(et)) {
                    et.setText("");
                    et.setTextColor(colorUser(et));
                }
            } else {
                // User left — if empty, restore gray default
                if (current.isEmpty()) {
                    showDefault(et, defaultValue);
                }
            }
        });
    }

    private static void showDefault(EditText et, String defaultValue) {
        et.setText(defaultValue);
        et.setTextColor(colorDefault(et));
    }

    /**
     * Returns the user-entered double, or the default if the field shows gray default text.
     */
    public static double getDouble(EditText et, double defaultValue) {
        String text = et.getText().toString().trim();
        if (text.isEmpty()) return defaultValue;
        // If showing gray default, use default value
        if (et.getCurrentTextColor() == colorDefault(et)) return defaultValue;
        try { return Double.parseDouble(text); }
        catch (Exception e) { return defaultValue; }
    }

    /**
     * Returns the user-entered int, or the default if the field shows gray default text.
     */
    public static int getInt(EditText et, int defaultValue) {
        String text = et.getText().toString().trim();
        if (text.isEmpty()) return defaultValue;
        if (et.getCurrentTextColor() == colorDefault(et)) return defaultValue;
        try { return Integer.parseInt(text); }
        catch (Exception e) { return defaultValue; }
    }

    /**
     * Returns the raw string value — empty string if showing gray default.
     */
    public static String getString(EditText et) {
        if (et.getCurrentTextColor() == colorDefault(et)) return "";
        return et.getText().toString().trim();
    }

    /**
     * Returns true if the field is showing the gray default (not user-entered).
     */
    public static boolean isDefault(EditText et) {
        return et.getCurrentTextColor() == colorDefault(et);
    }
}
