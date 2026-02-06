package com.josegarcia.appgym.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.josegarcia.appgym.data.dao.RoutineDao;
import com.josegarcia.appgym.data.dao.SplitDao;
import com.josegarcia.appgym.data.dao.WorkoutDao;
import com.josegarcia.appgym.data.database.AppDatabase;
import com.josegarcia.appgym.data.entities.BodyWeightLog;
import com.josegarcia.appgym.data.entities.Routine;
import com.josegarcia.appgym.data.entities.RoutineVolumeEntry;
import com.josegarcia.appgym.data.entities.Split;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeViewModel extends AndroidViewModel {
    private final RoutineDao routineDao;
    private final WorkoutDao workoutDao;
    private final LiveData<List<Routine>> activeRoutines;
    private final LiveData<Split> activeSplit;
    private final LiveData<BodyWeightLog> lastWeight;
    private final MutableLiveData<List<RoutineVolumeEntry>> weeklyVolume = new MutableLiveData<>();
    private final MutableLiveData<HeatmapResult> heatmapData = new MutableLiveData<>();

    public static class HeatmapResult {
        // List of Weeks. Each Week is a List<Boolean> of 7 days (Mon-Sun).
        // Actually, logic is List of Boolean status for sequential days.
        // Or we structure it as List<List<DayStatus>>?
        // Let's stick to flat list or structure it for the adapter.
        // Prompt asks for 7 days vertical (rows) x N weeks horizontal (cols).
        // So we should return data organized by weeks?
        // Or flat list and let adapter handle grid?
        // A Horizontal RecyclerView with a Vertical LinearLayout per item (Week) is best for infinite scroll.
        // So we need List<WeekData>.
        public List<WeekData> weeks;

        public HeatmapResult(List<WeekData> weeks) {
            this.weeks = weeks;
        }
    }

    public static class WeekData {
        public List<DayStatus> days = new ArrayList<>();
    }

    public static class DayStatus {
        public long date;
        public boolean active;
        public DayStatus(long date, boolean active) {
            this.date = date;
            this.active = active;
        }
    }

    public HomeViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        routineDao = db.routineDao();
        workoutDao = db.workoutDao();
        SplitDao splitDao = db.splitDao();

        lastWeight = db.bodyWeightDao().getLastLog();

        activeSplit = splitDao.getActiveSplit();

        // Use Transformations.switchMap to dynamically load routines based on Active Split
        activeRoutines = androidx.lifecycle.Transformations.switchMap(activeSplit, split -> {
            if (split == null) {
                return androidx.lifecycle.MutableLiveData.class.cast(getDefaultRoutines()); // Or empty
            }
            return routineDao.getRoutinesForSplit(split.id);
        });
    }

    public LiveData<List<Routine>> getActiveRoutines() {
        return activeRoutines;
    }

    public LiveData<BodyWeightLog> getLastWeight() {
        return lastWeight;
    }

    public LiveData<List<RoutineVolumeEntry>> getWeeklyVolume() {
        return weeklyVolume;
    }

    public LiveData<HeatmapResult> getHeatmapData() {
        return heatmapData;
    }

    public void loadWeeklyVolume() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Calendar cal = Calendar.getInstance();
            // Start of week (Monday)
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            int currentDoW = cal.get(Calendar.DAY_OF_WEEK);
            int daysFromMonday = (currentDoW - Calendar.MONDAY + 7) % 7;
            cal.add(Calendar.DAY_OF_YEAR, -daysFromMonday);
            long start = cal.getTimeInMillis();

            // End of week
            cal.add(Calendar.DAY_OF_YEAR, 7);
            long end = cal.getTimeInMillis();

            List<RoutineVolumeEntry> data = workoutDao.getVolumeByRoutine(start, end);
            weeklyVolume.postValue(data);
        });
    }

    public void loadHeatmap(int weeksCount) { // Now accepts weeks count
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Calendar cal = Calendar.getInstance();

            // Align to current week end or start?
            // "al acabar una semana, automaticamente aparezca una nueva columna"
            // Usually we show current week as the last column.

            // Find start of current week (Monday)
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);

            // Go to Monday of current week
            while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                cal.add(Calendar.DAY_OF_YEAR, -1);
            }

            // Now cal is this Monday.
            // We want to show N previous weeks? Or just current?
            // "aparezca una nueva columna correspondiente a la nueva semana" implies history.
            // Let's load 12 weeks (~3 months) history + current week if needed.

            long currentMonday = cal.getTimeInMillis();

            // Go back N weeks
            cal.add(Calendar.WEEK_OF_YEAR, -(weeksCount - 1));
            long startDate = cal.getTimeInMillis();

            // End date is changing (today or end of this week?)
            // We query ranges.

            List<WeekData> weeks = new ArrayList<>();
            Calendar iterator = Calendar.getInstance();
            iterator.setTimeInMillis(startDate);

            // Fetch all sessions in range efficiently?
            // Or just query days? Querying range is better.
            Calendar endCal = Calendar.getInstance();
            endCal.setTimeInMillis(currentMonday);
            endCal.add(Calendar.DAY_OF_YEAR, 7); // End of this week
            long endDate = endCal.getTimeInMillis();

            // Optimized Query: Only fetch dates (Long), not full objects
            List<Long> activeDates = workoutDao.getSessionDatesInRange(startDate, endDate);

            java.util.Set<Long> activeDays = new java.util.HashSet<>();
            for (Long dateTimestamp : activeDates) {
                if (dateTimestamp == null) continue;
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(dateTimestamp);
                c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0);
                activeDays.add(c.getTimeInMillis());
            }

            for (int w = 0; w < weeksCount; w++) {
                WeekData week = new WeekData();
                for (int d = 0; d < 7; d++) {
                    long t = iterator.getTimeInMillis();
                    boolean active = activeDays.contains(t);
                    week.days.add(new DayStatus(t, active));
                    iterator.add(Calendar.DAY_OF_YEAR, 1);
                }
                weeks.add(week);
            }

            heatmapData.postValue(new HeatmapResult(weeks));
        });
    }

    private LiveData<List<Routine>> getDefaultRoutines() {
        return routineDao.getAllRoutines(); // Fallback if no split logic active yet
    }
}
