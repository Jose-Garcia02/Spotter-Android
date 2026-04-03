# Plan de Ejecucion - Spotter/AppGym

Plan incremental y reversible para implementar `Rendimiento`, mover la gestion de ejercicios a `Ajustes` con CRUD completo y avanzar gradualmente hacia Material Design 3, manteniendo la esencia actual de la app.

---

## 1) Objetivo

Entregar una version estable que incluya:

- Nuevo apartado `Rendimiento`.
- Comparacion del entreno actual vs ultimo entreno por ejercicio y por `setOrder`.
- Omision visual de series no comparables (si falta una serie, no se muestra).
- Gestion de ejercicios en `Ajustes` con CRUD completo.
- Etiquetas musculares de seleccion unica y base fija:
  - Pecho
  - Espalda
  - Hombro
  - Biceps
  - Triceps
  - Pierna
  - Core
- Migracion visual progresiva hacia Material Design 3:
  - Primero modulos principales.
  - Luego resto de la app.

---

## 2) Principios de trabajo

1. Seguridad primero: cambios pequenos, aislados y testeables.
2. Rollback rapido: tags antes y despues de cada fase.
3. Datos protegidos: migraciones Room versionadas y probadas.
4. Regresion constante: validar flujo completo al terminar cada fase.
5. Estilo incremental: evolucion visual sin rediseno agresivo.

---

## 3) Estrategia Git y control de version

### Ramas sugeridas

- `epic/rendimiento-crud-m3`
- `feat/fase-1-modelo-ejercicios`
- `feat/fase-2-crud-ajustes`
- `feat/fase-3-rendimiento`
- `feat/fase-4-m3-core`
- `feat/fase-5-m3-resto`
- `release/x.y.z`
- `hotfix/x.y.z+1`

### Tags sugeridos

- `pre-phase-0`, `post-phase-0`
- `pre-phase-1`, `post-phase-1`
- ...
- `vX.Y.Z-rc1`, `vX.Y.Z`

### Regla de commits

- Un solo objetivo por commit (datos, UI o estilo; no mezclar todo).
- Mensajes claros y accionables.

---

## 4) Fases de ejecucion detalladas

## Fase 0 - Baseline estable y red de seguridad

### Tareas

- Generar snapshot de version actual estable.
- Exportar CSV de historial y peso corporal.
- Preparar segundo telefono para pruebas.
- Definir checklist de regresion minima.

### Archivos foco

- `README.md`
- `app/build.gradle.kts`

### Criterios de aceptacion

- Existe backup funcional de datos.
- Hay tag de baseline estable.
- La version actual puede reinstalarse y funcionar.

### Pruebas

- Abrir app, entrenar, guardar, ver historial y exportar/importar.

---

## Fase 1 - Modelo de datos para catalogo de ejercicios

### Tareas

- Crear entidad de catalogo de ejercicios.
- Incluir campos recomendados:
  - id
  - name (unico normalizado)
  - defaultUnit (KG/LBS/PL)
  - muscleTag (seleccion unica)
  - isActive (para borrado logico)
  - createdAt / updatedAt
- Crear DAO con operaciones CRUD y busqueda.
- Crear migracion Room.
- Sembrar datos iniciales con etiquetas base.

### Archivos foco

- `app/src/main/java/com/josegarcia/appgym/data/database/AppDatabase.java`
- `app/src/main/java/com/josegarcia/appgym/data/entities/`
- `app/src/main/java/com/josegarcia/appgym/data/dao/`
- `app/src/main/java/com/josegarcia/appgym/data/database/InitialData.java`

### Criterios de aceptacion

- Catalogo persistido correctamente.
- `muscleTag` obligatorio y unico por ejercicio.
- No se rompen rutinas ni seleccion de ejercicios existentes.

### Pruebas

- Migracion de BD con datos reales.
- Insercion, edicion, borrado logico y duplicados.

---

## Fase 2 - CRUD de ejercicios dentro de Ajustes

### Tareas

- Crear seccion/pantalla "Gestion de ejercicios" en `Ajustes`.
- Implementar:
  - Crear ejercicio
  - Editar nombre
  - Editar unidad por defecto
  - Editar etiqueta muscular (unica)
  - Eliminar (recomendado logico)
- Mover "crear ejercicio" desde entrenamiento libre hacia Ajustes.

### Archivos foco

- `app/src/main/java/com/josegarcia/appgym/ui/SettingsFragment.java`
- `app/src/main/res/layout/fragment_settings.xml`
- `app/src/main/java/com/josegarcia/appgym/ui/tracker/ExerciseSelectionActivity.java`
- `app/src/main/res/navigation/nav_graph.xml`

### Criterios de aceptacion

- CRUD completo accesible desde Ajustes.
- Selector libre solo selecciona, no crea ejercicios.
- Integridad mantenida en historicos y rutinas.

### Pruebas

- Crear/editar/eliminar desde UI.
- Verificar uso inmediato en entrenamiento libre.
- Verificar que historial y estadisticas no fallen tras renombrar.

---

## Fase 3 - Correcciones base de entrenamiento y datos

### Tareas

- Limpiar respaldo/caché al finalizar correctamente.
- Corregir importacion de peso corporal (formatos y validaciones).
- Sincronizar unidades (cabecera, historico previo, persistencia).
- Mejoras de UX en entrenamiento libre:
  - Ocultar teclado al presionar Enter en buscador.
  - Mantener orden por seleccion.
  - Permitir reordenar ejercicios manualmente.

### Archivos foco

- `app/src/main/java/com/josegarcia/appgym/ui/tracker/TrackerActivity.java`
- `app/src/main/java/com/josegarcia/appgym/data/CsvImporter.java`
- `app/src/main/java/com/josegarcia/appgym/ui/tracker/ExerciseSelectionActivity.java`
- `app/src/main/java/com/josegarcia/appgym/ui/tracker/ExerciseSelectionAdapter.java`
- `app/src/main/java/com/josegarcia/appgym/ui/tracker/TrackerAdapter.java`

### Criterios de aceptacion

- Al finalizar, no reaparece respaldo previo.
- Importacion de peso funciona con formatos previstos.
- Unidades consistentes en toda la experiencia.
- Orden y reordenamiento de ejercicios estable.

### Pruebas

- Flujos de sesion nueva, sesion editada y recuperacion tras cierre.
- Import/export de peso con archivos validos e invalidos.

---

## Fase 4 - Modulo Rendimiento

### Tareas

- Crear pantalla/fragmento `Rendimiento`.
- Obtener entreno actual y ultimo entreno comparable por ejercicio.
- Comparar por `setOrder` (serie 1 vs serie 1, etc).
- Clasificar resultado por serie en:
  - Mejora
  - Mantenimiento
  - Empeoramiento
- Omitir visualmente series no comparables.
- UI vistosa pero limpia (chips/cards/indicadores de color).

### Reglas funcionales

- Si no existe entreno previo de un ejercicio -> no se compara.
- Si falta set equivalente en actual o previo -> se omite esa serie.
- Comparacion recomendada por prioridad:
  1. Reps (con peso igual)
  2. Peso (con reps igual)
  3. Volumen de serie (`peso * reps`) como desempate

### Archivos foco

- `app/src/main/java/com/josegarcia/appgym/data/dao/WorkoutDao.java`
- `app/src/main/java/com/josegarcia/appgym/ui/stats/StatsFragment.java` (o nuevo fragmento)
- `app/src/main/res/navigation/nav_graph.xml`
- `app/src/main/res/menu/bottom_nav_menu.xml`
- `app/src/main/res/layout/fragment_stats.xml` (o nuevo layout)

### Criterios de aceptacion

- Resultados correctos por `setOrder`.
- Series faltantes no se muestran.
- Sin errores con historial vacio o parcial.

### Pruebas

- Caso 3 vs 3 series (comparacion completa).
- Caso 4 vs 3 series (se omite serie faltante).
- Caso ejercicio nuevo (sin historico).

---

## Fase 5 - Material Design 3 incremental (modulos principales)

### Tareas

- Ajustar tema y componentes base a lineamientos M3.
- Prioridad de modulos:
  1. Entrenar
  2. Ajustes
  3. Rendimiento
- Mantener colores, personalidad y jerarquia actual.

### Archivos foco

- `app/src/main/res/values/themes.xml`
- `app/src/main/res/values/colors.xml`
- Layouts de modulos principales

### Criterios de aceptacion

- Mejora visual y de consistencia sin perder esencia de Spotter.
- Compatibilidad con temas oscuro/claro/AMOLED.
- Navegacion intacta.

### Pruebas

- Revisión visual en ambos telefonos y distintos tamanos de pantalla.
- Verificacion de contraste y accesibilidad.

---

## Fase 6 - Expansion M3 al resto + estabilizacion final

### Tareas

- Extender ajustes M3 a Historial, Progreso, Peso, Setup y wizard.
- Congelar features.
- Ejecutar regresion completa.
- Corregir bloqueantes.
- Publicar release candidata y luego version estable.

### Criterios de aceptacion

- Sin bugs criticos (P0/P1).
- Sin perdida de datos.
- Version validada en segundo telefono.

---

## 5) Riesgos y mitigaciones

1. Riesgo: migracion Room falla con datos reales.
   - Mitigacion: test de migracion + backup CSV obligatorio.
2. Riesgo: renombrado de ejercicios rompe historico.
   - Mitigacion: normalizacion + regla explicita de propagacion.
3. Riesgo: comparacion de rendimiento inconsistente por unidades.
   - Mitigacion: validar unidad antes de comparar o normalizar criterio.
4. Riesgo: cambios visuales excesivos rompen identidad.
   - Mitigacion: despliegue visual por etapas y revision manual.

---

## 6) Definicion de terminado por modulo

### Rendimiento

- Compara por `setOrder`.
- Omite series faltantes.
- Muestra estados claros (mejora/mantiene/empeora).

### Gestion de ejercicios

- CRUD completo en Ajustes.
- Etiqueta unica obligatoria.
- Unidad por defecto editable.

### Material Design 3

- Aplicado primero en modulos principales.
- Coherencia visual global progresiva sin perder esencia.

### Estabilidad

- Migraciones validadas.
- Import/export operativo.
- Regresion completa aprobada.

---

## 7) Checklist final de release

1. Crear `release/x.y.z`.
2. Ejecutar regresion completa en ambos telefonos.
3. Validar setup, entrenamiento, historial, rendimiento, ajustes y CSV.
4. Verificar migracion desde una instalacion anterior real.
5. Generar `vX.Y.Z-rc1`, corregir incidencias, y publicar `vX.Y.Z`.
6. Mantener rama `hotfix` activa post-release.

---

## 8) Criterios de QA manual (resumen rapido)

- Setup inicial y cambio de rutina.
- Entrenamiento libre completo con reordenamiento.
- Finalizar entreno y validar no restauracion incorrecta.
- Importar/exportar historial y peso corporal.
- CRUD ejercicios desde Ajustes.
- Rendimiento con escenarios completos e incompletos.
- Cambio de tema (Oscuro/Claro/AMOLED).

---

Este plan debe ejecutarse en ciclos cortos, con pruebas al cierre de cada fase y tag estable antes de avanzar a la siguiente.

