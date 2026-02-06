package com.josegarcia.appgym.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "workout_sessions")
public class WorkoutSession {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long date; // Timestamp
    public String routineName;

    public WorkoutSession(long date, String routineName) {
        this.date = date;
        this.routineName = routineName;
    }
}
