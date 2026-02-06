package com.josegarcia.appgym.ui.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.josegarcia.appgym.R;
import com.josegarcia.appgym.data.entities.WorkoutSession;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<WorkoutSession> sessions;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(WorkoutSession session);
    }

    public HistoryAdapter(List<WorkoutSession> sessions, OnItemClickListener listener) {
        this.sessions = sessions;
        this.listener = listener;
    }

    public void updateData(List<WorkoutSession> newSessions) {
        this.sessions = newSessions;
        notifyDataSetChanged();
    }

    public WorkoutSession getSessionAt(int position) {
        if (sessions != null && position >= 0 && position < sessions.size()) {
            return sessions.get(position);
        }
        return null;
    }

    public void removeItem(int position) {
        if (sessions != null && position >= 0 && position < sessions.size()) {
            sessions.remove(position);
            notifyItemRemoved(position);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history_session, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkoutSession session = sessions.get(position);
        holder.bind(session, listener);
    }

    @Override
    public int getItemCount() {
        return sessions != null ? sessions.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay, tvMonth, tvRoutineName, tvDateFull;

        public ViewHolder(View view) {
            super(view);
            tvDay = view.findViewById(R.id.tvDay);
            tvMonth = view.findViewById(R.id.tvMonth);
            tvRoutineName = view.findViewById(R.id.tvRoutineName);
            tvDateFull = view.findViewById(R.id.tvDateFull);
        }

        void bind(WorkoutSession session, OnItemClickListener listener) {
            Date date = new Date(session.date);

            // Format: "25"
            SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());
            tvDay.setText(dayFormat.format(date));

            // Format: "ENE"
            SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault());
            tvMonth.setText(monthFormat.format(date).toUpperCase());

            tvRoutineName.setText(session.routineName);

            // Format: "Lunes, 25 Enero 2024"
            SimpleDateFormat fullFormat = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault());
            tvDateFull.setText(fullFormat.format(date));

            itemView.setOnClickListener(v -> listener.onItemClick(session));
        }
    }
}
