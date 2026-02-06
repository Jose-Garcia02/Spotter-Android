package com.josegarcia.appgym.ui.tracker;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.josegarcia.appgym.R;
import com.josegarcia.appgym.utils.Constants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TrackerAdapter extends RecyclerView.Adapter<TrackerAdapter.ExerciseViewHolder> {

    private final List<ExerciseEntry> exercises;

    public TrackerAdapter(List<ExerciseEntry> exercises) {
        this.exercises = exercises;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercise_card, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        ExerciseEntry exercise = exercises.get(position);
        holder.bind(exercise);
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    public class ExerciseViewHolder extends RecyclerView.ViewHolder {
        TextView tvExerciseName;
        TextView tvUnitToggle; // New
        TextView tvWeightHeader; // New
        LinearLayout setsContainer;
        Button btnAddSet;

        ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExerciseName = itemView.findViewById(R.id.tvExerciseName);
            tvUnitToggle = itemView.findViewById(R.id.tvUnitToggle);
            tvWeightHeader = itemView.findViewById(R.id.tvWeightHeader);
            setsContainer = itemView.findViewById(R.id.setsContainer);
            btnAddSet = itemView.findViewById(R.id.btnAddSet);
        }

        void bind(ExerciseEntry exercise) {
            tvExerciseName.setText(exercise.name);

            // Unit Logic
            tvUnitToggle.setText(exercise.unit.toUpperCase());
            String headerText;
            switch (exercise.unit) {
                case Constants.UNIT_PLACAS:
                    headerText = itemView.getContext().getString(R.string.header_plates);
                    break;
                case Constants.UNIT_LBS:
                    headerText = itemView.getContext().getString(R.string.header_lbs);
                    break;
                default:
                    headerText = itemView.getContext().getString(R.string.header_kg);
                    break;
            }
            if (tvWeightHeader != null) tvWeightHeader.setText(headerText);

            tvUnitToggle.setOnClickListener(v -> {
                switch (exercise.unit) {
                    case Constants.UNIT_KG:
                        exercise.unit = Constants.UNIT_LBS;
                        break;
                    case Constants.UNIT_LBS:
                        exercise.unit = Constants.UNIT_PLACAS;
                        break;
                    default:
                        exercise.unit = Constants.UNIT_KG;
                        break;
                }
                notifyItemChanged(getAdapterPosition());
            });

            // Manual Exercise Removal (Long Click)
            itemView.setOnLongClickListener(v -> {
                new androidx.appcompat.app.AlertDialog.Builder(itemView.getContext())
                        .setTitle("Eliminar Ejercicio")
                        .setMessage("¿Eliminar " + exercise.name + " de la sesión actual?")
                        .setPositiveButton("Eliminar", (dialog, which) -> {
                            int pos = getAdapterPosition();
                            if (pos != RecyclerView.NO_POSITION) {
                                exercises.remove(pos);
                                notifyItemRemoved(pos);
                                notifyItemRangeChanged(pos, exercises.size());
                            }
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
                return true;
            });

            // Rebuild sets view (simple way for dynamic list inside list)
            setsContainer.removeAllViews();
            for (int i = 0; i < exercise.sets.size(); i++) {
                SetEntry set = exercise.sets.get(i);
                View setView = LayoutInflater.from(itemView.getContext()).inflate(R.layout.item_set_row, setsContainer, false);

                TextView tvSetNumber = setView.findViewById(R.id.tvSetNumber);
                TextView tvPrevious = setView.findViewById(R.id.tvPrevious);
                EditText etWeight = setView.findViewById(R.id.etWeight);
                EditText etReps = setView.findViewById(R.id.etReps);
                ImageButton btnDelete = setView.findViewById(R.id.btnDeleteSet);

                tvSetNumber.setText(String.valueOf(i + 1));

                // Show Prev Data if available
                if (set.prevWeight > 0 || set.prevReps > 0) {
                    String unitLabel;
                    if (exercise.unit.equals(Constants.UNIT_PLACAS)) unitLabel = "pl";
                    else if (exercise.unit.equals(Constants.UNIT_LBS)) unitLabel = "lbs";
                    else unitLabel = "kg";

                    // If prevUnit existed it would be better, but we assume same unit for same exercise usually
                    tvPrevious.setText(String.format(java.util.Locale.US, "%.1f%s x %d", set.prevWeight, unitLabel, set.prevReps));
                } else {
                    tvPrevious.setText("-");
                }

                // Update weight hint based on unit
                String hint;
                switch (exercise.unit) {
                    case Constants.UNIT_PLACAS:
                        hint = itemView.getContext().getString(R.string.header_plates);
                        break;
                    case Constants.UNIT_LBS:
                        hint = itemView.getContext().getString(R.string.header_lbs);
                        break;
                    default:
                        hint = itemView.getContext().getString(R.string.header_kg);
                        break;
                }
                etWeight.setHint(hint.toLowerCase());

                // Set data
                if (set.weight > 0) etWeight.setText(String.valueOf(set.weight));
                if (set.reps > 0) etReps.setText(String.valueOf(set.reps));

                // Listeners to update model
                etWeight.addTextChangedListener(new SimpleTextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        try {
                           set.weight = Double.parseDouble(s.toString());
                        } catch (NumberFormatException e) {
                           set.weight = 0;
                        }
                    }
                });

                etReps.addTextChangedListener(new SimpleTextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        try {
                           set.reps = Integer.parseInt(s.toString());
                        } catch (NumberFormatException e) {
                           set.reps = 0;
                        }
                    }
                });

                // Delete Listener
                btnDelete.setOnClickListener(v -> {
                    exercise.sets.remove(set);
                    if (exercise.sets.isEmpty()) {
                        int pos = getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            exercises.remove(pos);
                            notifyItemRemoved(pos);
                            notifyItemRangeChanged(pos, exercises.size());
                            android.widget.Toast.makeText(itemView.getContext(), "Ejercicio eliminado de la sesión", android.widget.Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        notifyItemChanged(getAdapterPosition()); // Rebind to refresh list
                    }
                });

                setsContainer.addView(setView);
            }

            btnAddSet.setOnClickListener(v -> {
                // Add a new empty set based on previous if exists, or default
                SetEntry newSet = new SetEntry();
                if (!exercise.sets.isEmpty()) {
                    SetEntry last = exercise.sets.get(exercise.sets.size() - 1);
                    newSet.weight = last.weight; // Auto-fill weight
                    newSet.reps = last.reps;
                }
                exercise.sets.add(newSet);
                // Refresh this item
                notifyItemChanged(getAdapterPosition());
            });
        }
    }

    // Helper classes
    public static class ExerciseEntry implements Serializable {
        public String name;
        public String unit = Constants.UNIT_KG; // Default
        public List<SetEntry> sets = new ArrayList<>();
        public ExerciseEntry(String name) { this.name = name; }
    }

    public static class SetEntry implements Serializable {
        public double weight;
        public int reps;
        public double prevWeight;
        public int prevReps;
    }

    abstract static class SimpleTextWatcher implements TextWatcher {
         public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
         public void afterTextChanged(Editable s) {}
    }
}
