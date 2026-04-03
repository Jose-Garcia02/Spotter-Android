package com.josegarcia.appgym.ui.exercise_management;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;

import com.josegarcia.appgym.R;
import com.josegarcia.appgym.data.entities.ExerciseCatalog;

public class ExerciseManagementFragment extends Fragment implements ExerciseManagementAdapter.OnExerciseListener {
    private ExerciseManagementViewModel viewModel;
    private ExerciseManagementAdapter adapter;
    private RecyclerView rvExercises;
    private MaterialButton btnAddExercise;

    private static final String[] MUSCLE_TAGS = {"Pecho", "Espalda", "Hombro", "Biceps", "Triceps", "Pierna", "Core"};
    private static final String[] UNITS = {"kg", "lbs", "placas"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercise_management, container, false);

        rvExercises = view.findViewById(R.id.rvExercises);
        btnAddExercise = view.findViewById(R.id.btnAddExercise);

        viewModel = new ViewModelProvider(this).get(ExerciseManagementViewModel.class);
        adapter = new ExerciseManagementAdapter(this);

        rvExercises.setLayoutManager(new LinearLayoutManager(getContext()));
        rvExercises.setAdapter(adapter);

        viewModel.allExercises.observe(getViewLifecycleOwner(), exercises -> {
            adapter.setExercises(exercises);
        });

        btnAddExercise.setOnClickListener(v -> showCreateDialog());

        return view;
    }

    @Override
    public void onEdit(ExerciseCatalog exercise) {
        showEditDialog(exercise);
    }

    @Override
    public void onDelete(ExerciseCatalog exercise) {
        new AlertDialog.Builder(getContext())
            .setTitle("Eliminar ejercicio")
            .setMessage("¿Estás seguro de que deseas eliminar " + exercise.name + "?")
            .setPositiveButton("Eliminar", (dialog, which) -> viewModel.deleteExercise(exercise.id))
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void showCreateDialog() {
        showExerciseDialog(null);
    }

    private void showEditDialog(ExerciseCatalog exercise) {
        showExerciseDialog(exercise);
    }

    private void showExerciseDialog(ExerciseCatalog exercise) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_exercise_edit, null);

        EditText etName = dialogView.findViewById(R.id.etExerciseName);
        Spinner spinnerUnit = dialogView.findViewById(R.id.spinnerUnit);
        Spinner spinnerMuscle = dialogView.findViewById(R.id.spinnerMuscleTag);

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, UNITS);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(unitAdapter);

        ArrayAdapter<String> muscleAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, MUSCLE_TAGS);
        muscleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMuscle.setAdapter(muscleAdapter);

        if (exercise != null) {
            etName.setText(exercise.name);
            spinnerUnit.setSelection(getIndex(spinnerUnit, exercise.defaultUnit));
            spinnerMuscle.setSelection(getIndex(spinnerMuscle, exercise.muscleTag));
        }

        builder.setView(dialogView)
            .setTitle(exercise == null ? "Crear ejercicio" : "Editar ejercicio")
            .setPositiveButton("Guardar", (dialog, which) -> {
                String name = etName.getText().toString().trim();
                String unit = (String) spinnerUnit.getSelectedItem();
                String muscleTag = (String) spinnerMuscle.getSelectedItem();

                if (name.isEmpty()) {
                    android.widget.Toast.makeText(getContext(), "El nombre es obligatorio", android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }

                if (exercise == null) {
                    viewModel.insertExercise(name, unit, muscleTag);
                } else {
                    viewModel.updateExercise(exercise.id, name, unit, muscleTag);
                }
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private int getIndex(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equals(value)) {
                return i;
            }
        }
        return 0;
    }
}

