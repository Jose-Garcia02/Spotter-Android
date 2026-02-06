package com.josegarcia.appgym.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.josegarcia.appgym.data.entities.BodyWeightLog;

import java.util.List;

@Dao
public interface BodyWeightDao {
    @Insert
    long insert(BodyWeightLog log);

    @Update
    void update(BodyWeightLog log);

    @Delete
    void delete(BodyWeightLog log);

    @Query("SELECT * FROM body_weight_logs ORDER BY timestamp DESC")
    LiveData<List<BodyWeightLog>> getAllLogs();

    @Query("SELECT * FROM body_weight_logs ORDER BY timestamp DESC")
    List<BodyWeightLog> getAllLogsSync();

    @Query("SELECT * FROM body_weight_logs ORDER BY timestamp DESC LIMIT 1")
    LiveData<BodyWeightLog> getLastLog();
}
