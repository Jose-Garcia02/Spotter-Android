# AppGym - Documentación Técnica de Ingeniería
## Resumen Ejecutivo
**Proyecto:** AppGym (Spotter)  
**Descripción:** Aplicación Android para el seguimiento de entrenamientos de fuerza (gym) con análisis de volumen, progreso y gestión de planes de entrenamiento.  
**Stack Tecnológico:** Java, Room (SQLite), MVVM, Jetpack Navigation, Material Design 3, MPAndroidChart  
**Nivel de Complejidad:** Alto (manejo de estados complejos, caché, cálculos volumétricos, temas dinámicos)  
---
## 1. ANÁLISIS DE LA CAPA DE DATOS
### 1.1 Arquitectura de Base de Datos
#### Esquema Relacional
La base de datos Room utiliza 6 entidades principales con relaciones jerárquicas y de cascada:
```
Splits (Padre)
  ├── Routines (1:N)
  │   └── RoutineExercises (1:N)
  └── WorkoutSessions (relación implícita)
        └── ExerciseSets (1:N)
BodyWeightLogs (Independiente)
```
#### Diagrama E-R
| Tabla | Descripción | Claves Primarias | Claves Foráneas | Índices |
|-------|-------------|------------------|-----------------|---------|
| `splits` | Planes de entrenamiento (Classic o User) | `id` (AUTOINCREMENT) | - | - |
| `routines` | Rutinas dentro de un Split | `id` (AUTOINCREMENT) | `splitId` → `splits(id)` ON DELETE CASCADE | `splitId` |
| `routine_exercises` | Ejercicios de una rutina | `id` (AUTOINCREMENT) | `routineId` → `routines(id)` ON DELETE CASCADE | `routineId` |
| `workout_sessions` | Sesiones de entrenamiento registradas | `id` (AUTOINCREMENT) | - | - |
| `exercise_sets` | Series dentro de una sesión | `id` (AUTOINCREMENT) | `sessionId` → `workout_sessions(id)` ON DELETE CASCADE | `sessionId` |
| `body_weight_logs` | Registro de peso corporal | `id` (AUTOINCREMENT) | - | - |
---
## INFORMACIÓN DETALLADA DE ENTIDADES
### Split
```
- id: int (PK)
- name: String         // "Upper/Lower", "PPL", "Arnold Split"
- description: String  // Descripción de frecuencia
- isActive: boolean    // Flag activo
- type: String        // "Classic" o "Custom"
- isTemplate: boolean // Distingue templates
```
**Lógica:** Templates al activar se marcan como isTemplate=true. CASCADE DELETE asegura limpieza.
### Routine
```
- id: int (PK)
- splitId: int (FK)
- name: String
- colorResId: int
- isSystem: boolean
- orderIndex: int     // Orden de visualización
```
### RoutineExercise
```
- id: int (PK)
- routineId: int (FK)
- exerciseName: String
- order: int          // Secuencia
- targetSets: int     // Default 3
- targetUnit: String  // kg, lbs, placas
```
### WorkoutSession
```
- id: long (PK)
- date: long          // Timestamp
- routineName: String // Referencia por nombre
```
### ExerciseSet
```
- id: long (PK)
- sessionId: long (FK)
- exerciseName: String
- weight: double
- reps: int
- setOrder: int       // 1, 2, 3...
- unit: String        // kg, lbs, placas
```
**Normalización:** TRIM(UPPER()) en queries para evitar duplicados.
### BodyWeightLog
```
- id: long (PK)
- timestamp: long
- weight: float       // kg
- photoUri: String    // Opcional
```
---
## DICCIONARIO DE DAOs
### WorkoutDao - Métodos Críticos
| Método | SQL | Propósito |
|--------|-----|-----------|
| insertSession() | INSERT | Crear nueva sesión |
| insertSets() | INSERT batch | Persistir series en lote |
| getSetsForSession() | SELECT...WHERE sessionId | Recuperar series |
| getLastSetsForExercise() | SELECT...JOIN...TRIM(UPPER()) LIMIT 10 | Historial normalizando nombres |
| getSessionByDateAndRoutine() | SELECT...WHERE date AND routineName | Evitar duplicados |
| getDetailedProgressForExercise() | SELECT date, setOrder, weight, reps | Para gráficos |
| getGlobalVolumeHistory() | SELECT SUM(weight*reps) GROUP BY session | Histórico de volumen |
| getVolumeByRoutine() | SELECT routineName, SUM(...) BETWEEN | Volumen por rutina semanal |
| getSessionDatesInRange() | SELECT DISTINCT date BETWEEN | Optimizado para heatmap (solo fechas) |
**Optimización Clave:** getSessionDatesInRange() retorna List<Long> (ligero) vs objetos completos.
### RoutineDao
- getAllRoutines() → LiveData reactivo
- getRoutinesForSplit() → switchMap con activeSplit
- getRoutinesForSplitSync() → Versión bloqueante para threads de BD
### SplitDao
- setActiveSplit() → @Transaction (deactiva todos, activa uno - atómico)
- getTemplateSplits() → isTemplate = 1
- getUserSplits() → isTemplate = 0
### RoutineExerciseDao
- getExercisesForRoutineByName() → JOIN con routines
### BodyWeightDao
- getLastLog() → LiveData observable para card de peso
---
## MIGRACIONES
| V | Cambios Clave | Nota |
|---|---------------|------|
| 3→4 | Crear body_weight_logs | Feature de peso |
| 4→5 | Crear routines (sin FK) | Sistema inicial |
| 5→6 | Crear routine_exercises | Ejercicios por rutina |
| 6→7 | **CRÍTICA:** Crear splits, FK de Routines | Refactoring a Split→Routine→Exercise |
| 7→8 | targetSets/targetUnit, orderIndex | Multi-unidades |
| 8→9 | Agregar isTemplate | Plantillas vs custom |
**Bug 6→7:** "Default Split" creado pero no era válido. Solución: AppDatabase.onOpen() limpia si único y no es "Classic".
---
## 2. DESGLOSE DE LÓGICA DE NEGOCIO
### ExerciseTrackerViewModel
**Patron:** SavedStateHandle para sobrevivir rotaciones.
**Estado:** exerciseList, editingSessionId, sessionTimestamp, currentRoutineName.
### HomeViewModel
**Pattern:** Transformations.switchMap() → Cuando cambia Split → carga Routines dinámicamente.
**Cálculos:** loadWeeklyVolume(), loadHeatmap(int weeks).
#### Algoritmo de Heatmap
1. Alinear a LUNES (inicio de semana)
2. Query getSessionDatesInRange()
3. Construir HashSet<Long> de días activos (búsqueda O(1))
4. Generar List<WeekData> (7 días × N semanas)
5. Retornar HeatmapResult
**Optimización:** Solo retorna fechas (List<Long>), no objetos.
### SettingsViewModel
**Métodos:** setTheme(), exportHistory(), importHistory(), exportBodyWeight(), importBodyWeight().
**Thread:** Executor para operaciones de I/O (bg).
---
## CÁLCULO DE VOLUMEN
**Fórmula:** Volume = weight × reps
**SQL:**
```sql
SELECT SUM(sets.weight * sets.reps) as totalVolume
FROM exercise_sets sets
INNER JOIN workout_sessions session ON sets.sessionId = session.id
GROUP BY session.id
```
**Aplicaciones:**
1. HomeFragment - Donut Chart (volume por rutina semanal)
2. StatsFragment - LineChart (histórico)
3. HomeFragment - Heatmap (consistencia)
---
## NORMALIZACIÓN DE NOMBRES
**Query normaliza:**
```sql
WHERE TRIM(UPPER(exerciseName)) = TRIM(UPPER(:exerciseName))
```
**Previene:** "Press Con Mancuernas" vs "press mancuernas" sean diferentes.
---
## RESOLUCIÓN DE UNIDADES
**ExerciseUtils.shouldUsePlacas():** Heurística basada en palabras clave.
- "posterior", "bayesian", "extension", "laterales", "aperturas", "cruces", "jalon" → Placas.
---
## IMPORTACIÓN CSV
**Flujo:**
1. Parse línea → tokens[fecha, nombre, unit, rutina, S1-P, S1-R, ...]
2. sessionCache[dateStr|routineName] → evita N queries
3. Check duplicados: getSessionByDateAndRoutine()
4. parseAndAddSet() → máx 3 sets
5. Batch insert (operación única)
**Prevención de Duplicados:**
```
Si sesión existe + tiene sets → Skip (marker -1L en cache)
```
---
## 3. ARQUITECTURA DE UI Y CICLO DE VIDA
### TrackerActivity - Ciclo de Vida de Sesión
```
1. INICIO
   ├─ onCreate()
   │  ├─ ExerciseTrackerViewModel
   │  ├─ initializeRoutineName()
   │  ├─ checkEditMode()
   │  ├─ loadInitialData()
   │  └─ setupRecyclerView()
   │
2. EN PROGRESO
   ├─ showAddExerciseDialog() → Buscar ejercicios
   ├─ TrackerAdapter renderiza sets
   ├─ TextWatcher actualiza modelo en vivo
   ├─ saveCache() → SharedPreferences
   │
3. RECOVERY (Crash)
   ├─ checkCache() → restaurar desde prefs
   │
4. FINALIZACIÓN
   ├─ saveWorkout()
   │  ├─ Create WorkoutSession
   │  ├─ Batch insert ExerciseSets
   │  ├─ Limpiar cache
   │  └─ NavigateUp
```
### Caché en SharedPreferences
```java
PREFS_NAME = "workout_cache"
KEY_EXERCISES  → Gson serialized List<ExerciseEntry>
KEY_SESSION_ID → long (para edición)
KEY_TIMESTAMP  → long (original)
```
**Propósito:** Recuperación tras crash sin BD hasta "Finalizar".
### TrackerAdapter - Coordinación
```
ExerciseEntry (ViewModel)
  ├─ name: String
  ├─ unit: String (toggle kg↔lbs↔placas)
  └─ sets: List<SetEntry>
      ├─ weight: double (EditText input)
      ├─ reps: int (EditText input)
      ├─ prevWeight: double (read-only, historial)
      └─ prevReps: int (read-only)
ExerciseViewHolder.bind()
  ├─ Inflate R.layout.item_set_row
  ├─ TextWatcher → actualiza SetEntry en vivo
  ├─ Long-click → eliminar ejercicio
  └─ btnDeleteSet → remover set
```
---
## MÓDULO DE ESTADÍSTICAS (StatsFragment)
### Flujo de Transformación
```
1. QUERY BD
   └─ getDetailedProgressForExercise(exerciseName)
      └─ List<ExerciseProgressEntry>
2. PROCESAMIENTO
   ├─ Agrupar por setOrder
   ├─ Métrica: Max Weight, Volume, Reps
   └─ Filter por series visibles (ChipGroup)
3. TRANSFORMACIÓN A ENTRIES
   └─ List<Entry> → LineDataSet
4. RENDERIZADO
   ├─ LineChart.setData(LineData)
   ├─ CustomMarkerView → Tooltip
   └─ invalidate()
5. FILTRADO DINÁMICO
   ├─ ChipGroup listener
   ├─ Toggle dataset visibility
   └─ Chart update
```
### Sistema de Chips
**Uso:** Comparar progreso entre series (Set 1 vs Set 2).
---
## 4. GRAFO DE DEPENDENCIAS
### Matriz Simplificada
```
MainActivity
  ├─ AppDatabase (singleton)
  ├─ SharedPreferences (theme)
  └─ NavigationUI
TrackerActivity
  ├─ ExerciseTrackerViewModel
  ├─ WorkoutDao, RoutineExerciseDao
  ├─ ExerciseUtils
  ├─ SharedPreferences (cache)
  └─ CsvImporter
HomeFragment
  ├─ HomeViewModel
  ├─ WorkoutDao
  ├─ HeatmapAdapter
  ├─ PieChart (Donut)
  └─ BodyWeightLog
StatsFragment
  ├─ WorkoutDao
  ├─ LineChart
  ├─ CustomMarkerView
  └─ ChipGroup
HistoryFragment
  ├─ WorkoutDao
  ├─ HistoryAdapter
  └─ ItemTouchHelper (swipe-to-delete)
SettingsFragment
  ├─ SettingsViewModel
  ├─ CsvImporter
  ├─ ActivityResultLauncher (SAF)
  └─ AppCompatDelegate (theme)
CsvImporter
  ├─ WorkoutDao, BodyWeightDao
  ├─ SimpleDateFormat
  └─ I/O (InputStream, OutputStream)
DraftManager
  ├─ Split, Routine, RoutineExercise
  └─ Singleton pattern
InitialData
  ├─ Routine factory (templates)
  └─ Constants
```
---
## 5. MÉTODOS CRÍTICOS - TOP 5
### 1. TrackerActivity.loadSessionForEditing(long sessionId)
**Complejidad:** Alta  
**Responsabilidad:** Reconstruir sesión desde BD para edición.
**Pasos:**
1. Query getSetsForSession(sessionId)
2. Agrupar por exerciseName (LinkedHashMap preserva orden)
3. Mapear ExerciseSet → ExerciseEntry
4. detectar unit (primer set)
5. Post a main thread
**Impacto:** Cierra loop de edición de entrenamientos pasados.
---
### 2. HomeViewModel.loadHeatmap(int weeksCount)
**Complejidad:** Muy Alta  
**Responsabilidad:** Generar estructura de heatmap 7 días × N semanas.
**Algoritmo:**
1. Alinear a LUNES (Calendar loop hasta DAY_OF_WEEK == MONDAY)
2. Retroceder N semanas
3. Query getSessionDatesInRange() (solo List<Long>)
4. Construir HashSet<Long> para búsqueda O(1)
5. Generar List<WeekData> (7 días/semana × weeksCount)
6. postValue() → UI
**Optimización:** HashSet permite lookup O(1) de días activos.
---
### 3. CsvImporter.importFromStream(InputStream, WorkoutDao)
**Complejidad:** Alta  
**Responsabilidad:** Parse CSV, normalización, deduplicación, batch insert.
**Pasos:**
1. BufferedReader → línea por línea
2. Skip headers (si !isDigit(firstChar))
3. Parse: dateStr|exerciseName|unit|routineName|S1-P,S1-R,...
4. sessionCache[dateStr|routineName] → evita lookups repetidos
5. Check duplicados: getSessionByDateAndRoutine() + isEmpty(getSets)
6. parseAndAddSet() × 3 → ExerciseSet[]
7. dao.insertSets(batch) → único comando SQL
**Prevención Duplicados:** Marker -1L en cache si ya procesada.
---
### 4. StatsFragment.updateChart()
**Complejidad:** Media-Alta  
**Responsabilidad:** Transformar ExerciseProgressEntry en LineChart dinámico.
**Pasos:**
1. Agrupar currentDetailedData por setOrder
2. Determinar métrica (weight, volume, reps) vía RadioGroup
3. Crear LineDataSet por serie
4. Verificar visibilidad (ChipGroup)
5. LineChart.setData()
6. Configurar ejes (XAxis = fecha, YAxis = valor)
7. invalidate() → repaint
**Ventaja:** Chips permiten toggle dinámico (sin requery).
---
### 5. TrackerAdapter.ExerciseViewHolder.bind(ExerciseEntry)
**Complejidad:** Alta  
**Responsabilidad:** Renderizar tarjeta de ejercicio con sets dinámicos.
**Pasos:**
1. Mostrar nombre + unit toggle
2. Agrupar y renderizar sets (R.layout.item_set_row)
3. TextWatcher → actualiza SetEntry.weight/reps en vivo
4. tvPrevious → historial desde prevWeight/prevReps
5. Long-click → eliminar ejercicio
6. btnAddSet → agregar set
7. btnDeleteSet → remover set
**Ciclo de Vida:** onBindViewHolder() restaura estado desde ExerciseEntry (preserva inputs tras scroll).
---
## 6. PATRONES IMPLEMENTADOS
| Patrón | Ubicación | Beneficio |
|--------|-----------|-----------|
| MVVM | ViewModel + LiveData | Reactividad, separation of concerns |
| Repository | BodyWeightRepository | Abstracción de acceso |
| Singleton + Double-Checked Locking | AppDatabase.getDatabase() | Thread-safe, única instancia |
| Factory | InitialData | Crear templates |
| Adapter | RecyclerView adapters | Renderización flexible |
| Observer | LiveData | Observers notificados de cambios |
| Strategy | ExerciseUtils.shouldUsePlacas() | Polimorfismo |
| Transaction | SplitDao.setActiveSplit() | Atomicidad (deactiva todos → activa uno) |
---
## 7. CONSIDERACIONES DE RENDIMIENTO
### Optimizaciones
1. **Índices en BD:** splitId, routineId, sessionId
2. **Batch Inserts:** dao.insertSets(list) es una operación SQL única
3. **Caché en Memoria:** 
   - sessionCache en import (HashMap)
   - dataSetCache en stats (reutilizar LineDataSet)
4. **Executor Pool:** newFixedThreadPool(4) para BD
5. **Query Ligeras:** getSessionDatesInRange() retorna solo List<Long>
6. **Normalización SQL:** TRIM(UPPER()) en queries evita duplicados
### Cuellos de Botella Potenciales
| Cuello | Causa | Mitigación |
|--------|-------|-----------|
| getLastSetsForExercise() con 10K sets | N sin proper indexing | Índice en sessionId + pagination |
| Export CSV con 1K sesiones | Iteración secuencial | Batch export asíncrono |
| Heatmap con 100+ semanas | O(weeks*7*n) | Limitar a 12 semanas por defecto |
---
## 8. LISTA DE CLASES POR MÓDULO
### Data Layer
```
database/
  ├─ AppDatabase.java
  └─ InitialData.java
dao/
  ├─ WorkoutDao.java
  ├─ RoutineDao.java
  ├─ RoutineExerciseDao.java
  ├─ SplitDao.java
  └─ BodyWeightDao.java
entities/
  ├─ Split.java
  ├─ Routine.java
  ├─ RoutineExercise.java
  ├─ WorkoutSession.java
  ├─ ExerciseSet.java
  ├─ BodyWeightLog.java
  ├─ ExerciseProgressEntry.java
  ├─ RoutineVolumeEntry.java
  └─ SessionVolumeEntry.java
BodyWeightRepository.java
CsvImporter.java
```
### Business Logic & ViewModels
```
MainActivity.java
SplashActivity.java
SettingsViewModel.java
ExerciseTrackerViewModel.java
HomeViewModel.java
utils/
  ├─ Constants.java
  └─ logic/
      └─ ExerciseUtils.java
setup/
  ├─ DraftManager.java
  ├─ MySplitsActivity.java
  └─ [...]
```
### UI Layer
```
tracker/
  ├─ TrackerActivity.java
  ├─ ExerciseTrackerViewModel.java
  ├─ TrackerAdapter.java
  ├─ ExerciseSelectionActivity.java
  └─ dialogs/
home/
  ├─ HomeFragment.java
  ├─ HomeViewModel.java
  ├─ RoutineAdapter.java
  ├─ HeatmapAdapter.java
  └─ TrainFragment.java
history/
  ├─ HistoryFragment.java
  └─ HistoryAdapter.java
stats/
  ├─ StatsFragment.java
  └─ CustomMarkerView.java
weight_tracker/
  └─ [Components]
settings/
  ├─ SettingsFragment.java
  └─ SettingsViewModel.java
```
---
## 9. FLUJOS PRINCIPALES
### New Workout Session
```
HomeFragment → btnStartWorkout → Intent(ROUTINE_NAME, PRESELECTED_EXERCISES)
    ↓
TrackerActivity
  ├─ Load template (RoutineExerciseDao)
  ├─ User inputs weight/reps
  └─ saveWorkout()
       ├─ Create WorkoutSession
       ├─ Batch insert ExerciseSets
       └─ NavigateUp → HomeFragment actualiza (LiveData observers)
```
### Edit Session
```
HistoryFragment → Tap session → Intent(EDIT_SESSION_ID, EDIT_SESSION_TIMESTAMP)
    ↓
TrackerActivity.loadSessionForEditing()
  ├─ Query getSetsForSession()
  ├─ Group by exerciseName
  └─ Map to ExerciseEntry[]
User modifies → saveWorkout()
  ├─ deleteSetsForSession() → clear old
  ├─ insertSets() → insert new
  └─ Keep sessionId + timestamp original
```
### Import CSV
```
SettingsFragment → Import button → SAF file picker
    ↓
SettingsViewModel.importHistory(uri)
  ├─ Executor.execute()
  └─ CsvImporter.importFromStream(is, dao)
       ├─ sessionCache[key] → evita lookups
       ├─ Check duplicates
       ├─ Batch insert
       └─ statusMessage.postValue()
```
---
## CONCLUSIÓN
**AppGym** implementa una arquitectura **MVVM sólida** con **Room como BD persistente**, enfocada en **reactividad con LiveData** y **manejo robusto de estados**.
### Fortalezas
✅ Separación clara de capas  
✅ Consultas optimizadas  
✅ Recovery tras crashes (caché)  
✅ Importación/Exportación CSV robusta  
✅ Estadísticas ricas  
✅ Temas dinámicos  
### Áreas de Mejora
⚠️ Fragmentos muy grandes  
⚠️ Validación de entrada limitada  
⚠️ Testing unitario inexistente  
⚠️ Documentación JavaDoc limitada  
### Recomendaciones Futuras
1. Migrar a Kotlin
2. Room FTS para búsqueda de ejercicios
3. Tests unitarios e integración
4. Refactorizar Fragments gigantes
5. Jetpack Datastore vs SharedPreferences
