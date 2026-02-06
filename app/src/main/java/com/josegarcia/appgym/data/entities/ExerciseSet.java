package com.josegarcia.appgym.data.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "exercise_sets",
        foreignKeys = @ForeignKey(entity = WorkoutSession.class,
                parentColumns = "id",
                childColumns = "sessionId",
                onDelete = CASCADE),
        indices = {@Index("sessionId")})
public class ExerciseSet {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long sessionId;
    public String exerciseName;
    public double weight;
    public int reps;
    public int setOrder; // 1, 2, 3...
    public String unit; // "kg" or "plates"

    public ExerciseSet(long sessionId, String exerciseName, double weight, int reps, int setOrder) {
        this.sessionId = sessionId;
        this.exerciseName = exerciseName;
        this.weight = weight;
        this.reps = reps;
        this.setOrder = setOrder;
        this.unit = "kg"; // Default
    }
}
