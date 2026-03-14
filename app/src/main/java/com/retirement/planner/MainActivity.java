package com.retirement.planner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout  = findViewById(R.id.tabLayout);
        Button btnAbout      = findViewById(R.id.btnAbout);

        viewPager.setAdapter(new PagerAdapter(this));
        viewPager.setOffscreenPageLimit(2); // keep both fragments alive

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "Portfolio Planner" : "Expense Planner");
        }).attach();

        btnAbout.setOnClickListener(v ->
            startActivity(new Intent(this, AboutActivity.class)));
    }

    static class PagerAdapter extends FragmentStateAdapter {
        PagerAdapter(FragmentActivity fa) { super(fa); }

        @Override public int getItemCount() { return 2; }

        @Override
        public Fragment createFragment(int position) {
            return position == 0 ? new PortfolioFragment() : new ExpenseFragment();
        }
    }
}
