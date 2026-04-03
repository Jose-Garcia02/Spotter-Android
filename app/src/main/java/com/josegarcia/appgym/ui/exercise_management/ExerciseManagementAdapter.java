package com.josegarcia.appgym.ui.exercise_management;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.josegarcia.appgym.R;
import com.josegarcia.appgym.data.entities.ExerciseCatalog;

import java.util.ArrayList;
import java.util.List;

public class ExerciseManagementAdapter extends RecyclerView.Adapter<ExerciseManagementAdapter.ExerciseViewHolder> {
    private List<ExerciseCatalog> exercises = new ArrayList<>();
    private OnExerciseListener listener;

    public interface OnExerciseListener {
        void onEdit(ExerciseCatalog exercise);
        void onDelete(ExerciseCatalog exercise);
    }

    public ExerciseManagementAdapter(OnExerciseListener listener) {
        this.listener = listener;
    }

    public void setExercises(List<ExerciseCatalog> exercises) {
        this.exercises = exercises != null ? exercises : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public ExerciseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercise_catalog, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ExerciseViewHolder holder, int position) {
        ExerciseCatalog exercise = exercises.get(position);
        holder.bind(exercise);
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    class ExerciseViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvUnit, tvMuscleTag;
        ImageButton btnEdit, btnDelete;

        ExerciseViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvExerciseName);
            tvUnit = itemView.findViewById(R.id.tvExerciseUnit);
            tvMuscleTag = itemView.findViewById(R.id.tvExerciseMuscleTag);
            btnEdit = itemView.findViewById(R.id.btnEditExercise);
            btnDelete = itemView.findViewById(R.id.btnDeleteExercise);
        }

        void bind(ExerciseCatalog exercise) {
            tvName.setText(exercise.name);
            tvUnit.setText(exercise.defaultUnit);
            tvMuscleTag.setText(exercise.muscleTag);

            btnEdit.setOnClickListener(v -> listener.onEdit(exercise));
            btnDelete.setOnClickListener(v -> listener.onDelete(exercise));
        }
    }
}

