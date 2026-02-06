package com.josegarcia.appgym.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.josegarcia.appgym.R;
import com.josegarcia.appgym.ui.home.HomeViewModel.WeekData;
import com.josegarcia.appgym.ui.home.HomeViewModel.DayStatus;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HeatmapVerticalAdapter extends RecyclerView.Adapter<HeatmapVerticalAdapter.WeekViewHolder> {

    private final List<WeekData> weeks;

    public HeatmapVerticalAdapter(List<WeekData> weeks) {
        this.weeks = weeks;
    }

    @NonNull
    @Override
    public WeekViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate a layout that holds a vertical column of 7 days
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_heatmap_vertical_week, parent, false);
        return new WeekViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeekViewHolder holder, int position) {
        WeekData week = weeks.get(position);
        holder.bind(week);
    }

    @Override
    public int getItemCount() {
        return weeks.size();
    }

    static class WeekViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout daysContainer;

        WeekViewHolder(View itemView) {
            super(itemView);
            daysContainer = itemView.findViewById(R.id.containerDays);
        }

        void bind(WeekData week) {
            if (daysContainer == null) return;

            // Prepare 'Today' calendar for comparison
            Calendar todayCal = Calendar.getInstance();
            // Reset to midnight for clean comparison if needed, though simpler YEAR/DAY_OF_YEAR check works
            // strictly speaking depends on timezones, but local device time is usually sufficient for UI.
            int todayYear = todayCal.get(Calendar.YEAR);
            int todayDay = todayCal.get(Calendar.DAY_OF_YEAR);

            // Iterate through the 7 predefined day views in the layout
            // We assume the layout has 7 views with IDs day1..day7 or we iterate children

            for (int i = 0; i < 7; i++) {
                if (i >= daysContainer.getChildCount()) break;

                View dayView = daysContainer.getChildAt(i);

                if (i < week.days.size()) {
                    DayStatus status = week.days.get(i);

                    // Check if this day is Today
                    Calendar statusCal = Calendar.getInstance();
                    statusCal.setTimeInMillis(status.date);
                    boolean isToday = (statusCal.get(Calendar.YEAR) == todayYear &&
                                       statusCal.get(Calendar.DAY_OF_YEAR) == todayDay);

                    // Determinar fondo basado en actividad Y si es hoy
                    if (isToday) {
                        if (status.active) {
                            dayView.setBackgroundResource(R.drawable.bg_heatmap_today_fill);
                        } else {
                            dayView.setBackgroundResource(R.drawable.bg_heatmap_today_empty);
                        }
                    } else {
                        if (status.active) {
                            dayView.setBackgroundResource(R.drawable.bg_heatmap_fill);
                        } else {
                            dayView.setBackgroundResource(R.drawable.bg_heatmap_empty);
                        }
                    }

                    // Listener de Click
                    dayView.setOnClickListener(v -> {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                        String mapTxt = isToday ? " (Hoy)" : "";
                        Toast.makeText(v.getContext(), sdf.format(new Date(status.date)) + mapTxt, Toast.LENGTH_SHORT).show();
                    });

                    dayView.setVisibility(View.VISIBLE);
                } else {
                    dayView.setVisibility(View.INVISIBLE); // No debería pasar si los datos son 7 días correctos
                }
            }
        }
    }
}
