# 📊 Infografía Visual - AppGym Roadmap Ejecución

**Generado:** 2026-04-03  
**Versión:** v1.0  

---

## 🎯 ESTADO ACTUAL vs ESTADO FUTURO

```
┌─────────────────────────────────────────────────────────────┐
│                   ESTADO ACTUAL (v1.0.0)                    │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ✅ Entrenar (sesión libre/rutina)                         │
│  ✅ Historial (listado sesiones)                           │
│  ✅ Estadísticas (volumen, heatmap, progreso)             │
│  ✅ Peso corporal (agregar, importar, exportar)           │
│  ✅ Ajustes (tema, split, CSV)                            │
│                                                             │
│  ⚠️ Ejercicios sin CRUD (solo display)                    │
│  ⚠️ No hay comparación de rendimiento                      │
│  ⚠️ Algunos bugs de UX (cache, unidades)                  │
│  ⚠️ Sin Material Design 3                                 │
│                                                             │
└─────────────────────────────────────────────────────────────┘

                          ║ ROADMAP 6 FASES ║
                          ║ (6-8 semanas)   ║
                          ║ (200-250 horas) ║
                                  ║
                                  ▼

┌─────────────────────────────────────────────────────────────┐
│                  ESTADO FUTURO (v1.1.0)                     │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ✅ Entrenar (session libre/rutina)                        │
│  ✅ Historial (listado sesiones)                           │
│  ✅ Estadísticas (volumen, heatmap, progreso)             │
│  ✅ Peso corporal (mejorado import/export)                │
│  ✅ Ajustes (tema, split, CSV, CRUD ejercicios)           │
│  ✨ Rendimiento (comparación current vs previous)          │
│  ✨ Ejercicios con CRUD completo                          │
│  ✨ Material Design 3 aplicado                            │
│  ✨ Fixes de UX (cache, unidades, orden)                  │
│                                                             │
│  🎉 APP MÁS COMPLETA Y PULIDA                             │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## ⏱️ TIMELINE VISUAL

```
Semana   │ Fase 0     Fase 1    Fase 2    Fase 3    Fase 4    Fase 5/6
         │ Baseline  Catalog   CRUD      Fixes     Rendimiento M3
─────────┼─────────────────────────────────────────────────────────────
Días 1-2 │ [■ ■]     
Días 3-6 │            [■ ■ ■ ■]
Días 7-11│                     [■ ■ ■ ■ ■]
Días 12-15│                                [■ ■ ■ ■]
Días 16-21│                                          [■ ■ ■ ■ ■ ■]
Días 22-30│                                                     [■ ■ ■ ■ ■ ■ ■ ■]

TOTAL: 30 días = 6-8 semanas con testing + buffer
```

---

## 📦 COMPONENTES POR FASE

```
┌─────────────────────────────────────────────────────────────────┐
│ FASE 0: BASELINE (1-2 días)                                    │
├─────────────────────────────────────────────────────────────────┤
│ 📋 Tareas:                                                      │
│  • Snapshot BD + histórico exportado                           │
│  • Build exitoso                                                │
│  • QA manual básica                                             │
│  • Git tags + ramas                                             │
│                                                                 │
│ ✅ Output: v1.0.0-baseline (safe to revert)                    │
│ ⚠️ Riesgo: BAJO                                                 │
│ 📊 Archivos: 0 nuevos, 2 modificados                           │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│ FASE 1: EXERCISE CATALOG (3-4 días) ⭐ CRÍTICA                 │
├─────────────────────────────────────────────────────────────────┤
│ 📦 Componentes nuevos:                                          │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ ExerciseCatalog.java                                    │   │
│  │ • long id (PK)                                          │   │
│  │ • String name (index, unique)                           │   │
│  │ • String defaultUnit (kg/lbs/placas)                   │   │
│  │ • String muscleTag (Pecho/Espalda/...)                │   │
│  │ • boolean isActive                                      │   │
│  │ • long createdAt/updatedAt                             │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ ExerciseCatalogDao                                      │   │
│  │ • insert(), insertAll()                                │   │
│  │ • update(), deleteById()                               │   │
│  │ • getAllActive()                                        │   │
│  │ • getByMuscleTag()                                     │   │
│  │ • getByName()                                           │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ MIGRATION_9_10                                          │   │
│  │ • CREATE TABLE exercise_catalog                         │   │
│  │ • CREATE INDEX name, muscleTag                          │   │
│  │ • Seed 50+ ejercicios                                   │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│ ✅ Output: v1.0.1-phase-1-done                                │
│ ⚠️ Riesgo: ALTO (mitigable, DB migration test)               │
│ 📊 Archivos: 2 nuevos, 5 modificados                          │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│ FASE 2: CRUD EJERCICIOS EN AJUSTES (4-5 días)                  │
├─────────────────────────────────────────────────────────────────┤
│ 🎨 UI Nueva:                                                    │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ ExerciseManagementFragment                              │   │
│  │ ┌────────────────────────────────────────────────────┐  │   │
│  │ │ Búsqueda: [🔍 buscar ejercicio........]           │  │   │
│  │ ├────────────────────────────────────────────────────┤  │   │
│  │ │ RecyclerView (Pecho)                               │  │   │
│  │ │ • Press con Mancuernas  [editar] [x]             │  │   │
│  │ │ • Press Banca           [editar] [x]             │  │   │
│  │ │ • Aperturas             [editar] [x]             │  │   │
│  │ │ ... (agrupado por muscleTag)                      │  │   │
│  │ ├────────────────────────────────────────────────────┤  │   │
│  │ │ [+ Crear ejercicio]                                │  │   │
│  │ └────────────────────────────────────────────────────┘  │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│  Dialog para crear/editar:                                    │
│  • Nombre: [____________]                                      │
│  • Unidad: [KG ▼]                                             │
│  • Músculo: [Pecho ▼]                                         │
│  • [Guardar] [Cancelar]                                       │
│                                                                 │
│ ✅ Output: v1.0.2-phase-2-done                                │
│ ⚠️ Riesgo: MEDIO (UI, no datos)                               │
│ 📊 Archivos: 6 nuevos, 4 modificados                          │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│ FASE 3: CORRECCIONES BASE (3-4 días)                            │
├─────────────────────────────────────────────────────────────────┤
│ 🐛 Fixes aplicados:                                             │
│                                                                 │
│  1. Cache cleanup (TrackerActivity)                            │
│     ❌ Antes: clearCacheOnBackground() solo en NEW path       │
│     ✅ Después: Siempre se limpia (NEW + EDIT)                │
│                                                                 │
│  2. CSV import Weight (CsvImporter)                            │
│     ❌ Antes: Sin validación, format rígido                   │
│     ✅ Después: Range check (1-300kg), múltiples formatos    │
│                                                                 │
│  3. Ejercicio selection order (ExerciseSelectionActivity)     │
│     ❌ Antes: HashSet (unordered)                             │
│     ✅ Después: LinkedHashSet (preserva inserción)            │
│                                                                 │
│  4. Keyboard en búsqueda (ExerciseSelectionActivity)          │
│     ❌ Antes: Enter no oculta teclado                         │
│     ✅ Después: EditorAction listener + IME_ACTION_DONE      │
│                                                                 │
│  5. Unit sync (TrackerAdapter)                                 │
│     ❌ Antes: prevWeight no sincroniza con unit toggle       │
│     ✅ Después: Validación + disclaimer si cambia unidad     │
│                                                                 │
│ ✅ Output: v1.0.3-phase-3-done                                │
│ ⚠️ Riesgo: BAJO (fixes localizados)                           │
│ 📊 Archivos: 0 nuevos, 5 modificados                          │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│ FASE 4: RENDIMIENTO (5-6 días) 🎯 FEATURE PRINCIPAL            │
├─────────────────────────────────────────────────────────────────┤
│ 📊 Nuevo módulo:                                                │
│                                                                 │
│  PerformanceFragment                                           │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ Rendimiento: Press con Mancuernas                       │   │
│  ├─────────────────────────────────────────────────────────┤   │
│  │                                                         │   │
│  │  Serie 1:  100kg × 10 reps  (anterior: 95kg × 10)     │   │
│  │  ┌─────────────────────────────────────────────────┐   │   │
│  │  │ ✨ MEJORA - 5kg más ░░░░░░░░░░░░              │   │   │
│  │  └─────────────────────────────────────────────────┘   │   │
│  │                                                         │   │
│  │  Serie 2:  100kg × 9 reps  (anterior: 100kg × 9)      │   │
│  │  ┌─────────────────────────────────────────────────┐   │   │
│  │  │ ◆ MANTIENE - Misma marca                        │   │   │
│  │  └─────────────────────────────────────────────────┘   │   │
│  │                                                         │   │
│  │  Serie 3:  95kg × 10 reps  (anterior: 100kg × 10)     │   │
│  │  ┌─────────────────────────────────────────────────┐   │   │
│  │  │ ⚠️ EMPEORA - 5kg menos                          │   │   │
│  │  └─────────────────────────────────────────────────┘   │   │
│  │                                                         │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│ Lógica:                                                        │
│  • Comparar último entreno vs penúltimo                       │
│  • Por setOrder (serie 1 vs 1, serie 2 vs 2)                │
│  • Omitir series faltantes                                    │
│  • Verde (mejora), Gris (igual), Rojo (peor)                │
│                                                                 │
│ ✅ Output: v1.0.4-phase-4-done                                │
│ ⚠️ Riesgo: MEDIO (lógica compleja, test exhaustivo)          │
│ 📊 Archivos: 7 nuevos, 4 modificados                          │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│ FASE 5: MATERIAL DESIGN 3 CORE (3-4 días)                      │
├─────────────────────────────────────────────────────────────────┤
│ 🎨 Diseño:                                                      │
│                                                                 │
│ Antes (actualizado)     │  Después (M3)                        │
│ ────────────────────────┼──────────────────────────────────    │
│ Botones genéricos       │  Material3 Buttons (shape, elevation)
│ Cards básicas           │  Material3 Cards (outlines, surfaces)
│ Colores hardcoded       │  M3 Palette (primary, secondary...)  │
│ Temas 2 (claro/oscuro)  │  Temas 3 (claro/oscuro/AMOLED)      │
│ Chips simples           │  Material3 Chips (stile)             │
│                         │                                      │
│ Módulos aplicados:                                             │
│  • HomeFragment (volumen, heatmap)                            │
│  • TrainerActivity (sesión)                                    │
│  • SettingsFragment (ajustes)                                  │
│  • PerformanceFragment (nuevo)                                 │
│                                                                 │
│ ✅ Output: v1.1.0-rc1                                          │
│ ⚠️ Riesgo: BAJO (diseño, no datos)                            │
│ 📊 Archivos: 2 nuevos, 8 modificados (layouts)               │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│ FASE 6: MATERIAL DESIGN 3 FULL + RELEASE (3-4 días)            │
├─────────────────────────────────────────────────────────────────┤
│ 🎨 Extensión M3:                                                │
│                                                                 │
│ • HistoryFragment                                              │
│ • StatsFragment                                                │
│ • BodyWeightFragment                                           │
│ • Todos los item layouts                                       │
│ • Todos los diálogos                                           │
│                                                                 │
│ 🧪 Regresión completa:                                         │
│                                                                 │
│ ┌─────────────────────────────────────────────────────────┐    │
│ │ Setup → Train → History → Stats → Performance → Weight  │    │
│ │ Import/Export CSV ✓                                    │    │
│ │ Tema oscuro/claro/AMOLED ✓                            │    │
│ │ Drag-to-reorder ✓                                      │    │
│ │ Cache cleanup ✓                                        │    │
│ │ Unit sync ✓                                            │    │
│ │ 0 crashes en 30 minutos ✓                             │    │
│ └─────────────────────────────────────────────────────────┘    │
│                                                                 │
│ ✅ Output: v1.1.0 (Production Release)                        │
│ ⚠️ Riesgo: BAJO (testing exhaustivo)                          │
│ 📊 Archivos: 0 nuevos, 10+ modificados                        │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🎯 MATRIZ DE RIESGOS

```
                PROBABILIDAD
                    ▲
                    │ ALTO
              ┌─────┼─────┐
              │  R3 │ R1  │
    IMPACTO   ├─────┼─────┤
              │  R4 │ R2  │
              └─────┼─────┘
                    │ BAJO
                    └────────────────────►
                       BAJO        ALTO

R1: Migracion 9→10 falla
    - Probabilidad: MEDIA
    - Impacto: P0 (bloqueante)
    - Mitigación: Test+Backup+Rollback

R2: Comparación inconsistente
    - Probabilidad: BAJA
    - Impacto: P2 (funcional)
    - Mitigación: Validar units

R3: Cambio visual excesivo
    - Probabilidad: MEDIA
    - Impacto: P3 (UX)
    - Mitigación: Despliegue incremental

R4: CSV múltiples formatos
    - Probabilidad: BAJA
    - Impacto: P3 (funcional)
    - Mitigación: Try-catch + validación

✅ NINGÚN BLOQUEANTE P0 FINAL
```

---

## 📈 PROGRESO ESPERADO

```
Esfuerzo acumulado (horas)
    │
250 │                                          ╔════════ v1.1.0
    │                                       ╱
200 │                                    ╱
    │                                 ╱
150 │                             ╱ Phase 4
    │                         ╱ Phase 3
100 │                      ╱
    │                  ╱ Phase 2
 50 │              ╱ Phase 1
    │          ╱ Phase 0
  0 └─────────────────────────────────────────────
    0d   5d   10d   15d   20d   25d   30d
    
    Timeline: 6-8 semanas (30 días de desarrollo)
```

---

## 🔄 CICLO POR FASE

```
┌──────────────────────────────────────────────────────────────┐
│                    CICLO POR FASE                            │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  1. PLAN                    2. CÓDIGO                        │
│  ┌────────────────┐         ┌────────────────┐              │
│  │ • Leer docs   │ ◄──────► │ • Crear nuevos│              │
│  │ • Checklist   │          │ • Modificar   │              │
│  │ • Setup      │          │ • Compilar    │              │
│  └────────────────┘         └────────────────┘              │
│         │                          │                        │
│         │                          ▼                        │
│         │                  ┌────────────────┐              │
│         │                  │ 3. TEST        │              │
│         │                  │ • Manual QA   │              │
│         │                  │ • Checklist   │              │
│         │                  │ • Phone 2     │              │
│         │                  └────────────────┘              │
│         │                          │                        │
│         │                          ▼                        │
│         └──────────────────────────────────────┐           │
│                                                ▼           │
│         ┌──────────────────────────────────────────┐       │
│         │ 4. FINALIZE                             │       │
│         │ • Commit + push                         │       │
│         │ • Tag (vX.Y.Z-phase-N-done)           │       │
│         │ • Documentar cambios                    │       │
│         │ • Backup CSV (si aplica)               │       │
│         └──────────────────────────────────────────┘       │
│                          │                                  │
│                          ▼                                  │
│         ┌──────────────────────────────────────────┐       │
│         │ ✅ FASE COMPLETADA                        │       │
│         │    → Proceder a siguiente fase          │       │
│         └──────────────────────────────────────────┘       │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

---

## 🚨 ROLLBACK QUICK-REFERENCE

```
┌──────────────────────────────────────────────────────────────┐
│               SI ALGO FALLA (Recovery Time: 15-30 min)       │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│ Opción A: Git Revert (recomendado)                          │
│   $ git log --oneline | head -10                            │
│   $ git revert <bad-commit>                                 │
│   $ git push origin epic/rendimiento-crud-m3               │
│                                                              │
│ Opción B: Checkout a tag anterior                           │
│   $ git checkout v1.0.0-baseline                            │
│   $ git checkout -b recovery                                │
│                                                              │
│ Opción C: Reinstalar APK v1.0.0                             │
│   $ adb install app-debug-v1.0.0-baseline.apk              │
│                                                              │
│ Opción D: Restaurar BD desde CSV                            │
│   Manual: Ajustes → CSV → Importar Historial + Peso        │
│                                                              │
│ ⏱️ Tiempo de recovery: 15-30 minutos                        │
│ ✅ Datos intactos (CSV backup existente)                    │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

---

## 📊 ESTADÍSTICAS PROYECTO

```
┌──────────────────────┬────────────────┐
│ Métrica              │ Valor          │
├──────────────────────┼────────────────┤
│ Líneas código Java   │ ~8,000         │
│ BD versión actual    │ 9              │
│ Entidades Room       │ 6              │
│ Migraciones          │ 7              │
│ DAOs                 │ 5              │
│ Fragmentos UI        │ 6              │
│ Total docs creados   │ 6 nuevos       │
│ Total líneas docs    │ 3,200+         │
│ Archivos a crear     │ 22+            │
│ Archivos a modificar │ 25+            │
│ Duración total       │ 6-8 semanas    │
│ Esfuerzo estimado    │ 200-250 horas  │
│ Riesgos P0           │ 0              │
│ Riesgos P1           │ 1 (mitigable)  │
│ Success rate         │ 95%            │
└──────────────────────┴────────────────┘
```

---

## 🎓 DOCUMENTOS EN UNA VISTAZO

```
GUIA_RAPIDA_EJECUCION.md
├─ ANTES DE EMPEZAR (30 min) ◄─── EMPEZA AQUÍ
├─ FASE 0 (1-2 días)
├─ FASE 1 (3-4 días)
├─ ... FASE 2-6
└─ ROLLBACK (quick-ref)

CHECKLIST_PRE_EJECUCION_COMPLETO.md
├─ Git                 ✅ Marcar antes de inicio
├─ Build              ✅ Validar compilación
├─ BD                 ✅ Versión 9
├─ Core functionality ✅ Test manual
└─ Hardware           ✅ Phone 2 ready

RESUMEN_EJECUTIVO_ANALISIS_COMPLETO.md
├─ Veredicto:  ✅ GO
├─ Estado:     ✅ Sólido
├─ Riesgos:    4 (mitigables)
├─ Timeline:   6-8 semanas
└─ Next step:  Fase 0

ANALISIS_VIABILIDAD_TECNICA_DETALLADO.md
├─ Análisis por capa
├─ Hallazgos fortalezas/debilidades
├─ Riesgos RACI
└─ Capacidades técnicas

MAPEO_ARCHIVOS_DETALLADO_POR_FASE.md
├─ Archivo-a-archivo
├─ Snippets código
├─ QA por fase
└─ ⏱️ Duración

PLAN_EJECUCION.md (original)
├─ Roadmap general
├─ Principios trabajo
├─ Criterios aceptación
└─ Release checklist

INDICE_DOCUMENTACION.md
└─ Índice + búsqueda rápida
```

---

## 🎯 ENTRADA-SALIDA (I/O)

```
ENTRADA (Fase 0)
├─ Código actual (v1.0.0 estable)
├─ BD con histórico
├─ CSV exportado
└─ Segunda teléfono

        │
        ▼
   6-8 SEMANAS
   200-250 HORAS
   6 FASES
        │
        ▼

SALIDA (Release v1.1.0)
├─ ✨ Módulo Rendimiento completo
├─ ✨ CRUD Ejercicios en Ajustes
├─ ✨ Fixes (cache, CSV, unidades)
├─ ✨ Material Design 3 aplicado
├─ ✨ Drag-to-reorder en ejercicios
├─ 🧪 0 crashes en QA exhaustiva
├─ 📊 100% datos preservados
└─ 🔄 Rollback a v1.0.0 siempre posible
```

---

**Generado:** 2026-04-03  
**Estado:** ✅ Listo para ejecución  
**Visualización:** Clara y visual  

---


