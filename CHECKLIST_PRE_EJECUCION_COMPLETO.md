# Checklist Pre-Ejecución - AppGym

**Objetivo:** Verificar que todo está listo para comenzar Fase 0 sin problemas.

**Fecha de validación:** 2026-04-03

---

## ✅ INFRAESTRUCTURA GIT

- [ ] Repositorio Git inicializado y limpio
- [ ] Branch `main` sin cambios uncommitted
- [ ] `.gitignore` configurado (excluye build/, .gradle/, local.properties)
- [ ] Al menos 1 commit anterior para rollback

```bash
# Validar con:
cd /home/josegarcia/AndroidStudioProjects/AppGym
git status  # Debe estar limpio
git log --oneline | head -5  # Mostrar histórico
```

---

## ✅ COMPILACIÓN Y BUILD

- [ ] `./gradlew clean build` exitoso (sin errores críticos)
- [ ] APK generada: `app/build/outputs/apk/debug/app-debug.apk`
- [ ] No hay dependencias vencidas o conflictivas
- [ ] Target SDK ≥ 31 (para Material 3)
- [ ] Min SDK ≥ 26 (compatibilidad razonable)

```bash
# Validar con:
cd /home/josegarcia/AndroidStudioProjects/AppGym
./gradlew clean build
ls -lh app/build/outputs/apk/debug/app-debug.apk
```

---

## ✅ BASE DE DATOS

- [ ] DB versión actual = 9 (verificar `AppDatabase.java` línea 31)
- [ ] Migraciones 3→9 presentes (verificar línea 145)
- [ ] Tabla `exercise_catalog` NO existe aún (sera creada en Fase 1)
- [ ] Datos históricos intactos (verificar al abrir app)

```bash
# Validar con adb:
adb shell sqlite3 /data/data/com.josegarcia.appgym/databases/gym_database.db ".schema"
adb shell sqlite3 /data/data/com.josegarcia.appgym/databases/gym_database.db "SELECT COUNT(*) FROM workout_sessions;"
```

---

## ✅ FUNCIONALIDAD CORE

### App abre y funciona

- [ ] No crashes en arranque
- [ ] Bottom navigation visible y responde
- [ ] Fragmentos principales cargan (Home, Train, History, Stats, Settings)
- [ ] Temas (claro/oscuro/AMOLED) funcionan

**Prueba:**
```
1. Instalar APK debug en emulador o phone
2. Abrir app
3. Navegar Home → Train → History → Stats → Settings
4. Cambiar tema → verificar cambio
5. Cerrar y reabrir → recordar última pantalla
```

### Entrenar funciona

- [ ] Crear sesión nueva
- [ ] Seleccionar rutina
- [ ] Agregar ejercicios (lista predeterminada funciona)
- [ ] Agregar series (peso + reps)
- [ ] Guardar sesión sin errores
- [ ] No aparece respaldo después de cerrar app

**Prueba:**
```
1. Train → seleccionar rutina Upper A
2. Agregar 3 ejercicios, 3 series cada uno
3. Guardar sesión
4. Cerrar app completamente
5. Reabrir → NO debe mostrar dialog de sesión anterior
6. Historial → debe mostrar sesión guardada
```

### Historial funciona

- [ ] Listado muestra sesiones recientes
- [ ] Click en sesión → detalles correctos
- [ ] Exportar historial a CSV (Ajustes)
- [ ] Archivo CSV legible y válido

**Prueba:**
```
1. History → ver lista de sesiones
2. Click en una → detalles
3. Settings → CSV → Exportar Historial
4. Archivo guardado en Downloads
5. Abrir CSV con editor de texto → formato correcto
```

### Peso corporal funciona

- [ ] Agregar peso nuevo
- [ ] Exportar peso a CSV
- [ ] Importar CSV de peso (sin errores)

**Prueba:**
```
1. BodyWeight → agregar peso actual
2. Settings → CSV → Exportar Peso
3. Modificar CSV: agregar 5 líneas nuevas
4. Settings → CSV → Importar Peso
5. BodyWeight → verificar nuevos registros
```

---

## ✅ SEGUNDO TELÉFONO

- [ ] Disponible y funcional
- [ ] Mismo OS que phone principal (o compatible)
- [ ] APK base instalada y funcionando
- [ ] Datos sincronizados con phone principal (backup)
- [ ] Cuenta de USB debug habilitada (si Android Studio)

**Checklist:**
```
1. Conectar phone 2 a USB
2. adb devices → debe listar phone
3. Instalar app base
4. Abrir app → funciona
5. Crear sesión de prueba → guardar
6. Exportar CSV → guardar en cloud o USB
```

---

## ✅ DOCUMENTACIÓN

### Archivos presentes

- [ ] `PLAN_EJECUCION.md` (356 líneas, roadmap maestro)
- [ ] `ARQUITECTURA_Y_DOCUMENTACION.md` (517 líneas, referencia técnica)
- [ ] `ANALISIS_VIABILIDAD_TECNICA_DETALLADO.md` (recién creado)
- [ ] `MAPEO_ARCHIVOS_DETALLADO_POR_FASE.md` (recién creado)
- [ ] `README.md` (descripción general)

### Versionado

- [ ] `app/build.gradle.kts` tiene versionCode y versionName definidos
- [ ] Version será 1.0.0 (baseline estable)

---

## ✅ HERRAMIENTAS Y AMBIENTE

- [ ] JDK 11+ instalado (`java -version`)
- [ ] Android Studio abierto sin errores de Gradle
- [ ] Emulador o device real conectado (`adb devices`)
- [ ] `adb` disponible en PATH (para debug de BD)
- [ ] Git disponible en PATH (`git --version`)

```bash
# Validar todo:
java -version
adb version
git --version
./gradlew --version
```

---

## ✅ BACKUP Y RECUPERACIÓN

### Datos exportados

- [ ] CSV histórico exportado y guardado en nube (Google Drive/OneDrive)
- [ ] CSV peso exportado y guardado en nube
- [ ] APK base v1.0.0 guardada (para rollback rápido)
- [ ] Snapshot de BD actual (en dev para test de migraciones)

**Checklist:**
```
1. Exportar historial CSV → guardar en Downloads
2. Exportar peso CSV → guardar en Downloads
3. Descargar APK desde Android Studio
4. Guardar ambos CSV en nube (Google Drive o similar)
5. Crear tag git: git tag -a v1.0.0-baseline -m "Baseline estable pre-fases"
6. git push origin v1.0.0-baseline
```

### Rollback procedures probado

- [ ] Cambiar a commit anterior: `git checkout <commit>` ✓
- [ ] Reinstalar APK anterior: `adb install app-debug-old.apk` ✓
- [ ] Restaurar CSV a BD: `CsvImporter.importHistoryFromStream()` funciona ✓

---

## ✅ COMUNICACIÓN Y TRACKING

- [ ] Project tracker configurado (GitHub Issues, Trello, Asana, etc.)
- [ ] Cada fase con issue o tarjeta de tracking
- [ ] Criterios de aceptación claros por fase
- [ ] Plan de comunicación post-fase (resumen de cambios)

**Ejemplo issue Fase 0:**
```
[FASE 0] Baseline Estable y Red de Seguridad

Objetivo:
- Crear snapshot seguro
- Validar builds
- Documentar plan

Tareas:
- [ ] Tag git v1.0.0-baseline
- [ ] Exportar datos
- [ ] Validar build
- [ ] Documentar cambios

Criterios aceptación:
- Compilación exitosa
- APK funcional
- CSV exportables
```

---

## ✅ TESTING MANUAL PRE-FASE 0

### Sesión de prueba rápida (15 minutos)

```
PASO 1: Setup (2 min)
- Abrir app
- Seleccionar rutina (ej. Upper/Lower)

PASO 2: Entrenar (8 min)
- Agregar 3 ejercicios
- Agregar 3 series a cada uno
- Cambiar algunos pesos/reps
- Cambiar unidad (kg → lbs) en al menos 1

PASO 3: Guardar (2 min)
- Click "Finalizar"
- Sesión guardada sin errores

PASO 4: Verificar (3 min)
- Cerrar app completamente
- Reabrir
- Navegar Historial → debe mostrar sesión
- NO debe mostrar dialog de respaldo
```

**Resultado esperado:** ✅ Pasa sin errores

---

## CONDICIONES DE "LISTO PARA FASE 0"

**Todos los checkboxes arriba deben estar marcados.**

Si hay alguno sin marcar:
1. **BLOQUEANTE:** No compilar, BD corrupta, app crashea → FIX PRIMERO
2. **MODERADO:** Segundo phone no disponible → CONTINUAR CON UN PHONE
3. **BAJO:** Documentación incompleta → COMPLETAR DESPUÉS

---

## REGISTRO DE VALIDACIÓN

| Fecha | Validador | Estado | Notas |
|-------|-----------|--------|-------|
| 2026-04-03 | Asistente AI | ✅ GO | Análisis completado, listo para Fase 0 |
| | | | |

---

## PRÓXIMOS PASOS (Fase 0)

1. ✅ Completar todos los checkboxes arriba
2. ✅ Crear rama: `git checkout -b epic/rendimiento-crud-m3`
3. ✅ Crear tag: `git tag -a v1.0.0-baseline -m "Baseline antes de cambios"`
4. ✅ Proceder a **Fase 1: Modelo Exercise Catalog**

---

## CONTACTO Y ESCALONAMIENTO

Si hay problemas durante las fases:

1. Revisar documento `ANALISIS_VIABILIDAD_TECNICA_DETALLADO.md` sección "Riesgos"
2. Consultar `MAPEO_ARCHIVOS_DETALLADO_POR_FASE.md` para instrucciones específicas
3. Ejecutar rollback a commit anterior si es necesario
4. Reinstalar APK v1.0.0-baseline para recuperación rápida

---

**Estado actual:** ✅ **LISTO PARA INICIAR FASE 0**

**Siguiente validación:** Después de Fase 0 completa (1-2 días)


