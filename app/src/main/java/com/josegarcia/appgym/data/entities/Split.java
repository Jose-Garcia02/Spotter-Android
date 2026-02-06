package com.josegarcia.appgym.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "splits")
public class Split {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name; // e.g. "Upper/Lower", "PPL", "Bro Split"
    public String description;
    public boolean isActive;
    public String type; // Classic, Hybrid, Custom
    public boolean isTemplate; // New field

    public Split(String name, String description, boolean isActive, String type) {
        this.name = name;
        this.description = description;
        this.isActive = isActive;
        this.type = type;
        this.isTemplate = false; // Default
    }
}
