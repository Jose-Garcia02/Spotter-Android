package com.josegarcia.appgym.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.josegarcia.appgym.data.dao.BodyWeightDao;
import com.josegarcia.appgym.data.database.AppDatabase;
import com.josegarcia.appgym.data.entities.BodyWeightLog;

import java.util.List;

public class BodyWeightRepository {
    private final BodyWeightDao mBodyWeightDao;
    private final LiveData<List<BodyWeightLog>> mAllLogs;

    public BodyWeightRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mBodyWeightDao = db.bodyWeightDao();
        mAllLogs = mBodyWeightDao.getAllLogs();
    }

    public LiveData<List<BodyWeightLog>> getAllLogs() {
        return mAllLogs;
    }

    public LiveData<BodyWeightLog> getLatestLog() {
        return mBodyWeightDao.getLastLog();
    }

    public void insert(BodyWeightLog log) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mBodyWeightDao.insert(log);
        });
    }

    public void delete(BodyWeightLog log) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mBodyWeightDao.delete(log);
        });
    }
}
