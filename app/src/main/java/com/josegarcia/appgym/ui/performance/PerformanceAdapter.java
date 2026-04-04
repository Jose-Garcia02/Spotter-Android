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
public class PerformanceAdapter extends RecyclerView.Adapter<PerformanceAdapter.ViewHolder> {
    private List<PerformanceComparison> items = new ArrayList<>();
    public void submitList(List<PerformanceComparison> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_performance_set_comparison, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PerformanceComparison item = items.get(position);
        holder.bind(item);
    }
    @Override
    public int getItemCount() {
        return items.size();
    }
    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvSetNumber;
        private final TextView tvPrevStats;
        private final TextView tvCurrentStats;
        private final TextView tvTrend;
        public ViewHolder(@NonNull View itemView) {
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
