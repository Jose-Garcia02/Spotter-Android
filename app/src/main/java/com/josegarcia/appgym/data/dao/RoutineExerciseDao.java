package com.josegarcia.appgym.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.josegarcia.appgym.data.entities.RoutineExercise;

import java.util.List;

@Dao
public interface RoutineExerciseDao {
    @Insert
    void insertAll(List<RoutineExercise> exercises);

    @Query("SELECT * FROM routine_exercises WHERE routineId = :routineId ORDER BY `order` ASC")
    List<RoutineExercise> getExercisesForRoutine(int routineId);

    // Helper to get by name directly via join, simplifying calling code
    @Query("SELECT re.* FROM routine_exercises re INNER JOIN routines r ON re.routineId = r.id WHERE r.name = :routineName ORDER BY re.`order` ASC")
    List<RoutineExercise> getExercisesForRoutineByName(String routineName);

    @Delete
    void delete(RoutineExercise routineExercise);

    @Update
    void update(RoutineExercise exercise);
}
