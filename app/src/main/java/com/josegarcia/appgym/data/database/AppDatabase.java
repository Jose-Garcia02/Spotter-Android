package com.josegarcia.appgym.data.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.josegarcia.appgym.R;
import com.josegarcia.appgym.data.dao.BodyWeightDao;
import com.josegarcia.appgym.data.dao.RoutineDao;
import com.josegarcia.appgym.data.dao.WorkoutDao;
import com.josegarcia.appgym.data.dao.RoutineExerciseDao;
import com.josegarcia.appgym.data.dao.SplitDao;
import com.josegarcia.appgym.data.entities.BodyWeightLog;
import com.josegarcia.appgym.data.entities.ExerciseSet;
import com.josegarcia.appgym.data.entities.Routine;
import com.josegarcia.appgym.data.entities.RoutineExercise;
import com.josegarcia.appgym.data.entities.Split;
import com.josegarcia.appgym.data.entities.WorkoutSession;
import com.josegarcia.appgym.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {WorkoutSession.class, ExerciseSet.class, BodyWeightLog.class, Routine.class, RoutineExercise.class, Split.class}, version = 9, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract WorkoutDao workoutDao();
    public abstract BodyWeightDao bodyWeightDao();
    public abstract RoutineDao routineDao();
    public abstract RoutineExerciseDao routineExerciseDao();
    public abstract SplitDao splitDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `body_weight_logs` " +
                    "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`timestamp` INTEGER NOT NULL, " +
                    "`weight` REAL NOT NULL, " +
                    "`photoUri` TEXT)");
        }
    };

    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `routines` " +
                    "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`name` TEXT, " +
                    "`colorResId` INTEGER NOT NULL, " +
                    "`isSystem` INTEGER NOT NULL)");
        }
    };

    static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `routine_exercises` " +
                    "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`routineId` INTEGER NOT NULL, " +
                    "`exerciseName` TEXT, " +
                    "`order` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`routineId`) REFERENCES `routines`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_routine_exercises_routineId` ON `routine_exercises` (`routineId`)");
        }
    };

    static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Create Splits table
            database.execSQL("CREATE TABLE IF NOT EXISTS `splits` " +
                    "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`name` TEXT, `description` TEXT, `isActive` INTEGER NOT NULL)");

            // Create new Routines table with foreign key to Splits
            database.execSQL("CREATE TABLE IF NOT EXISTS `routines_new` " +
                    "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`splitId` INTEGER NOT NULL, " +
                    "`name` TEXT, " +
                    "`colorResId` INTEGER NOT NULL, " +
                    "`isSystem` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`splitId`) REFERENCES `splits`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)");

            database.execSQL("CREATE INDEX IF NOT EXISTS `index_routines_new_splitId` ON `routines_new` (`splitId`)");

            // Insert default split
            database.execSQL("INSERT INTO `splits` (`id`, `name`, `description`, `isActive`) VALUES (1, 'Default Split', 'Plan predeterminado', 1)");

            // Copy data from old routines to new table, assigning splitId=1
            database.execSQL("INSERT INTO `routines_new` (`id`, `splitId`, `name`, `colorResId`, `isSystem`) " +
                    "SELECT `id`, 1, `name`, `colorResId`, `isSystem` FROM `routines`");

            // Drop old table
            database.execSQL("DROP TABLE `routines`");

            // Rename new table
            database.execSQL("ALTER TABLE `routines_new` RENAME TO `routines`");
        }
    };

    static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Add type to splits
            database.execSQL("ALTER TABLE `splits` ADD COLUMN `type` TEXT");

            // Add orderIndex to routines
            database.execSQL("ALTER TABLE `routines` ADD COLUMN `orderIndex` INTEGER NOT NULL DEFAULT 0");

            // Add targetSets and targetUnit to routine_exercises
            database.execSQL("ALTER TABLE `routine_exercises` ADD COLUMN `targetSets` INTEGER NOT NULL DEFAULT 4");
            database.execSQL("ALTER TABLE `routine_exercises` ADD COLUMN `targetUnit` TEXT");
        }
    };

    static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE `splits` ADD COLUMN `isTemplate` INTEGER NOT NULL DEFAULT 0");
            // Set existing Classic splits as templates
            database.execSQL("UPDATE `splits` SET `isTemplate` = 1 WHERE `type` = 'Classic'");
        }
    };

    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "gym_database")
                            .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
                            .addCallback(new Callback() {
                                @Override
                                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                                    super.onOpen(db);
                                    // Prepopulate logic
                                    databaseWriteExecutor.execute(() -> {
                                        SplitDao splitDao = INSTANCE.splitDao();
                                        RoutineDao rDao = INSTANCE.routineDao();
                                        RoutineExerciseDao reDao = INSTANCE.routineExerciseDao();

                                        List<Split> currentSplits = splitDao.getAllSplits();

                                        // Cleanup migration artifact "Default Split" if it's the only one or invalid
                                        // This fixes the "Black Screen" issue where Default Split blocked initialization but wasn't Classic
                                        if (currentSplits.size() == 1 && "Default Split".equals(currentSplits.get(0).name)) {
                                            splitDao.delete(currentSplits.get(0));
                                            currentSplits.clear();
                                        }

                                        // RECOVERY: Ensure the ACTIVE split is treated as a User split (not a template)
                                        // This fixes the issue where an active classic split disappears from "My Plans"
                                        databaseWriteExecutor.execute(() -> {
                                            // Direct SQL or DAO
                                            INSTANCE.getOpenHelper().getWritableDatabase()
                                                .execSQL("UPDATE splits SET isTemplate = 0 WHERE isActive = 1");
                                        });

                                        // If empty (fresh install or after cleanup), populate Classics
                                        if (currentSplits.isEmpty()) {
                                            // Split 1: Upper/Lower (Active by default -> changed to false)
                                            Split s1 = new Split("Upper / Lower", "Frecuencia 4 días", false, "Classic");
                                            s1.isTemplate = true;
                                            long ulId = splitDao.insert(s1);

                                            // Use new helper
                                            List<Routine> ulRoutines = InitialData.getUpperLowerRoutines((int)ulId);
                                            List<Long> ulRoutineIds = rDao.insertAll(ulRoutines);

                                            // Populate exercises for Upper/Lower
                                            // Assuming order follows insertion, we traverse
                                            for(int i=0; i<ulRoutines.size(); i++) {
                                                populateExercises(reDao, ulRoutineIds.get(i).intValue(), ulRoutines.get(i).name);
                                            }

                                            // Split 2: PPL (Push Pull Legs)
                                            Split s2 = new Split("Push / Pull / Legs", "Frecuencia 6 días", false, "Classic");
                                            s2.isTemplate = true;
                                            long pplId = splitDao.insert(s2);
                                            List<Routine> pplRoutines = InitialData.getPPLRoutines((int)pplId);
                                            List<Long> pplIds = rDao.insertAll(pplRoutines);
                                            for(int i=0; i<pplRoutines.size(); i++) {
                                                populateExercises(reDao, pplIds.get(i).intValue(), pplRoutines.get(i).name);
                                            }

                                            // Split 3: Arnold
                                            Split s3 = new Split("Arnold Split", "Pecho/Espalda + Hombro/Brazo + Pierna", false, "Classic");
                                            s3.isTemplate = true;
                                            long arnoldId = splitDao.insert(s3);
                                            List<Routine> arnoldRoutines = InitialData.getArnoldRoutines((int)arnoldId);
                                            List<Long> arnoldIds = rDao.insertAll(arnoldRoutines);
                                            for(int i=0; i<arnoldRoutines.size(); i++) {
                                                populateExercises(reDao, arnoldIds.get(i).intValue(), arnoldRoutines.get(i).name);
                                            }

                                            // Split 4: Full Body
                                            Split s4 = new Split("Full Body", "Frecuencia 3 días", false, "Classic");
                                            s4.isTemplate = true;
                                            long fbId = splitDao.insert(s4);
                                            List<Routine> fbRoutines = InitialData.getFullBodyRoutines((int)fbId);
                                            List<Long> fbIds = rDao.insertAll(fbRoutines);
                                            for(int i=0; i<fbRoutines.size(); i++) {
                                                populateExercises(reDao, fbIds.get(i).intValue(), fbRoutines.get(i).name);
                                            }
                                        }
                                    });
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static void populateExercises(RoutineExerciseDao dao, int routineId, String routineName) {
        List<RoutineExercise> list = new ArrayList<>();
        int order = 1;

        String[] exercises = InitialData.getExercisesForRoutine(routineName);
        for (String ex : exercises) {
            // Default targets: 3 sets (per new requirement), KG
            list.add(new RoutineExercise(routineId, ex, order++, 3, "KG"));
        }
        if (!list.isEmpty()) {
            dao.insertAll(list);
        }
    }
}
