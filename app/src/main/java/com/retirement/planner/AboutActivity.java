package com.retirement.planner;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    private static final String BITCOIN_ADDRESS = "bc1qqky52mh2zxcmu7jcxcgtgaf24ennal4hjrv7rt";
    private static final String CREATOR_EMAIL   = "yogesh249@gmail.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);  // ← always first

        // Read version dynamically
        try {
            String version = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName;
            ((TextView) findViewById(R.id.tvVersion)).setText(version);
        } catch (Exception e) {
            ((TextView) findViewById(R.id.tvVersion)).setText("—");
        }

        // Copy Bitcoin address to clipboard
        Button btnCopy = findViewById(R.id.btnCopyBitcoin);
        btnCopy.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Bitcoin Address", BITCOIN_ADDRESS);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Bitcoin address copied! ₿ Thank you! ☕", Toast.LENGTH_LONG).show();
        });

        // Open email client
        Button btnEmail = findViewById(R.id.btnEmail);
        btnEmail.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + CREATOR_EMAIL));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Retirement Planner App");
            try {
                startActivity(Intent.createChooser(intent, "Send Email"));
            } catch (Exception e) {
                Toast.makeText(this, "No email app found. Write to: " + CREATOR_EMAIL, Toast.LENGTH_LONG).show();
            }
        });
    }
}
