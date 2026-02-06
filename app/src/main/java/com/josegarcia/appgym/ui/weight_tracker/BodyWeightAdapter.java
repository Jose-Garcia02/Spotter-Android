package com.josegarcia.appgym.ui.weight_tracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.josegarcia.appgym.R;
import com.josegarcia.appgym.data.entities.BodyWeightLog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BodyWeightAdapter extends RecyclerView.Adapter<BodyWeightAdapter.WeightViewHolder> {

    private List<BodyWeightLog> logs = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onDeleteClick(BodyWeightLog log);
    }

    public BodyWeightAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setLogs(List<BodyWeightLog> logs) {
        this.logs = logs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WeightViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_body_weight, parent, false);
        return new WeightViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull WeightViewHolder holder, int position) {
        BodyWeightLog log = logs.get(position);
        holder.bind(log);
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    static class WeightViewHolder extends RecyclerView.ViewHolder {
        private final TextView dateText;
        private final TextView weightText;
        private final ImageButton deleteBtn;
        private final OnItemClickListener listener;
        private BodyWeightLog currentLog;

        public WeightViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            this.listener = listener;
            dateText = itemView.findViewById(R.id.text_date);
            weightText = itemView.findViewById(R.id.text_weight);
            deleteBtn = itemView.findViewById(R.id.btn_delete);

            deleteBtn.setOnClickListener(v -> {
                if (currentLog != null) {
                    listener.onDeleteClick(currentLog);
                }
            });
        }

        public void bind(BodyWeightLog log) {
            this.currentLog = log;
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            dateText.setText(sdf.format(new Date(log.timestamp)));
            weightText.setText(String.format(Locale.getDefault(), "%.1f kg", log.weight));
        }
    }
}
