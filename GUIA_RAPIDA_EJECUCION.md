# Guía Rápida de Ejecución - AppGym

**Objetivo:** Instrucciones paso-a-paso para ejecutar cada fase sin perder tiempo.

**Tiempo de lectura:** 5 minutos  
**Tiempo de ejecución por fase:** 3-6 días

---

## 🚀 ANTES DE EMPEZAR (30 minutos)

### 1. Leer documentación (obligatorio)
```bash
cd /home/josegarcia/AndroidStudioProjects/AppGym

# Roadmap general
cat PLAN_EJECUCION.md | less

# Análisis técnico (skim)
head -100 ANALISIS_VIABILIDAD_TECNICA_DETALLADO.md

# Verificación pre-inicio
cat CHECKLIST_PRE_EJECUCION_COMPLETO.md
```

### 2. Completar checklist
```bash
# ✅ Marcar TODOS los checkboxes en:
CHECKLIST_PRE_EJECUCION_COMPLETO.md
```

### 3. Crear rama principal
```bash
git checkout main
git pull origin main

# Crear rama epic
git checkout -b epic/rendimiento-crud-m3

# Crear tag baseline
git tag -a v1.0.0-baseline -m "Snapshot before changes"
git push origin --tags
```

### 4. Validar compilación
```bash
./gradlew clean build
# Esperar a que termine (puede tardar 5-10 min)
# Resultado esperado: BUILD SUCCESSFUL
```

### 5. Exportar datos de seguridad
```bash
# Manual en app:
# Settings → CSV → Exportar Historial
# Settings → CSV → Exportar Peso
# Guardar en Google Drive o nube
```

**✅ Listo para Fase 0**

---

## 📋 FASE 0 - Baseline Estable (1-2 días)

### Tareas

1. ✅ Compilación exitosa
2. ✅ Datos exportados
3. ✅ APK generada
4. ✅ QA manual ejecutada

### QA Manual (15 minutos)

```
PASO 1: Setup (2 min)
└─ Abrir app → seleccionar rutina Upper/Lower

PASO 2: Entrenar (8 min)
├─ Agregar 3 ejercicios
├─ Agregar 3 series a cada uno
├─ Cambiar algún peso/reps
└─ Toggle unidad kg → lbs

PASO 3: Guardar (2 min)
└─ Click "Finalizar" → guardar sesión

PASO 4: Verificar (3 min)
├─ Cerrar app
├─ Reabrir
├─ Historial → debe mostrar sesión
└─ NO debe aparecer dialog de respaldo
```

**Resultado esperado:** ✅ Pasa sin errores

### Finalizar Fase 0

```bash
git add .
git commit -m "Phase 0: baseline estable y rojo de seguridad"

git tag -a v1.0.0-phase-0-done -m "Phase 0 completed"
git push origin epic/rendimiento-crud-m3
git push origin --tags

# Guardar este tag en nube/docs para referencia
echo "v1.0.0-phase-0-done" >> RELEASE_NOTES.txt
```

---

## 🗄️ FASE 1 - Exercise Catalog (3-4 días)

### Archivos a Crear

#### 1. `app/src/main/java/com/josegarcia/appgym/data/entities/ExerciseCatalog.java`

```java
package com.josegarcia.appgym.data.entities;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "exercise_catalog", indices = {
        @Index("name"),
        @Index("muscleTag")
})
public class ExerciseCatalog {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    public String name;        // UNIQUE
    public String defaultUnit; // kg, lbs, placas
    public String muscleTag;   // Pecho, Espalda, Hombro, Biceps, Triceps, Pierna, Core
    public boolean isActive;   // soft delete
    public long createdAt;
    public long updatedAt;
    
    public ExerciseCatalog(String name, String defaultUnit, String muscleTag) {
        this.name = name;
        this.defaultUnit = defaultUnit;
        this.muscleTag = muscleTag;
        this.isActive = true;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }
}
```

#### 2. `app/src/main/java/com/josegarcia/appgym/data/dao/ExerciseCatalogDao.java`

```java
package com.josegarcia.appgym.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.josegarcia.appgym.data.entities.ExerciseCatalog;

import java.util.List;

@Dao
public interface ExerciseCatalogDao {
    @Insert
    long insert(ExerciseCatalog exercise);
    
    @Insert
    List<Long> insertAll(List<ExerciseCatalog> exercises);
    
    @Update
    void update(ExerciseCatalog exercise);
    
    @Query("SELECT * FROM exercise_catalog WHERE isActive = 1 ORDER BY muscleTag, name ASC")
    LiveData<List<ExerciseCatalog>> getAllActive();
    
    @Query("SELECT * FROM exercise_catalog WHERE isActive = 1 AND muscleTag = :muscleTag ORDER BY name ASC")
    List<ExerciseCatalog> getByMuscleTag(String muscleTag);
    
    @Query("SELECT * FROM exercise_catalog WHERE name = :name LIMIT 1")
    ExerciseCatalog getByName(String name);
    
    @Query("UPDATE exercise_catalog SET isActive = 0 WHERE id = :id")
    void deleteById(long id);
}
```

### Modificar AppDatabase.java

```java
// En línea 31, cambiar:
// @Database(entities = {...}, version = 9, ...)
// A:
@Database(entities = {WorkoutSession.class, ExerciseSet.class, BodyWeightLog.class, 
                     Routine.class, RoutineExercise.class, Split.class, 
                     ExerciseCatalog.class}, version = 10, exportSchema = false)

// En línea 38, agregar después de splitDao():
public abstract ExerciseCatalogDao exerciseCatalogDao();

// En línea 145, cambiar:
// .addMigrations(MIGRATION_3_4, ..., MIGRATION_8_9)
// A:
.addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, 
               MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)

// Agregar Migration 9→10 después de MIGRATION_8_9:
static final Migration MIGRATION_9_10 = new Migration(9, 10) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `exercise_catalog` " +
                "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT UNIQUE NOT NULL, " +
                "`defaultUnit` TEXT, " +
                "`muscleTag` TEXT NOT NULL, " +
                "`isActive` INTEGER NOT NULL DEFAULT 1, " +
                "`createdAt` INTEGER NOT NULL, " +
                "`updatedAt` INTEGER NOT NULL)");
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_exercise_catalog_name` ON `exercise_catalog` (`name`)");
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_exercise_catalog_muscleTag` ON `exercise_catalog` (`muscleTag`)");
    }
};

// En onOpen() callback (línea ~219), agregar seed logic:
// if (exercise_catalog empty) → populate 50+ exercises
```

### Agregar datos iniciales

En `InitialData.java`, agregar método:

```java
public static List<ExerciseCatalog> getExerciseCatalogSeeds() {
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
    
    // Hombro (Shoulders)
    exercises.add(new ExerciseCatalog("Press Militar", "kg", "Hombro"));
    exercises.add(new ExerciseCatalog("Elevaciones Laterales", "kg", "Hombro"));
    exercises.add(new ExerciseCatalog("Hombro Posterior", "kg", "Hombro"));
    
    // Biceps
    exercises.add(new ExerciseCatalog("Curl Predicador", "kg", "Biceps"));
    exercises.add(new ExerciseCatalog("Curl Mancuerna", "kg", "Biceps"));
    
    // Triceps
    exercises.add(new ExerciseCatalog("Extension de Triceps", "kg", "Triceps"));
    exercises.add(new ExerciseCatalog("Press Frances", "kg", "Triceps"));
    
    // Pierna (Legs)
    exercises.add(new ExerciseCatalog("Sentadilla Libre", "kg", "Pierna"));
    exercises.add(new ExerciseCatalog("Sentadilla Hack", "kg", "Pierna"));
    exercises.add(new ExerciseCatalog("Prensa de Piernas", "placas", "Pierna"));
    exercises.add(new ExerciseCatalog("Curl Femoral", "kg", "Pierna"));
    exercises.add(new ExerciseCatalog("Extension de Cuadriceps", "kg", "Pierna"));
    exercises.add(new ExerciseCatalog("Pantorrilla", "kg", "Pierna"));
    
    // Core
    exercises.add(new ExerciseCatalog("Abdominales Maquina", "kg", "Core"));
    exercises.add(new ExerciseCatalog("Planchas", "kg", "Core"));
    
    // Agregar más ejercicios hasta llegar a 50+
    // ...
    
    return exercises;
}
```

### Testing

```bash
# 1. Compilar
./gradlew clean build

# 2. Instalar en dev phone
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 3. Abrir app (debe migrar automático)
adb shell am start com.josegarcia.appgym/.MainActivity

# 4. Verificar migración vía adb
adb shell sqlite3 /data/data/com.josegarcia.appgym/databases/gym_database.db ".schema exercise_catalog"

# 5. Verificar datos
adb shell sqlite3 /data/data/com.josegarcia.appgym/databases/gym_database.db "SELECT COUNT(*) FROM exercise_catalog;"
# Resultado esperado: 50+

# 6. Manual QA
# - Abrir app sin crashes
# - Train → Ejercicio libre → buscar ejercicio → debe poblar desde catalog
# - Crear sesión → guardar → debe funcionar
```

### Finalizar Fase 1

```bash
git add .
git commit -m "Phase 1: Exercise Catalog model + migration 9->10"

git tag -a v1.0.1-phase-1-done -m "Phase 1 completed"
git push origin epic/rendimiento-crud-m3
git push origin --tags
```

---

## 🛠️ FASE 2 - CRUD en Ajustes (4-5 días)

Ver `MAPEO_ARCHIVOS_DETALLADO_POR_FASE.md` sección "FASE 2" para detalles.

**Resumen rápido:**
1. Crear `ExerciseManagementFragment.java` + ViewModel + Adapter
2. Modificar `SettingsFragment.java` agregar botón
3. Agregar a `nav_graph.xml`
4. Testing CRUD completo

```bash
git commit -m "Phase 2: CRUD Exercise Management in Settings"
git tag -a v1.0.2-phase-2-done
```

---

## 🐛 FASE 3 - Correcciones (3-4 días)

Ver `MAPEO_ARCHIVOS_DETALLADO_POR_FASE.md` sección "FASE 3".

**Fixes rápidos:**
```bash
# 1. TrackerActivity.java (línea ~380)
# Cambiar: clearCacheOnBackground() dentro if editId == -1
# A: clearCacheOnBackground() siempre (fuera del if)

# 2. CsvImporter.java (líneas 213-253)
# Agregar: validación peso (1-300 kg), múltiples formatos fecha

# 3. ExerciseSelectionActivity.java (línea 22)
# Cambiar: HashSet → LinkedHashSet

# 4. ExerciseSelectionActivity.java (agregar EditorAction)
# Agregar: etSearch.setOnEditorActionListener()

# 5. TrackerAdapter.java (línea ~70)
# Agregar: validación prevUnit vs currentUnit
```

```bash
git commit -m "Phase 3: Fix cache cleanup, CSV import, exercise order, unit sync"
git tag -a v1.0.3-phase-3-done
```

---

## 📊 FASE 4 - Rendimiento (5-6 días)

Ver `MAPEO_ARCHIVOS_DETALLADO_POR_FASE.md` sección "FASE 4".

**Arquitectura:**
```
PerformanceFragment
  ↓
PerformanceViewModel (getDetailedComparison)
  ↓
WorkoutDao.getLastWorkoutForExercise()
WorkoutDao.getSessionsForExercise()
  ↓
ExerciseSets comparación por setOrder
  ↓
PerformanceAdapter (muestra verde/gris/rojo)
```

```bash
git commit -m "Phase 4: Performance comparison module (green/gray/red by setOrder)"
git tag -a v1.0.4-phase-4-done
```

---

## 🎨 FASE 5 - Material Design 3 Core (3-4 días)

```bash
# Actualizar themes.xml, colors.xml, layouts principales
git commit -m "Phase 5: Material Design 3 theme (core modules)"
git tag -a v1.1.0-rc1-phase-5-done
```

---

## ✨ FASE 6 - Material Design 3 Full + Release (3-4 días)

```bash
# Extender M3 a resto UI, regresión final
git commit -m "Phase 6: Material Design 3 full + final stabilization"

# Release
git tag -a v1.1.0 -m "Production release: Rendimiento, CRUD, M3"
git checkout main
git merge epic/rendimiento-crud-m3
git push origin main
git push origin --tags
```

---

## 🔄 ROLLBACK (Si algo falla)

```bash
# Opción 1: Volver a commit anterior
git log --oneline
git revert <bad-commit>

# Opción 2: Volver a tag anterior
git checkout v1.0.0-baseline
git checkout -b recovery

# Opción 3: Reinstalar APK v1.0.0
adb install app-debug-v1.0.0-baseline.apk

# Opción 4: Restaurar DB desde CSV
# Manual: Ajustes → CSV → Importar Historial + Peso
```

---

## 📱 Testing en Phone 2 (QA)

**Antes de cada release:**

```bash
# 1. Build APK release
./gradlew build -PdebugOrRelease=release

# 2. Instalar en phone 2
adb -s <device2-id> install app/build/outputs/apk/release/app-release.apk

# 3. Test completo (ver CHECKLIST_PRE_EJECUCION)
# - Setup, train, historial, stats, peso, ajustes
# - Import/export CSV
# - Cambiar tema
# - 30 minutos de QA manual

# 4. Si pasa: ✅ Listo para release
# 5. Si falla: Revisar logs
#    adb logcat | grep AppGym
```

---

## 🏁 DESPUÉS DE CADA FASE

```bash
# 1. Commit + push
git add .
git commit -m "Phase X: [descripción]"
git push origin epic/rendimiento-crud-m3

# 2. Tag
git tag -a vX.Y.Z-phase-X-done -m "Phase X completed"
git push origin --tags

# 3. Documentar cambios
# Actualizar RELEASE_NOTES.txt o changelog

# 4. Backup
# Exportar CSV de datos en phone principal (si hay cambios)
```

---

## 📞 REFERENCIAS RÁPIDAS

| Pregunta | Respuesta | Documento |
|----------|-----------|-----------|
| ¿Dónde está el roadmap? | PLAN_EJECUCION.md | Root/ |
| ¿Qué archivos cambio en Fase 2? | MAPEO_ARCHIVOS_DETALLADO_POR_FASE.md § FASE 2 | Root/ |
| ¿Cómo rollback si falla? | Sección ROLLBACK arriba | Este doc |
| ¿Cuántos días por fase? | Ver tabla al inicio de cada fase | Este doc |
| ¿Cómo test en phone 2? | Sección "Testing en Phone 2" | Este doc |

---

## ✅ CHECKLIST POR FASE

### Antes de Fase

- [ ] Rama creada
- [ ] Documentación leída
- [ ] Archivos necesarios identificados
- [ ] Build exitoso

### Durante Fase

- [ ] Código escrito
- [ ] Tests manuales ejecutados
- [ ] Commits con mensajes claros
- [ ] Sin errores de compilación

### Después de Fase

- [ ] Push a rama epic
- [ ] Tag creado y pushido
- [ ] QA en phone 2 completado
- [ ] RELEASE_NOTES.txt actualizado

---

**Listo para comenzar. ¡Buen trabajo! 🚀**


