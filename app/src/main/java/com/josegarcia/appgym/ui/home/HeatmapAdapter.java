package com.josegarcia.appgym.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.josegarcia.appgym.R;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.widget.Toast;

public class HeatmapAdapter extends RecyclerView.Adapter<HeatmapAdapter.ViewHolder> {

    private final List<Boolean> activityStatus; // true = active, false = inactive
    private final List<Long> dateTimestamps; // New field for dates

    public HeatmapAdapter(List<Boolean> activityStatus, List<Long> dateTimestamps) {
        this.activityStatus = activityStatus;
        this.dateTimestamps = dateTimestamps;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_heatmap_day, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        boolean active = activityStatus.get(position);
        if (active) {
            holder.itemView.setBackgroundResource(R.drawable.bg_heatmap_fill);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.bg_heatmap_empty);
        }

        // Add click listener to show date
        holder.itemView.setOnClickListener(v -> {
            if (dateTimestamps != null && position < dateTimestamps.size()) {
                long ts = dateTimestamps.get(position);
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                Toast.makeText(v.getContext(), sdf.format(new Date(ts)), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return activityStatus.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
