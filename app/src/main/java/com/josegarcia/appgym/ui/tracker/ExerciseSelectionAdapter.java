package com.josegarcia.appgym.ui.tracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.josegarcia.appgym.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExerciseSelectionAdapter extends RecyclerView.Adapter<ExerciseSelectionAdapter.SelectViewHolder> {

    private List<String> allExercises = new ArrayList<>();
    private final Set<String> selectedExercises = new HashSet<>();
    private final OnSelectionChangedListener listener;

    public interface OnSelectionChangedListener {
        void onSelectionChanged(int count);
    }

    public ExerciseSelectionAdapter(OnSelectionChangedListener listener) {
        this.listener = listener;
    }

    public void setExercises(List<String> exercises) {
        this.allExercises = exercises;
        notifyDataSetChanged();
    }

    public ArrayList<String> getSelectedExercises() {
        return new ArrayList<>(selectedExercises);
    }

    @NonNull
    @Override
    public SelectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise_select, parent, false);
        return new SelectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectViewHolder holder, int position) {
        String name = allExercises.get(position);
        holder.bind(name);
    }

    @Override
    public int getItemCount() {
        return allExercises.size();
    }

    class SelectViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView nameView;

        public SelectViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.cbSelect);
            nameView = itemView.findViewById(R.id.tvExerciseName);

            itemView.setOnClickListener(v -> {
                checkBox.setChecked(!checkBox.isChecked());
                toggleSelection(allExercises.get(getAdapterPosition()), checkBox.isChecked());
            });

            checkBox.setOnClickListener(v ->
                toggleSelection(allExercises.get(getAdapterPosition()), checkBox.isChecked())
            );
        }

        void bind(String name) {
            nameView.setText(name);
            checkBox.setChecked(selectedExercises.contains(name));
        }

        private void toggleSelection(String name, boolean isChecked) {
            if (isChecked) {
                selectedExercises.add(name);
            } else {
                selectedExercises.remove(name);
            }
            listener.onSelectionChanged(selectedExercises.size());
        }
    }
}
