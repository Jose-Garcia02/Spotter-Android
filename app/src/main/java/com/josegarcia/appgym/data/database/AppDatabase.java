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
import com.josegarcia.appgym.data.dao.ExerciseCatalogDao;
import com.josegarcia.appgym.data.entities.BodyWeightLog;
import com.josegarcia.appgym.data.entities.ExerciseSet;
import com.josegarcia.appgym.data.entities.Routine;
import com.josegarcia.appgym.data.entities.RoutineExercise;
import com.josegarcia.appgym.data.entities.Split;
import com.josegarcia.appgym.data.entities.WorkoutSession;
import com.josegarcia.appgym.data.entities.ExerciseCatalog;
import com.josegarcia.appgym.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {WorkoutSession.class, ExerciseSet.class, BodyWeightLog.class, Routine.class, RoutineExercise.class, Split.class, ExerciseCatalog.class}, version = 10, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract WorkoutDao workoutDao();
    public abstract BodyWeightDao bodyWeightDao();
    public abstract RoutineDao routineDao();
    public abstract RoutineExerciseDao routineExerciseDao();
    public abstract SplitDao splitDao();
    public abstract ExerciseCatalogDao exerciseCatalogDao();

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

    static final Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `exercise_catalog` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`name` TEXT NOT NULL UNIQUE, " +
                    "`defaultUnit` TEXT, " +
                    "`muscleTag` TEXT NOT NULL, " +
                    "`isActive` INTEGER NOT NULL DEFAULT 1, " +
                    "`createdAt` INTEGER NOT NULL, " +
                    "`updatedAt` INTEGER NOT NULL)");
            database.execSQL("CREATE INDEX `index_exercise_catalog_name` ON `exercise_catalog` (`name`)");
            database.execSQL("CREATE INDEX `index_exercise_catalog_muscleTag` ON `exercise_catalog` (`muscleTag`)");
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
                            .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
                            .addCallback(new Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                }

                                @Override
                                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                                    super.onOpen(db);
                                    // Seed synchronously to ensure data is ready
                                    seedIfEmpty();
                                    cleanupAndValidate();
                                }

                                private void seedIfEmpty() {
                                    try {
                                        SplitDao splitDao = INSTANCE.splitDao();
                                        List<Split> existingSplits = splitDao.getAllSplits();

                                        if (!existingSplits.isEmpty()) {
                                            return; // Already has data
                                        }

                                        // Seed exercise catalog
                                        ExerciseCatalogDao catalogDao = INSTANCE.exerciseCatalogDao();
                                        if (catalogDao.getCount() == 0) {
                                            List<ExerciseCatalog> catalog = getExerciseCatalogSeeds();
                                            catalogDao.insertAll(catalog);
                                        }

                                        // Seed classic splits
                                        RoutineDao rDao = INSTANCE.routineDao();
                                        RoutineExerciseDao reDao = INSTANCE.routineExerciseDao();

                                        // Split 1: Upper/Lower
                                        Split s1 = new Split("Upper / Lower", "Frecuencia 4 días", false, "Classic");
                                        s1.isTemplate = false; // User split, not template
                                        long ulId = splitDao.insert(s1);
                                        List<Routine> ulRoutines = InitialData.getUpperLowerRoutines((int)ulId);
                                        List<Long> ulRoutineIds = rDao.insertAll(ulRoutines);
                                        for(int i=0; i<ulRoutines.size(); i++) {
                                            populateExercisesSync(reDao, ulRoutineIds.get(i).intValue(), ulRoutines.get(i).name);
                                        }

                                        // Split 2: PPL
                                        Split s2 = new Split("Push / Pull / Legs", "Frecuencia 6 días", false, "Classic");
                                        s2.isTemplate = false; // User split, not template
                                        long pplId = splitDao.insert(s2);
                                        List<Routine> pplRoutines = InitialData.getPPLRoutines((int)pplId);
                                        List<Long> pplIds = rDao.insertAll(pplRoutines);
                                        for(int i=0; i<pplRoutines.size(); i++) {
                                            populateExercisesSync(reDao, pplIds.get(i).intValue(), pplRoutines.get(i).name);
                                        }

                                        // Split 3: Arnold
                                        Split s3 = new Split("Arnold Split", "Pecho/Espalda + Hombro/Brazo + Pierna", false, "Classic");
                                        s3.isTemplate = false; // User split, not template
                                        long arnoldId = splitDao.insert(s3);
                                        List<Routine> arnoldRoutines = InitialData.getArnoldRoutines((int)arnoldId);
                                        List<Long> arnoldIds = rDao.insertAll(arnoldRoutines);
                                        for(int i=0; i<arnoldRoutines.size(); i++) {
                                            populateExercisesSync(reDao, arnoldIds.get(i).intValue(), arnoldRoutines.get(i).name);
                                        }

                                        // Split 4: Full Body
                                        Split s4 = new Split("Full Body", "Frecuencia 3 días", false, "Classic");
                                        s4.isTemplate = false; // User split, not template
                                        long fbId = splitDao.insert(s4);
                                        List<Routine> fbRoutines = InitialData.getFullBodyRoutines((int)fbId);
                                        List<Long> fbIds = rDao.insertAll(fbRoutines);
                                        for(int i=0; i<fbRoutines.size(); i++) {
                                            populateExercisesSync(reDao, fbIds.get(i).intValue(), fbRoutines.get(i).name);
                                        }
                                    } catch (Exception e) {
                                        android.util.Log.e("AppDatabase", "Error seeding data", e);
                                    }
                                }


                                private void cleanupAndValidate() {
                                    try {
                                        SplitDao splitDao = INSTANCE.splitDao();
                                        List<Split> currentSplits = splitDao.getAllSplits();

                                        // Cleanup migration artifact "Default Split"
                                        if (currentSplits.size() == 1 && "Default Split".equals(currentSplits.get(0).name)) {
                                            splitDao.delete(currentSplits.get(0));
                                        }

                                        // RECOVERY: Ensure the ACTIVE split is treated as a User split
                                        INSTANCE.getOpenHelper().getWritableDatabase()
                                            .execSQL("UPDATE splits SET isTemplate = 0 WHERE isActive = 1");
                                    } catch (Exception e) {
                                        android.util.Log.e("AppDatabase", "Error in cleanup", e);
                                    }
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static void populateExercisesSync(RoutineExerciseDao dao, int routineId, String routineName) {
        List<RoutineExercise> list = new ArrayList<>();
        int order = 1;

        String[] exercises = InitialData.getExercisesForRoutine(routineName);
        for (String ex : exercises) {
            list.add(new RoutineExercise(routineId, ex, order++, 3, "KG"));
        }
        if (!list.isEmpty()) {
            dao.insertAll(list);
        }
    }

    private static List<ExerciseCatalog> getExerciseCatalogSeeds() {
        List<ExerciseCatalog> exercises = new ArrayList<>();

        // Pecho (Chest)
        exercises.add(new ExerciseCatalog("Press con Mancuernas", "kg", "Pecho"));
        exercises.add(new ExerciseCatalog("Press Banca", "kg", "Pecho"));
        exercises.add(new ExerciseCatalog("Aperturas", "kg", "Pecho"));
        exercises.add(new ExerciseCatalog("Press Inclinado", "kg", "Pecho"));

        // Espalda (Back)
        exercises.add(new ExerciseCatalog("Remo T", "kg", "Espalda"));
        exercises.add(new ExerciseCatalog("Jalon al Pecho", "kg", "Espalda"));
        exercises.add(new ExerciseCatalog("Remo en Maquina", "kg", "Espalda"));
        exercises.add(new ExerciseCatalog("Dominadas", "kg", "Espalda"));
        exercises.add(new ExerciseCatalog("Remo Mancuerna", "kg", "Espalda"));
        exercises.add(new ExerciseCatalog("Pullover en Polea", "kg", "Espalda"));

        // Hombro (Shoulders)
        exercises.add(new ExerciseCatalog("Press Militar", "kg", "Hombro"));
        exercises.add(new ExerciseCatalog("Elevaciones Laterales", "kg", "Hombro"));
        exercises.add(new ExerciseCatalog("Hombro Posterior", "kg", "Hombro"));
        exercises.add(new ExerciseCatalog("Press Arnold", "kg", "Hombro"));

        // Biceps
        exercises.add(new ExerciseCatalog("Curl Predicador", "kg", "Biceps"));
        exercises.add(new ExerciseCatalog("Curl Mancuerna", "kg", "Biceps"));
        exercises.add(new ExerciseCatalog("Curl Barra", "kg", "Biceps"));

        // Triceps
        exercises.add(new ExerciseCatalog("Extension de Triceps", "kg", "Triceps"));
        exercises.add(new ExerciseCatalog("Press Frances", "kg", "Triceps"));
        exercises.add(new ExerciseCatalog("Fondos", "kg", "Triceps"));

        // Pierna (Legs)
        exercises.add(new ExerciseCatalog("Sentadilla Libre", "kg", "Pierna"));
        exercises.add(new ExerciseCatalog("Sentadilla Hack", "kg", "Pierna"));
        exercises.add(new ExerciseCatalog("Prensa de Piernas", "placas", "Pierna"));
        exercises.add(new ExerciseCatalog("Curl Femoral", "kg", "Pierna"));
        exercises.add(new ExerciseCatalog("Extension de Cuadriceps", "kg", "Pierna"));
        exercises.add(new ExerciseCatalog("Pantorrilla", "kg", "Pierna"));
        exercises.add(new ExerciseCatalog("Aductores", "kg", "Pierna"));

        // Core
        exercises.add(new ExerciseCatalog("Abdominales Maquina", "kg", "Core"));
        exercises.add(new ExerciseCatalog("Planchas", "kg", "Core"));
        exercises.add(new ExerciseCatalog("Cable Woodchop", "kg", "Core"));

        return exercises;
    }
}
