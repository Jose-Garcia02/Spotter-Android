package com.josegarcia.appgym.ui.tracker;

import androidx.appcompat.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.josegarcia.appgym.R;
import com.josegarcia.appgym.data.dao.WorkoutDao;
import com.josegarcia.appgym.data.database.AppDatabase;
import com.josegarcia.appgym.data.entities.ExerciseSet;
import com.josegarcia.appgym.data.entities.WorkoutSession;
import com.josegarcia.appgym.utils.Constants;
import com.josegarcia.appgym.utils.logic.ExerciseUtils;

import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import android.content.SharedPreferences;
import java.lang.reflect.Type;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import android.util.TypedValue;
import com.josegarcia.appgym.data.dao.RoutineExerciseDao;
import com.josegarcia.appgym.data.entities.RoutineExercise;

import android.view.inputmethod.InputMethodManager;
import android.content.Context;

public class TrackerActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TrackerAdapter adapter;
    private ExerciseTrackerViewModel viewModel;

    private static final String PREFS_NAME = "workout_cache";
    private static final String KEY_EXERCISES = "cached_exercises";
    private static final String KEY_SESSION_ID = "cached_session_id"; // For editing existing sessions
    private static final String KEY_TIMESTAMP = "cached_timestamp"; // For maintaining original date

    // Moved to ViewModel
    // private long editingSessionId = -1;
    // private long sessionTimestamp = -1;
    // private String currentRoutineName;

    // region Ciclo de Vida
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);

        viewModel = new ViewModelProvider(this).get(ExerciseTrackerViewModel.class);

        // Configuración de Insets e Interfaz
        setupEdgeToEdge();

        // Inicializar Nombre de Rutina
        initializeRoutineName();

        // Verificar Modo de Edición
        checkEditMode();

        TextView title = findViewById(R.id.tvRoutineTitle);
        title.setText(getString(R.string.tracker_title_format, viewModel.getCurrentRoutineName()));

        setupRecyclerView();

        // Lógica de Inicio - Cargar datos si es necesario
        if (!viewModel.isLoaded()) {
            loadInitialData();
        } else {
            finishSetup(); // Ya cargado, solo vincular adaptador
        }

        setupButtons();
    }
    // endregion

    // region Configuración UI
    private void setupEdgeToEdge() {
        TextView title = findViewById(R.id.tvRoutineTitle);
        final int originalPaddingTop = title.getPaddingTop();
        final int originalPaddingBottom = title.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(title, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), originalPaddingTop + insets.top, v.getPaddingRight(), originalPaddingBottom);
            return windowInsets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.recyclerViewExercises), (v, windowInsets) -> {
             Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
             v.setPadding(0, 0, 0, insets.bottom + (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics()));
             return windowInsets;
        });
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerViewExercises);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Corrección de Teclado (Micro-Modulo 6.3)
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    hideKeyboard();
                }
            }
        });
    }

    private void setupButtons() {
        String finalRoutineName = viewModel.getCurrentRoutineName();
        findViewById(R.id.btnFinishWorkout).setOnClickListener(v -> saveWorkout(finalRoutineName));
        findViewById(R.id.btnAddExercise).setOnClickListener(v -> showAddExerciseDialog());
    }

    private void hideKeyboard() {
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            currentFocus.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            }
        }
    }
    // endregion

    // region Lógica de Inicialización
    private void initializeRoutineName() {
        String intentRoutineName = getIntent().getStringExtra("ROUTINE_NAME");
        if (intentRoutineName == null) intentRoutineName = "General";

        if (viewModel.getCurrentRoutineName() == null) {
            viewModel.setCurrentRoutineName(intentRoutineName);
        }
    }

    private void checkEditMode() {
        if (getIntent().hasExtra("EDIT_SESSION_ID") && viewModel.getEditingSessionId() == -1) {
            viewModel.setEditingSessionId(getIntent().getLongExtra("EDIT_SESSION_ID", -1));
            viewModel.setSessionTimestamp(getIntent().getLongExtra("EDIT_SESSION_TIMESTAMP", System.currentTimeMillis()));
            String newRoutineName = getIntent().getStringExtra("ROUTINE_NAME");
            if (newRoutineName != null) {
                viewModel.setCurrentRoutineName(newRoutineName);
            }
        }
    }

    private void loadInitialData() {
        ArrayList<String> preselected = getIntent().getStringArrayListExtra("PRESELECTED_EXERCISES");

        if (viewModel.getEditingSessionId() != -1) {
            loadSessionForEditing(viewModel.getEditingSessionId());
        } else if (preselected != null && !preselected.isEmpty()) {
            List<TrackerAdapter.ExerciseEntry> list = createSessionFromList(preselected);
            viewModel.setExerciseList(list);
            finishSetup();
            loadPreviousData(); // Cargar historial para los preseleccionados
        } else {
            loadExercisesForRoutine(viewModel.getCurrentRoutineName());
        }
    }
    // endregion

    // region Carga de Rutinas/Plantillas
    private void loadExercisesForRoutine(String routineName) {
        if (viewModel.isLoaded()) return;

        // Preferencia por ID para evitar duplicados (Micro-Modulo 6.2)
        int routineId = getIntent().getIntExtra("ROUTINE_ID", -1);
        if (routineId != -1) {
            loadTemplateById(routineId);
        } else {
            loadTemplate(routineName); // Fallback nombre
        }
    }

    private void finishSetup() {
        adapter = new TrackerAdapter(viewModel.getExerciseList());
        recyclerView.setAdapter(adapter);
        checkCache();
        viewModel.setLoaded(true);
    }

    private void showAddExerciseDialog() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<String> allExercises = AppDatabase.getDatabase(this).workoutDao().getAllExerciseNames();
            runOnUiThread(() -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.select_exercise));

                // Use a custom layout for better search experience or standard
                // Simple search approach: AutoCompleteTextView inside dialog
                // Or just a single choice list if list isn't huge.
                // Assuming list is manageable for now, or use AutoCompleteTextView.

                // Let's use a simple single choice items first for simplicity as mandated by "clean code" (YAGNI)
                // unless user specifically requested search (implied by flexibility).
                // "buscando en todos los ejercicios disponibles" implies search capability if list is long.
                // An AutoCompleteTextView in a View is safer.

                // Create view programmatically to avoid another layout file for now or create one?
                // Creating a simple layout is cleaner.

                final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_dropdown_item_1line, allExercises);

                final AutoCompleteTextView input = new AutoCompleteTextView(this);
                input.setAdapter(adapter);
                input.setThreshold(1); // Start searching from 1 character
                input.setHint(getString(R.string.select_exercise));
                input.setPadding(40, 40, 40, 40);

                builder.setView(input);

                builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    String selected = input.getText().toString().trim();
                    if (!selected.isEmpty()) {
                        addExerciseToTracker(selected);
                    }
                });
                builder.setNegativeButton(getString(R.string.close), (dialog, which) -> dialog.cancel());

                builder.show();
            });
        });
    }

    private void addExerciseToTracker(String exerciseName) {
        TrackerAdapter.ExerciseEntry entry = new TrackerAdapter.ExerciseEntry(exerciseName);
        if (ExerciseUtils.shouldUsePlacas(exerciseName)) {
            entry.unit = Constants.UNIT_PLACAS;
        }
        entry.sets.add(new TrackerAdapter.SetEntry());

        List<TrackerAdapter.ExerciseEntry> list = viewModel.getExerciseList();
        list.add(entry);

        adapter.notifyItemInserted(list.size() - 1);
        recyclerView.scrollToPosition(list.size() - 1);

        loadHistoryForSingleExercise(entry, list.size() - 1);
    }

    private void loadHistoryForSingleExercise(TrackerAdapter.ExerciseEntry exercise, int position) {
         AppDatabase.databaseWriteExecutor.execute(() -> {
             List<ExerciseSet> historySets = AppDatabase.getDatabase(this).workoutDao().getLastSetsForExercise(exercise.name);
             if (historySets != null && !historySets.isEmpty()) {
                long lastSessionId = historySets.get(0).sessionId;
                List<ExerciseSet> lastSessionSets = new ArrayList<>();
                for (ExerciseSet s : historySets) {
                    if (s.sessionId == lastSessionId) {
                        lastSessionSets.add(s);
                    } else {
                        break;
                    }
                }
                lastSessionSets.sort(Comparator.comparingInt(s -> s.setOrder));

                for (int i = 0; i < lastSessionSets.size(); i++) {
                    ExerciseSet hSet = lastSessionSets.get(i);
                    if (i >= exercise.sets.size()) {
                        exercise.sets.add(new TrackerAdapter.SetEntry());
                    }
                    TrackerAdapter.SetEntry uSet = exercise.sets.get(i);
                    uSet.prevWeight = hSet.weight;
                    uSet.prevReps = hSet.reps;
                }
                runOnUiThread(() -> adapter.notifyItemChanged(position));
             }
         });
    }

    private void loadPreviousData() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            WorkoutDao dao = AppDatabase.getDatabase(this).workoutDao();
            boolean dataUpdated = false;

            List<TrackerAdapter.ExerciseEntry> currentList = viewModel.getExerciseList();

            for (TrackerAdapter.ExerciseEntry exercise : currentList) {
                // Obtener historial
                List<ExerciseSet> historySets = dao.getLastSetsForExercise(exercise.name);

                if (historySets != null && !historySets.isEmpty()) {
                    // Filtrar series SOLO de la última sesión
                    // historySets está ordenado por fecha DESC
                    long lastSessionId = historySets.get(0).sessionId;

                    int targetSetIndex = 0;
                    for (ExerciseSet histSet : historySets) {
                        if (histSet.sessionId != lastSessionId) break; // Detener si encontramos una sesión más antigua

                        if (targetSetIndex < exercise.sets.size()) {
                            TrackerAdapter.SetEntry currentSet = exercise.sets.get(targetSetIndex);
                            currentSet.prevWeight = histSet.weight;
                            currentSet.prevReps = histSet.reps;
                            dataUpdated = true;
                            targetSetIndex++;
                        }
                    }
                }
            }

            if (dataUpdated) {
                runOnUiThread(() -> {
                    if (adapter != null) {
                        // Suppress warning as we are updating potentially all items with history data
                        // noinspection NotifyDataSetChanged
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private void loadSessionForEditing(long sessionId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<ExerciseSet> sets = AppDatabase.getDatabase(this).workoutDao().getSetsForSession(sessionId);

            List<TrackerAdapter.ExerciseEntry> loadedList = new ArrayList<>();
            TrackerAdapter.ExerciseEntry currentEntry = null;

            for (ExerciseSet set : sets) {
                if (currentEntry == null || !currentEntry.name.equals(set.exerciseName)) {
                    currentEntry = new TrackerAdapter.ExerciseEntry(set.exerciseName);
                    if (set.unit != null) currentEntry.unit = set.unit;
                    loadedList.add(currentEntry);
                }

                TrackerAdapter.SetEntry setEntry = new TrackerAdapter.SetEntry();
                setEntry.weight = set.weight;
                setEntry.reps = set.reps;
                currentEntry.sets.add(setEntry);
            }

            runOnUiThread(() -> {
                viewModel.setExerciseList(loadedList);
                adapter = new TrackerAdapter(loadedList);
                recyclerView.setAdapter(adapter);
                loadPreviousData();
                viewModel.setLoaded(true);
            });
        });
    }

    private void saveWorkout(String routineName) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long sessionId;
            WorkoutDao dao = AppDatabase.getDatabase(this).workoutDao();

            long editId = viewModel.getEditingSessionId();

            if (editId != -1) {
                // Actualizar sesión existente
                sessionId = editId;
                WorkoutSession session = new WorkoutSession(viewModel.getSessionTimestamp(), routineName);
                session.id = sessionId;
                dao.updateSession(session);

                // Reemplazar series
                dao.deleteSetsForSession(sessionId);
            } else {
                // Crear nueva sesión
                WorkoutSession session = new WorkoutSession(System.currentTimeMillis(), routineName);
                sessionId = dao.insertSession(session);
            }

            // Recopilar series
            List<ExerciseSet> setsToSave = new ArrayList<>();
            for (TrackerAdapter.ExerciseEntry exercise : viewModel.getExerciseList()) {
                int order = 1;
                for (TrackerAdapter.SetEntry set : exercise.sets) {
                    // Save if it has meaningful data
                    if (set.reps > 0 || set.weight > 0) {
                        ExerciseSet dbSet = new ExerciseSet(
                                sessionId,
                                exercise.name,
                                set.weight,
                                set.reps,
                                order++ // Auto-increment order
                        );
                        dbSet.unit = exercise.unit;
                        setsToSave.add(dbSet);
                    }
                }
            }

            // Insertar series
            if (!setsToSave.isEmpty()) {
                dao.insertSets(setsToSave);
            }

            if (editId == -1) {
                clearCacheOnBackground();
            }

            // Retroalimentación UI
            runOnUiThread(() -> {
                Toast.makeText(this, R.string.workout_saved, Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private void saveCache() {
        if (viewModel.getExerciseList() == null || viewModel.getExerciseList().isEmpty()) return;

        boolean hasData = false;
        for (TrackerAdapter.ExerciseEntry exercise : viewModel.getExerciseList()) {
            for (TrackerAdapter.SetEntry set : exercise.sets) {
                if (set.weight > 0 || set.reps > 0) {
                    hasData = true;
                    break;
                }
            }
            if (hasData) break;
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String suffix = "_" + viewModel.getCurrentRoutineName();

        if (hasData) {
            Gson gson = new Gson();
            String jsonExercises = gson.toJson(viewModel.getExerciseList());
            editor.putString(KEY_EXERCISES + suffix, jsonExercises);
            editor.putLong(KEY_SESSION_ID + suffix, viewModel.getEditingSessionId());
            editor.putLong(KEY_TIMESTAMP + suffix, viewModel.getSessionTimestamp());
        } else {
            editor.remove(KEY_EXERCISES + suffix);
            editor.remove(KEY_SESSION_ID + suffix);
            editor.remove(KEY_TIMESTAMP + suffix);
        }
        editor.apply();
    }

    private void checkCache() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String suffix = "_" + viewModel.getCurrentRoutineName();

        if (prefs.contains(KEY_EXERCISES + suffix)) {
             // ¡Existe caché específico para esta rutina!
             // createCustomRestoreDialog verifica la lógica
             // Solo mostrar diálogo de restauración si NO estamos en modo edición
             // ¿O si el ID de sesión coincide?
             // Ideally restore is only for new sessions crashed.
             if (viewModel.getEditingSessionId() == -1) {
                 createCustomRestoreDialog(prefs, suffix);
             }
        }
    }

    private void createCustomRestoreDialog(SharedPreferences prefs, String suffix) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_restore_session, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        TextView title = view.findViewById(R.id.tvDialogTitle);
        TextView message = view.findViewById(R.id.tvDialogMessage);

        // Usar ruta completa para evitar ambigüedad
        android.widget.Button btnRecover = view.findViewById(R.id.btnRecover);
        android.widget.Button btnDiscard = view.findViewById(R.id.btnDiscard);

        title.setText(R.string.cache_session_title);
        message.setText(getString(R.string.cache_session_message, viewModel.getCurrentRoutineName()));

        btnRecover.setOnClickListener(v -> {
            restoreCache(prefs, suffix);
            dialog.dismiss();
        });

        btnDiscard.setOnClickListener(v -> {
            clearCache(suffix);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void restoreCache(SharedPreferences prefs, String suffix) {
        String json = prefs.getString(KEY_EXERCISES + suffix, "[]");
        // La rutina es implícita
        long restoredId = prefs.getLong(KEY_SESSION_ID + suffix, -1);
        long restoredTs = prefs.getLong(KEY_TIMESTAMP + suffix, -1);

        viewModel.setEditingSessionId(restoredId);
        viewModel.setSessionTimestamp(restoredTs);

        Gson gson = new Gson();
        Type type = new TypeToken<List<TrackerAdapter.ExerciseEntry>>(){}.getType();
        List<TrackerAdapter.ExerciseEntry> restoredList = gson.fromJson(json, type);
        viewModel.setExerciseList(restoredList);

        // Las claves de restauración son específicas de la rutina, por lo que currentRoutineName ya es el correcto
        TextView title = findViewById(R.id.tvRoutineTitle);
        title.setText(getString(R.string.tracker_title_format, viewModel.getCurrentRoutineName()));

        // Reconfigurar adaptador
        adapter = new TrackerAdapter(restoredList);
        recyclerView.setAdapter(adapter);
    }

    private void clearCache() {
        // Helper sobrecargado para la rutina actual
        clearCache("_" + viewModel.getCurrentRoutineName());
    }

    private void clearCache(String suffix) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_EXERCISES + suffix);
        editor.remove(KEY_SESSION_ID + suffix);
        editor.remove(KEY_TIMESTAMP + suffix);
        // Clean up legacy keys if they exist to be safe?
        // editor.remove(KEY_ROUTINE);
        // editor.remove(KEY_EXERCISES);
        editor.apply();
    }

    private void clearCacheOnBackground() {
         clearCache();
    }

    private List<TrackerAdapter.ExerciseEntry> createSessionFromList(List<String> exercises) {
        List<TrackerAdapter.ExerciseEntry> list = new ArrayList<>();
        for (String name : exercises) {
            TrackerAdapter.ExerciseEntry entry = new TrackerAdapter.ExerciseEntry(name);

            if (ExerciseUtils.shouldUsePlacas(name)) {
                entry.unit = Constants.UNIT_PLACAS;
            }

            // Siempre 3 series por defecto para entrenamiento libre
            int setCount = 3;

            for (int i = 0; i < setCount; i++) {
                entry.sets.add(new TrackerAdapter.SetEntry());
            }
            list.add(entry);
        }
        return list;
    }


    @Override
    protected void onPause() {
        super.onPause();
        saveCache();
    }

    private void loadTemplateById(int routineId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            RoutineExerciseDao dao = AppDatabase.getDatabase(this).routineExerciseDao();
            List<RoutineExercise> template = dao.getExercisesForRoutine(routineId);

            processTemplateLoad(template);
        });
    }

    private void loadTemplate(String routineName) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            RoutineExerciseDao dao = AppDatabase.getDatabase(this).routineExerciseDao();
            // This might return duplicates if routine names are not unique. Prefer loadTemplateById.
            List<RoutineExercise> template = dao.getExercisesForRoutineByName(routineName);

            processTemplateLoad(template);
        });
    }

    private void processTemplateLoad(List<RoutineExercise> template) {
        runOnUiThread(() -> {
            if (template != null && !template.isEmpty()) {
                List<TrackerAdapter.ExerciseEntry> sessionList = new ArrayList<>();
                for (RoutineExercise re : template) {
                    TrackerAdapter.ExerciseEntry entry = new TrackerAdapter.ExerciseEntry(re.exerciseName);
                    entry.unit = re.targetUnit != null ? re.targetUnit : "KG";

                    // Pre-populate sets based on targetSets
                    for (int i = 0; i < re.targetSets; i++) {
                        entry.sets.add(new TrackerAdapter.SetEntry());
                    }
                    sessionList.add(entry);
                }
                viewModel.setExerciseList(sessionList);
                adapter = new TrackerAdapter(sessionList);
                recyclerView.setAdapter(adapter);
                // Load previous data for template exercises
                checkCache();
                loadPreviousData();
                viewModel.setLoaded(true);
            } else {
                Toast.makeText(this, "Plantilla vacía o no encontrada", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
