package com.josegarcia.appgym.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.josegarcia.appgym.data.entities.Routine;

import java.util.List;

@Dao
public interface RoutineDao {
    @Insert
    long insert(Routine routine);

    @Insert
    List<Long> insertAll(List<Routine> routines);

    @Update
    void update(Routine routine);

    @Delete
    void delete(Routine routine);

    @Query("SELECT * FROM routines ORDER BY id ASC")
    LiveData<List<Routine>> getAllRoutines();

    @Query("SELECT * FROM routines WHERE splitId = :splitId ORDER BY id ASC")
    LiveData<List<Routine>> getRoutinesForSplit(int splitId);

    @Query("SELECT * FROM routines WHERE splitId = :splitId ORDER BY id ASC")
    List<Routine> getRoutinesForSplitSync(int splitId); // For background thread checks

    @Query("SELECT * FROM routines WHERE id = :id")
    Routine getRoutineById(int id);

    @Query("SELECT count(*) FROM routines")
    int getCount();
}
