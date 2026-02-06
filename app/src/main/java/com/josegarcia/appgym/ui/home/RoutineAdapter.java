package com.josegarcia.appgym.ui.home;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.josegarcia.appgym.R;
import com.josegarcia.appgym.data.entities.Routine;

import java.util.ArrayList;
import java.util.List;

public class RoutineAdapter extends RecyclerView.Adapter<RoutineAdapter.RoutineViewHolder> {

    private List<Routine> routines = new ArrayList<>();
    private final OnRoutineClickListener listener;

    public interface OnRoutineClickListener {
        void onRoutineClick(Routine routine);
    }

    public RoutineAdapter(OnRoutineClickListener listener) {
        this.listener = listener;
    }

    public void setRoutines(List<Routine> routines) {
        this.routines = routines;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RoutineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_routine_card, parent, false);
        return new RoutineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoutineViewHolder holder, int position) {
        Routine routine = routines.get(position);
        holder.bind(routine, listener);
    }

    @Override
    public int getItemCount() {
        return routines.size();
    }

    static class RoutineViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView card;
        private final TextView nameText;

        public RoutineViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardRoutine);
            nameText = itemView.findViewById(R.id.textRoutineName);
        }

        public void bind(Routine routine, OnRoutineClickListener listener) {
            nameText.setText(routine.name);

            // Set dynamic color if needed
            int color = ContextCompat.getColor(itemView.getContext(), routine.colorResId != 0 ? routine.colorResId : R.color.text_primary);
            nameText.setTextColor(color);
            card.setStrokeColor(color);

            itemView.setOnClickListener(v -> listener.onRoutineClick(routine));
        }
    }
}
