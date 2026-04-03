# Guía Rápida de Ejecución - Fases del Plan

## 🚀 ESTADO: LISTO PARA COMENZAR

---

## FASE 0: Baseline Estable (1-2 días)

### ✅ Tareas Completables Ahora Mismo:

```bash
# 1. Crear rama y tag
cd /home/josegarcia/AndroidStudioProjects/AppGym
git checkout -b epic/rendimiento-crud-m3
git tag pre-phase-0
git push origin pre-phase-0

# 2. Exportar CSV de respaldo (desde app)
# - Abrir app → Ajustes → Exportar Historial
# - Guardar como: "backup_historial_phase0.csv"
# - Abrir app → Ajustes → Exportar Peso Corporal
# - Guardar como: "backup_peso_phase0.csv"

# 3. Documentar checklist de regresión (archivo)
```

### 📋 Checklist de Regresión Mínima (llenar después de cada fase):

```markdown
## Checklist Regresión - Fase X

- [ ] Setup inicial: crear/cambiar rutina
- [ ] Entrenar: agregar ejercicios, completar sets
- [ ] Finalizar: guardar sesión
- [ ] Historial: ver sesiones guardadas
- [ ] Edición: editar sesión anterior
- [ ] Estadísticas: gráficos cargan correctamente
- [ ] Peso: importar/exportar CSV
- [ ] Rendimiento: (a partir de Fase 4) compara correctamente
- [ ] Temas: oscuro/claro cambia sin issues
- [ ] Ningún crash observado
```

### 🎯 Deliverable Fase 0:
- Tag `pre-phase-0` en git
- 2 archivos CSV de respaldo
- Documento checklist (create en workspace)

---

## FASE 1: Catalogo de Ejercicios (2-3 días)

### 📝 Checklist de Implementación:

```bash
# Crear rama feature
git checkout -b feat/fase-1-modelo-ejercicios

# PASO 1: Crear entidad ExerciseCatalog
# Archivo: app/src/main/java/com/josegarcia/appgym/data/entities/ExerciseCatalog.java
# Contenido: Clase con @Entity, fields: id, name, defaultUnit, muscleTag, isActive, timestamps
```

### 🗂️ Archivos a Crear:

1. **`ExerciseCatalog.java`** - Entity con 7 campos
2. **`ExerciseCatalogDao.java`** - DAO con CRUD + búsqueda
3. **`InitialData.java`** - Actualizar para poblar catalogo

### 🔄 Modificaciones:

1. **`AppDatabase.java`**
   - Agregar `ExerciseCatalog.class` a entidades
   - Cambiar version de 9 a 10
   - Agregar `MIGRATION_9_10` method

2. **Base de Datos Migración (SQL)**
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
   ```

### ✅ Criterios de Aceptación:
- BD migra sin errores (v9→10)
- 7 ejercicios base creados (uno por etiqueta muscular)
- Búsqueda por nombre funciona (TRIM/UPPER)
- HistorialesExistentes NO afectados

### 🏷️ Deliverable:
```bash
git add .
git commit -m "Feat: Fase 1 - Entidad ExerciseCatalog con migracion 9->10"
git tag post-phase-1
git push origin feat/fase-1-modelo-ejercicios post-phase-1
```

---

## FASE 2: CRUD Ejercicios en Ajustes (3-4 días)

### 📝 Checklist:

```bash
git checkout -b feat/fase-2-crud-ajustes
```

### 🗂️ Archivos a Crear:

1. **`ExerciseManagementFragment.java`** - UI principal
2. **`ExerciseManagementViewModel.java`** - Lógica
3. **`ExerciseManagementAdapter.java`** - RecyclerView
4. **`fragment_exercise_management.xml`** - Layout fragment
5. **`item_exercise_management.xml`** - Item layout

### 🔄 Modificaciones:

1. **`SettingsFragment.java`**
   - Agregar botón "Gestionar Ejercicios"
   - Navegación a ExerciseManagementFragment

2. **`ExerciseSelectionActivity.java`**
   - Remover creación de ejercicios
   - Solo seleccionar de catalogo

3. **`nav_graph.xml`**
   - Agregar fragment navigation

4. **`ExerciseCatalogDao.java`**
   - Métodos: `update()`, `softDelete()`, `getActiveExercises()`

### ✅ Criterios:
- CRUD funciona desde UI
- Ejercicios renombrados reflejan en historial
- Borrado lógico (isActive=false) preserva datos
- ExerciseSelectionActivity solo selecciona

### 🏷️ Deliverable:
```bash
git commit -m "Feat: Fase 2 - CRUD Ejercicios en Ajustes"
git tag post-phase-2
git push origin feat/fase-2-crud-ajustes post-phase-2
```

---

## FASE 3: Correcciones UX + Caché (2-3 días)

### 📝 Tareas:

1. **Limpiar caché al finalizar**
   - `TrackerActivity.saveWorkout()` → eliminar SharedPreferences

2. **Validar import CSV peso**
   - `CsvImporter.java` → aceptar dd/mm/yyyy y mm/dd/yyyy

3. **Sincronizar unidades**
   - Revisar `ExerciseUtils.shouldUsePlacas()`
   - Asegurar coherencia en toda la app

4. **Teclado: Ocultar al presionar Enter**
   - `ExerciseSelectionActivity` searchInput

5. **Reordenamiento manual**
   - `ExerciseSelectionAdapter` + `ItemTouchHelper`

### 🔄 Archivos a Modificar:
- `TrackerActivity.java` (caché)
- `ExerciseSelectionActivity.java` (teclado + drag-drop)
- `CsvImporter.java` (validación)
- `ExerciseSelectionAdapter.java` (drag-drop)

### ✅ Criterios:
- Caché se limpia correctamente
- Import CSV no falla con formatos válidos
- Drag-drop de ejercicios funciona
- Enter en búsqueda oculta teclado

### 🏷️ Deliverable:
```bash
git commit -m "Feat: Fase 3 - Correcciones UX + Caché + Validaciones"
git tag post-phase-3
git push origin feat/fase-3-correcciones post-phase-3
```

---

## FASE 4: Módulo Rendimiento (4-5 días)

### 📝 Tareas:

1. **Crear Fragment + ViewModel**
   - `PerformanceFragment.java`
   - `PerformanceViewModel.java`
   - `PerformanceAdapter.java` (opcional: CardView por serie)

2. **Crear DTO**
   - `PerformanceComparisonEntry.java`
   - Campos: setOrder, currentWeight, currentReps, prevWeight, prevReps, status

3. **Lógica de Comparación**
   ```
   - Si no existe set previo → OMITIR (no mostrar)
   - Si weight > prevWeight OR reps > prevReps → MEJORA
   - Si weight < prevWeight AND reps < prevReps → EMPEORA
   - Si no cambia → MANTIENE
   - Comparar por volumen (weight × reps) como desempate
   ```

4. **Nuevas Queries en WorkoutDao**
   - `getLastSessionForExercise(exerciseName)`
   - `getLastSetForExerciseAndOrder(exerciseName, setOrder)`

5. **UI Layout**
   - Spinner: seleccionar ejercicio
   - Cards/Chips: mostrar por setOrder
   - Colores según status (verde/gris/rojo)

6. **Navegación**
   - Agregar a `nav_graph.xml`
   - Agregar a `bottom_nav_menu.xml`

### 🗂️ Archivos a Crear:
- `ui/performance/PerformanceFragment.java`
- `ui/performance/PerformanceViewModel.java`
- `ui/performance/PerformanceAdapter.java`
- `data/entities/PerformanceComparisonEntry.java`
- `res/layout/fragment_performance.xml`
- `res/layout/item_performance_set.xml`

### 🔄 Modificaciones:
- `WorkoutDao.java` (nuevas queries)
- `nav_graph.xml` (agregar fragment)
- `res/menu/bottom_nav_menu.xml` (agregar item)

### ✅ Criterios:
- Comparación correcta por setOrder
- Series faltantes omitidas (null-check)
- Estados claros (colores distintos)
- Funciona con histórico vacío
- Casos testeados: 3vs3, 4vs3, sin historial

### 🏷️ Deliverable:
```bash
git commit -m "Feat: Fase 4 - Módulo Rendimiento con comparación por setOrder"
git tag post-phase-4
git push origin feat/fase-4-rendimiento post-phase-4
```

---

## FASE 5: Material Design 3 - Módulos Principales (2-3 días)

### 📝 Prioridad de Módulos:
1. Entrenar (TrackerActivity)
2. Ajustes (SettingsFragment)
3. Rendimiento (PerformanceFragment)

### 🔄 Cambios:
- Actualizar `res/values/colors.xml` a M3
- Actualizar `res/values/themes.xml` a M3
- Remplacer componentes genéricos por Material 3
- Asegurar temas oscuro/claro/AMOLED

### ✅ Criterios:
- Coherencia visual M3
- Sin pérdida de funcionalidad
- Contraste y accesibilidad OK
- Temas dinámicos funcionan

### 🏷️ Deliverable:
```bash
git commit -m "Feat: Fase 5 - Material Design 3 en módulos principales"
git tag post-phase-5
git push origin feat/fase-5-m3-core post-phase-5
```

---

## FASE 6: M3 Expansión + Release (3-4 días)

### 📝 Tareas:
1. Extender M3 a Historial, Stats, Peso, Setup
2. Regresión completa (todas las features)
3. Crear release candidate
4. Corregir bloqueantes
5. Publicar versión final

### 🔄 Modificaciones en cascada:
- Todos los layouts restantes
- Temas y colores consistentes
- Validación en segundo teléfono

### ✅ Criterios:
- SIN bugs P0/P1
- SIN pérdida de datos
- Segundo teléfono validado
- Release notes preparadas

### 🏷️ Deliverable:
```bash
# Release candidate
git checkout -b release/1.0.0
git commit -m "Release: v1.0.0-rc1"
git tag v1.0.0-rc1

# Después de QA, versión final
git commit -m "Release: v1.0.0 - Rendimiento, CRUD Ejercicios, Material Design 3"
git tag v1.0.0
git push origin release/1.0.0 v1.0.0-rc1 v1.0.0
```

---

## 📊 Timeline Recomendado

```
Semana 1:
- Lunes: Fase 0 (baseline + setup)
- Martes-Jueves: Fase 1 + Fase 2
- Viernes: QA Fase 1-2 + planeación Fase 3

Semana 2:
- Lunes-Martes: Fase 3 (UX + caché)
- Miércoles-Viernes: Fase 4 (Rendimiento) + Fase 5 inicio

Semana 3:
- Lunes-Miércoles: Fase 5 (M3)
- Jueves-Viernes: Fase 6 (expansión + release)

Post-Release:
- Monitoreo en segundo teléfono
- Hotfixes si necesario (rama hotfix/x.y.z+1)
```

---

## 🔐 Reglas de Oro

1. **Commits pequeños:** Un objetivo por commit (BD, UI, o lógica; no mezclar)
2. **Tags antes de cambios mayores:** `pre-phase-X` + `post-phase-X`
3. **Prueba en segundo teléfono:** Después de cada Fase completa
4. **Exporta CSV:** Antes y después de cambios BD
5. **Git status limpio:** Antes de cambiar de rama
6. **Review manual:** Antes de merge a main

---

## ✅ AHORA ESTOY LISTO

**Cuando le digas:**
1. **"Comienza Fase X"** → Ejecuto todas las tareas de esa Fase automáticamente
2. **"Revisa el código"** → Valido errors y best practices
3. **"Siguiente paso"** → Avanzo a la siguiente Fase según plan
4. **"Déjame revisar"** → Te muestro el estado y espero feedback

---

**¿Cuándo quieres que comience FASE 0?**

