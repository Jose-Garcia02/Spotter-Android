# Mapeo Detallado de Archivos por Fase - AppGym

**Propósito:** Guía de referencia rápida para saber exactamente qué archivos crear, modificar o leer en cada fase.

---

## FASE 0 - Baseline Estable y Red de Seguridad (1-2 días)

### Archivos a CREAR

| Archivo | Lineas | Propósito | Prioridad |
|---------|--------|----------|-----------|
| `PLAN_EJECUCION.md` | 356 | Plan maestro | ✅ Ya existe |
| `ANALISIS_VIABILIDAD_TECNICA_DETALLADO.md` | 400 | Análisis técnico | ✅ Ya existe |
| `validate_status.sh` | 311 | Script validación | ✅ Ya existe |

### Archivos a MODIFICAR

| Archivo | Cambios | Líneas aprox | Nota |
|---------|---------|--------------|------|
| `README.md` | Agregar sección "Versión Estable" con APK baseline | 10 | Documentación |
| `app/build.gradle.kts` | Versión de app → 1.0.0 (baseline) | 5 | Versionado |

### Archivos a REVISAR (sin modificar)

- `app/src/main/AndroidManifest.xml` - Validar permisos
- `gradle.properties` - Verificar config
- `app/build.gradle.kts` - Deps intactas

### QA Manual

```
1. Ejecutar: ./gradlew clean build
2. Generar APK debug en emulador/phone
3. Exportar histórico: Ajustes → CSV → Exportar Historial
4. Exportar peso: Ajustes → CSV → Exportar Peso
5. Crear sesión completa (5 ejercicios, 3 series cada uno)
6. Guardar sesión
7. Verificar en Historial
8. Registrar estado base en documento
```

---

## FASE 1 - Modelo de Datos Exercise Catalog (3-4 días)

### Archivos a CREAR

| Archivo | Líneas | Estructura |
|---------|--------|-----------|
| `app/src/main/java/com/josegarcia/appgym/data/entities/ExerciseCatalog.java` | 50-60 | Entidad Room con @Entity |
| `app/src/main/java/com/josegarcia/appgym/data/dao/ExerciseCatalogDao.java` | 40-50 | DAO con CRUD + búsqueda |

### Archivos a MODIFICAR

| Archivo | Localización | Cambios | Líneas |
|---------|--------------|---------|--------|
| `app/src/main/java/com/josegarcia/appgym/data/database/AppDatabase.java` | Línea 31 | Agregar `ExerciseCatalog.class` a @Database entities | 1 |
| `app/src/main/java/com/josegarcia/appgym/data/database/AppDatabase.java` | Línea 38 | Agregar método abstracto `abstract ExerciseCatalogDao exerciseCatalogDao();` | 1 |
| `app/src/main/java/com/josegarcia/appgym/data/database/AppDatabase.java` | Línea 31 | Cambiar `version = 9` → `version = 10` | 1 |
| `app/src/main/java/com/josegarcia/appgym/data/database/AppDatabase.java` | Línea 145 | Agregar `MIGRATION_9_10` a addMigrations(...) | 1 |
| `app/src/main/java/com/josegarcia/appgym/data/database/AppDatabase.java` | Después línea 134 | Crear Migration 9→10 (CREATE TABLE exercise_catalog) | 20-30 |
| `app/src/main/java/com/josegarcia/appgym/data/database/InitialData.java` | Final de clase | Agregar método `getExerciseCatalogSeeds()` con 50+ ejercicios | 100-150 |
| `app/src/main/java/com/josegarcia/appgym/data/database/AppDatabase.java` | Línea 219 (onOpen callback) | Agregar lógica de seed de exercise_catalog si vacía | 30-40 |

### Importes a AGREGAR

```java
// En AppDatabase.java
import com.josegarcia.appgym.data.dao.ExerciseCatalogDao;
import com.josegarcia.appgym.data.entities.ExerciseCatalog;
```

### ExerciseCatalog.java - Contenido Mínimo

```java
@Entity(tableName = "exercise_catalog", indices = {@Index("name"), @Index("muscleTag")})
public class ExerciseCatalog {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    @ColumnInfo(index = true)
    public String name;        // UNIQUE, normalized
    
    public String defaultUnit; // kg, lbs, placas
    public String muscleTag;   // Pecho, Espalda, Hombro, Biceps, Triceps, Pierna, Core
    public boolean isActive;   // soft delete flag
    public long createdAt;
    public long updatedAt;
}
```

### ExerciseCatalogDao.java - Contenido Mínimo

```java
@Dao
public interface ExerciseCatalogDao {
    @Insert
    long insert(ExerciseCatalog exercise);
    
    @Insert
    List<Long> insertAll(List<ExerciseCatalog> exercises);
    
    @Update
    void update(ExerciseCatalog exercise);
    
    @Query("DELETE FROM exercise_catalog WHERE id = :id")
    void deleteById(long id);
    
    @Query("SELECT * FROM exercise_catalog WHERE isActive = 1 ORDER BY muscleTag, name ASC")
    LiveData<List<ExerciseCatalog>> getAllActive();
    
    @Query("SELECT * FROM exercise_catalog WHERE isActive = 1 AND muscleTag = :muscleTag ORDER BY name ASC")
    List<ExerciseCatalog> getByMuscleTag(String muscleTag);
    
    @Query("SELECT * FROM exercise_catalog WHERE name = :name LIMIT 1")
    ExerciseCatalog getByName(String name);
}
```

### MIGRATION_9_10 - Pseudo-código

```sql
CREATE TABLE IF NOT EXISTS `exercise_catalog` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    `name` TEXT UNIQUE NOT NULL,
    `defaultUnit` TEXT,
    `muscleTag` TEXT NOT NULL,
    `isActive` INTEGER NOT NULL DEFAULT 1,
    `createdAt` INTEGER NOT NULL,
    `updatedAt` INTEGER NOT NULL
);

CREATE INDEX IF NOT EXISTS `index_exercise_catalog_name` ON `exercise_catalog` (`name`);
CREATE INDEX IF NOT EXISTS `index_exercise_catalog_muscleTag` ON `exercise_catalog` (`muscleTag`);
```

### QA Manual - Fase 1

```
1. Compilar: ./gradlew clean build (debe exitoso)
2. Instalar APK en dev phone
3. Abrir app → debe migrar BD automático sin errores
4. Verificar via ADB: sqlite3 /data/.../gym_database.db ".schema exercise_catalog"
5. Verificar datos iniciales: SELECT COUNT(*) FROM exercise_catalog (debe ~50)
6. Navegar a entrenamiento libre → lista de ejercicios debe poblar desde catalog
7. Crear sesión con ejercicio catálogo → guardar → verificar histórico
8. Test rollback: desinstalar, instalar APK anterior, verificar BD no corrupción
```

---

## FASE 2 - CRUD de Ejercicios en Ajustes (4-5 días)

### Archivos a CREAR

| Archivo | Líneas | Contenido |
|---------|--------|----------|
| `app/src/main/java/com/josegarcia/appgym/ui/exercise_management/ExerciseManagementFragment.java` | 150-180 | Fragment para CRUD |
| `app/src/main/java/com/josegarcia/appgym/ui/exercise_management/ExerciseManagementViewModel.java` | 100-130 | ViewModel con Lifecycle |
| `app/src/main/java/com/josegarcia/appgym/ui/exercise_management/ExerciseManagementAdapter.java` | 130-160 | RecyclerView adapter |
| `app/src/main/res/layout/fragment_exercise_management.xml` | 80-100 | Layout principal |
| `app/src/main/res/layout/dialog_exercise_edit.xml` | 100-120 | Dialog para crear/editar |
| `app/src/main/res/layout/item_exercise_row.xml` | 60-80 | Item del RecyclerView |

### Archivos a MODIFICAR

| Archivo | Localización | Cambios | Líneas |
|---------|--------------|---------|--------|
| `app/src/main/java/com/josegarcia/appgym/ui/SettingsFragment.java` | Línea ~100 | Agregar botón "Gestionar Ejercicios" con navegación | 5-8 |
| `app/src/main/res/layout/fragment_settings.xml` | Final de layout | Agregar botón Material con icon ic_tune o similar | 10-15 |
| `app/src/main/res/navigation/nav_graph.xml` | Final de archivo | Agregar fragment destino exercise_management_graph | 15-20 |
| `app/src/main/java/com/josegarcia/appgym/ui/SettingsViewModel.java` | Si existe | Agregar métodos: updateExerciseName(), updateMuscleTag(), deleteExercise() | 20-30 |

### Flujo de Datos

```
SettingsFragment 
    → click "Gestionar Ejercicios"
    → navegar a ExerciseManagementFragment
    → ExerciseManagementViewModel.getAllExercises() → ExerciseCatalogDao
    → mostrar RecyclerView con ExerciseManagementAdapter
    → click item → Dialog para editar
    → guardar cambios via ViewModel → ExerciseCatalogDao.update()
```

### QA Manual - Fase 2

```
1. Abrir Settings → "Gestionar Ejercicios"
2. Ver lista de ~50 ejercicios agrupados por muscleTag
3. Click en un ejercicio → Dialog para editar
4. Cambiar nombre → guardar
5. Volver a lista → verificar cambio reflejado
6. Crear nuevo ejercicio → nombre + muscleTag + unidad
7. Eliminar ejercicio (soft delete) → ver que desaparece de lista
8. Entrenamiento libre → buscar ejercicio editado → debe usar nombre nuevo
9. Historial → sesión anterior → debe reflejar cambios si se aplican retroactivo
```

---

## FASE 3 - Correcciones Base y UX (3-4 días)

### Correcciones a Realizar

#### 3.1 - TrackerActivity.java - Fix cache cleanup (Línea ~380)

**Problema:** El método `saveWorkout()` no limpia el cache cuando se edita una sesión existente.

**Localización:** `app/src/main/java/com/josegarcia/appgym/ui/tracker/TrackerActivity.java`, línea ~363-420

**Cambios:**
```java
// ANTES (problema)
private void saveWorkout(String routineName) {
    AppDatabase.databaseWriteExecutor.execute(() -> {
        long editId = viewModel.getEditingSessionId();
        // ... insert/update ...
        if (editId == -1) {  // Solo en ruta NEW
            clearCacheOnBackground();
        }
    });
}

// DESPUÉS (corregido)
private void saveWorkout(String routineName) {
    AppDatabase.databaseWriteExecutor.execute(() -> {
        long editId = viewModel.getEditingSessionId();
        // ... insert/update ...
        // Limpiar cache siempre (tanto para NEW como para EDIT)
        clearCacheOnBackground();
    });
}
```

**Líneas afectadas:** 1 (mover clearCacheOnBackground() fuera del if)

---

#### 3.2 - CsvImporter.java - Mejorar importBodyWeight (Líneas 213-253)

**Problema:** Parse simple sin validación, no soporta múltiples formatos, sin detección de duplicados.

**Localización:** `app/src/main/java/com/josegarcia/appgym/data/CsvImporter.java`, línea 213-253

**Mejoras:**
1. Agregar validación de rango de peso (1-300 kg)
2. Soportar múltiples formatos de fecha (DD/MM/YYYY, MM/DD/YYYY, YYYY-MM-DD)
3. Detección de duplicados por timestamp ±1 día
4. Logging de errores y líneas saltadas

**Pseudo-código:**
```java
public static void importBodyWeightFromStream(InputStream inputStream, BodyWeightDao dao) {
    // ...
    for (String line : lines) {
        String[] tokens = line.split(",");
        try {
            // Intentar parseado
            long timestamp = parseLongOrDate(tokens[0]);  // Soportar múltiples formatos
            float weight = Float.parseFloat(tokens[2].trim());
            
            // Validar rango
            if (weight < 1f || weight > 300f) {
                Log.w("ImportBodyWeight", "Peso fuera de rango: " + weight);
                continue;  // Saltar línea
            }
            
            // Detección de duplicados
            BodyWeightLog existing = dao.getByTimestampRange(timestamp - 86400000, timestamp + 86400000);
            if (existing != null) {
                Log.i("ImportBodyWeight", "Duplicado detectado, saltando");
                continue;
            }
            
            // Insertar
            dao.insert(new BodyWeightLog(timestamp, weight));
        } catch (Exception e) {
            Log.e("ImportBodyWeight", "Error en línea: " + line, e);
        }
    }
}
```

**Métodos DAO nuevos:**
- `BodyWeightDao.getByTimestampRange(long start, long end)` → BodyWeightLog

---

#### 3.3 - ExerciseSelectionActivity.java - Preservar orden + EditorAction

**Localización:** `app/src/main/java/com/josegarcia/appgym/ui/tracker/ExerciseSelectionActivity.java`, línea 22-50

**Cambios:**
1. Línea 22: `HashSet` → `LinkedHashSet` (preserva orden de inserción)
2. Agregar EditorAction listener a EditText de búsqueda

**Pseudo-código:**
```java
// ANTES (línea 22)
private final Set<String> selectedExercises = new HashSet<>();

// DESPUÉS
private final Set<String> selectedExercises = new LinkedHashSet<>();

// Agregar después de findViewById(etSearch) (aprox línea 40)
etSearch.setOnEditorActionListener((v, actionId, event) -> {
    if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        return true;
    }
    return false;
});
```

**Líneas afectadas:** 5-10

---

#### 3.4 - TrackerAdapter.java - Sincronizar unidades (Línea ~70)

**Problema:** Al toggle unidades, prevWeight no se convierte ni se advierte al usuario.

**Localización:** `app/src/main/java/com/josegarcia/appgym/ui/tracker/TrackerAdapter.java`, línea ~68-96

**Cambios:**
1. Almacenar prevUnit junto a prevWeight
2. Si prevUnit ≠ currentUnit, mostrar disclaimer o convertir

**Pseudo-código:**
```java
// En bind()
// ANTES
tvUnitToggle.setOnClickListener(v -> {
    switch (exercise.unit) {
        case Constants.UNIT_KG: exercise.unit = Constants.UNIT_LBS; break;
        case Constants.UNIT_LBS: exercise.unit = Constants.UNIT_PLACAS; break;
        case Constants.UNIT_PLACAS: exercise.unit = Constants.UNIT_KG; break;
    }
    // Se asume prevWeight misma unidad - PROBLEMA
});

// DESPUÉS
tvUnitToggle.setOnClickListener(v -> {
    String oldUnit = exercise.unit;
    switch (exercise.unit) {
        case Constants.UNIT_KG: exercise.unit = Constants.UNIT_LBS; break;
        case Constants.UNIT_LBS: exercise.unit = Constants.UNIT_PLACAS; break;
        case Constants.UNIT_PLACAS: exercise.unit = Constants.UNIT_KG; break;
    }
    
    // Si prevWeight está en diferente unidad, mostrar warning
    if (prevWeight != null && !prevUnit.equals(oldUnit)) {
        Toast.makeText(context, "Nota: peso anterior en " + prevUnit, Toast.LENGTH_SHORT).show();
    }
    
    notifyItemChanged(position);
});
```

---

#### 3.5 - TrackerAdapter + ExerciseSelectionAdapter - Agregar drag-to-reorder (Opcional, Fase 3B)

**Localización:** `app/src/main/java/com/josegarcia/appgym/ui/tracker/TrackerActivity.java`, onCreate()

**Pseudo-código:**
```java
ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(
    ItemTouchHelper.UP | ItemTouchHelper.DOWN,  // Movement flags
    0                                             // Swipe flags
) {
    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        // Cambiar orden en adapter + notificar
        adapter.moveItem(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }
    
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        // No usar para swiping
    }
};

new ItemTouchHelper(callback).attachToRecyclerView(rvExercises);
```

**Líneas afectadas:** 15-30

---

### QA Manual - Fase 3

```
1. TrackerActivity fix cache:
   - Crear sesión nueva → guardar → abrir app → NO debe aparecer dialog de respaldo
   - Editar sesión existente → cambiar datos → guardar → abrir app → NO debe respaldo

2. CsvImporter fix:
   - Exportar peso → abrir CSV → agregar línea con formato diferente (DD/MM/YYYY)
   - Agregar peso fuera de rango (0 kg, 500 kg) → importar → debe saltarse
   - Importar misma línea 2 veces → debe detectar duplicado
   
3. ExerciseSelectionActivity:
   - Buscar ejercicio → presionar Enter → teclado se oculta ✓
   - Seleccionar: Pecho → Espalda → Pierna → orden debe ser Pecho, Espalda, Pierna
   - Volver a seleccionar → mismo orden preservado

4. TrackerAdapter unidades:
   - Entrenar: kg → reps 10 @ 100kg
   - Toggle a lbs → debe mostrar conversión o aviso
   - Toggle a placas → debe mostrar aviso
   
5. Drag-to-reorder (opcional):
   - Lista de ejercicios → arrastrar Pecho a posición 3 → guardar → volver → orden preservado
```

---

## FASE 4 - Módulo Rendimiento (5-6 días)

### Archivos a CREAR

| Archivo | Líneas | Contenido |
|---------|--------|----------|
| `app/src/main/java/com/josegarcia/appgym/ui/performance/PerformanceFragment.java` | 180-220 | Fragment principal |
| `app/src/main/java/com/josegarcia/appgym/ui/performance/PerformanceViewModel.java` | 120-150 | ViewModel con lógica |
| `app/src/main/java/com/josegarcia/appgym/ui/performance/PerformanceAdapter.java` | 150-180 | Adapter para resultados |
| `app/src/main/java/com/josegarcia/appgym/data/entities/PerformanceComparison.java` | 40-50 | POJO para resultado |
| `app/src/main/res/layout/fragment_performance.xml` | 80-100 | Layout contenedor |
| `app/src/main/res/layout/item_performance_exercise.xml` | 60-80 | Item ejercicio |
| `app/src/main/res/layout/item_performance_set_comparison.xml` | 50-70 | Item serie comparada |

### Archivos a MODIFICAR

| Archivo | Localización | Cambios | Líneas |
|---------|--------------|---------|--------|
| `app/src/main/java/com/josegarcia/appgym/data/dao/WorkoutDao.java` | Final | Agregar query: `getLastWorkoutForRoutine(routineName)` | 15-20 |
| `app/src/main/java/com/josegarcia/appgym/data/dao/WorkoutDao.java` | Final | Agregar query: `getSessionsForExercise(exerciseName, limit)` | 10-15 |
| `app/src/main/res/navigation/nav_graph.xml` | Final | Agregar destino performanceFragment | 20-25 |
| `app/src/main/res/menu/bottom_nav_menu.xml` | (opcional) | Si performance es tab: agregar item 6 | 5-8 |
| `app/src/main/java/com/josegarcia/appgym/ui/stats/StatsFragment.java` | (opcional) | Si performance es subsection: agregar tab | 10-20 |

### Lógica de Comparación (PerformanceViewModel)

```java
public class PerformanceComparison {
    public int setOrder;
    public String status;  // MEJORA, EMPEORA, MANTIENE
    public double currentWeight;
    public int currentReps;
    public double previousWeight;
    public int previousReps;
    public String unit;
    
    // Color basado en status
    public int getStatusColor() {
        switch (status) {
            case "MEJORA": return Color.GREEN;
            case "EMPEORA": return Color.RED;
            case "MANTIENE": return Color.GRAY;
            default: return Color.BLACK;
        }
    }
}

public class PerformanceViewModel extends ViewModel {
    public LiveData<List<PerformanceComparison>> compareCurrentVsPrevious(String exerciseName) {
        return new MutableLiveData<List<PerformanceComparison>>() {{
            // 1. Obtener último entreno con este ejercicio
            WorkoutSession current = dao.getLastWorkoutForExercise(exerciseName);
            
            // 2. Obtener penúltimo entreno
            List<WorkoutSession> history = dao.getSessionsForExercise(exerciseName, 2);
            if (history.size() < 2) return;  // Sin comparación posible
            
            WorkoutSession previous = history.get(1);
            
            // 3. Obtener series y comparar por setOrder
            List<ExerciseSet> currentSets = dao.getSetsForSession(current.id);
            List<ExerciseSet> previousSets = dao.getSetsForSession(previous.id);
            
            // 4. Comparar serie por serie
            List<PerformanceComparison> results = new ArrayList<>();
            for (ExerciseSet currentSet : currentSets) {
                ExerciseSet prevSet = previousSets.stream()
                    .filter(s -> s.setOrder == currentSet.setOrder)
                    .findFirst()
                    .orElse(null);
                
                if (prevSet == null) continue;  // Omitir si no existe en previo
                
                String status;
                if (currentSet.weight > prevSet.weight) {
                    status = "MEJORA";
                } else if (currentSet.weight < prevSet.weight) {
                    status = "EMPEORA";
                } else {
                    status = "MANTIENE";
                }
                
                results.add(new PerformanceComparison(
                    currentSet.setOrder, status,
                    currentSet.weight, currentSet.reps,
                    prevSet.weight, prevSet.reps,
                    currentSet.unit
                ));
            }
            
            setValue(results);
        }};
    }
}
```

### QA Manual - Fase 4

```
1. Abrir Rendimiento → sin data → mostrar "Sin entrenamientos para comparar"
2. Hacer 2 sesiones con mismo ejercicio (ej: Press Mancuernas)
3. Abrir Rendimiento → mostrar comparación por série
4. Escenario 1: Mejor desempeño → tarjeta verde
5. Escenario 2: Peor desempeño → tarjeta roja
6. Escenario 3: Misma marca → tarjeta gris
7. Escenario 4: Series faltantes (4 vs 3) → omitir serie faltante
8. Escenario 5: Ejercicio sin historial → no mostrar en comparación
9. Cambiar de ejercicio → actualizar comparación dinámicamente
```

---

## FASE 5 - Material Design 3 Core (3-4 días)

### Archivos a CREAR

| Archivo | Cambios |
|---------|---------|
| `app/src/main/res/values-v31/themes.xml` | Tema M3 para Android 12+ |
| `app/src/main/res/values/attrs.xml` (si no existe) | Attrs custom para M3 |

### Archivos a MODIFICAR

| Archivo | Cambios | Prioridad |
|---------|---------|-----------|
| `app/src/main/res/values/themes.xml` | Paleta de colores M3 (primary, secondary, tertiary, error, etc.) | P0 |
| `app/src/main/res/values/colors.xml` | Agregar colores M3 estándar | P0 |
| `app/src/main/res/values/styles.xml` | Material button styles | P1 |
| `app/src/main/res/layout/activity_tracker.xml` | Usar MaterialButton, MaterialCardView | P0 |
| `app/src/main/res/layout/fragment_home.xml` | Actualizar componentes | P0 |
| `app/src/main/res/layout/fragment_settings.xml` | Actualizar componentes | P0 |
| `app/src/main/res/layout/fragment_performance.xml` | Usar M3 chips, cards | P0 |

### Temas M3 - Template

```xml
<!-- values/themes.xml -->
<style name="Theme.AppGym" parent="Theme.Material3.DayNight">
    <!-- Palette M3 -->
    <item name="colorPrimary">@color/md_theme_primary</item>
    <item name="colorSecondary">@color/md_theme_secondary</item>
    <item name="colorTertiary">@color/md_theme_tertiary</item>
    <item name="colorError">@color/md_theme_error</item>
    
    <!-- Surface colors -->
    <item name="colorSurface">@color/md_theme_surface</item>
    <item name="colorSurfaceVariant">@color/md_theme_surfaceVariant</item>
    
    <!-- Components -->
    <item name="buttonStyle">@style/Widget.Material3.Button</item>
    <item name="materialCardViewStyle">@style/Widget.Material3.CardView</item>
</style>
```

### QA Manual - Fase 5

```
1. Tema claro → verificar colores primarios correctos
2. Tema oscuro → verificar contraste adecuado
3. Tema AMOLED (si existe) → colores negros puros
4. Todos los botones → Material3 shape + elevation
5. Cards → elevación y sombra M3
6. Chips → estilo M3
7. Barra inferior → color surface
8. Navegación → color primario
```

---

## FASE 6 - Material Design 3 Full + Estabilización (3-4 días)

### Archivos a MODIFICAR

**Resto de layouts (aplicar M3):**
- `app/src/main/res/layout/fragment_history.xml`
- `app/src/main/res/layout/fragment_stats.xml`
- `app/src/main/res/layout/fragment_body_weight.xml`
- `app/src/main/res/layout/activity_setup_wizard.xml`
- `app/src/main/res/layout/fragment_exercise_management.xml` (actualizar desde Fase 2)

**Componentes comunes:**
- `app/src/main/res/layout/item_*.xml` - Todos los item layouts
- `app/src/main/res/layout/dialog_*.xml` - Todos los diálogos

### QA Manual - Fase 6 (Regresión Completa)

```
SETUP & NAVEGACIÓN:
- [ ] Setup wizard completo (seleccionar split, crear rutina)
- [ ] Cambiar de split en Settings
- [ ] Cambiar de tema (3 opciones)
- [ ] Navegación bottom nav fluida

ENTRENAR:
- [ ] Crear sesión libre (5 ejercicios)
- [ ] Reordenar ejercicios (drag)
- [ ] Cambiar unidades (kg/lbs/placas)
- [ ] Guardar sesión sin errores

HISTORIAL:
- [ ] Ver listado de sesiones
- [ ] Click en sesión → detalles
- [ ] Editar sesión → cambiar datos → guardar
- [ ] Eliminar sesión (soft delete)

ESTADÍSTICAS:
- [ ] Volumen semanal (donut chart)
- [ ] Progreso por ejercicio (line chart)
- [ ] Heatmap (días activos)
- [ ] Rendimiento (comparación)

PESO:
- [ ] Agregar peso corporal
- [ ] Importar CSV con múltiples formatos
- [ ] Exportar peso → CSV válido

AJUSTES:
- [ ] Gestionar ejercicios (CRUD)
- [ ] Exportar/importar historial
- [ ] Cambiar tema
- [ ] Ver información app

DATOS:
- [ ] Exportar historial → archivo CSV leíble
- [ ] Importar historial en phone nuevo → datos intactos
- [ ] Exportar peso → archivo CSV
- [ ] Importar peso con formatos variados
```

### Blockers conocidos y resoluciones

| Bloqueo | Resolución |
|---------|-----------|
| Migracion 9→10 falla | Rollback a APK v9, copiar BD, test en dev |
| Unidades inconsistentes en comparación | Normalizar a kg antes de comparar |
| Cache no se limpia | Verificar clearCacheOnBackground() se ejecuta |

---

## RESUMEN POR FASE

| Fase | Duración | Archivos Nuevos | Archivos Modificados | Riesgo | Estado |
|------|----------|-----------------|----------------------|--------|--------|
| 0 | 1-2d | 3 | 2 | BAJO | 📋 Planificación |
| 1 | 3-4d | 2 | 5 | ALTO | 🔧 Crítica (DB) |
| 2 | 4-5d | 6 | 4 | MEDIO | 🎨 UI |
| 3 | 3-4d | 0 | 5 | BAJO | 🐛 Fixes |
| 4 | 5-6d | 7 | 4 | MEDIO | 📊 Feature |
| 5 | 3-4d | 2 | 8 | BAJO | 🎨 Design |
| 6 | 3-4d | 0 | 10+ | BAJO | ✅ Release |

**Ruta crítica total:** 23-30 días de desarrollo (6-8 semanas con testing + descansos)


