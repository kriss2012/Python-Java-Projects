package com.logiclegend.expensetodo.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.charts.PieChart;

/**
 * Android Fragment displaying MPAndroidChart visual breakdown for category expenses.
 */
public class AnalyticsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        PieChart pieChart = new PieChart(requireContext());
        pieChart.setDescription(null);
        pieChart.setHoleRadius(40f);
        return pieChart;
    }
}

