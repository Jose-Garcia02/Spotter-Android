package com.josegarcia.appgym.utils.logic;

import com.josegarcia.appgym.utils.Constants;

public class ExerciseUtils {

    /**
     * Determina si un ejercicio debe usar "Placas" (pl) como unidad por defecto
     * basándose en el nombre del ejercicio.
     * Esta lógica centralizada facilita la gestión de metadatos de ejercicios.
     *
     * @param exerciseName El nombre del ejercicio.
     * @return true si el ejercicio suele ser en máquina multipolea o de placas.
     */
    public static boolean shouldUsePlacas(String exerciseName) {
        if (exerciseName == null) return false;
        String lower = exerciseName.toLowerCase();

        return lower.contains("posterior") ||
               lower.contains("bayesian") ||
               (lower.contains("extension") && (lower.contains("cuadriceps") || lower.contains("triceps"))) ||
               lower.contains("laterales") ||
               lower.contains("aperturas") ||
               lower.contains("cruces") ||
               lower.contains("jalon") ||
               lower.contains("remo en maquina");
    }

    /**
     * Obtiene el número de series sugerido por defecto para un ejercicio dado.
     *
     * @param exerciseName El nombre del ejercicio.
     * @return Número de series (e.g., 2 para ejercicios muy taxantes, 3 estándar).
     */
    public static int getDefaultSetCount(String exerciseName) {
        if (Constants.EX_SENTADILLA_HACK.equals(exerciseName) ||
            Constants.EX_SENTADILLA_LIBRE.equals(exerciseName) ||
            Constants.EX_PRENSA_PIERNA.equals(exerciseName)) {
            return 2;
        }
        return 3;
    }
}
