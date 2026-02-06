package com.josegarcia.appgym.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.josegarcia.appgym.data.entities.Routine;
import com.josegarcia.appgym.data.entities.Split;

import java.util.List;

@Dao
public interface SplitDao {
    @Insert
    long insert(Split split);

    @Query("SELECT * FROM splits WHERE isActive = 1 LIMIT 1")
    LiveData<Split> getActiveSplit();

    @Query("SELECT * FROM splits")
    List<Split> getAllSplits();

    @Query("SELECT * FROM splits WHERE isTemplate = 1")
    List<Split> getTemplateSplits();

    @Query("SELECT * FROM splits WHERE isTemplate = 0")
    List<Split> getUserSplits();

    @Query("SELECT * FROM splits WHERE id = :id")
    Split getSplitById(int id);

    @Query("UPDATE splits SET isActive = 0")
    void deactivateAll();

    @Query("UPDATE splits SET isActive = 1 WHERE id = :splitId")
    void activateSplit(int splitId);

    @Transaction
    default void setActiveSplit(int splitId) {
        deactivateAll();
        activateSplit(splitId);
    }

    @Delete
    void delete(Split split);
}
