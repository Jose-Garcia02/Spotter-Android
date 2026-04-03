# Mapeo de Archivos por Fase - AppGym

## Estructura de Directorios Relevantes

```
app/src/main/
├── java/com/josegarcia/appgym/
│   ├── data/
│   │   ├── database/
│   │   │   ├── AppDatabase.java                    [MODIFICAR Fase 1]
│   │   │   └── InitialData.java                    [MODIFICAR Fase 1]
│   │   ├── dao/
│   │   │   ├── WorkoutDao.java                     [MODIFICAR Fase 4]
│   │   │   ├── ExerciseCatalogDao.java             [CREAR Fase 1] ⭐
│   │   │   └── [otras DAOs existentes]
│   │   └── entities/
│   │       ├── ExerciseCatalog.java                [CREAR Fase 1] ⭐
│   │       ├── PerformanceComparisonEntry.java     [CREAR Fase 4] ⭐
│   │       └── [entidades existentes]
│   ├── ui/
│   │   ├── tracker/
│   │   │   ├── TrackerActivity.java                [MODIFICAR Fase 3]
│   │   │   ├── ExerciseSelectionActivity.java      [MODIFICAR Fase 2, Fase 3]
│   │   │   ├── ExerciseSelectionAdapter.java       [MODIFICAR Fase 3]
│   │   │   └── [otros trackers]
│   │   ├── settings/
│   │   │   ├── SettingsFragment.java               [MODIFICAR Fase 2]
│   │   │   ├── ExerciseManagementFragment.java     [CREAR Fase 2] ⭐
│   │   │   ├── ExerciseManagementViewModel.java    [CREAR Fase 2] ⭐
│   │   │   ├── ExerciseManagementAdapter.java      [CREAR Fase 2] ⭐
│   │   │   └── SettingsViewModel.java              [POSIBLE ajustes Fase 2]
│   │   ├── performance/                            [NUEVA CARPETA Fase 4]
│   │   │   ├── PerformanceFragment.java            [CREAR Fase 4] ⭐
│   │   │   ├── PerformanceViewModel.java           [CREAR Fase 4] ⭐
│   │   │   └── PerformanceAdapter.java             [CREAR Fase 4] ⭐
│   │   ├── home/
│   │   │   ├── HomeFragment.java                   [POSIBLE Fase 5 M3]
│   │   │   └── HomeViewModel.java                  [SIN CAMBIOS]
│   │   ├── stats/
│   │   │   └── StatsFragment.java                  [POSIBLE Fase 5-6 M3]
│   │   ├── history/
│   │   │   └── HistoryFragment.java                [POSIBLE Fase 6 M3]
│   │   └── weight_tracker/
│   │       └── BodyWeightFragment.java             [POSIBLE Fase 6 M3]
│   ├── data/
│   │   └── CsvImporter.java                        [MODIFICAR Fase 3]
│   ├── utils/
│   │   ├── Constants.java                          [POSIBLE Fase 1 (etiquetas)]
│   │   └── logic/
│   │       └── ExerciseUtils.java                  [POSIBLE Fase 3 (unidades)]
│   ├── MainActivity.java                            [SIN CAMBIOS]
│   └── SplashActivity.java                          [SIN CAMBIOS]
└── res/
    ├── layout/
    │   ├── fragment_exercise_management.xml        [CREAR Fase 2] ⭐
    │   ├── item_exercise_management.xml            [CREAR Fase 2] ⭐
    │   ├── fragment_performance.xml                [CREAR Fase 4] ⭐
    │   ├── item_performance_set.xml                [CREAR Fase 4] ⭐
    │   ├── activity_tracker.xml                    [POSIBLE Fase 5]
    │   ├── fragment_stats.xml                      [POSIBLE Fase 5-6]
    │   ├── fragment_history.xml                    [POSIBLE Fase 6]
    │   └── [otros layouts existentes]
    ├── menu/
    │   └── bottom_nav_menu.xml                     [MODIFICAR Fase 4]
    ├── navigation/
    │   └── nav_graph.xml                           [MODIFICAR Fase 2, Fase 4]
    ├── values/
    │   ├── colors.xml                              [MODIFICAR Fase 5-6]
    │   ├── themes.xml                              [MODIFICAR Fase 5-6]
    │   ├── strings.xml                             [MODIFICAR Fase 2, Fase 4]
    │   └── dimens.xml                              [POSIBLE Fase 5]
    ├── values-night/
    │   ├── colors.xml                              [MODIFICAR Fase 5-6]
    │   └── themes.xml                              [MODIFICAR Fase 5-6]
    └── values-amoled/
        ├── colors.xml                              [CREAR/MODIFICAR Fase 5-6]
        └── themes.xml                              [CREAR/MODIFICAR Fase 5-6]
```

---

## FASE 0: Baseline (Sin cambios de código)

**Archivos a LEER (solo visualización, sin modificar):**
- `AppDatabase.java` (entender versión actual)
- `WorkoutDao.java` (entender queries actuales)
- `nav_graph.xml` (estructura navegación)

**Archivos a CREAR (documentos):**
- `CHECKLIST_REGRESION_FASE0.md` (en root del proyecto)

**Operaciones Git:**
```bash
git tag pre-phase-0
```

---

## FASE 1: Catalogo de Ejercicios

### ✅ CREAR (desde cero):

1. **`app/src/main/java/com/josegarcia/appgym/data/entities/ExerciseCatalog.java`**
   ```
   Campos:
   - id: int (PK, autoincrement)
   - name: String (UNIQUE, normalized)
   - defaultUnit: String (kg, lbs, plates)
   - muscleTag: String (unica de 7 opciones)
   - isActive: boolean
   - createdAt: long
   - updatedAt: long
   ```

2. **`app/src/main/java/com/josegarcia/appgym/data/dao/ExerciseCatalogDao.java`**
   ```
   Métodos mínimos:
   - insert(ExerciseCatalog)
   - update(ExerciseCatalog)
   - delete(int id)
   - softDelete(int id) [isActive = false]
   - getAll()
   - getActiveExercises()
   - getByName(String name) [TRIM/UPPER]
   - searchByName(String query)
   - getByMuscleTag(String muscleTag)
   ```

### ✏️ MODIFICAR:

1. **`app/src/main/java/com/josegarcia/appgym/data/database/AppDatabase.java`**
   ```
   - Agregar ExerciseCatalog.class a @Database entities
   - Cambiar version de 9 a 10
   - Agregar static MIGRATION_9_10
   - Agregar @Dao abstract method: ExerciseCatalogDao exerciseCatalogDao()
   ```

2. **`app/src/main/java/com/josegarcia/appgym/data/database/InitialData.java`**
   ```
   - Crear método: populateExerciseCatalog(Database db)
   - Insertar 7 ejercicios (uno por etiqueta):
     * "Press de Banca" → Pecho
     * "Dominadas" → Espalda
     * "Press Militar" → Hombro
     * "Curl de Mancuernas" → Biceps
     * "Fondos" → Triceps
     * "Sentadilla" → Pierna
     * "Plancha" → Core
   ```

### 📋 SQL Migration (dentro de AppDatabase):
```sql
CREATE TABLE IF NOT EXISTS `exercise_catalog` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
  `name` TEXT UNIQUE NOT NULL,
  `defaultUnit` TEXT NOT NULL,
  `muscleTag` TEXT NOT NULL,
  `isActive` INTEGER NOT NULL,
  `createdAt` INTEGER NOT NULL,
  `updatedAt` INTEGER NOT NULL
);
CREATE INDEX IF NOT EXISTS `index_exercise_catalog_muscleTag` 
  ON `exercise_catalog` (`muscleTag`);
```

**Archivos Afectados: 3 nuevos, 2 modificados**

---

## FASE 2: CRUD Ejercicios en Ajustes

### ✅ CREAR (desde cero):

1. **`app/src/main/java/com/josegarcia/appgym/ui/settings/ExerciseManagementFragment.java`**
   - Fragment con RecyclerView de ejercicios
   - Botones: Crear, Editar, Eliminar
   - ViewModel integration

2. **`app/src/main/java/com/josegarcia/appgym/ui/settings/ExerciseManagementViewModel.java`**
   - LiveData<List<ExerciseCatalog>>
   - Métodos: insert(), update(), softDelete(), search()

3. **`app/src/main/java/com/josegarcia/appgym/ui/settings/ExerciseManagementAdapter.java`**
   - RecyclerView Adapter
   - ViewHolder con campos editables
   - Click listeners para CRUD

4. **`app/src/main/res/layout/fragment_exercise_management.xml`**
   - FAB o botón crear
   - RecyclerView
   - Material Design 3 (desde Fase 5)

5. **`app/src/main/res/layout/item_exercise_management.xml`**
   - CardView con nombre (EditText)
   - Dropdown unidad
   - RadioGroup etiqueta (7 opciones)
   - Botón eliminar

### ✏️ MODIFICAR:

1. **`app/src/main/java/com/josegarcia/appgym/ui/SettingsFragment.java`**
   ```
   - Agregar botón "Gestionar Ejercicios"
   - navigate(R.id.action_settings_to_exercise_management)
   ```

2. **`app/src/main/java/com/josegarcia/appgym/ui/tracker/ExerciseSelectionActivity.java`**
   ```
   - Cambiar: buscar exercises de ExerciseCatalog (activos)
   - Remover: diálogo crear nuevo ejercicio
   - Solo seleccionar de lista existente
   ```

3. **`app/src/main/res/navigation/nav_graph.xml`**
   ```xml
   - Agregar fragment ExerciseManagementFragment
   - Agregar action desde SettingsFragment
   ```

4. **`app/src/main/res/values/strings.xml`**
   ```xml
   - <string name="manage_exercises">Gestionar Ejercicios</string>
   - <string name="create_exercise">Crear Ejercicio</string>
   - <string name="edit_exercise">Editar Ejercicio</string>
   - <string name="muscle_tag">Etiqueta Muscular</string>
   ```

5. **`app/src/main/java/com/josegarcia/appgym/data/dao/ExerciseCatalogDao.java`**
   ```
   - Agregar método: updateExercise(ExerciseCatalog)
   - Agregar método: softDelete(int id)
   - Agregar método: getActiveExercises()
   ```

**Archivos Afectados: 5 nuevos, 4 modificados**

---

## FASE 3: Correcciones UX + Caché

### ✏️ MODIFICAR:

1. **`app/src/main/java/com/josegarcia/appgym/ui/tracker/TrackerActivity.java`**
   ```
   Cambios:
   - saveWorkout(): agregar limpieza SharedPreferences
     prefs.edit().clear().apply() ← después de insertSession()
   ```

2. **`app/src/main/java/com/josegarcia/appgym/ui/tracker/ExerciseSelectionActivity.java`**
   ```
   Cambios:
   - searchInput: agregar OnEditorActionListener(Enter) → ocultar teclado
   - loadExercises(): integrar ItemTouchHelper para drag-drop
   - Preservar selección al reordenar
   ```

3. **`app/src/main/java/com/josegarcia/appgym/ui/tracker/ExerciseSelectionAdapter.java`**
   ```
   Cambios:
   - Implementar ItemTouchHelper.Callback
   - onMove() para reordenar
   - notifyItemMoved() para UI
   ```

4. **`app/src/main/java/com/josegarcia/appgym/data/CsvImporter.java`**
   ```
   Cambios:
   - parseBodyWeightLine(): aceptar múltiples formatos de fecha
     * dd/mm/yyyy
     * mm/dd/yyyy
     * yyyy-mm-dd
   - Agregar try-catch con validación
   ```

5. **`app/src/main/java/com/josegarcia/appgym/utils/logic/ExerciseUtils.java`**
   ```
   Revisión (sin cambios si ya está correcto):
   - shouldUsePlacas() → lógica heurística correcta
   - Normalización de nombres consistente
   ```

**Archivos Afectados: 0 nuevos, 5 modificados**

---

## FASE 4: Módulo Rendimiento

### ✅ CREAR (desde cero):

1. **`app/src/main/java/com/josegarcia/appgym/data/entities/PerformanceComparisonEntry.java`** (DTO)
   ```
   Campos:
   - exerciseName: String
   - setOrder: int
   - currentWeight: double
   - currentReps: int
   - prevWeight: double
   - prevReps: int
   - status: String [MEJORA|MANTIENE|EMPEORA|SIN_HISTORIAL]
   - volumeCurrent: double
   - volumePrev: double
   ```

2. **`app/src/main/java/com/josegarcia/appgym/ui/performance/PerformanceFragment.java`**
   - Spinner para seleccionar ejercicio
   - RecyclerView para mostrar sets
   - Comparación lógica

3. **`app/src/main/java/com/josegarcia/appgym/ui/performance/PerformanceViewModel.java`**
   - LiveData<List<PerformanceComparisonEntry>>
   - getComparisons(exerciseName)
   - Lógica comparación

4. **`app/src/main/java/com/josegarcia/appgym/ui/performance/PerformanceAdapter.java`**
   - RecyclerView con Cards por setOrder
   - Colores según status

5. **`app/src/main/res/layout/fragment_performance.xml`**
   - Spinner ejercicios
   - RecyclerView

6. **`app/src/main/res/layout/item_performance_set.xml`**
   - CardView con Set #X
   - Mostrar current vs prev
   - Indicador status (color/icono)

### ✏️ MODIFICAR:

1. **`app/src/main/java/com/josegarcia/appgym/data/dao/WorkoutDao.java`**
   ```
   Agregar:
   - getLastSessionForExercise(String exerciseName)
   - getLastSetForExerciseAndOrder(String exerciseName, int setOrder)
   ```

2. **`app/src/main/res/navigation/nav_graph.xml`**
   ```xml
   - Agregar fragment PerformanceFragment
   ```

3. **`app/src/main/res/menu/bottom_nav_menu.xml`**
   ```xml
   - Agregar item navigation_performance
   - Icon + label "Rendimiento"
   ```

4. **`app/src/main/res/values/strings.xml`**
   ```xml
   - <string name="performance_title">Rendimiento</string>
   - <string name="improvement">Mejora</string>
   - <string name="maintained">Mantenimiento</string>
   - <string name="decline">Empeoramiento</string>
   - <string name="no_history">Sin Historial</string>
   ```

**Archivos Afectados: 6 nuevos, 4 modificados**

---

## FASE 5: Material Design 3 - Módulos Principales

### ✏️ MODIFICAR (TEMAS):

1. **`app/src/main/res/values/colors.xml`**
   ```
   - Actualizar a palette M3
   - Colores: primary, secondary, tertiary, error, etc.
   ```

2. **`app/src/main/res/values/themes.xml`**
   ```
   - MaterialComponents theme base
   - Atributos M3
   ```

3. **`app/src/main/res/values-night/colors.xml`** (tema oscuro)
   ```
   - Variantes oscuras de colores
   ```

4. **`app/src/main/res/values-night/themes.xml`** (tema oscuro)

### ✏️ MODIFICAR (LAYOUTS):

Prioridad 1 - Entrenar:
- `app/src/main/res/layout/activity_tracker.xml`
- `app/src/main/res/layout/activity_exercise_selection.xml`

Prioridad 2 - Ajustes:
- `app/src/main/res/layout/fragment_settings.xml`
- `app/src/main/res/layout/fragment_exercise_management.xml` (ya creado, actualizar si es necesario)

Prioridad 3 - Rendimiento:
- `app/src/main/res/layout/fragment_performance.xml` (ya creado, actualizar si es necesario)

**Cambios esperados:**
- Remplacer Button genéricos por `com.google.android.material.button.MaterialButton`
- Remplacer EditText por `com.google.android.material.textfield.TextInputEditText`
- Usar `CardView` con elevation M3
- Colores vía tema (not hardcoded)

**Archivos Afectados: 0 nuevos, ~8 layouts modificados, 4 archivos colores/temas**

---

## FASE 6: M3 Expansión + Release

### ✏️ MODIFICAR (LAYOUTS RESTANTES):

Módulos:
- `fragment_history.xml`
- `fragment_stats.xml`
- `fragment_body_weight.xml`
- Layouts en `setup/` y `wizard/`

### 🏷️ GIT OPERATIONS:

```bash
# Crear rama release
git checkout -b release/1.0.0

# Commit y tags
git commit -m "Release: v1.0.0-rc1 - Rendimiento, CRUD, M3"
git tag v1.0.0-rc1

# Después de QA en segundo teléfono
git commit -m "Release: v1.0.0 - Estable"
git tag v1.0.0

# Merge a main
git checkout main
git merge release/1.0.0
git push origin main v1.0.0
```

**Archivos Afectados: ~10 layouts, temas finales**

---

## 📌 RESUMEN POR TIPO DE CAMBIO

### CREAR (14 archivos nuevos):
- **Fase 1:** ExerciseCatalog.java, ExerciseCatalogDao.java
- **Fase 2:** ExerciseManagementFragment.java, ViewModel, Adapter, 2 layouts
- **Fase 4:** PerformanceComparisonEntry.java, PerformanceFragment.java, ViewModel, Adapter, 2 layouts
- **Fase 5-6:** 2 layouts colores/temas para AMOLED (si aplica)

### MODIFICAR (16 archivos existentes):
- **Fase 1:** AppDatabase.java, InitialData.java
- **Fase 2:** SettingsFragment.java, ExerciseSelectionActivity.java, nav_graph.xml, strings.xml, ExerciseCatalogDao.java (ampliación)
- **Fase 3:** TrackerActivity.java, ExerciseSelectionActivity.java (ampliación), ExerciseSelectionAdapter.java, CsvImporter.java
- **Fase 4:** WorkoutDao.java, nav_graph.xml (ampliación), bottom_nav_menu.xml, strings.xml (ampliación)
- **Fase 5-6:** colors.xml (2x), themes.xml (2x), ~10 layouts

### NO TOCAR:
- MainActivity.java
- SplashActivity.java
- Excepto expansión visual M3 en Fase 5-6

---

## ✅ CHECKLIST DE ARCHIVOS

### Antes de Fase 1:
- [ ] AppDatabase.java está en version 9
- [ ] No hay tabla exercise_catalog
- [ ] ExerciseCatalogDao no existe

### Después de Fase 1:
- [ ] BD versión 10
- [ ] ExerciseCatalog.java creado
- [ ] ExerciseCatalogDao.java creado
- [ ] 7 ejercicios iniciales poblados

### Después de Fase 2:
- [ ] ExerciseManagementFragment.java existe
- [ ] CRUD funciona desde UI
- [ ] ExerciseSelectionActivity solo selecciona

### Después de Fase 3:
- [ ] SharedPreferences limpiado al finalizar
- [ ] Drag-drop funciona
- [ ] Teclado se oculta con Enter

### Después de Fase 4:
- [ ] PerformanceFragment existe
- [ ] bottom_nav_menu.xml incluye Rendimiento
- [ ] Comparaciones correctas

### Después de Fase 5:
- [ ] colors.xml es M3
- [ ] themes.xml es M3
- [ ] Módulos principales visuales M3

### Después de Fase 6:
- [ ] Todos los layouts M3
- [ ] v1.0.0 tagged
- [ ] Release notes preparadas

