package com.josegarcia.appgym.data;

import android.content.Context;
import android.util.Log;

import com.josegarcia.appgym.data.dao.BodyWeightDao;
import com.josegarcia.appgym.data.dao.WorkoutDao;
import com.josegarcia.appgym.data.database.AppDatabase;
import com.josegarcia.appgym.data.entities.BodyWeightLog;
import com.josegarcia.appgym.data.entities.ExerciseSet;
import com.josegarcia.appgym.data.entities.WorkoutSession;
import com.josegarcia.appgym.utils.Constants;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CsvImporter {
    private static final String TAG = "CsvImporter";

    public static void importFromStream(InputStream inputStream, WorkoutDao dao) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            SimpleDateFormat dateFormat = new SimpleDateFormat("d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
            Map<String, Long> sessionCache = new HashMap<>();

            int setCounter = 0;
            int skippedLines = 0;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                if (!Character.isDigit(line.trim().charAt(0))) continue; // Header check

                String[] tokens = line.split(",");
                if (tokens.length < 10) continue; // Expect Unit column now

                // Fecha,Nombre,Unidad,Día de Rutina,S1-P,S1-R,S2-P,S2-R,S3-P,S3-R
                String dateStr = tokens[0].trim();
                String rawName = tokens[1].trim();
                String unit = tokens[2].trim();
                String routineName = tokens[3].trim();

                String exerciseName = normalizeExerciseName(rawName);
                String sessionKey = dateStr + "|" + routineName;
                Long sessionId = sessionCache.get(sessionKey);

                if (sessionId == null) {
                    long dateMillis;
                    try {
                        java.util.Date parsedDate = dateFormat.parse(dateStr);
                        if (parsedDate != null) dateMillis = parsedDate.getTime();
                        else continue;
                    } catch (Exception e) {
                        continue;
                    }

                    WorkoutSession existingSession = dao.getSessionByDateAndRoutine(dateMillis, routineName);
                    if (existingSession != null) {
                        sessionId = existingSession.id;
                        if (!dao.getSetsForSession(sessionId).isEmpty()) {
                            sessionCache.put(sessionKey, -1L); // Skip
                            skippedLines++;
                            continue;
                        }
                    } else {
                        WorkoutSession session = new WorkoutSession(dateMillis, routineName);
                        sessionId = dao.insertSession(session);
                    }
                    sessionCache.put(sessionKey, sessionId);
                } else if (sessionId == -1L) {
                    skippedLines++;
                    continue;
                }

                List<ExerciseSet> setsToInsert = new ArrayList<>();
                // Indices shifted by 1 due to Unit column
                parseAndAddSet(setsToInsert, sessionId, exerciseName, tokens[4], tokens[5], 1, unit);
                if (tokens.length >= 8) parseAndAddSet(setsToInsert, sessionId, exerciseName, tokens[6], tokens[7], 2, unit);
                if (tokens.length >= 10) parseAndAddSet(setsToInsert, sessionId, exerciseName, tokens[8], tokens[9], 3, unit);

                if (!setsToInsert.isEmpty()) {
                    dao.insertSets(setsToInsert);
                    setCounter += setsToInsert.size();
                }
            }
            Log.d(TAG, "Import finished. Added " + setCounter + " sets.");
        } catch (Exception e) {
            Log.e(TAG, "Error importing CSV stream", e);
        }
    }

    public static void exportDatabaseToCsv(Context context, OutputStream outputStream) {
        AppDatabase db = AppDatabase.getDatabase(context);
        AppDatabase.databaseWriteExecutor.execute(() -> {
             try {
                 WorkoutDao dao = db.workoutDao();
                 List<WorkoutSession> sessions = dao.getAllSessions(); // Need to fetch sets for each

                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
                 writer.write("Fecha,Nombre Ejercicio,Unidad,Día Rutina,Set 1 Peso,Set 1 Reps,Set 2 Peso,Set 2 Reps,Set 3 Peso,Set 3 Reps\n");

                 SimpleDateFormat dateFormat = new SimpleDateFormat("d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));

                 for (WorkoutSession session : sessions) {
                     List<ExerciseSet> sets = dao.getSetsForSession(session.id);
                     if (sets.isEmpty()) continue;

                     // Group by exercise name maintain order
                     // Since sets are ordered by ID/Order, we iterate and group
                     Map<String, List<ExerciseSet>> exerciseGroup = new java.util.LinkedHashMap<>();
                     for (ExerciseSet set : sets) {
                         if (!exerciseGroup.containsKey(set.exerciseName)) {
                             exerciseGroup.put(set.exerciseName, new ArrayList<>());
                         }
                         exerciseGroup.get(set.exerciseName).add(set);
                     }

                     String dateStr = dateFormat.format(new java.util.Date(session.date));

                     for (Map.Entry<String, List<ExerciseSet>> entry : exerciseGroup.entrySet()) {
                         String exerciseName = entry.getKey();
                         List<ExerciseSet> exSets = entry.getValue();

                         StringBuilder line = new StringBuilder();
                         line.append(dateStr).append(",");
                         line.append(exerciseName).append(",");

                         // Determine unit (check first set)
                         String unit = Constants.UNIT_KG;
                         if (!exSets.isEmpty()) {
                             String firstUnit = exSets.get(0).unit;
                             if (firstUnit != null && !firstUnit.isEmpty()) {
                                 unit = firstUnit;
                             }
                         }
                         // Normalize unit display if needed or keep raw? csv usually raw but readable
                         if (unit.equalsIgnoreCase(Constants.UNIT_PLACAS)) unit = "PL";
                         else if (unit.equalsIgnoreCase(Constants.UNIT_LBS)) unit = "LBS";
                         else unit = "KG";

                         line.append(unit).append(",");
                         line.append(session.routineName).append(",");

                         // Limit to 3 sets as per CSV format or extend?
                         // The format seems to support dynamic but header implies 3.
                         // Let's write up to 3 sets to match format.

                         for (int i = 0; i < 3; i++) {
                             if (i < exSets.size()) {
                                 ExerciseSet set = exSets.get(i);
                                 line.append(set.weight).append(",");
                                 line.append(set.reps);
                             } else {
                                 line.append(","); // Empty weight
                                 line.append("");  // Empty reps
                             }
                             if (i < 2) line.append(",");
                         }
                         line.append("\n");
                         writer.write(line.toString());
                     }
                 }
                 writer.flush();
                 writer.close();
                 Log.d(TAG, "Export finished successfully.");

             } catch (Exception e) {
                 Log.e(TAG, "Error exporting CSV", e);
             }
        });
    }

    public static void exportBodyWeightToCsv(Context context, OutputStream outputStream) {
        AppDatabase db = AppDatabase.getDatabase(context);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                BodyWeightDao dao = db.bodyWeightDao();
                List<BodyWeightLog> logs = dao.getAllLogsSync();

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
                writer.write("Fecha (Timestamp),Fecha (Legible),Peso (kg)\n");

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

                for (BodyWeightLog log : logs) {
                    StringBuilder line = new StringBuilder();
                    line.append(log.timestamp).append(",");
                    line.append(dateFormat.format(new java.util.Date(log.timestamp))).append(",");
                    line.append(log.weight);
                    line.append("\n");
                    writer.write(line.toString());
                }
                writer.flush();
                writer.close();
                Log.d(TAG, "Body Weight Export finished successfully.");

            } catch (Exception e) {
                 Log.e(TAG, "Error exporting Body Weight CSV", e);
            }
        });
    }

    public static void importBodyWeightFromStream(InputStream inputStream, BodyWeightDao dao) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            // Simple validation map to avoid full duplicates if needed, but timestamp collision is rare unless exact copy.
            // Let's just iterate and insert.
            // Better: Check if timestamp exists? BodyWeightLog ID is auto-gen.
            // If we import the same file twice, we will duplicate data unless we check.
            // Let's check strict dupe on timestamp? Or Timestamp + Weight?
            // For now, let's assume raw import. User responsibility.
            // Or better, check if timestamp exists within a small margin?
            // Let's just parse.

            int added = 0;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                if (!Character.isDigit(line.trim().charAt(0))) continue; // Header check

                String[] tokens = line.split(",");
                if (tokens.length < 3) continue;

                // Format: Timestamp, ReadableDate, Weight
                try {
                    long timestamp = Long.parseLong(tokens[0].trim());
                    float weight = Float.parseFloat(tokens[2].trim());

                    // Basic duplicate check by timestamp?
                    // We can't easily query by timestamp efficiently without an index or specific query.
                    // Let's add the query to DAO later if needed. For now plain insert.
                    BodyWeightLog log = new BodyWeightLog(timestamp, weight, null);
                    dao.insert(log);
                    added++;
                } catch (Exception e) {
                    Log.e(TAG, "Skipping invalid line: " + line, e);
                }
            }
            Log.d(TAG, "Body Weight Import finished. Added " + added + " entries.");
        } catch (Exception e) {
            Log.e(TAG, "Error importing Body Weight CSV stream", e);
        }
    }


    private static String normalizeExerciseName(String raw) {
        // Since CSV is now standardized, we just trim the input.
        return raw.trim();
    }

    private static void parseAndAddSet(List<ExerciseSet> list, long sessionId, String name, String weightStr, String repsStr, int order, String explicitUnit) {
        try {
            double weight = Double.parseDouble(weightStr.trim());
            int reps = Integer.parseInt(repsStr.trim());
            if (weight > 0 && reps > 0) {
                ExerciseSet set = new ExerciseSet(sessionId, name, weight, reps, order);
                if (explicitUnit != null && !explicitUnit.isEmpty()) {
                    set.unit = explicitUnit;
                } else {
                    set.unit = resolveUnit(name);
                }
                list.add(set);
            }
        } catch (NumberFormatException e) {
            // Ignore invalid numbes
        }
    }

    private static String resolveUnit(String exerciseName) {
        String lower = exerciseName.toLowerCase(java.util.Locale.ROOT);
        if (lower.contains("posterior") ||
            lower.contains("bayesian") ||
            (lower.contains("extension") && (lower.contains("cuadriceps") || lower.contains("triceps"))) ||
            lower.contains("laterales") ||
            lower.contains("aperturas") ||
            lower.contains("cruces")) {
            return Constants.UNIT_PLACAS;
        }
        return Constants.UNIT_KG;
    }
}
