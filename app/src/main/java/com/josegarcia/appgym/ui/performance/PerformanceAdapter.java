package com.josegarcia.appgym.ui.performance;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.josegarcia.appgym.R;
import com.josegarcia.appgym.data.entities.PerformanceComparison;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
public class PerformanceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private List<PerformanceComparison> items = new ArrayList<>();
    public void submitList(List<PerformanceComparison> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }
    @Override
    public int getItemViewType(int position) {
        return items.get(position).isHeader ? TYPE_HEADER : TYPE_ITEM;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_performance_exercise_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_performance_set_comparison, parent, false);
            return new ItemViewHolder(view);
        }
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PerformanceComparison item = items.get(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind(item);
        } else if (holder instanceof ItemViewHolder) {
            ((ItemViewHolder) holder).bind(item);
        }
    }
    @Override
    public int getItemCount() {
        return items.size();
    }
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvHeaderExerciseName;
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHeaderExerciseName = itemView.findViewById(R.id.tvHeaderExerciseName);
        }
        public void bind(PerformanceComparison item) {
            tvHeaderExerciseName.setText(item.exerciseName);
        }
    }
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvSetNumber;
        private final TextView tvPrevStats;
        private final TextView tvCurrentStats;
        private final TextView tvTrend;
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSetNumber = itemView.findViewById(R.id.tvSetNumber);
            tvPrevStats = itemView.findViewById(R.id.tvPrevStats);
            tvCurrentStats = itemView.findViewById(R.id.tvCurrentStats);
            tvTrend = itemView.findViewById(R.id.tvTrend);
        }
        public void bind(PerformanceComparison item) {
            tvSetNumber.setText(String.valueOf(item.setOrder));
            if ("NUEVO".equals(item.status)) {
                tvPrevStats.setText("-");
                tvTrend.setText("NUEVO");
            } else {
                tvPrevStats.setText(formatStats(item.previousWeight, item.previousReps, item.unit));
                // Calculate simple trend text if possible
                if (item.currentWeight != item.previousWeight) {
                    double diff = item.currentWeight - item.previousWeight;
                    String sign = diff > 0 ? "+" : "";
                    tvTrend.setText(String.format(Locale.US, "%s%.1f %s", sign, diff, item.unit));
                } else if (item.currentReps != item.previousReps) {
                    int diff = item.currentReps - item.previousReps;
                    String sign = diff > 0 ? "+" : "";
                    tvTrend.setText(String.format(Locale.US, "%s%d reps", sign, diff));
                } else {
                    tvTrend.setText("=");
                }
            }
            tvCurrentStats.setText(formatStats(item.currentWeight, item.currentReps, item.unit));
            tvTrend.setTextColor(item.getStatusColor());
        }
        private String formatStats(double weight, int reps, String unit) {
            String wStr = (weight % 1 == 0) ? String.format(Locale.US, "%.0f", weight) : String.format(Locale.US, "%.1f", weight);
            return String.format(Locale.US, "%s%s x %d", wStr, unit, reps);
        }
    }
}
