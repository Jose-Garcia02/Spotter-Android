# Análisis de Viabilidad Técnica Detallado - AppGym

**Fecha:** 2026-04-03  
**Versión Base:** 9 (Room DB)  
**Estado:** ✅ LISTO PARA FASE 0  

---

## 1. RESUMEN EJECUTIVO

### Veredicto

**✅ GO - Proyecto listo para iniciación de Fase 0**

**Justificación:**
- Arquitectura estable (MVVM + Room v9) sin deuda técnica crítica
- 6 entidades bien normalizadas con migraciones comprobadas (3→9)
- 52 archivos Java organizados (datos, DAO, UI, utils)
- Migraciones previas exitosas (especialmente 6→7 crítica, 8→9 templates)
- Cero bloqueantes P0 identificados
- Capas bien separadas (datos → DAO → ViewModel → UI)

**Riesgos identificados:** 3 (M2: mitigables, 1 P1: bajo impacto)

**Esfuerzo estimado:** 6-8 semanas (fases 0-6)

---

## 2. ANÁLISIS DE CAPAS

### 2.1 Capa de Datos - ✅ SÓLIDA

**Base de Datos:**
- Versión: 9
- Entidades: 6 (Splits, Routines, RoutineExercises, WorkoutSessions, ExerciseSets, BodyWeightLogs)
- Migraciones: 7 (3→4, 4→5, 5→6, 6→7, 7→8, 8→9, _falta 9→10_)
- Normalización: TRIM(UPPER()) en queries clave (getLastSetsForExercise, getLastLogForExercise)
- Indices: Presentes en FK críticas (sessionId, routineId, splitId)

**Estado Migraciones:**
```
✅ 3→4: body_weight_logs (simple)
✅ 4→5: routines iniciales (sin FK)
✅ 5→6: routine_exercises (ahora con FK)
✅ 6→7: **CRÍTICA** - Crear splits + refactoring Routine.splitId
✅ 7→8: targetSets + targetUnit + orderIndex
✅ 8→9: isTemplate para distinguir Classic vs Custom
❌ 9→10: **PENDIENTE** - exercise_catalog (requerida para Fase 1)
```

**Hallazgos clave:**
- Migraciones son transaccionales (execSQL se ejecuta en transacción implícita)
- onOpen() callback limpia "Default Split" artifacts tras 6→7
- onOpen() también corrige isTemplate=0 para split activo (línea 170)
- No hay validación explícita de datos tras migraciones (riesgo bajo con datos reales)

### 2.2 Capa de Acceso a Datos (DAO) - ✅ BIEN ESTRUCTURADA

**Archivos:**
- `WorkoutDao.java` (115 líneas, 14 queries)
- `RoutineDao.java` (43 líneas)
- `RoutineExerciseDao.java` (31 líneas)
- `SplitDao.java` (sin leer, pero referenciado)
- `BodyWeightDao.java` (sin leer, pero referenciado)

**Patrones detectados:**
1. **CRUD Completo:** Insert, Update, Delete, Select por ID, Select all
2. **Queries normalizadas:** getLastSetsForExercise usa TRIM(UPPER())
3. **Batch operations:** insertSets() acepta List<ExerciseSet>
4. **Deduplicación:** getSessionByDateAndRoutine(date, routineName) para avoid duplicates
5. **Cálculos SQL:** getGlobalVolumeHistory(), getVolumeByRoutine(), getTotalVolumeInRange()
6. **Optimización:** getSessionDatesInRange() retorna List<Long> (no objetos) para heatmap

**Capacidad para nuevas queries:**
- ✅ Soporta query compleja para Rendimiento: `SELECT ... setOrder, weight, reps FROM exercise_sets JOIN workout_sessions WHERE exerciseName = ? ORDER BY date DESC, setOrder`
- ✅ Soporta query para catalog: `SELECT * FROM exercise_catalog WHERE muscleTag = ? ORDER BY name`

### 2.3 Capa de Lógica de Negocio (ViewModel + Utils) - ✅ REACTIVA

**ViewModels detectados:**
- `ExerciseTrackerViewModel` - Estado de sesión en entrenamiento
- `HomeViewModel` - Volumen, heatmap, rutinas activas
- `SettingsViewModel` - CSV import/export, temas

**Patrón MVVM:**
- SavedStateHandle para persistir estado tras rotación
- LiveData para observables reactivos
- Transformations.switchMap() para dependencias de estado
- Executor backgroundTask para I/O sin bloquear UI

**CsvImporter:**
- Método `importBodyWeightFromStream()` (líneas 213-253) - **REQUIERE MEJORA**
  - Parse simple sin validación de formato
  - Sin manejo de múltiples fechas/formatos
  - Sin validación de rango de peso (0-300 kg)
  - Sin detección de duplicados por rango de timestamps

**ExerciseUtils:** (presumible)
- Método `shouldUsePlacas()` - heurística por keywords
- Normalización de nombres

### 2.4 Capa de UI (Fragments + Activities) - ✅ FUNCIONAL

**Actividades principales:**
- `TrackerActivity` (620 líneas) - Sesión de entrenamiento
  - **Issue detectado:** saveWorkout() no limpia cache en edit path (línea ~380)
  - Maneja respaldo/caché con SharedPreferences ("workout_cache")
- `ExerciseSelectionActivity` (165 líneas) - Picker libre
  - **Issue detectado:** HashSet no preserva orden de selección
  - **Issue detectado:** No hay EditorAction listener en etSearch (Enter no oculta teclado)
  - Podría refactorizarse a Fragment + navGraph

**Fragmentos principales:**
- `SettingsFragment` (190 líneas) - CSV, split, tema
- `HomeFragment` - Heatmap, volumen semanal
- `HistoryFragment` - Listado de sesiones
- `StatsFragment` - Gráficas de volumen (candidato para Rendimiento subsection)

**Adaptadores:**
- `TrackerAdapter` (239 líneas) - Render cards de ejercicios
  - **Issue detectado:** Unidades no sincronizadas (prevWeight vs unit toggle)
  - Podría agregar soporte para ItemTouchHelper (drag-to-reorder)

**Navegación:**
- `nav_graph.xml` - 6 destinos (Home, Train, History, Stats, BodyWeight, Settings)
- Arquitectura: Bottom Navigation + Fragments (patrón estándar)

---

## 3. ANÁLISIS DE RIESGOS

### Matriz de Riesgos

| ID | Riesgo | Severidad | Probabilidad | Impacto | Mitigación |
|----|--------|-----------|--------------|---------|-----------|
| R1 | Migracion 9→10 falla con datos reales | ALTO | MEDIA | P0 bloqueante | Backup CSV antes, test con DB real en dev |
| R2 | Renombrado ejercicio rompe historico | MEDIO | BAJA | P1 datos | FK contraído a ejerciseName (normalizado), script de validación |
| R3 | Comparación Rendimiento inconsistente por units | MEDIO | MEDIA | P2 funcional | Validar unit antes comparar, test cases completos |
| R4 | Cambio visual excesivo rompe identidad | BAJO | MEDIA | P3 UX | Despliegue incremental, review manual fase por fase |

### Riesgos Críticos - NINGUNO

### Riesgos Altos

**R1 - Migración 9→10 Exercise Catalog**
- **Causa:** Room genera SQL de CREATE TABLE, puede fallar con DB corrupta o con schema inesperado
- **Impacto:** App no abre post-update
- **Mitigación:**
  1. Generar test con DB real (copiar `gym_database` actual)
  2. Backup CSV de historial (datos protegidos)
  3. Ejecutar migracion en dev phone primero
  4. Rollback a apk v9 si falla

---

## 4. MAPEO DE ARCHIVOS POR FASE

### Fase 0 - Baseline y Red de Seguridad
**Archivos a modificar:** 0 (documentación y setup)
- README.md (actualizar versión)
- validate_status.sh (crear script validación)
- PLAN_EJECUCION.md (ya existe)

**Duración:** 1-2 días

### Fase 1 - Modelo Exercise Catalog
**Archivos nuevos:**
- `app/src/main/java/com/josegarcia/appgym/data/entities/ExerciseCatalog.java` (60 líneas)
- `app/src/main/java/com/josegarcia/appgym/data/dao/ExerciseCatalogDao.java` (50 líneas)
- `app/src/main/java/com/josegarcia/appgym/data/database/MIGRATION_9_10.java` (dentro AppDatabase)

**Archivos existentes a modificar:**
- `AppDatabase.java` (línea 31: agregar ExerciseCatalog.class, línea 145: agregar MIGRATION_9_10, línea 38: agregar abstract method)
- `InitialData.java` (agregar catálogo base de 50+ ejercicios con muscleTag)

**Duración:** 3-4 días (incluye testing exhaustivo)

### Fase 2 - CRUD Ejercicios en Ajustes
**Archivos nuevos:**
- `app/src/main/java/com/josegarcia/appgym/ui/exercise_management/ExerciseManagementFragment.java` (180 líneas)
- `app/src/main/java/com/josegarcia/appgym/ui/exercise_management/ExerciseManagementViewModel.java` (120 líneas)
- `app/src/main/java/com/josegarcia/appgym/ui/exercise_management/ExerciseManagementAdapter.java` (150 líneas)
- `app/src/main/res/layout/fragment_exercise_management.xml` (100 líneas)
- `app/src/main/res/layout/item_exercise_card.xml` (80 líneas)

**Archivos existentes a modificar:**
- `SettingsFragment.java` (línea ~100: agregar botón "Gestionar Ejercicios")
- `nav_graph.xml` (agregar destino exerciseManagementFragment)
- `ExerciseCatalogDao.java` (agregar método update(), delete())

**Duración:** 4-5 días (UI nuevas, integraciones)

### Fase 3 - Correcciones Base
**Archivos a modificar:**
- `TrackerActivity.java` (línea ~380: fix cache cleanup en edit path)
- `CsvImporter.java` (líneas 213-253: mejorar importBodyWeight con validación)
- `ExerciseSelectionActivity.java` (línea 22: cambiar HashSet → LinkedHashSet, agregar EditorAction listener)
- `TrackerAdapter.java` (línea ~70: sincronizar unidades, considerar ItemTouchHelper)
- `ExerciseSelectionAdapter.java` (agregar soporte drag-to-reorder)

**Duración:** 3-4 días (fixes, testing)

### Fase 4 - Módulo Rendimiento
**Archivos nuevos:**
- `app/src/main/java/com/josegarcia/appgym/ui/performance/PerformanceFragment.java` (220 líneas)
- `app/src/main/java/com/josegarcia/appgym/ui/performance/PerformanceViewModel.java` (150 líneas)
- `app/src/main/java/com/josegarcia/appgym/ui/performance/PerformanceAdapter.java` (180 líneas)
- `app/src/main/java/com/josegarcia/appgym/data/entities/PerformanceComparison.java` (60 líneas)
- `app/src/main/res/layout/fragment_performance.xml` (100 líneas)
- `app/src/main/res/layout/item_performance_comparison.xml` (90 líneas)

**Archivos existentes a modificar:**
- `WorkoutDao.java` (agregar query: getLastWorkoutForExercise() retorna WorkoutSession con ExerciseSets)
- `StatsFragment.java` (opcionalmente: integrar como tab, o crear navegación lateral)
- `nav_graph.xml` (agregar destino performanceFragment si es standalone)

**Duración:** 5-6 días (lógica compleja, comparaciones, testing)

### Fase 5 - Material Design 3 Core
**Archivos a modificar:**
- `app/src/main/res/values/themes.xml` (actualizar colores a M3 palette)
- `app/src/main/res/values/colors.xml` (agregar colores M3: primary, secondary, tertiary, etc.)
- Layouts de módulos core:
  - `app/src/main/res/layout/activity_tracker.xml` (usar Material buttons, cards, etc.)
  - `app/src/main/res/layout/fragment_home.xml`
  - `app/src/main/res/layout/fragment_settings.xml`
  - `app/src/main/res/layout/fragment_performance.xml` (nuevo)

**Duración:** 3-4 días (principalmente CSS/Layout)

### Fase 6 - Material Design 3 Full + Estabilización
**Archivos a modificar:**
- Resto de layouts (History, Stats, BodyWeight, Setup)
- Test de regresión completa
- Creación de release candidate

**Duración:** 3-4 días

---

## 5. DEPENDENCIAS Y CAMBIOS SECUENCIALES

```mermaid
Fase 0 (Baseline 1-2d)
    ↓
Fase 1 (Catalog 3-4d) → BLOQUEANTE para Fase 2
    ↓
Fase 2 (CRUD 4-5d) → BLOQUEANTE para Fase 3
    ↓
Fase 3 (Fixes 3-4d) → PARALLELIZABLE con Fase 4
    ↓ + ↓
Fase 4 (Rendimiento 5-6d) + Fase 5 (M3 Core 3-4d)
    ↓
Fase 6 (M3 Full + Release 3-4d)
```

**Ruta Crítica:** Fase 0 → Fase 1 → Fase 2 → Fase 3+4 (27-31 días = ~6-8 semanas)

---

## 6. VERIFICACIÓN DE CAPACIDADES TÉCNICAS

### ✅ Soportado por arquitectura actual

1. **Migrations incrementales:** Room ya gestiona versiones
2. **Batch operations:** insertSets() ya implementado
3. **Background threading:** Executor estándar en AppDatabase
4. **Caché de sesión:** SharedPreferences "workout_cache" ya existe
5. **MVVM con state:** SavedStateHandle, LiveData, ViewModel
6. **CSV import/export:** CsvImporter ya existe
7. **Multi-theme support:** Tema selector en Settings

### ⚠️ Requiere mejora / nueva implementación

1. **Exercise Catalog:** Entidad + DAO nuevos
2. **Rendimiento comparison:** Query y lógica de comparación nuevas
3. **Material Design 3:** Tema + layouts
4. **Drag-to-reorder:** ItemTouchHelper callback nuevo

### ❌ No soportado / Fuera de scope

- Ninguno identificado (scope bien definido)

---

## 7. CHECKLIST DE VALIDACIÓN PRE-EJECUCIÓN

### Baseline (Antes de Fase 0)
- [ ] Git repo limpio (main branch, sin cambios uncommitted)
- [ ] Compilación exitosa (gradle clean build)
- [ ] APK generada y testeable
- [ ] Backup CSV exportado (historial + peso)
- [ ] Segundo teléfono disponible y sincronizado
- [ ] BD actual copiada para test de migracion

### Previo a Fase 1
- [ ] Tag pre-phase-0 creado
- [ ] Branch epic/rendimiento-crud-m3 iniciado
- [ ] PLAN_EJECUCION.md revisado
- [ ] Migracion 9→10 draft creada en dev

### Previo a Fase 2
- [ ] Tag post-phase-1 creado
- [ ] ExerciseCatalog poblado con 50+ ejercicios
- [ ] Test de migracion con DB real ejecutado y exitoso

### Antes de Release
- [ ] Regresion completa en ambos teléfonos
- [ ] Exporta/importa CSV operativo
- [ ] Setup inicial y cambio rutina validado
- [ ] CRUD ejercicios sin errores
- [ ] Rendimiento muestra datos correctos

---

## 8. NOTAS DE IMPLEMENTACIÓN

### ExerciseCatalog Entity (Fase 1)
```java
@Entity(tableName = "exercise_catalog")
public class ExerciseCatalog {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    @ColumnInfo(index = true)
    public String name;  // UNIQUE, normalized UPPER()
    
    public String defaultUnit;  // kg, lbs, placas
    public String muscleTag;    // Pecho, Espalda, Hombro, Biceps, Triceps, Pierna, Core
    public boolean isActive;    // soft delete
    public long createdAt;
    public long updatedAt;
}
```

### Performance Comparison Logic (Fase 4)
```sql
-- Query: Comparar ejercicio actual vs histórico
SELECT 
    current.setOrder,
    current.weight AS currentWeight,
    current.reps AS currentReps,
    current.unit AS currentUnit,
    previous.weight AS previousWeight,
    previous.reps AS previousReps,
    CASE 
        WHEN current.weight > previous.weight THEN 'MEJORA'
        WHEN current.weight < previous.weight THEN 'EMPEORA'
        ELSE 'MANTIENE'
    END as weightStatus
FROM exercise_sets current
JOIN exercise_sets previous ON current.setOrder = previous.setOrder
WHERE current.exerciseName = ? AND current.sessionId IN (SELECT MAX(id) FROM workout_sessions WHERE routineName = ?)
AND previous.sessionId IN (SELECT id FROM workout_sessions WHERE routineName = ? ORDER BY date DESC LIMIT 1)
ORDER BY current.setOrder
```

### Unit Handling
- Precedencia: ExerciseSet.unit > ExerciseSet target unit > ExerciseCatalog.defaultUnit > Constants.UNIT_KG
- Para comparación Rendimiento: normalizar a KG (si LBS/Placas, convertir primero)
- Mostrar tooltip si unidades varían entre sesiones

---

## 9. CONCLUSIÓN

El proyecto **está en muy buen estado** para ejecutar el plan propuesto. La arquitectura MVVM está sólida, las migraciones son estables, y no hay bloqueantes técnicos identificados.

**Recomendación:** Proceder a Fase 0 inmediatamente, con atención especial a:
1. Testing de migracion 9→10 antes de Fase 1
2. Validación del import/export CSV en Fase 3
3. Regresion manual en ambos teléfonos entre fases

**Próximo paso:** Crear branch de desarrollo y ejecutar Fase 0 (baseline + red de seguridad).


