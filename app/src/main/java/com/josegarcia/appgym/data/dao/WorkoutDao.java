package com.josegarcia.appgym.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;


import com.josegarcia.appgym.data.entities.ExerciseProgressEntry;
import com.josegarcia.appgym.data.entities.SessionVolumeEntry; // Added
import com.josegarcia.appgym.data.entities.ExerciseSet;
import com.josegarcia.appgym.data.entities.WorkoutSession;
import com.josegarcia.appgym.data.entities.RoutineVolumeEntry; // Added

import java.util.List;

@Dao
public interface WorkoutDao {
    @Insert
    long insertSession(WorkoutSession session);

    @Update
    void updateSession(WorkoutSession session);

    @Insert
    void insertSets(List<ExerciseSet> sets);

    @Query("DELETE FROM exercise_sets WHERE sessionId = :sessionId")
    void deleteSetsForSession(long sessionId);

    @Query("SELECT COUNT(*) FROM workout_sessions")
    int getSessionCount();

    @Query("SELECT * FROM workout_sessions ORDER BY date DESC")
    List<WorkoutSession> getAllSessions();

    // Evitar duplicados al importar
    @Query("SELECT * FROM workout_sessions WHERE date = :date AND routineName = :routineName LIMIT 1")
    WorkoutSession getSessionByDateAndRoutine(long date, String routineName);

    @Delete
    void deleteSession(WorkoutSession session); // New delete method

    @Query("SELECT * FROM exercise_sets WHERE sessionId = :sessionId ORDER BY id ASC")
    List<ExerciseSet> getSetsForSession(long sessionId);

    // Obtener series recientes
    @Query("SELECT sets.* FROM exercise_sets sets INNER JOIN workout_sessions session ON sets.sessionId = session.id WHERE TRIM(UPPER(sets.exerciseName)) = TRIM(UPPER(:exerciseName)) ORDER BY session.date DESC, sets.setOrder ASC LIMIT 10")
    List<ExerciseSet> getLastSetsForExercise(String exerciseName);

    // Obtener ultimo log
    @Query("SELECT sets.* FROM exercise_sets sets INNER JOIN workout_sessions session ON sets.sessionId = session.id WHERE TRIM(UPPER(sets.exerciseName)) = TRIM(UPPER(:exerciseName)) ORDER BY session.date DESC LIMIT 1")
    ExerciseSet getLastLogForExercise(String exerciseName);

    @Query("SELECT * FROM workout_sessions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    List<WorkoutSession> getSessionsBetweenDates(long startDate, long endDate);

    // Método de utilidad para correcciones manuales
    @Query("DELETE FROM workout_sessions WHERE date = :dateStamp")
    void deleteSessionByDate(long dateStamp);

    // Consultas estadísticas
    @Query("SELECT DISTINCT exerciseName FROM exercise_sets ORDER BY exerciseName ASC")
    List<String> getExercisesWithHistory();

    @Query("SELECT DISTINCT exerciseName FROM exercise_sets WHERE exerciseName LIKE '%' || :query || '%' ORDER BY exerciseName ASC")
    List<String> searchExercisesWithHistory(String query);

    @Query("SELECT DISTINCT exerciseName FROM exercise_sets UNION SELECT DISTINCT exerciseName FROM routine_exercises ORDER BY exerciseName ASC")
    List<String> getAllExerciseNames();

    // Métodos de sanitización de nombres antiguos
    @Query("UPDATE exercise_sets SET exerciseName = :newName WHERE exerciseName = :oldName")
    void renameExercise(String oldName, String newName);

    // Consulta de progreso detallado
    @Query("SELECT session.date, sets.setOrder, sets.weight, sets.reps, sets.unit FROM exercise_sets sets " +
            "INNER JOIN workout_sessions session ON sets.sessionId = session.id " +
            "WHERE sets.exerciseName = :exerciseName " +
            "ORDER BY session.date ASC, sets.setOrder ASC")
    List<ExerciseProgressEntry> getDetailedProgressForExercise(String exerciseName);

    // Consulta de Volumen de Carga Global
    @Query("SELECT session.date, SUM(sets.weight * sets.reps) as totalVolume FROM exercise_sets sets " +
            "INNER JOIN workout_sessions session ON sets.sessionId = session.id " +
            "GROUP BY session.id " +
            "ORDER BY session.date ASC")
    List<SessionVolumeEntry> getGlobalVolumeHistory();

    // Consulta Volumen por Rutina en rango de fechas
    @Query("SELECT session.routineName, SUM(sets.weight * sets.reps) as volume FROM exercise_sets sets " +
            "INNER JOIN workout_sessions session ON sets.sessionId = session.id " +
            "WHERE session.date BETWEEN :startDate AND :endDate " +
            "GROUP BY session.routineName")
    List<RoutineVolumeEntry> getVolumeByRoutine(long startDate, long endDate);

    // Consulta para el Heatmap (Optimizado)
    // Retorna solo las fechas únicas de sesiones en el rango, mucho más ligero que traer objetos completos.
    @Query("SELECT DISTINCT date FROM workout_sessions WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    List<Long> getSessionDatesInRange(long startDate, long endDate);

    // Consulta de Volumen Semanal Total
    // Retorna un solo valor (suma total) para un rango dado (ej. la semana actual).
    @Query("SELECT SUM(sets.weight * sets.reps) FROM exercise_sets sets " +
           "INNER JOIN workout_sessions session ON sets.sessionId = session.id " +
           "WHERE session.date BETWEEN :startDate AND :endDate")
    Long getTotalVolumeInRange(long startDate, long endDate);

    // Consulta de Constancia (Contador de dias activos vs total dias)
    // Útil para estadísticas rápidas "X entrenamientos este mes"
    @Query("SELECT COUNT(DISTINCT date) FROM workout_sessions WHERE date BETWEEN :startDate AND :endDate")
    int getActiveDaysCount(long startDate, long endDate);
}
