package com.josegarcia.appgym.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.josegarcia.appgym.data.entities.ExerciseCatalog;

import java.util.List;

@Dao
public interface ExerciseCatalogDao {
    @Insert
    long insert(ExerciseCatalog exercise);

    @Insert
    List<Long> insertAll(List<ExerciseCatalog> exercises);

    @Update
    void update(ExerciseCatalog exercise);

    @Query("SELECT * FROM exercise_catalog WHERE isActive = 1 ORDER BY muscleTag, name ASC")
    LiveData<List<ExerciseCatalog>> getAllActive();

    @Query("SELECT * FROM exercise_catalog WHERE isActive = 1 AND muscleTag = :muscleTag ORDER BY name ASC")
    List<ExerciseCatalog> getByMuscleTag(String muscleTag);

    @Query("SELECT * FROM exercise_catalog WHERE name = :name LIMIT 1")
    ExerciseCatalog getByName(String name);

    @Query("UPDATE exercise_catalog SET isActive = 0 WHERE id = :id")
    void deleteById(long id);

    @Query("SELECT COUNT(*) FROM exercise_catalog")
    int getCount();
}

