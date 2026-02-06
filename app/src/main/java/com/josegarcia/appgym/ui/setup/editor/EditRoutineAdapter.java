package com.josegarcia.appgym.ui.setup.editor;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.josegarcia.appgym.R;
import com.josegarcia.appgym.data.entities.RoutineExercise;

import java.util.ArrayList;
import java.util.List;

public class EditRoutineAdapter extends RecyclerView.Adapter<EditRoutineAdapter.ViewHolder> {

    private List<RoutineExercise> exercises = new ArrayList<>();
    private final OnExerciseActionListener listener;

    public interface OnExerciseActionListener {
        void onDelete(RoutineExercise exercise);
        void onUpdate(RoutineExercise exercise);
    }

    public EditRoutineAdapter(OnExerciseActionListener listener) {
        this.listener = listener;
    }

    public void setExercises(List<RoutineExercise> exercises) {
        this.exercises = exercises;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_edit_routine_exercise, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(exercises.get(position));
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        EditText etSets;
        Button btnUnit;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvExerciseName);
            etSets = itemView.findViewById(R.id.etTargetSets);
            btnUnit = itemView.findViewById(R.id.btnTargetUnit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bind(RoutineExercise exercise) {
            name.setText(exercise.exerciseName);

            // Remove previous listeners to avoid loops/wrong triggers
            etSets.setOnFocusChangeListener(null);
            btnUnit.setOnClickListener(null);

            etSets.setText(String.valueOf(exercise.targetSets));
            btnUnit.setText(exercise.targetUnit);

            etSets.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    try {
                        int sets = Integer.parseInt(etSets.getText().toString());
                        if (sets != exercise.targetSets) {
                            exercise.targetSets = sets;
                            listener.onUpdate(exercise);
                        }
                    } catch (NumberFormatException e) {
                        etSets.setText(String.valueOf(exercise.targetSets)); // Revert
                    }
                }
            });

            btnUnit.setOnClickListener(v -> {
                // Toggle Logic: KG -> LBS -> PL -> KG
                switch (exercise.targetUnit) {
                    case "KG": exercise.targetUnit = "LBS"; break;
                    case "LBS": exercise.targetUnit = "PL"; break;
                    case "PL": exercise.targetUnit = "KG"; break;
                    default: exercise.targetUnit = "KG"; break;
                }
                btnUnit.setText(exercise.targetUnit);
                listener.onUpdate(exercise);
            });

            btnDelete.setOnClickListener(v -> listener.onDelete(exercise));
        }
    }
}
