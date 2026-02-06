package com.josegarcia.appgym.data.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "routine_exercises",
        foreignKeys = @ForeignKey(entity = Routine.class,
                                  parentColumns = "id",
                                  childColumns = "routineId",
                                  onDelete = ForeignKey.CASCADE),
        indices = {@Index("routineId")})
public class RoutineExercise {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int routineId;
    public String exerciseName;
    public int order; // To maintain sequence
    public int targetSets;
    public String targetUnit; // KG, LBS, SEC

    public RoutineExercise(int routineId, String exerciseName, int order, int targetSets, String targetUnit) {
        this.routineId = routineId;
        this.exerciseName = exerciseName;
        this.order = order;
        this.targetSets = targetSets;
        this.targetUnit = targetUnit;
    }
}
