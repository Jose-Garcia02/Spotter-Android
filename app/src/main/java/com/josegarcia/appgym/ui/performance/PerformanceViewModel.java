package com.josegarcia.appgym.ui.performance;
import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.josegarcia.appgym.data.dao.WorkoutDao;
import com.josegarcia.appgym.data.database.AppDatabase;
import com.josegarcia.appgym.data.entities.ExerciseSet;
import com.josegarcia.appgym.data.entities.PerformanceComparison;
import com.josegarcia.appgym.data.entities.WorkoutSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class PerformanceViewModel extends AndroidViewModel {
    private final WorkoutDao workoutDao;
    private final MutableLiveData<List<PerformanceComparison>> comparisonResults = new MutableLiveData<>();
    private final MutableLiveData<List<String>> routinesWithHistory = new MutableLiveData<>();
    private final MutableLiveData<String> comparisonHeader = new MutableLiveData<>();
    public PerformanceViewModel(@NonNull Application application) {
        super(application);
        workoutDao = AppDatabase.getDatabase(application).workoutDao();
    }
    public LiveData<List<PerformanceComparison>> getComparisonResults() {
        return comparisonResults;
    }
    public LiveData<List<String>> getRoutinesWithHistory() {
        return routinesWithHistory;
    }
    public LiveData<String> getComparisonHeader() {
        return comparisonHeader;
    }
    public void loadRoutines() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<String> routines = workoutDao.getRoutineNamesWithHistory();
            routinesWithHistory.postValue(routines);
        });
    }
    public void compareCurrentVsPrevious(String routineName) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<WorkoutSession> history = workoutDao.getSessionsForRoutine(routineName, 2);
            if (history == null || history.size() < 2) {
                comparisonHeader.postValue("No hay sesiones previas de " + routineName + " para comparar.");
                comparisonResults.postValue(new ArrayList<>());
                return;
            }
            WorkoutSession current = history.get(0); // The most recent
            WorkoutSession previous = history.get(1); // The one before
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yy", java.util.Locale.getDefault());
            String dateCur = sdf.format(new java.util.Date(current.date));
            String datePrev = sdf.format(new java.util.Date(previous.date));
            comparisonHeader.postValue("Comparando " + routineName + " (" + dateCur + " vs " + datePrev + ")");
            List<ExerciseSet> currentSets = workoutDao.getSetsForSession(current.id);
            List<ExerciseSet> previousSets = workoutDao.getSetsForSession(previous.id);
            List<PerformanceComparison> results = new ArrayList<>();
            // Group sets by exercise name to display them with a header
            Map<String, List<ExerciseSet>> groupedCurrent = new HashMap<>();
            // To maintain order, we keep a list of unique names as they appear
            List<String> orderedExercises = new ArrayList<>();
            for (ExerciseSet set : currentSets) {
                if (!groupedCurrent.containsKey(set.exerciseName)) {
                    groupedCurrent.put(set.exerciseName, new ArrayList<>());
                    orderedExercises.add(set.exerciseName);
                }
                groupedCurrent.get(set.exerciseName).add(set);
            }
            for (String exerciseName : orderedExercises) {
                // Add Header flag object
                PerformanceComparison header = new PerformanceComparison();
                header.isHeader = true;
                header.exerciseName = exerciseName;
                results.add(header);
                List<ExerciseSet> setsForExercise = groupedCurrent.get(exerciseName);
                for (ExerciseSet currentSet : setsForExercise) {
                    ExerciseSet prevSet = null;
                    for (ExerciseSet s : previousSets) {
                        if (s.exerciseName.equalsIgnoreCase(exerciseName) && s.setOrder == currentSet.setOrder) {
                            prevSet = s;
                            break;
                        }
                    }
                    PerformanceComparison comp = new PerformanceComparison();
                    comp.isHeader = false;
                    comp.exerciseName = exerciseName; // Keep track if needed
                    comp.setOrder = currentSet.setOrder;
                    comp.currentWeight = currentSet.weight;
                    comp.currentReps = currentSet.reps;
                    comp.unit = currentSet.unit != null ? currentSet.unit : "kg";
                    if (prevSet == null) {
                        comp.status = "NUEVO";
                        comp.previousWeight = 0;
                        comp.previousReps = 0;
                    } else {
                        comp.previousWeight = prevSet.weight;
                        comp.previousReps = prevSet.reps;
                        if (currentSet.weight > prevSet.weight) {
                            comp.status = "MEJORA";
                        } else if (currentSet.weight < prevSet.weight) {
                            comp.status = "EMPEORA";
                        } else {
                            if (currentSet.reps > prevSet.reps) {
                                comp.status = "MEJORA";
                            } else if (currentSet.reps < prevSet.reps) {
                                comp.status = "EMPEORA";
                            } else {
                                comp.status = "MANTIENE";
                            }
                        }
                    }
                    results.add(comp);
                }
            }
            comparisonResults.postValue(results);
        });
    }
}
