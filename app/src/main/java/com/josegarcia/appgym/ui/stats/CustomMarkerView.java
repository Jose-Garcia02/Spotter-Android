package com.josegarcia.appgym.ui.stats;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.josegarcia.appgym.R;
import com.josegarcia.appgym.data.entities.BodyWeightLog;
import com.josegarcia.appgym.data.entities.ExerciseProgressEntry;
import com.josegarcia.appgym.utils.Constants;

import java.util.Date;
import java.util.Locale;

public class CustomMarkerView extends MarkerView {
    private final TextView tvValue;
    private final TextView tvLabel;
    private String currentUnit;

    public CustomMarkerView(Context context, int layoutResource, String currentUnit) {
        super(context, layoutResource);
        this.currentUnit = currentUnit != null ? currentUnit : Constants.UNIT_KG;
        tvValue = findViewById(R.id.tvMarkerValue);
        tvLabel = findViewById(R.id.tvMarkerLabel);
    }

    // Constructor for tools
    public CustomMarkerView(Context context, int layoutResource) {
        this(context, layoutResource, Constants.UNIT_KG);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        if (e.getData() instanceof ExerciseProgressEntry) {
            ExerciseProgressEntry entry = (ExerciseProgressEntry) e.getData();

            String unitLabel = (entry.unit != null && !entry.unit.isEmpty()) ? entry.unit : currentUnit;
            if (unitLabel.equalsIgnoreCase(Constants.UNIT_PLACAS)) {
                unitLabel = "pl";
            } else if (unitLabel.equalsIgnoreCase(Constants.UNIT_LBS)) {
                unitLabel = "lbs";
            } else if (unitLabel.equalsIgnoreCase(Constants.UNIT_KG)) {
                unitLabel = "kg";
            }

            // Arriba: Peso
            tvValue.setText(String.format(Locale.getDefault(), "%.1f %s", entry.weight, unitLabel));
            // Abajo: Reps
            tvLabel.setText(String.format(Locale.getDefault(), "%d Reps", entry.reps));
        } else if (e.getData() instanceof BodyWeightLog) {
            BodyWeightLog log = (BodyWeightLog) e.getData();
            // Arriba: Peso
            tvValue.setText(String.format(Locale.getDefault(), "%.1f kg", log.weight));
            // Abajo: Fecha
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM", Locale.getDefault());
            tvLabel.setText(sdf.format(new Date(log.timestamp)));
        } else {
             // Fallback for simple Entry
             tvValue.setText(String.format(Locale.getDefault(), "%.1f", e.getY()));
             tvLabel.setText("");
        }
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2f), -getHeight());
    }
}
