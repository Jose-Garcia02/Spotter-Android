package com.josegarcia.appgym.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "body_weight_logs")
public class BodyWeightLog {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long timestamp;
    public float weight;
    public String photoUri; // Changed from path to uri string for better compatibility

    public BodyWeightLog(long timestamp, float weight, String photoUri) {
        this.timestamp = timestamp;
        this.weight = weight;
        this.photoUri = photoUri;
    }
}
