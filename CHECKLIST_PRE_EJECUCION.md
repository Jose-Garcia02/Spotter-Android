# Checklist Pre-Ejecución - AppGym

## 🎯 VERIFICACIÓN FINAL ANTES DE COMENZAR

### ✅ ESTADO DEL PROYECTO

```bash
# Ejecutar en la raíz del proyecto:
cd /home/josegarcia/AndroidStudioProjects/AppGym

# 1. Verificar Git
git status
# Debe mostrar: "On branch main" (limpio, sin cambios)

# 2. Verificar versión BD actual
grep "version = " app/src/main/java/com/josegarcia/appgym/data/database/AppDatabase.java
# Debe mostrar: version = 9

# 3. Verificar compilación
# En Android Studio:
# - Build > Clean Project
# - Build > Rebuild Project
# Debe terminar sin errores (warnings OK)

# 4. Verificar app en emulator/device
# - Instalar APK
# - Abrir app
# - Flujo básico: Setup → Entrenar → Finalizar → Historial
# Debe funcionar sin crashes
```

### 📋 REQUISITOS PREVIOS (Validados ✓)

- ✅ Git repository existente (main branch)
- ✅ BD Room versión 9 actual
- ✅ Compilación exitosa (Android Studio)
- ✅ App funcional en segundo teléfono (baseline)
- ✅ CSV backup de historial exportado
- ✅ CSV backup de peso exportado
- ✅ Documentos de plan listos:
  - ✅ PLAN_EJECUCION.md
  - ✅ ARQUITECTURA_Y_DOCUMENTACION.md
  - ✅ ANALISIS_VIABILIDAD_Y_PLAN_DETALLADO.md
  - ✅ GUIA_EJECUCION_RAPIDA.md
  - ✅ MAPEO_ARCHIVOS_POR_FASE.md (este archivo)

---

## 🚀 FASE 0: BASELINE ESTABLE

### Pre-Fase 0 Checklist (AHORA MISMO)

- [ ] **Git:** `git status` limpio (sin cambios)
- [ ] **Rama:** En `main` branch
- [ ] **Compilación:** `Build > Clean & Rebuild` sin errores
- [ ] **App funcional:** Abrir → Setup → Entrenar → Guardar → Historial OK
- [ ] **Documentación:** Los 5 documentos de plan están en el proyecto

### Fase 0 - Tareas Ejecutables

#### Tarea 1: Crear rama de desarrollo
```bash
cd /home/josegarcia/AndroidStudioProjects/AppGym
git checkout -b epic/rendimiento-crud-m3
git push origin epic/rendimiento-crud-m3
```
✅ Deliverable: Rama `epic/rendimiento-crud-m3` creada

#### Tarea 2: Crear tag pre-phase-0
```bash
git tag pre-phase-0
git push origin pre-phase-0
```
✅ Deliverable: Tag `pre-phase-0` en git

#### Tarea 3: Exportar CSV backup
Desde la app:
```
- Abrir app → Ajustes
- Botón "Exportar Historial" → Guardar como "BACKUP_HISTORIAL_PHASE0.csv"
- Botón "Exportar Peso" → Guardar como "BACKUP_PESO_PHASE0.csv"
```
✅ Deliverable: 2 archivos CSV en descargas

#### Tarea 4: Validar Regresión Mínima
```
Test en app:
- [ ] Setup inicial: crear o cambiar rutina → OK
- [ ] Entrenar: agregar 3 ejercicios, 2 sets → OK
- [ ] Finalizar: guardar sesión → OK
- [ ] Historial: ver sesión guardada → OK
- [ ] Editar: editar sesión anterior → OK
- [ ] Estadísticas: abrir stats, gráfico carga → OK
- [ ] Peso: agregar entrada de peso → OK
- [ ] Tema: cambiar a oscuro/claro → OK
- [ ] Ningún crash en cualquier pantalla → OK
```
✅ Deliverable: Checklist completado y documentado

#### Tarea 5: Documento final Fase 0
Crear archivo: `CHECKLIST_REGRESION_FASE0.md`
```markdown
# Checklist Regresión - Fase 0 (Baseline)

Fecha: [HOY]
Dispositivo: [Nombre/Model]
Versión BD: 9
Versión App: 1.0

## Resultados:
- [ ] Setup: PASS
- [ ] Entrenar: PASS
- [ ] Finalizar: PASS
- [ ] Historial: PASS
- [ ] Edición: PASS
- [ ] Estadísticas: PASS
- [ ] Peso: PASS
- [ ] Temas: PASS
- [ ] Crashes: NONE

## Notas:
[Agregar observaciones si aplica]

Firmado: [Tu nombre]
```
✅ Deliverable: Documento guardado

---

### ✅ Fase 0 COMPLETADA cuando:

- [ ] `git log --oneline | grep pre-phase-0` muestra el tag
- [ ] 2 archivos CSV de backup existen
- [ ] Checklist de regresión documentado
- [ ] Segunda teléfono (si aplica) también testea OK
- [ ] Ningún cambio de código aún

---

## 🔄 TRANSICIÓN A FASE 1

**Momento:** Después de Fase 0 PASS

```bash
# Asegurarse en rama epic/rendimiento-crud-m3
git checkout epic/rendimiento-crud-m3
git pull origin epic/rendimiento-crud-m3

# Crear rama feature
git checkout -b feat/fase-1-modelo-ejercicios

# Verificar estamos en rama correcta
git branch -v
# Debe mostrar: * feat/fase-1-modelo-ejercicios epic/rendimiento-crud-m3
```

---

## 📊 MATRIZ DE RIESGOS (Pre-Ejecución)

| # | Riesgo | Probabilidad | Impacto | Mitigación | Status |
|---|--------|--------------|---------|-----------|---------|
| 1 | BD v9→10 falla | Media | Alto | Backup CSV + test local | 🟢 LISTO |
| 2 | Compilación error Fase 1 | Baja | Medio | Maven clean rebuild | 🟢 OK |
| 3 | Conflictos git merge | Media | Medio | Commits pequeños + tags | 🟢 PLAN |
| 4 | Segundo teléfono incompatible | Baja | Alto | Testar Fase 0 primero | 🟢 VALIDADO |
| 5 | Pérdida de datos histórico | Muy Baja | Crítico | Backup CSV + snapshot BD | 🟢 OK |

---

## 🎬 COMANDO RÁPIDO: VALIDAR ESTADO

```bash
#!/bin/bash
echo "=== VALIDACIÓN PRE-FASE-1 ==="
cd /home/josegarcia/AndroidStudioProjects/AppGym

echo "✓ Rama actual:"
git branch | grep "^*"

echo "✓ Cambios pendientes:"
git status --short

echo "✓ Últimos commits:"
git log --oneline -5

echo "✓ Tags existentes:"
git tag | tail -5

echo "✓ Versión BD actual:"
grep "version = " app/src/main/java/com/josegarcia/appgym/data/database/AppDatabase.java | tail -1

echo "✓ Compilación status:"
# Solo si hay gradle wrapper
if [ -f "./gradlew" ]; then
  ./gradlew --version | grep "Gradle"
fi

echo "=== VALIDACIÓN COMPLETA ==="
```

Guarda este script como: `validate_status.sh`

---

## 🏁 SEÑALES DE "LISTO PARA FASE 1"

Debes ver TODOS estos ✅:

- ✅ `git log --oneline` muestra último commit es documental (sin cambios código)
- ✅ Tag `pre-phase-0` existe (`git tag | grep pre-phase-0`)
- ✅ Rama `epic/rendimiento-crud-m3` existe y es actual
- ✅ Rama `feat/fase-1-modelo-ejercicios` NO existe (crearemos)
- ✅ `AppDatabase.java` línea `version = 9`
- ✅ App compila sin errores (`gradle build`)
- ✅ App funciona en emulator (all basic flows pass)
- ✅ CSV backups existen
- ✅ Checklist de regresión completado

Si **TODOS** tienen ✅, entonces:

```bash
echo "🚀 LISTO PARA FASE 1"
```

---

## 📱 VALIDACIÓN EN SEGUNDO TELÉFONO (Opcional pero Recomendado)

Si tienes acceso a segundo device:

```
1. Instalar APK (versión actual, pre-phase-0)
2. Ejecutar mismo flujo de regresión
3. Verificar ningún crash
4. Anotar cualquier diferencia vs primer device
5. Marcar como "BASELINE OK" en documentación
```

---

## 📝 DOCUMENTO FINAL: ESTADO PRE-FASE-1

Crear archivo: `ESTADO_PROYECTO_PRE_FASE_1.md`

```markdown
# Estado del Proyecto - Pre Fase 1

**Fecha:** [HOY]
**Rama:** epic/rendimiento-crud-m3
**BD Versión:** 9
**Git Tag:** pre-phase-0

## Validaciones Completadas
- [x] Compilación exitosa
- [x] App funcional (todas pantallas)
- [x] CSV backup exportado
- [x] Segundo teléfono validado (si aplica)
- [x] Documentación completa

## Estadísticas Baseline
- Ejercicios con historial: [N]
- Sesiones totales: [N]
- Peso corporal logs: [N]
- Rutinas definidas: [N]

## Próximo Paso
Iniciar FASE 1: Modelo de Catalogo de Ejercicios

## Firma
- Desarrollador: [Tu nombre]
- Timestamp: [Ahora]
- Verificación: ✅ LISTO
```

---

## ❓ PREGUNTAS ANTES DE COMENZAR

**¿Compilación OK?**
```
→ Si NO: `git reset --hard HEAD`, luego `Build > Clean & Rebuild`
→ Si SÍ: Continuar
```

**¿App funciona?**
```
→ Si NO: Revert cambios, verificar SDK/gradle
→ Si SÍ: Continuar
```

**¿CSV backups hechos?**
```
→ Si NO: Hacerlo ahora desde app
→ Si SÍ: Continuar
```

**¿Tamaño BD?**
```
→ Si >100MB: Revisar si hay datos corruptos
→ Si <100MB: Normal
```

**¿Quieres proceder a Fase 1?**
```
→ Si SÍ: Ejecutar comando:
   git checkout -b feat/fase-1-modelo-ejercicios
   
→ Si NO: Esperar y revisar plan nuevamente
```

---

## 🎯 DECISIÓN FINAL

**Cuando estés 100% seguro de que todo está en orden, dile al asistente:**

```
"He verificado todo. COMIENZA FASE 1 ahora."
```

En ese momento:
1. ✅ Crearemos la rama feat/fase-1-modelo-ejercicios
2. ✅ Comenzaré a crear archivos (ExerciseCatalog.java, ExerciseCatalogDao.java)
3. ✅ Realizaré modificaciones (AppDatabase.java, InitialData.java)
4. ✅ Validaré compilación
5. ✅ Crearemos tag post-phase-1

---

## 📞 SOPORTE DURANTE EJECUCIÓN

Si en cualquier momento necesitas:
- ✅ Pausar → Dilo, haremos `git stash`
- ✅ Revertir → Haremos `git reset --hard HEAD~N`
- ✅ Revisar código → Mostraré diffs y explicaciones
- ✅ Testear → Compilaremos y validaremos errores
- ✅ Saltar a otra Fase → Posible si es compatible

---

**Estado Actual:** 🟢 LISTO PARA FASE 0
**Próximo Paso:** Confirmar cuando hayas validado el checklist

✨ **¡Análisis Completado! Espero tu confirmación para comenzar.** ✨

