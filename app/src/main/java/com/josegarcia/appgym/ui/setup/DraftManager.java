package com.josegarcia.appgym.ui.setup;

import com.josegarcia.appgym.data.entities.Routine;
import com.josegarcia.appgym.data.entities.RoutineExercise;
import com.josegarcia.appgym.data.entities.Split;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DraftManager {
    private static DraftManager instance;

    private Split draftSplit;
    private List<Routine> draftRoutines;
    private Map<Integer, List<RoutineExercise>> draftExercises; // Key: Routine "temp" ID or Index

    private DraftManager() {
        draftExercises = new HashMap<>();
        draftRoutines = new ArrayList<>();
    }

    public static synchronized DraftManager getInstance() {
        if (instance == null) {
            instance = new DraftManager();
        }
        return instance;
    }

    public void startDraft(Split split, List<Routine> routines) {
        this.draftSplit = split;
        this.draftRoutines = routines;
        this.draftExercises.clear();

        // Initialize empty exercise lists for each routine
        // We use the routine's orderIndex or a temp ID as key
        // Since Routines are not in DB, id is 0. keys can be indices.
        for (int i = 0; i < routines.size(); i++) {
            draftExercises.put(i, new ArrayList<>());
        }
    }

    public Split getDraftSplit() {
        return draftSplit;
    }

    public List<Routine> getDraftRoutines() {
        return draftRoutines;
    }

    public List<RoutineExercise> getExercisesForRoutine(int routineIndex) {
        return draftExercises.getOrDefault(routineIndex, new ArrayList<>());
    }

    public void addExercise(int routineIndex, RoutineExercise exercise) {
        if (!draftExercises.containsKey(routineIndex)) {
            draftExercises.put(routineIndex, new ArrayList<>());
        }
        draftExercises.get(routineIndex).add(exercise);
    }

    public void setExercises(int routineIndex, List<RoutineExercise> exercises) {
        draftExercises.put(routineIndex, exercises);
    }

    public void updateRoutineName(int routineIndex, String newName) {
        if (routineIndex >= 0 && routineIndex < draftRoutines.size()) {
            draftRoutines.get(routineIndex).name = newName;
        }
    }

    public void clear() {
        draftSplit = null;
        draftRoutines.clear();
        draftExercises.clear();
    }

    public boolean hasDraft() {
        return draftSplit != null;
    }
}
