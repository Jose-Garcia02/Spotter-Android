package com.josegarcia.appgym.ui.setup.wizard;
import com.josegarcia.appgym.ui.setup.wizard.ConfigureSplitActivity;
import com.josegarcia.appgym.ui.setup.SplitAdapter;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.josegarcia.appgym.R;
import com.josegarcia.appgym.data.database.AppDatabase;
import com.josegarcia.appgym.data.dao.RoutineDao;
import com.josegarcia.appgym.data.dao.RoutineExerciseDao;
import com.josegarcia.appgym.data.entities.Routine;
import com.josegarcia.appgym.data.entities.RoutineExercise;
import com.josegarcia.appgym.data.entities.Split;
import com.josegarcia.appgym.ui.setup.DraftManager;

import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClassicSplitListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_split); // Reuse existing list layout

        // Apply Window Insets
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.recyclerSplits), (v, windowInsets) -> {
            androidx.core.graphics.Insets insets = windowInsets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            // Add padding to existing padding
            v.setPadding(v.getPaddingLeft(), insets.top, v.getPaddingRight(), insets.bottom);
            return windowInsets;
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerSplits);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load splits async
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Filter by template
            List<Split> templateSplits = AppDatabase.getDatabase(this).splitDao().getTemplateSplits();

            runOnUiThread(() -> recyclerView.setAdapter(new SplitAdapter(templateSplits, split -> {
                cloneAndOpenSplit(split);
            })));
        });
    }

    private void cloneAndOpenSplit(Split template) {
        Toast.makeText(this, "Preparando borrador...", Toast.LENGTH_SHORT).show();
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(this);

            // 1. Prepare User Split Object (Memory Only, NO INSERT)
            Split draftSplit = new Split(template.name, template.description, false, template.type);

            // 2. Fetch Original Routines
            RoutineDao routineDao = db.routineDao();
            RoutineExerciseDao reDao = db.routineExerciseDao();
            List<Routine> originalRoutines = routineDao.getRoutinesForSplitSync(template.id);

            // 3. Prepare Draft Routines and Exercises
            List<Routine> draftRoutines = new ArrayList<>();
            DraftManager draftManager = DraftManager.getInstance();

            // We need to initialize the DraftManager first with the list of Routines
            for (int i = 0; i < originalRoutines.size(); i++) {
                Routine original = originalRoutines.get(i);
                // Creating a draft routine with ID 0 (temp)
                Routine draftRoutine = new Routine(0, original.name, original.colorResId, original.isSystem, original.orderIndex);
                draftRoutines.add(draftRoutine);
            }

            // Initialize Draft Manager
            draftManager.startDraft(draftSplit, draftRoutines);

            // Now Populate Exercises
            for (int i = 0; i < originalRoutines.size(); i++) {
                Routine original = originalRoutines.get(i);
                List<RoutineExercise> originalExercises = reDao.getExercisesForRoutine(original.id);

                for (RoutineExercise ex : originalExercises) {
                    // Create copy with routineId 0
                    RoutineExercise draftEx = new RoutineExercise(0, ex.exerciseName, ex.order, ex.targetSets, ex.targetUnit);
                    draftManager.addExercise(i, draftEx);
                }
            }

            runOnUiThread(() -> {
                Intent intent = new Intent(this, ConfigureSplitActivity.class);
                intent.putExtra("IS_DRAFT", true);
                intent.putExtra("SPLIT_NAME", draftSplit.name);
                startActivity(intent);
                finish(); // Close list
            });
        });
    }
}
