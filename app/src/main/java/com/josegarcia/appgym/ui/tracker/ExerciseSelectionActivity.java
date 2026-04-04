package com.josegarcia.appgym.ui.tracker;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.josegarcia.appgym.R;
import com.josegarcia.appgym.data.database.AppDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ExerciseSelectionActivity extends AppCompatActivity {

    private ExerciseSelectionAdapter adapter;
    private List<String> fullList = new ArrayList<>();
    private Button btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_selection);

        // Apply Insets to root
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, windowInsets) -> {
            androidx.core.graphics.Insets insets = windowInsets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars() | androidx.core.view.WindowInsetsCompat.Type.ime());
            v.setPadding(0, insets.top, 0, 0); // Only top padding for status bar to content
            // However, list needs bottom padding for FAB or navigation bar.
            // But this activity uses a button at bottom (btnStartFreeWorkout) which is probably gone in the new XML??
            // Checking XML: activity_exercise_selection.xml has a Button btnStartFreeWorkout at bottom?
            // Wait, the read_file of activity_exercise_selection.xml didn't show btnStartFreeWorkout at the bottom in the 1-50 lines.
            // Let's check the lines 50+ of activity_exercise_selection.xml to be sure.
            return windowInsets;
        });

        // Specific insets for Button at bottom
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.btnStartFreeWorkout), (v, windowInsets) -> {
             androidx.core.graphics.Insets insets = windowInsets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
             // Add bottom margin to avoid nav bar overlap
             android.view.ViewGroup.MarginLayoutParams params = (android.view.ViewGroup.MarginLayoutParams) v.getLayoutParams();
             params.bottomMargin = insets.bottom + 32; // basic margin + insets
             v.setLayoutParams(params);
             return androidx.core.view.WindowInsetsCompat.CONSUMED;
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerExercises);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnStart = findViewById(R.id.btnStartFreeWorkout);
        EditText searchInput = findViewById(R.id.etSearch);

        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE || actionId == android.view.inputmethod.EditorInfo.IME_ACTION_GO || actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true;
            }
            return false;
        });

        adapter = new ExerciseSelectionAdapter(count ->
            btnStart.setText("Iniciar Entrenamiento (" + count + ")")
        );
        recyclerView.setAdapter(adapter);

        // Load exercises
        loadExercises();

        androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback callback = new androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(
                androidx.recyclerview.widget.ItemTouchHelper.UP | androidx.recyclerview.widget.ItemTouchHelper.DOWN,
                0
        ) {
            @Override
            public boolean onMove(@androidx.annotation.NonNull RecyclerView recyclerView, @androidx.annotation.NonNull RecyclerView.ViewHolder viewHolder, @androidx.annotation.NonNull RecyclerView.ViewHolder target) {
                adapter.moveItem(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(@androidx.annotation.NonNull RecyclerView.ViewHolder viewHolder, int direction) { }
        };
        new androidx.recyclerview.widget.ItemTouchHelper(callback).attachToRecyclerView(recyclerView);

        // Search logic
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Create new exercise logic
        findViewById(R.id.btnCreateExercise).setOnClickListener(v -> showCreateExerciseDialog());

        // Start logic
        btnStart.setOnClickListener(v -> {
            ArrayList<String> selected = adapter.getSelectedExercises();
            if (selected.isEmpty()) {
                Toast.makeText(this, "Selecciona al menos un ejercicio", Toast.LENGTH_SHORT).show();
                return;
            }
            // Check calling intent action
            if (getIntent().getBooleanExtra("IS_SELECTION_MODE", false)) {
                // Return result mode
                Intent resultIntent = new Intent();
                resultIntent.putStringArrayListExtra("SELECTED_EXERCISES", selected);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                // Default mode (Tracker)
                Intent intent = new Intent(this, TrackerActivity.class);
                intent.putExtra("ROUTINE_NAME", "Entrenamiento Libre");
                intent.putStringArrayListExtra("PRESELECTED_EXERCISES", selected); // Pass selection to TrackerActivity
                startActivity(intent);
                finish(); // Close selection activity
            }
        });
    }

    private void loadExercises() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<String> exercises = AppDatabase.getDatabase(this).workoutDao().getAllExerciseNames();
            runOnUiThread(() -> {
                fullList = new ArrayList<>(exercises);
                Collections.sort(fullList);
                adapter.setExercises(fullList);
            });
        });
    }

    private void filter(String query) {
        String lower = query.toLowerCase();
        List<String> filtered = fullList.stream()
                .filter(s -> s.toLowerCase().contains(lower))
                .collect(Collectors.toList());
        adapter.setExercises(filtered);
    }

    private void showCreateExerciseDialog() {
        // Custom Dialog Layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_generic_input, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        tvTitle.setText("Nuevo Ejercicio");

        EditText etInput = dialogView.findViewById(R.id.etDialogInput);
        etInput.setHint("Nombre del ejercicio");

        dialogView.findViewById(R.id.btnDialogPositive).setOnClickListener(v -> {
            String name = etInput.getText().toString().trim();
            if (!name.isEmpty()) {
                if (!fullList.contains(name)) {
                    fullList.add(name);
                    Collections.sort(fullList);
                    adapter.setExercises(fullList);
                }
                dialog.dismiss();
            } else {
                etInput.setError("Nombre requerido");
            }
        });

        dialogView.findViewById(R.id.btnDialogNegative).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
