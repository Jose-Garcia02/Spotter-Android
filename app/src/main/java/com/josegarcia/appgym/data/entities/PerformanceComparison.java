package com.josegarcia.appgym.data.entities;
import android.graphics.Color;
public class PerformanceComparison {
    public boolean isHeader; // New flags for grouping by exercise
    public String exerciseName;

    public int setOrder;
    public String status;  // MEJORA, EMPEORA, MANTIENE, NUEVO
    public double currentWeight;
    public int currentReps;
    public double previousWeight;
    public int previousReps;
    public String unit;
    // Color basado en status
    public int getStatusColor() {
        if (status == null) return Color.GRAY;
        switch (status) {
            case "MEJORA": return Color.GREEN;
            case "EMPEORA": return Color.RED;
            case "MANTIENE": return Color.GRAY;
            case "NUEVO": return Color.BLUE; // Or some other neutral color
            default: return Color.GRAY;
        }
    }
}
