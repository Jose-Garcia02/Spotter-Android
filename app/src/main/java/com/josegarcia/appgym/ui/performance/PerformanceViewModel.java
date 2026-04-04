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
import java.util.List;
public class PerformanceViewModel extends AndroidViewModel {
    private final WorkoutDao workoutDao;
    private final MutableLiveData<List<PerformanceComparison>> comparisonResults = new MutableLiveData<>();
    private final MutableLiveData<List<String>> exercisesWithHistory = new MutableLiveData<>();
    private final MutableLiveData<String> comparisonHeader = new MutableLiveData<>();
    public PerformanceViewModel(@NonNull Application application) {
        super(application);
        workoutDao = AppDatabase.getDatabase(application).workoutDao();
    }
    public LiveData<List<PerformanceComparison>> getComparisonResults() {
        return comparisonResults;
    }
    public LiveData<List<String>> getExercisesWithHistory() {
        return exercisesWithHistory;
    }
    public LiveData<String> getComparisonHeader() {
        return comparisonHeader;
    }
    public void loadExercises() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<String> exercises = workoutDao.getExercisesWithHistory();
            exercisesWithHistory.postValue(exercises);
        });
    }
    public void compareCurrentVsPrevious(String exerciseName) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<WorkoutSession> history = workoutDao.getSessionsForExercise(exerciseName, 2);
            if (history == null || history.size() < 2) {
                comparisonHeader.postValue("No hay sesiones previas suficientes de " + exerciseName + " para comparar.");
                comparisonResults.postValue(new ArrayList<>());
                return;
            }
            WorkoutSession current = history.get(0); // The most recent
            WorkoutSession previous = history.get(1); // The one before
            // Date format could go to strings but we construct here for simplicity, assuming simple MM/dd.
            // Ideally format in Fragment but we pass the msg here
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yy", java.util.Locale.getDefault());
            String dateCur = sdf.format(new java.util.Date(current.date));
            String datePrev = sdf.format(new java.util.Date(previous.date));
            comparisonHeader.postValue("Comparando " + exerciseName + " (" + dateCur + " vs " + datePrev + ")");
            List<ExerciseSet> currentSets = workoutDao.getSetsForSession(current.id);
            List<ExerciseSet> previousSets = workoutDao.getSetsForSession(previous.id);
            List<PerformanceComparison> results = new ArrayList<>();
            // We only look at current Sets that match the exercise
            for (ExerciseSet currentSet : currentSets) {
                if (!currentSet.exerciseName.equalsIgnoreCase(exerciseName)) continue;
                ExerciseSet prevSet = null;
                for (ExerciseSet s : previousSets) {
                    if (s.exerciseName.equalsIgnoreCase(exerciseName) && s.setOrder == currentSet.setOrder) {
                        prevSet = s;
                        break;
                    }
                }
                PerformanceComparison comp = new PerformanceComparison();
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
                    // Basic Logic
                    // We value weight > reps. So if weight is more, it's green.
                    // If weight is same but reps are more, it's green.
                    if (currentSet.weight > prevSet.weight) {
                        comp.status = "MEJORA";
                    } else if (currentSet.weight < prevSet.weight) {
                        comp.status = "EMPEORA";
                    } else {
                        // Same weight
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
            comparisonResults.postValue(results);
        });
    }
}
