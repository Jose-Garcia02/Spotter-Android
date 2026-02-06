package com.josegarcia.appgym.ui.weight_tracker;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.josegarcia.appgym.R;
import com.josegarcia.appgym.data.entities.BodyWeightLog;
import com.josegarcia.appgym.ui.stats.CustomMarkerView;
import com.josegarcia.appgym.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BodyWeightFragment extends Fragment {

    private BodyWeightViewModel mViewModel;
    private BodyWeightAdapter adapter;
    private LineChart chart;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_body_weight, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Apply Window Insets for status bar padding
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.root_layout), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Apply top padding to avoid status bar/camera
            v.setPadding(0, insets.top, 0, 0);
            return windowInsets;
        });

        mViewModel = new ViewModelProvider(this).get(BodyWeightViewModel.class);
        chart = view.findViewById(R.id.chart_body_weight);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_weight_logs);
        FloatingActionButton fab = view.findViewById(R.id.fab_add_weight);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BodyWeightAdapter(this::deleteLog);
        recyclerView.setAdapter(adapter);

        // Chart Setup
        chart.setTouchEnabled(true);
        chart.setPinchZoom(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);

        // Use CustomMarkerView
        CustomMarkerView marker = new CustomMarkerView(getContext(), R.layout.view_chart_marker, Constants.UNIT_KG);
        marker.setChartView(chart);
        chart.setMarker(marker);

        mViewModel.getAllLogs().observe(getViewLifecycleOwner(), logs -> {
            // Update List
            adapter.setLogs(logs);
            // Update Chart
            updateChart(logs);
        });

        fab.setOnClickListener(v -> showAddDialog());
    }

    private void showAddDialog() {
        if (getContext() == null) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_generic_input, null);
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        tvTitle.setText("Nuevo Registro");

        EditText etInput = dialogView.findViewById(R.id.etDialogInput);
        etInput.setHint("Peso en KG");
        etInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        dialogView.findViewById(R.id.btnDialogPositive).setOnClickListener(v -> {
            String txt = etInput.getText().toString();
            if (!txt.isEmpty()) {
                float val = Float.parseFloat(txt);
                mViewModel.insert(new BodyWeightLog(System.currentTimeMillis(), val, null));
                dialog.dismiss();
            }
        });

        dialogView.findViewById(R.id.btnDialogNegative).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void deleteLog(BodyWeightLog log) {
        if (getContext() == null) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_generic_confirmation, null);
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextView tvMessage = dialogView.findViewById(R.id.tvDialogMessage);

        tvTitle.setText("Eliminar Registro");
        tvMessage.setText("¿Estás seguro de que deseas eliminar este registro de peso?");

        dialogView.findViewById(R.id.btnDialogPositive).setOnClickListener(v -> {
            mViewModel.delete(log);
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.btnDialogNegative).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void updateChart(List<BodyWeightLog> logs) {
        if (getContext() == null) return;

        if (logs == null || logs.isEmpty()) {
            chart.setNoDataText("Registra tu primer pesaje");
            chart.setNoDataTextColor(ContextCompat.getColor(getContext(), R.color.text_primary));
            chart.clear();
            return;
        }

        List<Entry> entries = new ArrayList<>();
        // Logs are DESC by default from DAO, need ASC for chart
        List<BodyWeightLog> ascLogs = new ArrayList<>(logs);
        Collections.reverse(ascLogs);

        for (int i = 0; i < ascLogs.size(); i++) {
             entries.add(new Entry(i, ascLogs.get(i).weight, ascLogs.get(i))); // Add data object for Marker
        }

        LineDataSet dataSet = new LineDataSet(entries, "Peso Corporal");
        dataSet.setColor(ContextCompat.getColor(getContext(), R.color.vivid_blue)); // Spotter Blue
        dataSet.setLineWidth(2f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // Smooth
        dataSet.setDrawCircles(false); // Clean
        dataSet.setDrawValues(false);

        // Gradient Fill
        dataSet.setDrawFilled(true);
        if (android.os.Build.VERSION.SDK_INT >= 18) {
             android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable(
                    android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[] { ContextCompat.getColor(getContext(), R.color.vivid_blue), android.graphics.Color.TRANSPARENT }
             );
             dataSet.setFillDrawable(drawable);
        } else {
             dataSet.setFillColor(ContextCompat.getColor(getContext(), R.color.vivid_blue));
             dataSet.setFillAlpha(100);
        }

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        // Lógica de Diseño
        chart.getAxisRight().setEnabled(false);

        // Eje Izquierdo
        chart.getAxisLeft().setTextColor(ContextCompat.getColor(getContext(), R.color.text_secondary));
        chart.getAxisLeft().setGridColor(0xFF333333);
        chart.getAxisLeft().enableGridDashedLine(0f, 0f, 0f);
        chart.getAxisLeft().setDrawGridLines(true);

        // Eje X
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(ContextCompat.getColor(getContext(), R.color.text_secondary));
        xAxis.setDrawGridLines(false); // Limpio

        xAxis.setValueFormatter(new ValueFormatter() {
             private final SimpleDateFormat fmt = new SimpleDateFormat("dd/MM", Locale.getDefault());
             @Override
             public String getFormattedValue(float value) {
                 int index = (int) value;
                 if (index >= 0 && index < ascLogs.size()) {
                     return fmt.format(new Date(ascLogs.get(index).timestamp));
                 }
                 return "";
             }
        });

        chart.getLegend().setTextColor(ContextCompat.getColor(getContext(), R.color.text_primary));

        chart.invalidate();
    }
}
