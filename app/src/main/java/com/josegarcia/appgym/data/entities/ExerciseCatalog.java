package com.josegarcia.appgym.data.entities;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "exercise_catalog", indices = {
        @Index("name"),
        @Index("muscleTag")
})
public class ExerciseCatalog {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;        // UNIQUE
    public String defaultUnit; // kg, lbs, placas
    public String muscleTag;   // Pecho, Espalda, Hombro, Biceps, Triceps, Pierna, Core
    public boolean isActive;   // soft delete
    public long createdAt;
    public long updatedAt;

    @androidx.room.Ignore
    public ExerciseCatalog(String name, String defaultUnit, String muscleTag) {
        this.name = name;
        this.defaultUnit = defaultUnit;
        this.muscleTag = muscleTag;
        this.isActive = true;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Constructor vacío para Room
    public ExerciseCatalog() {
    }
}
