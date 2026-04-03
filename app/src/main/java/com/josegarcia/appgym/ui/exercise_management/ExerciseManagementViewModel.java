package com.josegarcia.appgym.ui.exercise_management;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.josegarcia.appgym.data.database.AppDatabase;
import com.josegarcia.appgym.data.dao.ExerciseCatalogDao;
import com.josegarcia.appgym.data.entities.ExerciseCatalog;

import java.util.List;

public class ExerciseManagementViewModel extends AndroidViewModel {
    private ExerciseCatalogDao dao;
    public LiveData<List<ExerciseCatalog>> allExercises;

    public ExerciseManagementViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        dao = db.exerciseCatalogDao();
        allExercises = dao.getAllActive();
    }

    public void insertExercise(String name, String unit, String muscleTag) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            ExerciseCatalog exercise = new ExerciseCatalog(name, unit, muscleTag);
            dao.insert(exercise);
        });
    }

    public void updateExercise(long id, String name, String unit, String muscleTag) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            ExerciseCatalog exercise = new ExerciseCatalog(name, unit, muscleTag);
            exercise.id = id;
            dao.update(exercise);
        });
    }

    public void deleteExercise(long id) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            dao.deleteById(id);
        });
    }
}

