package com.josegarcia.appgym.ui.tracker;

import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.josegarcia.appgym.ui.tracker.TrackerAdapter.ExerciseEntry;

import java.util.ArrayList;
import java.util.List;

public class ExerciseTrackerViewModel extends ViewModel {

    private final SavedStateHandle savedStateHandle;

    private static final String KEY_EXERCISE_LIST = "exercise_list";
    private static final String KEY_SESSION_ID = "session_id";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_ROUTINE_NAME = "routine_name";

    // State surviving configuration changes
    private List<ExerciseEntry> exerciseList;
    private long editingSessionId = -1;
    private long sessionTimestamp = -1;
    private String currentRoutineName;
    private boolean isLoaded = false;

    public ExerciseTrackerViewModel(SavedStateHandle savedStateHandle) {
        this.savedStateHandle = savedStateHandle;

        // Restore state
        if (savedStateHandle.contains(KEY_EXERCISE_LIST)) {
            exerciseList = savedStateHandle.get(KEY_EXERCISE_LIST);
            isLoaded = true;
        }
        if (savedStateHandle.contains(KEY_SESSION_ID)) {
            editingSessionId = savedStateHandle.get(KEY_SESSION_ID);
        }
        if (savedStateHandle.contains(KEY_TIMESTAMP)) {
            sessionTimestamp = savedStateHandle.get(KEY_TIMESTAMP);
        }
        if (savedStateHandle.contains(KEY_ROUTINE_NAME)) {
            currentRoutineName = savedStateHandle.get(KEY_ROUTINE_NAME);
        }
    }

    public List<ExerciseEntry> getExerciseList() {
        if (exerciseList == null) {
            exerciseList = new ArrayList<>();
        }
        return exerciseList;
    }

    public void setExerciseList(List<ExerciseEntry> list) {
        this.exerciseList = list;
        savedStateHandle.set(KEY_EXERCISE_LIST, new ArrayList<>(list)); // Ensure it's serializable list
    }

    public long getEditingSessionId() {
        return editingSessionId;
    }

    public void setEditingSessionId(long id) {
        this.editingSessionId = id;
        savedStateHandle.set(KEY_SESSION_ID, id);
    }

    public long getSessionTimestamp() {
        return sessionTimestamp;
    }

    public void setSessionTimestamp(long timestamp) {
        this.sessionTimestamp = timestamp;
        savedStateHandle.set(KEY_TIMESTAMP, timestamp);
    }

    public String getCurrentRoutineName() {
        return currentRoutineName;
    }

    public void setCurrentRoutineName(String name) {
        this.currentRoutineName = name;
        savedStateHandle.set(KEY_ROUTINE_NAME, name);
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public void setLoaded(boolean loaded) {
        isLoaded = loaded;
    }
}
