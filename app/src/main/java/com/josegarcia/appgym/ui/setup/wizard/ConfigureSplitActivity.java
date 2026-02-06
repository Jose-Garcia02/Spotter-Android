package com.josegarcia.appgym.ui.setup.wizard;

import com.josegarcia.appgym.ui.setup.DraftManager;
import com.josegarcia.appgym.ui.setup.editor.DayEditorFragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.josegarcia.appgym.R;
import com.josegarcia.appgym.data.database.AppDatabase;
import com.josegarcia.appgym.data.entities.Routine;
import com.josegarcia.appgym.data.entities.RoutineExercise;

import java.util.List;

public class ConfigureSplitActivity extends AppCompatActivity implements DayEditorFragment.DayEditorListener {

    private int splitId;
    private boolean isDraft; // New flag
    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    // Add field to store routines list locally for updates
    private List<Routine> currentRoutines;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_split);

        isDraft = getIntent().getBooleanExtra("IS_DRAFT", false);

        // Apps Window Insets
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, windowInsets) -> {
            androidx.core.graphics.Insets insets = windowInsets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, insets.top, 0, insets.bottom);
            return windowInsets;
        });

        splitId = getIntent().getIntExtra("SPLIT_ID", -1);
        String splitName = getIntent().getStringExtra("SPLIT_NAME");

        TextView title = findViewById(R.id.tvSplitTitle);
        title.setText(getString(R.string.configure_title, splitName));

        viewPager = findViewById(R.id.viewPagerDays);
        tabLayout = findViewById(R.id.tabLayoutDays);

        if (isDraft) {
            // Load from DraftManager
            List<Routine> routines = DraftManager.getInstance().getDraftRoutines();
            if (routines != null && !routines.isEmpty()) {
                setupPager(routines);
            } else {
                Toast.makeText(this, "Error de borrador", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            // Load routines for this split from DB
            AppDatabase.getDatabase(this).routineDao().getRoutinesForSplit(splitId)
                    .observe(this, routines -> {
                        if (routines != null && !routines.isEmpty()) {
                            setupPager(routines);
                        }
                    });
        }

        Button btnActivate = findViewById(R.id.btnActivateSplit);
        btnActivate.setText(R.string.save_final_routine); // Always save/activate
        btnActivate.setOnClickListener(v -> activateSplit());
    }

    private void setupPager(List<Routine> routines) {
        this.currentRoutines = routines; // Store reference
        FragmentStateAdapter adapter = new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                // If draft, pass index or temp ID logic.
                // Existing routines in DB have valid ID > 0.
                // Draft routines have ID 0.
                if (isDraft) {
                    return DayEditorFragment.newInstance(0, true, position);
                } else {
                    return DayEditorFragment.newInstance(routines.get(position).id);
                }
            }

            @Override
            public int getItemCount() {
                return routines.size();
            }
        };
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(routines.get(position).name)
        ).attach();
    }

    @Override
    public void onRoutineRenamed(int index, String newName) {
        refreshTabTitle(index, newName);
    }

    public void refreshTabTitle(int position, String newName) {
        if (currentRoutines != null && position >= 0 && position < currentRoutines.size()) {
            currentRoutines.get(position).name = newName;
            // Refresh Tabs
            if (tabLayout != null && tabLayout.getTabAt(position) != null) {
                tabLayout.getTabAt(position).setText(newName);
            }
        }
    }

    private void activateSplit() {
        if (isDraft) {
            commitDraft();
        } else {
            // Existing logic
            AppDatabase.databaseWriteExecutor.execute(() -> {
                AppDatabase.getDatabase(this).splitDao().setActiveSplit(splitId);
                runOnUiThread(() -> finishSuccess());
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // If we represent a draft and the activity is destroyed,
        // we should conceptually clear the draft if it wasn't saved?
        // Actually, DraftManager is a Singleton. If user rotates screen, onDestroy is called.
        // We shouldn't clear on rotation. We should clear only if finishing without success?
        if (isFinishing() && isDraft) {
            // If finishing and we are still in draft mode (meaning we didn't transition to a saved state successfully yet,
            // or user pressed back), clean up.
            // But wait, if we committed successfully, we cleared it in commitDraft().
            // So this is safe to call again (clear() clears data fields).
            // However, verify commitDraft clears BEFORE calling finishSuccess which calls startActivity (causing this to finish).
            // commitDraft calls clear() then finishSuccess().
            // finishSuccess calls startActivity then finish().
            // So if we clear here too, it's redundant but safe.
            // Crucial: If user presses BACK, isFinishing() is true. We MUST clear draft so next time wizard starts fresh.
            // DraftManager.startDraft() overwrites anyway, but clearing releases memory.
            com.josegarcia.appgym.ui.setup.DraftManager.getInstance().clear();
        }
    }

    private void commitDraft() {
        // MICRO-MODULE 4.4: Transactional Save
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(this);
            DraftManager dm = DraftManager.getInstance();
            if (!dm.hasDraft()) return;

            db.runInTransaction(() -> {
                // 1. Insert Split
                long newSplitId = db.splitDao().insert(dm.getDraftSplit());

                // 2. Insert Routines & Exercises
                List<Routine> routines = dm.getDraftRoutines();
                for (int i = 0; i < routines.size(); i++) {
                    Routine r = routines.get(i);
                    // Copy Routine with new Split ID
                    Routine newR = new Routine((int)newSplitId, r.name, r.colorResId, r.isSystem, i + 1);
                    long newRoutineId = db.routineDao().insert(newR);

                    // Insert Exercises
                    List<RoutineExercise> exercises = dm.getExercisesForRoutine(i);
                    if (exercises != null && !exercises.isEmpty()) {
                        for (RoutineExercise ex : exercises) {
                            // Assign correct routineId
                            ex.routineId = (int) newRoutineId;
                        }
                        db.routineExerciseDao().insertAll(exercises);
                    }
                }

                // 3. Set Active
                db.splitDao().setActiveSplit((int)newSplitId);
            });

            // Clear draft
            dm.clear();

            runOnUiThread(() -> finishSuccess());
        });
    }

    private void finishSuccess() {
        // Mark setup as completed if it wasn't
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        prefs.edit().putBoolean("is_setup_completed", true).apply();

        Toast.makeText(this, "¡Rutina guardada y activada!", Toast.LENGTH_SHORT).show();
        // Navigate back to Main/Home clearing stack
        android.content.Intent intent = new android.content.Intent(this, com.josegarcia.appgym.MainActivity.class);
        intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
