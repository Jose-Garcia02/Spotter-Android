package com.josegarcia.appgym.data.database;

import com.josegarcia.appgym.R;
import com.josegarcia.appgym.data.entities.Routine;
import com.josegarcia.appgym.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class InitialData {

    // Helper to generic Routines given a Split ID
    public static List<Routine> getUpperLowerRoutines(int splitId) {
        List<Routine> routines = new ArrayList<>();
        routines.add(new Routine(splitId, Constants.ROUTINE_UPPER_A, R.color.primary_accent, true, 1));
        routines.add(new Routine(splitId, Constants.ROUTINE_UPPER_B, R.color.secondary_accent, true, 2));
        routines.add(new Routine(splitId, Constants.ROUTINE_LOWER_A, R.color.primary_accent, true, 3));
        routines.add(new Routine(splitId, Constants.ROUTINE_LOWER_B, R.color.secondary_accent, true, 4));
        // Removed ROUTINE_FREE based on requirement
        return routines;
    }

    public static List<Routine> getPPLRoutines(int splitId) {
        List<Routine> routines = new ArrayList<>();
        routines.add(new Routine(splitId, Constants.ROUTINE_PUSH_A, R.color.vivid_red, true, 1));
        routines.add(new Routine(splitId, Constants.ROUTINE_PULL_A, R.color.vivid_blue, true, 2));
        routines.add(new Routine(splitId, Constants.ROUTINE_LEGS_A, R.color.vivid_green, true, 3));
        routines.add(new Routine(splitId, Constants.ROUTINE_PUSH_B, R.color.vivid_red, true, 4));
        routines.add(new Routine(splitId, Constants.ROUTINE_PULL_B, R.color.vivid_blue, true, 5));
        routines.add(new Routine(splitId, Constants.ROUTINE_LEGS_B, R.color.vivid_green, true, 6));
        return routines;
    }

    public static List<Routine> getArnoldRoutines(int splitId) {
        List<Routine> routines = new ArrayList<>();
        routines.add(new Routine(splitId, Constants.ROUTINE_ARNOLD_CHEST_BACK_A, R.color.vivid_gold, true, 1));
        routines.add(new Routine(splitId, Constants.ROUTINE_ARNOLD_SHOULDERS_ARMS_A, R.color.vivid_purple, true, 2));
        routines.add(new Routine(splitId, Constants.ROUTINE_ARNOLD_LEGS_A, R.color.vivid_green, true, 3));
        routines.add(new Routine(splitId, Constants.ROUTINE_ARNOLD_CHEST_BACK_B, R.color.vivid_gold, true, 4));
        routines.add(new Routine(splitId, Constants.ROUTINE_ARNOLD_SHOULDERS_ARMS_B, R.color.vivid_purple, true, 5));
        routines.add(new Routine(splitId, Constants.ROUTINE_ARNOLD_LEGS_B, R.color.vivid_green, true, 6));
        return routines;
    }

    public static List<Routine> getFullBodyRoutines(int splitId) {
        List<Routine> routines = new ArrayList<>();
        routines.add(new Routine(splitId, Constants.ROUTINE_FULLBODY_A, R.color.vivid_blue, true, 1));
        routines.add(new Routine(splitId, Constants.ROUTINE_FULLBODY_B, R.color.vivid_red, true, 2));
        routines.add(new Routine(splitId, Constants.ROUTINE_FULLBODY_C, R.color.vivid_green, true, 3));
        return routines;
    }

    public static String[] getExercisesForRoutine(String routineName) {
        switch (routineName) {
            case Constants.ROUTINE_UPPER_A:
                return new String[]{
                    Constants.EX_PRESS_MANCUERNAS, Constants.EX_REMO_T, Constants.EX_ELEVACIONES_LATERALES,
                    Constants.EX_JALON_PECHO, Constants.EX_APERTURAS, Constants.EX_CURL_PREDICADOR, Constants.EX_PRESS_FRANCES
                };
            case Constants.ROUTINE_UPPER_B:
                return new String[]{
                    Constants.EX_PRESS_MANCUERNAS, Constants.EX_JALON_PECHO, Constants.EX_PRESS_MILITAR,
                    Constants.EX_REMO_MAQUINA, Constants.EX_HOMBRO_POSTERIOR, Constants.EX_EXTENSION_TRICEPS, Constants.EX_CURL_BAYESIAN
                };
            case Constants.ROUTINE_LOWER_A:
                return new String[]{
                    Constants.EX_ADUCTORES, Constants.EX_SENTADILLA_HACK, Constants.EX_CURL_FEMORAL,
                    Constants.EX_EXTENSION_CUADRICEPS, Constants.EX_PANTORRILLA
                };
            case Constants.ROUTINE_LOWER_B:
                return new String[]{
                    Constants.EX_ADUCTORES, Constants.EX_SENTADILLA_LIBRE, Constants.EX_CURL_FEMORAL,
                    Constants.EX_PUENTE_GLUTEO, Constants.EX_PANTORRILLA
                };
            case Constants.ROUTINE_FULLBODY_A:
                return new String[]{
                    Constants.EX_PRESS_MANCUERNAS, Constants.EX_REMO_T, Constants.EX_SENTADILLA_LIBRE,
                    Constants.EX_CURL_FEMORAL, Constants.EX_PRESS_MILITAR
                };
            case Constants.ROUTINE_FULLBODY_B:
                return new String[]{
                    Constants.EX_JALON_PECHO, Constants.EX_SENTADILLA_HACK, Constants.EX_PRESS_MANCUERNAS,
                    Constants.EX_PUENTE_GLUTEO, Constants.EX_ELEVACIONES_LATERALES
                };
            case Constants.ROUTINE_FULLBODY_C:
                return new String[]{
                    Constants.EX_SENTADILLA_LIBRE, Constants.EX_REMO_MAQUINA, Constants.EX_APERTURAS,
                    Constants.EX_EXTENSION_CUADRICEPS, Constants.EX_EXTENSION_TRICEPS
                };
            case Constants.ROUTINE_PUSH_A:
            case Constants.ROUTINE_PUSH_B:
                return new String[]{
                    Constants.EX_PRESS_MANCUERNAS, Constants.EX_PRESS_MILITAR, Constants.EX_ELEVACIONES_LATERALES,
                    Constants.EX_APERTURAS, Constants.EX_EXTENSION_TRICEPS, Constants.EX_PRESS_FRANCES
                };
            case Constants.ROUTINE_PULL_A:
            case Constants.ROUTINE_PULL_B:
                return new String[]{
                    Constants.EX_JALON_PECHO, Constants.EX_REMO_T, Constants.EX_REMO_MAQUINA,
                    Constants.EX_HOMBRO_POSTERIOR, Constants.EX_CURL_PREDICADOR, Constants.EX_CURL_BAYESIAN
                };
            case Constants.ROUTINE_LEGS_A:
            case Constants.ROUTINE_LEGS_B:
            case Constants.ROUTINE_ARNOLD_LEGS_A:
            case Constants.ROUTINE_ARNOLD_LEGS_B:
                return new String[]{
                   Constants.EX_SENTADILLA_LIBRE, Constants.EX_PRENSA_PIERNA, Constants.EX_CURL_FEMORAL,
                   Constants.EX_EXTENSION_CUADRICEPS, Constants.EX_PANTORRILLA
                };
            case Constants.ROUTINE_ARNOLD_CHEST_BACK_A:
            case Constants.ROUTINE_ARNOLD_CHEST_BACK_B:
                return new String[]{
                    Constants.EX_PRESS_MANCUERNAS, Constants.EX_REMO_T, Constants.EX_APERTURAS,
                    Constants.EX_JALON_PECHO, Constants.EX_PULLOVER_POLEA
                };
            case Constants.ROUTINE_ARNOLD_SHOULDERS_ARMS_A:
            case Constants.ROUTINE_ARNOLD_SHOULDERS_ARMS_B:
                return new String[]{
                    Constants.EX_PRESS_MILITAR, Constants.EX_ELEVACIONES_LATERALES, Constants.EX_HOMBRO_POSTERIOR,
                    Constants.EX_CURL_PREDICADOR, Constants.EX_EXTENSION_TRICEPS, Constants.EX_CURL_BAYESIAN, Constants.EX_PRESS_FRANCES
                };
            default: return new String[]{};
        }
    }
}
