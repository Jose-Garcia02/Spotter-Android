package com.josegarcia.appgym.ui.stats;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.josegarcia.appgym.R;
import com.josegarcia.appgym.data.database.AppDatabase;
import com.josegarcia.appgym.data.entities.ExerciseProgressEntry;
import com.josegarcia.appgym.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatsFragment extends Fragment {

    private Spinner spinner;
    private RadioGroup radioGroupMetric;
    private LineChart chart;
    private TextView summaryView;
    private ChipGroup chipGroupSeries;
    private Button btnWeightTracker;

    private List<ExerciseProgressEntry> currentDetailedData;
    private String currentExerciseName;

    private long referenceTimestamp = 0;

    private final int[] SERIES_COLORS = {
            Color.rgb(59, 130, 246), // vivid_blue
            Color.rgb(239, 68, 68),  // vivid_red
            Color.rgb(34, 197, 94),  // vivid_green
            Color.rgb(234, 179, 8),  // vivid_yellow
            Color.MAGENTA,
            Color.CYAN
    };

    private static final String KEY_CURRENT_EXERCISE = "current_exercise";

    private final AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String selected = (String) parent.getItemAtPosition(position);
            if (selected != null && !selected.equals(currentExerciseName)) {
                currentExerciseName = selected;
                loadExerciseData(selected);
            }
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CURRENT_EXERCISE, currentExerciseName);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spinner = view.findViewById(R.id.spinnerExercises);
        radioGroupMetric = view.findViewById(R.id.radioGroupMetric);
        chart = view.findViewById(R.id.chartProgress);
        summaryView = view.findViewById(R.id.tvStatsSummary);
        chipGroupSeries = view.findViewById(R.id.chipGroupSeries);
        btnWeightTracker = view.findViewById(R.id.btn_open_weight_tracker);

        if (savedInstanceState != null) {
            currentExerciseName = savedInstanceState.getString(KEY_CURRENT_EXERCISE);
        }

        setupChart();
        loadExercises();

        btnWeightTracker.setOnClickListener(v -> checkWeightDataAndNavigate(v));

        // Listener attached in loadExercises now
        radioGroupMetric.setOnCheckedChangeListener((group, checkedId) -> updateChart());
        // Chips have individual listeners now
    }

    private void checkWeightDataAndNavigate(View view) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<?> logs = AppDatabase.getDatabase(getContext()).bodyWeightDao().getAllLogsSync();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (logs != null && !logs.isEmpty()) {
                        Navigation.findNavController(view).navigate(R.id.bodyWeightFragment);
                    } else {
                        Toast.makeText(getContext(), "No hay datos de peso registrados aún.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void setupChart() {
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.setDrawGridBackground(false);
        // chart.setBackgroundColor(Color.TRANSPARENT); // Optional

        // Custom Marker
        CustomMarkerView marker = new CustomMarkerView(getContext(), R.layout.view_chart_marker, Constants.UNIT_KG); // Unit defaults to KG, dynamic would be better
        marker.setChartView(chart);
        chart.setMarker(marker);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.parseColor("#AAAAAA"));
        xAxis.setDrawGridLines(false); // Clean look
        xAxis.setValueFormatter(new ValueFormatter() {
            private final SimpleDateFormat mFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
            @Override
            public String getFormattedValue(float value) {
                long millis = (long) value + referenceTimestamp;
                return mFormat.format(new Date(millis));
            }
        });

        chart.getAxisLeft().setTextColor(Color.parseColor("#AAAAAA"));
        chart.getAxisLeft().setGridColor(Color.parseColor("#333333"));
        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setTextColor(Color.WHITE);
        chart.getLegend().setForm(Legend.LegendForm.CIRCLE); // Change legend form to CIRCLE
    }

    private void loadExercises() {
        if (getContext() == null) return;
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<String> exercises = AppDatabase.getDatabase(getContext()).workoutDao().getExercisesWithHistory();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    // Detach listener to prevent auto-trigger on setAdapter if we have a saved state
                    spinner.setOnItemSelectedListener(null);

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                            R.layout.item_text_dark, exercises);
                    ArrayAdapter<String> defaultAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, exercises);
                    defaultAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(defaultAdapter);

                    // Restore selection if exists
                    if (currentExerciseName != null) {
                        int pos = defaultAdapter.getPosition(currentExerciseName);
                        if (pos >= 0) {
                            spinner.setSelection(pos, false); // false = no animation
                        }
                    }

                    // Re-attach listener
                    spinner.setOnItemSelectedListener(spinnerListener);

                    // Force load data if we have a selection (either restored or default 0)
                    if (spinner.getSelectedItem() != null) {
                        String selected = (String) spinner.getSelectedItem();
                        // If restored state matches spinner, or if it's first load
                        if (currentExerciseName == null || currentExerciseName.equals(selected)) {
                             currentExerciseName = selected; // Ensure sync
                             loadExerciseData(currentExerciseName);
                        } else {
                             // Fallback if needed
                             currentExerciseName = selected;
                             loadExerciseData(currentExerciseName);
                        }
                    }
                });
            }
        });
    }

    private void loadExerciseData(String exerciseName) {
        if (getContext() == null) return;
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<ExerciseProgressEntry> data = AppDatabase.getDatabase(getContext())
                    .workoutDao().getDetailedProgressForExercise(exerciseName);

            if (!data.isEmpty()) {
                referenceTimestamp = data.get(0).date;
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    currentDetailedData = data;
                    if (data.isEmpty()) {
                        summaryView.setText("No hay datos suficientes para este ejercicio.");
                        chart.clear();
                        chipGroupSeries.removeAllViews();
                    } else {
                        // Populate chips for series found (1, 2, 3...)
                        populateSeriesChips(data);
                        updateChart();
                        summaryView.setText(""); // Clear placeholder
                    }
                });
            }
        });
    }

    private void populateSeriesChips(List<ExerciseProgressEntry> data) {
        chipGroupSeries.removeAllViews();
        // Find max series index involved
        int maxSeries = 0;
        for (ExerciseProgressEntry e : data) maxSeries = Math.max(maxSeries, e.setOrder);

        // Limit to reasonable number, e.g., 6 series
        maxSeries = Math.min(maxSeries, 6);

        for (int i = 1; i <= maxSeries; i++) {
            Chip chip = new Chip(getContext());
            chip.setText("Serie " + i);
            chip.setCheckable(true);
            chip.setChecked(true);
            chip.setTextColor(Color.WHITE);
            chip.setChipIconVisible(false);
            chip.setCheckedIconVisible(false);
            chip.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            // To make it look like a button, not a switch
            // We rely on standard chip styling but ensure no check icon

            chip.setId(View.generateViewId());
            chip.setTag(i);

            chip.setOnClickListener(v -> {
                Chip clickedChip = (Chip) v;
                int clickedTag = (int) clickedChip.getTag();

                // Analyze state AFTER the auto-toggle
                boolean isCheckedNow = clickedChip.isChecked();

                // Count how many others are checked
                int otherCheckedCount = 0;
                for (int j = 0; j < chipGroupSeries.getChildCount(); j++) {
                    Chip c = (Chip) chipGroupSeries.getChildAt(j);
                    if (c != clickedChip && c.isChecked()) {
                        otherCheckedCount++;
                    }
                }

                if (!isCheckedNow) {
                    // Was checked, now unchecked.
                    if (otherCheckedCount == 0) {
                        // Case: It was the ONLY one checked. User toggled it off.
                        // Action: Reset to show ALL.
                        checkAll();
                    } else {
                        // Case: It was one of multiple checked.
                        // Action: Focus ONLY this one (User wants to isolate it).
                        checkOnly(clickedTag);
                    }
                } else {
                    // Was unchecked, now checked.
                    // Case: User clicked an inactive series.
                    // Action: Focus ONLY this one (Swap selection to this).
                    checkOnly(clickedTag);
                }

                updateChart();
            });


            chipGroupSeries.addView(chip);
        }
    }

    private void checkOnly(int targetTag) {
        for (int i = 0; i < chipGroupSeries.getChildCount(); i++) {
            Chip c = (Chip) chipGroupSeries.getChildAt(i);
            int tag = (int) c.getTag();
            c.setChecked(tag == targetTag);
        }
    }

    private void checkAll() {
        for (int i = 0; i < chipGroupSeries.getChildCount(); i++) {
            Chip c = (Chip) chipGroupSeries.getChildAt(i);
            c.setChecked(true);
        }
    }

    private boolean isSingleSelectionActive() {
        int checkedCount = 0;
        for (int i = 0; i < chipGroupSeries.getChildCount(); i++) {
            Chip c = (Chip) chipGroupSeries.getChildAt(i);
            if (c.isChecked()) checkedCount++;
        }
        return checkedCount == 1;
    }

    private boolean isOnlyChecked(int targetTag) {
        for (int i = 0; i < chipGroupSeries.getChildCount(); i++) {
            Chip c = (Chip) chipGroupSeries.getChildAt(i);
            int tag = (int) c.getTag();
            if (c.isChecked() && tag != targetTag) return false;
            if (!c.isChecked() && tag == targetTag) return false;
        }
        return true;
    }

    private void updateChart() {
        if (currentDetailedData == null || currentDetailedData.isEmpty()) return;

        boolean showWeight = radioGroupMetric.getCheckedRadioButtonId() == R.id.rbWeight;
        List<ILineDataSet> dataSets = new ArrayList<>();

        // Group data by series index
        Map<Integer, List<Entry>> seriesEntries = new HashMap<>();

        for (ExerciseProgressEntry entry : currentDetailedData) {
            float x = (float) (entry.date - referenceTimestamp);
            float y = showWeight ? (float) entry.weight : (float) entry.reps;

            if (!seriesEntries.containsKey(entry.setOrder)) {
                seriesEntries.put(entry.setOrder, new ArrayList<>());
            }
            // Only add if we want to show this series
            if (isSeriesChecked(entry.setOrder)) {
                seriesEntries.get(entry.setOrder).add(new Entry(x, y, entry));
            }
        }

        for (Map.Entry<Integer, List<Entry>> set : seriesEntries.entrySet()) {
            int setIndex = set.getKey();
            if (!isSeriesChecked(setIndex)) continue;

            LineDataSet d = new LineDataSet(set.getValue(), "Serie " + setIndex);
            int colorIndex = (setIndex - 1) % SERIES_COLORS.length;
            d.setColor(SERIES_COLORS[colorIndex]);

            // Estilo Spotter Dark - Líneas lineales con puntos
            d.setMode(LineDataSet.Mode.LINEAR); // Líneas lineales
            d.setDrawFilled(false); // Sin área rellena
            d.setDrawCircles(true); // Mostrar puntos
            d.setCircleRadius(4f);
            d.setCircleHoleRadius(2f);
            d.setCircleColor(SERIES_COLORS[colorIndex]);
            d.setCircleHoleColor(Color.BLACK); // Color del agujero coincide con el fondo aprox.
            d.setLineWidth(2f);
            d.setDrawValues(false);

            dataSets.add(d);
        }

        if (dataSets.isEmpty()) {
            chart.clear();
        } else {
            LineData lineData = new LineData(dataSets);
            chart.setData(lineData);

            // Ajuste de posicionamiento de la leyenda
            chart.getLegend().setYOffset(10f); // Separar ligeramente del eje

            chart.invalidate();
            chart.animateX(500);
        }
    }

    private boolean isSeriesChecked(int setOrder) {
        for (int i = 0; i < chipGroupSeries.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupSeries.getChildAt(i);
            if (chip.isChecked() && (int)chip.getTag() == setOrder) {
                return true;
            }
        }
        return false;
    }
}
