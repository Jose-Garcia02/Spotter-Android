package com.josegarcia.appgym.ui.weight_tracker;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.josegarcia.appgym.data.BodyWeightRepository;
import com.josegarcia.appgym.data.entities.BodyWeightLog;

import java.util.List;

public class BodyWeightViewModel extends AndroidViewModel {
    private final BodyWeightRepository mRepository;
    private final LiveData<List<BodyWeightLog>> mAllLogs;
    private final LiveData<BodyWeightLog> mLatestLog;

    public BodyWeightViewModel(@NonNull Application application) {
        super(application);
        mRepository = new BodyWeightRepository(application);
        mAllLogs = mRepository.getAllLogs();
        mLatestLog = mRepository.getLatestLog();
    }

    public LiveData<List<BodyWeightLog>> getAllLogs() {
        return mAllLogs;
    }

    public LiveData<BodyWeightLog> getLatestLog() {
        return mLatestLog;
    }

    public void insert(BodyWeightLog log) {
        mRepository.insert(log);
    }

    public void delete(BodyWeightLog log) {
        mRepository.delete(log);
    }
}
