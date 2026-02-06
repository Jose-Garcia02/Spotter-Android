package com.josegarcia.appgym.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.josegarcia.appgym.R;
import com.josegarcia.appgym.data.database.AppDatabase;
import com.josegarcia.appgym.data.entities.BodyWeightLog;
import com.josegarcia.appgym.data.entities.RoutineVolumeEntry;
import com.josegarcia.appgym.ui.tracker.TrackerActivity;
import com.josegarcia.appgym.utils.Constants;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private PieChart donutChart;
    private RecyclerView heatmapRecycler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // UI Components
        donutChart = view.findViewById(R.id.chartDonut);
        heatmapRecycler = view.findViewById(R.id.recyclerHeatmap); // Placeholder for 5.5

        // Weight Tracker Logic
        com.google.android.material.card.MaterialCardView weightCard = view.findViewById(R.id.cardWeightTracker);
        TextView tvWeight = view.findViewById(R.id.tvCurrentWeight);
        TextView tvSubtitle = view.findViewById(R.id.tvWeightSubtitle);

        weightCard.setOnClickListener(v -> showWeightInputDialog());

        HomeViewModel viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        viewModel.getLastWeight().observe(getViewLifecycleOwner(), log -> {
            boolean isRecent = false;

            if (log != null) {
                tvWeight.setText(String.format("%.1f kg", log.weight));

                // Check recency (7 days)
                long diff = System.currentTimeMillis() - log.timestamp;
                if (diff < 7 * 24 * 60 * 60 * 1000L) {
                    isRecent = true;
                    tvSubtitle.setText("Actualizado hace " + (diff / (24 * 60 * 60 * 1000L)) + " días");
                } else {
                    tvSubtitle.setText("Hace tiempo que no te pesas");
                }
            } else {
                tvWeight.setText("Registrar Peso");
                tvSubtitle.setText("Toca para añadir");
            }

            // Update Card Stroke
            int colorRes = isRecent ? R.color.vivid_blue : R.color.vivid_yellow;
            weightCard.setStrokeColor(getResources().getColor(colorRes, null));
        });

        // 5.3 Donut Chart Logic
        setupDonutChart();
        viewModel.getWeeklyVolume().observe(getViewLifecycleOwner(), this::updateDonutChart);
        viewModel.loadWeeklyVolume();

        // Heatmap Logic
        setupHeatmap();
        // Update observer
        viewModel.getHeatmapData().observe(getViewLifecycleOwner(), this::updateHeatmap);
        viewModel.loadHeatmap(12); // Load last 12 weeks (~3 months) history

        return view;
    }

    // region Métodos Privados UI
    private void setupDonutChart() {
        if (donutChart == null) return;
        donutChart.setDescription(null);
        donutChart.setDrawHoleEnabled(true);
        donutChart.setHoleColor(Color.TRANSPARENT);
        donutChart.setTransparentCircleColor(Color.WHITE);
        donutChart.setTransparentCircleAlpha(110);
        donutChart.setHoleRadius(58f);
        donutChart.setTransparentCircleRadius(61f);
        donutChart.setDrawCenterText(true);
        donutChart.setRotationEnabled(false);
        donutChart.setHighlightPerTapEnabled(true);
        donutChart.setCenterText("Total\nTonelaje");
        donutChart.setCenterTextColor(getResources().getColor(R.color.text_primary, null));
        donutChart.setCenterTextSize(12f);
        donutChart.getLegend().setTextColor(getResources().getColor(R.color.text_secondary, null));
        donutChart.setEntryLabelColor(Color.WHITE);
        donutChart.setEntryLabelTextSize(10f);
        donutChart.setNoDataText("Sin datos esta semana");
        donutChart.setNoDataTextColor(getResources().getColor(R.color.text_secondary, null));
    }

    private void updateDonutChart(List<RoutineVolumeEntry> data) {
        if (data == null || data.isEmpty()) {
            donutChart.clear();
            donutChart.setCenterText("0 Kg");
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        float total = 0;
        for (RoutineVolumeEntry entry : data) {
            if (entry.volume > 0) {
                entries.add(new PieEntry(entry.volume, entry.routineName));
                total += entry.volume;
            }
        }

        if (entries.isEmpty()) {
            donutChart.clear();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        // Custom Colors
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#3B82F6")); // Blue
        colors.add(Color.parseColor("#EF4444")); // Red
        colors.add(Color.parseColor("#22C55E")); // Green
        colors.add(Color.parseColor("#EAB308")); // Yellow
        colors.add(Color.parseColor("#A855F7")); // Purple

        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(dataSet);
        donutChart.setData(pieData);
        donutChart.setCenterText((int)total + " Kg\nSemanal");
        donutChart.invalidate();
        donutChart.animateY(1000);
    }

    private void setupHeatmap() {
        if (heatmapRecycler == null) return;
        // Horizontal layout for Weeks
        heatmapRecycler.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        // Reverse layout to show latest at right?
        // Usually GitHub style is Left -> Right (Old -> New).
        // My ViewModel loads startDate as (current - 12 weeks).
        // So index 0 is oldest.
        // So Standard Horizontal is fine.
    }

    private void updateHeatmap(HomeViewModel.HeatmapResult result) {
        if (result == null || result.weeks == null) return;
        heatmapRecycler.setAdapter(new HeatmapVerticalAdapter(result.weeks));
        // Scroll to end to show latest
        heatmapRecycler.scrollToPosition(result.weeks.size() - 1);
    }

    private void showWeightInputDialog() {
        if (getContext() == null) return;

        // Uses custom dialog layout
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_generic_input, null);
        builder.setView(dialogView);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        }

        TextView title = dialogView.findViewById(R.id.tvDialogTitle);
        EditText input = dialogView.findViewById(R.id.etDialogInput);
        Button btnSave = dialogView.findViewById(R.id.btnDialogPositive);
        Button btnCancel = dialogView.findViewById(R.id.btnDialogNegative);

        title.setText("Registrar Peso Corporal");
        input.setHint("Ej. 75.5");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);

        btnSave.setOnClickListener(v -> {
            String txt = input.getText().toString();
            if (!txt.isEmpty()) {
                try {
                    float weight = Float.parseFloat(txt);
                    saveWeight(weight);
                    dialog.dismiss();
                } catch (NumberFormatException e) {
                    input.setError("Invalido");
                }
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void saveWeight(float weight) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase.getDatabase(getContext()).bodyWeightDao().insert(
                    new BodyWeightLog(System.currentTimeMillis(), weight, null)
            );
        });
    }
    // endregion
}
