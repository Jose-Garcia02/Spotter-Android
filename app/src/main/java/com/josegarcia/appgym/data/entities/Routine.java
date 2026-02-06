package com.josegarcia.appgym.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "routines",
        foreignKeys = @androidx.room.ForeignKey(entity = Split.class,
                parentColumns = "id",
                childColumns = "splitId",
                onDelete = androidx.room.ForeignKey.CASCADE),
        indices = {@androidx.room.Index("splitId")})
public class Routine {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int splitId; // Link to parent Split
    public String name;
    public int colorResId; // Resource ID for identifying color logic or int color logic
    public boolean isSystem;
    public int orderIndex;

    public Routine(int splitId, String name, int colorResId, boolean isSystem, int orderIndex) {
        this.splitId = splitId;
        this.name = name;
        this.colorResId = colorResId;
        this.isSystem = isSystem;
        this.orderIndex = orderIndex;
    }
}
