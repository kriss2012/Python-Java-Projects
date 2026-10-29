package com.logiclegend.expensetodo.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * Main Android Activity hosting ViewPager2 tabs for Expense, To-Do, and Analytics.
 */
public class MainActivity extends AppCompatActivity {
    private static final String[] TAB_TITLES = {"Expenses", "To-Do Tasks", "Analytics & Budgets"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewPager2 viewPager = new ViewPager2(this);
        TabLayout tabLayout = new TabLayout(this);

        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @Override
            public int getItemCount() {
                return 3;
            }

            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    case 0: return new ExpenseTrackerFragment();
                    case 1: return new TodoListFragment();
                    case 2: default: return new AnalyticsFragment();
                }
            }
        });

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(TAB_TITLES[position])).attach();
    }
}

