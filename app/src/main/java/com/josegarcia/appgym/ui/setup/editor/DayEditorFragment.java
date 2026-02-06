package com.josegarcia.appgym.ui.setup.editor;
import com.josegarcia.appgym.ui.setup.DraftManager;
import com.josegarcia.appgym.utils.ui.DialogHelper;

import androidx.appcompat.app.AlertDialog; // Import missing AlertDialog

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.EditText;
import android.text.TextWatcher;
import android.text.Editable;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.josegarcia.appgym.R;
import com.josegarcia.appgym.data.database.AppDatabase;
import com.josegarcia.appgym.data.entities.Routine;
import com.josegarcia.appgym.data.entities.RoutineExercise;
import com.josegarcia.appgym.data.entities.Split;
import com.josegarcia.appgym.ui.tracker.ExerciseSelectionActivity;
import com.josegarcia.appgym.utils.Constants;
import com.josegarcia.appgym.data.database.InitialData;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import static android.app.Activity.RESULT_OK;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;

public class DayEditorFragment extends Fragment {

    // Interface for callbacks
    public interface DayEditorListener {
        void onRoutineRenamed(int index, String newName);
    }

    private DayEditorListener listener;

    private static final String ARG_ROUTINE_ID = "routine_id";
    private static final String ARG_IS_DRAFT = "is_draft";
    private static final String ARG_ROUTINE_INDEX = "routine_index";

    private int routineId;
    private boolean isDraft;
    private int routineIndex;

    private EditRoutineAdapter adapter;
    private ActivityResultLauncher<Intent> selectionLauncher;
    private LinearLayout emptyStateLayout;

    public static DayEditorFragment newInstance(int routineId) {
        return newInstance(routineId, false, -1);
    }

    public static DayEditorFragment newInstance(int routineId, boolean isDraft, int routineIndex) {
        DayEditorFragment fragment = new DayEditorFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ROUTINE_ID, routineId);
        args.putBoolean(ARG_IS_DRAFT, isDraft);
        args.putInt(ARG_ROUTINE_INDEX, routineIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof DayEditorListener) {
            listener = (DayEditorListener) context;
        } else {
            // Optional: throw exception or just log
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    private final EditRoutineAdapter.OnExerciseActionListener actionListener = new EditRoutineAdapter.OnExerciseActionListener() {
        @Override
        public void onDelete(RoutineExercise exercise) {
            deleteExercise(exercise);
        }

        @Override
        public void onUpdate(RoutineExercise exercise) {
            updateExercise(exercise);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            routineId = getArguments().getInt(ARG_ROUTINE_ID);
            isDraft = getArguments().getBoolean(ARG_IS_DRAFT, false);
            routineIndex = getArguments().getInt(ARG_ROUTINE_INDEX, -1);
        }

        selectionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        ArrayList<String> selected = result.getData().getStringArrayListExtra("SELECTED_EXERCISES");
                        if (selected != null && !selected.isEmpty()) {
                            addExercises(selected);
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_day_editor, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Name Editor logic
        EditText etName = view.findViewById(R.id.etRoutineName);
        if (etName != null) {
            setupNameEditor(etName);
        }

        RecyclerView recyclerView = view.findViewById(R.id.recyclerExercises);
        emptyStateLayout = view.findViewById(R.id.layoutEmptyState);
        Button btnLoadTemplate = view.findViewById(R.id.btnLoadTemplate);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EditRoutineAdapter(actionListener);
        recyclerView.setAdapter(adapter);

        // MICRO-MODULE 6.3: Keyboard UX Fix
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    if (getActivity() != null) {
                       View currentFocus = getActivity().getCurrentFocus();
                       if (currentFocus != null) {
                           currentFocus.clearFocus();
                           InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                           if (imm != null) {
                               imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
                           }
                       }
                    }
                }
            }
        });

        view.findViewById(R.id.fabAddExercise).setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ExerciseSelectionActivity.class);
            intent.putExtra("IS_SELECTION_MODE", true);
            selectionLauncher.launch(intent);
        });

        btnLoadTemplate.setOnClickListener(v -> showTemplateSelector());

        loadExercises();
    }

    private void showTemplateSelector() {
        if (getContext() == null) return;

        final String[] options = {
            "Arnold: Pecho / Espalda",
            "Arnold: Hombro / Brazos",
            "Arnold: Pierna",
            "Full Body (Var. A)",
            "Full Body (Var. B)",
            "Full Body (Var. C)",
            "Upper (Torso)",
            "Lower (Pierna)",
            "Push (Empuje)",
            "Pull (Tracción)",
            "Legs (PPL)"
        };

        // Corresponding Constants
        final String[] values = {
            Constants.ROUTINE_ARNOLD_CHEST_BACK_A,
            Constants.ROUTINE_ARNOLD_SHOULDERS_ARMS_A,
            Constants.ROUTINE_ARNOLD_LEGS_A,
            Constants.ROUTINE_FULLBODY_A,
            Constants.ROUTINE_FULLBODY_B,
            Constants.ROUTINE_FULLBODY_C,
            Constants.ROUTINE_UPPER_A,
            Constants.ROUTINE_LOWER_A,
            Constants.ROUTINE_PUSH_A,
            Constants.ROUTINE_PULL_A,
            Constants.ROUTINE_LEGS_A
        };

        // Custom Dialog Layout
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_generic_list, null);
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        // Transparent background for rounded corners
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        tvTitle.setText("Seleccionar Plantilla Base");

        ListView listView = dialogView.findViewById(R.id.lvDialogOptions);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.item_text_dark, options);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedRoutineKey = values[position];

            String[] exercises = InitialData.getExercisesForRoutine(selectedRoutineKey);
            addExercises(new ArrayList<>(Arrays.asList(exercises)));

            String baseName = determineBaseName(selectedRoutineKey);
            renameRoutineDynamic(baseName);

            dialog.dismiss();
        });

        dialogView.findViewById(R.id.btnDialogCancel).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private String determineBaseName(String key) {
        if (key.contains("Arnold") && key.contains("Chest")) return "Chest & Back";
        if (key.contains("Arnold") && key.contains("Shoulders")) return "Shoulders & Arms";
        if (key.contains("Arnold") && key.contains("Legs")) return "Legs";

        if (key.contains("Full Body")) return "Full Body";

        if (key.contains("Upper")) return "Upper";
        if (key.contains("Lower")) return "Lower";
        if (key.contains("Push")) return "Push";
        if (key.contains("Pull")) return "Pull";
        if (key.contains("Legs")) return "Legs"; // PPL Legs

        return "Rutina";
    }

    private void renameRoutineDynamic(String baseName) {
         if (getContext() == null) return;

         if (isDraft) {
             List<Routine> siblings = DraftManager.getInstance().getDraftRoutines();
             Routine current = siblings.get(routineIndex);

             // Same logic but in memory
             String finalName = baseName;
             if ("Full Body".equals(baseName)) {
                 int fbCount = 0;
                 for (int i=0; i<siblings.size(); i++) {
                     if (i != routineIndex && siblings.get(i).name.startsWith("Full Body")) {
                         fbCount++;
                     }
                 }
                 char suffix = (char) ('A' + fbCount);
                 finalName = "Full Body " + suffix;
             } else {
                 boolean taken = false;
                 for (int i=0; i<siblings.size(); i++) {
                     if (i != routineIndex && siblings.get(i).name.equals(baseName)) {
                         taken = true;
                         break;
                     }
                 }
                 if (taken) {
                     char suffix = 'A';
                     while (true) {
                         String candidate = baseName + " " + suffix;
                         boolean candidateTaken = false;
                         for (int i=0; i<siblings.size(); i++) {
                            if (i != routineIndex && siblings.get(i).name.equals(candidate)) {
                                candidateTaken = true;
                                break;
                            }
                         }
                         if (!candidateTaken) {
                             finalName = candidate;
                             if (suffix == 'B') {
                                 // Rename older to A?
                                 for (int i=0; i<siblings.size(); i++) {
                                     if (i != routineIndex && siblings.get(i).name.equals(baseName)) {
                                         siblings.get(i).name = baseName + " A";
                                         break;
                                     }
                                 }
                             }
                             break;
                         }
                         suffix++;
                     }
                 }
             }

             DraftManager.getInstance().updateRoutineName(routineIndex, finalName);
             // Need to update Tab Text? The activity handles tabs.
             // ConfigureSplitActivity is using TabLayoutMediator which reads from adapter.
             // We need to notify ConfigActivity to refresh tabs.
             // Or update the Routine object inside DraftManager list properly (we did).
             // And call notifyDataSetChanged on ViewPager adapter?
             // TabLayoutMediator handles it if dataset changes?
             // Actually, ConfigureSplitActivity sets up Pager with `routines` list reference.
             // If we modify the name in that list, and call `adapter.notifyDataSetChanged()` in Activity, it updates title.
             // Or just recreate tabs.
             // For now, let's just update data. The user might not see TAB update until reopen or if we trigger callback.
             // DayEditorFragment doesn't have reference to Activity logic.
             return;
         }

         // Existing DB Logic
         AppDatabase.databaseWriteExecutor.execute(() -> {
             AppDatabase db = AppDatabase.getDatabase(getContext());
             Routine current = db.routineDao().getRoutineById(routineId);
             if (current == null) return;

             List<Routine> siblings = db.routineDao().getRoutinesForSplitSync(current.splitId);

             String finalName = baseName;

             // Logic specifically for Full Body Counting
             if ("Full Body".equals(baseName)) {
                 int fbCount = 0;
                 for (Routine r : siblings) {
                     // Don't count self if it's already named Full Body (rename case), but here we are initializing.
                     if (r.id != current.id && r.name.startsWith("Full Body")) {
                         fbCount++;
                     }
                 }
                 // If 0 exist -> Full Body A
                 // If 1 exist -> Full Body B
                 // etc.
                 char suffix = (char) ('A' + fbCount);
                 finalName = "Full Body " + suffix;
             }
             // Logic for A/B/C generic conflict (Upper, Lower, etc)
             else {
                 // Check if exact name exists
                 boolean taken = false;
                 for (Routine r : siblings) {
                     if (r.id != current.id && r.name.equals(baseName)) {
                         taken = true;
                         break;
                     }
                 }

                 if (taken) {
                     // If "Upper" is taken, we might want "Upper B" and rename older to "Upper A"?
                     // Or just "Upper B".
                     // Let's iterate A-Z suffix
                     char suffix = 'A';
                     while (true) {
                         String candidate = baseName + " " + suffix;
                         boolean candidateTaken = false;
                         for (Routine r : siblings) {
                            if (r.id != current.id && r.name.equals(candidate)) {
                                candidateTaken = true;
                                break;
                            }
                         }
                         if (!candidateTaken) {
                             finalName = candidate;

                             // Optional: Rename the original "Base" to "Base A" if we are becoming "Base B"
                             // and the original was just "Base"
                             if (suffix == 'B') {
                                 for (Routine r : siblings) {
                                     if (r.id != current.id && r.name.equals(baseName)) {
                                         r.name = baseName + " A";
                                         db.routineDao().update(r);
                                         break;
                                     }
                                 }
                             }
                             break;
                         }
                         suffix++;
                     }
                 }
             }

             current.name = finalName;
             db.routineDao().update(current);
         });
    }

    private void loadExercises() {
        if (getContext() == null) return;

        if (isDraft) {
            // Load from DraftManager
            List<RoutineExercise> exercises = DraftManager.getInstance().getExercisesForRoutine(routineIndex);
            if (adapter != null) {
                adapter.setExercises(new ArrayList<>(exercises)); // Copy to avoid adapter mutating manager directly?
                // Actually adapter just displays.
                updateEmptyState(exercises.isEmpty());
            }
            return;
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<RoutineExercise> exercises = AppDatabase.getDatabase(getContext())
                    .routineExerciseDao().getExercisesForRoutine(routineId);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    adapter.setExercises(exercises);
                    updateEmptyState(exercises.isEmpty());
                });
            }
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        if (emptyStateLayout != null) {
            emptyStateLayout.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
    }

    private void addExercises(List<String> newExercises) {
        if (getContext() == null) return;

        if (isDraft) {
            List<RoutineExercise> current = DraftManager.getInstance().getExercisesForRoutine(routineIndex);
            int startOrder = current.size() + 1;

            for (String name : newExercises) {
                // Draft Routine ID is 0 or temp?
                // RoutineExercise constructor(routineId, name, order, sets, unit)
                // Routine ID doesn't matter until commit.
                RoutineExercise re = new RoutineExercise(0, name, startOrder++, 4, "KG");
                DraftManager.getInstance().addExercise(routineIndex, re);
            }
            loadExercises(); // Refresh UI
            return;
        }

        // Existing DB logic
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<RoutineExercise> current = AppDatabase.getDatabase(getContext())
                    .routineExerciseDao().getExercisesForRoutine(routineId);
            int startOrder = current.size() + 1;

            List<RoutineExercise> toInsert = new ArrayList<>();
            for (String name : newExercises) {
                toInsert.add(new RoutineExercise(routineId, name, startOrder++, 4, "KG"));
            }

            AppDatabase.getDatabase(getContext()).routineExerciseDao().insertAll(toInsert);
            loadExercises();
        });
    }

    private void deleteExercise(RoutineExercise exercise) {
        if (getContext() == null) return;

        DialogHelper.showConfirmationDialog(
            getContext(),
            getString(R.string.delete_exercise_title),
            getString(R.string.delete_exercise_message, exercise.exerciseName),
            getString(R.string.delete),
            () -> {
                if (isDraft) {
                    List<RoutineExercise> list = DraftManager.getInstance().getExercisesForRoutine(routineIndex);
                    list.remove(exercise);
                    loadExercises();
                } else {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        AppDatabase.getDatabase(getContext()).routineExerciseDao().delete(exercise);
                        loadExercises();
                    });
                }
            }
        );
    }

    private void updateExercise(RoutineExercise exercise) {
        if (getContext() == null) return;
        if (isDraft) {
            // It's already updated in the object reference held by Adapter -> DraftManager?
            // Yes, EditRoutineAdapter usually modifies the object directly if binding allows?
            // Let's check EditRoutineAdapter.
            // If adapter updates fields, we just need to ensure it persists.
            // DraftManager holds the reference.
            // So nothing to "save" to DB.
            return;
        }
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase.getDatabase(getContext()).routineExerciseDao().update(exercise);
        });
    }

    private void setupNameEditor(EditText etName) {
        // Load initial name
        if (isDraft) {
            List<Routine> routines = DraftManager.getInstance().getDraftRoutines();
            if (routineIndex >= 0 && routineIndex < routines.size()) {
                etName.setText(routines.get(routineIndex).name);
            }
        } else {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                Routine r = AppDatabase.getDatabase(getContext()).routineDao().getRoutineById(routineId);
                if (r != null && getActivity() != null) {
                    getActivity().runOnUiThread(() -> etName.setText(r.name));
                }
            });
        }

        // Save on change
        etName.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String newName = s.toString().trim();
                if (newName.isEmpty()) return;

                if (isDraft) {
                    DraftManager.getInstance().updateRoutineName(routineIndex, newName);
                    // Decoupled call
                    if (listener != null) {
                        listener.onRoutineRenamed(routineIndex, newName);
                    }
                } else {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        Routine r = AppDatabase.getDatabase(getContext()).routineDao().getRoutineById(routineId);
                        if (r != null) {
                            r.name = newName;
                            AppDatabase.getDatabase(getContext()).routineDao().update(r);
                        }
                    });
                }
            }
        });
    }

    abstract static class SimpleTextWatcher implements TextWatcher {
         public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
         public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }
}
