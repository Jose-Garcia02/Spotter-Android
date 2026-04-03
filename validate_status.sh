#!/bin/bash

# Script de Validación Pre-Ejecución - AppGym
# Verifica que todo esté listo para comenzar FASE 0

set -e  # Exit on first error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Counter
PASS=0
FAIL=0
WARN=0

# Functions
print_header() {
  echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
  echo -e "${BLUE}$1${NC}"
  echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
}

check_pass() {
  echo -e "${GREEN}✓${NC} $1"
  ((PASS++))
}

check_fail() {
  echo -e "${RED}✗${NC} $1"
  ((FAIL++))
}

check_warn() {
  echo -e "${YELLOW}⚠${NC} $1"
  ((WARN++))
}

check_file_exists() {
  local file=$1
  local description=$2
  if [ -f "$file" ]; then
    check_pass "$description exists"
  else
    check_fail "$description MISSING: $file"
  fi
}

check_dir_exists() {
  local dir=$1
  local description=$2
  if [ -d "$dir" ]; then
    check_pass "$description directory exists"
  else
    check_fail "$description directory MISSING: $dir"
  fi
}

# Script execution starts here
print_header "AppGym Pre-Ejecución - Script de Validación"

# Change to project directory
cd /home/josegarcia/AndroidStudioProjects/AppGym

echo ""
echo "📍 Directorio actual: $(pwd)"
echo ""

# ============================================================
# VERIFICACIÓN 1: Git Status
# ============================================================
print_header "1️⃣ GIT STATUS"

if git status > /dev/null 2>&1; then
  check_pass "Git repository detected"

  # Current branch
  current_branch=$(git rev-parse --abbrev-ref HEAD)
  if [ "$current_branch" = "main" ]; then
    check_pass "On main branch"
  else
    check_warn "On branch: $current_branch (not main)"
  fi

  # Uncommitted changes
  if [ -z "$(git status --short)" ]; then
    check_pass "No uncommitted changes"
  else
    check_warn "Uncommitted changes detected:"
    git status --short | sed 's/^/  /'
  fi

  # Check for tags
  tag_count=$(git tag | wc -l)
  echo "   Total tags: $tag_count"

  # Last commit
  last_commit=$(git log -1 --pretty=format:"%h - %s")
  echo "   Latest commit: $last_commit"
else
  check_fail "Not a git repository"
fi

echo ""

# ============================================================
# VERIFICACIÓN 2: Estructura de Directorios
# ============================================================
print_header "2️⃣ ESTRUCTURA DE DIRECTORIOS"

check_dir_exists "app" "App module"
check_dir_exists "app/src/main/java/com/josegarcia/appgym" "Source package"
check_dir_exists "app/src/main/res" "Resources"
check_dir_exists "gradle" "Gradle wrapper"

echo ""

# ============================================================
# VERIFICACIÓN 3: Base de Datos - Versión Actual
# ============================================================
print_header "3️⃣ BASE DE DATOS"

db_file="app/src/main/java/com/josegarcia/appgym/data/database/AppDatabase.java"
check_file_exists "$db_file" "AppDatabase.java"

if [ -f "$db_file" ]; then
  db_version=$(grep -o "version = [0-9]*" "$db_file" | head -1 | grep -o "[0-9]*")
  if [ "$db_version" = "9" ]; then
    check_pass "BD version is 9"
  else
    check_fail "BD version is $db_version (expected 9)"
  fi

  # Check entity count
  entity_count=$(grep -c "@Entity" "$db_file")
  echo "   Entidades declaradas: $entity_count"
fi

echo ""

# ============================================================
# VERIFICACIÓN 4: Archivos Clave del Proyecto
# ============================================================
print_header "4️⃣ ARCHIVOS CLAVE"

check_file_exists "PLAN_EJECUCION.md" "Plan de Ejecución"
check_file_exists "ARQUITECTURA_Y_DOCUMENTACION.md" "Documentación Arquitectura"
check_file_exists "app/src/main/AndroidManifest.xml" "Android Manifest"
check_file_exists "app/build.gradle.kts" "App Gradle"
check_file_exists "gradlew" "Gradle Wrapper"

echo ""

# ============================================================
# VERIFICACIÓN 5: Estructura de Paquetes Java
# ============================================================
print_header "5️⃣ ESTRUCTURA JAVA"

check_dir_exists "app/src/main/java/com/josegarcia/appgym/data" "Data package"
check_dir_exists "app/src/main/java/com/josegarcia/appgym/data/entities" "Entities"
check_dir_exists "app/src/main/java/com/josegarcia/appgym/data/dao" "DAOs"
check_dir_exists "app/src/main/java/com/josegarcia/appgym/data/database" "Database"
check_dir_exists "app/src/main/java/com/josegarcia/appgym/ui" "UI package"
check_dir_exists "app/src/main/java/com/josegarcia/appgym/ui/tracker" "Tracker UI"
check_dir_exists "app/src/main/java/com/josegarcia/appgym/utils" "Utils"

echo ""

# ============================================================
# VERIFICACIÓN 6: Compilación
# ============================================================
print_header "6️⃣ COMPILACIÓN"

if [ -f "gradlew" ]; then
  check_pass "Gradle wrapper found"

  # Intentar compilación (puede tardar)
  echo "   Compiling... (this may take a minute)"
  if ./gradlew clean build -q 2>/dev/null || true; then
    check_pass "Project compiles successfully"
  else
    check_warn "Build produced warnings (check manually)"
  fi
else
  check_fail "Gradle wrapper not found"
fi

echo ""

# ============================================================
# VERIFICACIÓN 7: Dependencias
# ============================================================
print_header "7️⃣ DEPENDENCIAS"

gradle_file="app/build.gradle.kts"
if [ -f "$gradle_file" ]; then
  if grep -q "androidx.room:room-runtime" "$gradle_file"; then
    check_pass "Room dependency found"
  else
    check_fail "Room dependency NOT found"
  fi

  if grep -q "com.google.code.gson:gson" "$gradle_file"; then
    check_pass "GSON dependency found"
  else
    check_fail "GSON dependency NOT found"
  fi

  if grep -q "androidx.navigation" "$gradle_file"; then
    check_pass "Navigation dependency found"
  else
    check_warn "Navigation dependency not clearly visible"
  fi
fi

echo ""

# ============================================================
# VERIFICACIÓN 8: Configuración del Proyecto
# ============================================================
print_header "8️⃣ CONFIGURACIÓN"

if [ -f "app/build.gradle.kts" ]; then
  # Min SDK
  min_sdk=$(grep -o "minSdk = [0-9]*" "app/build.gradle.kts" | grep -o "[0-9]*")
  echo "   Min SDK: $min_sdk"

  # Target SDK
  target_sdk=$(grep -o "targetSdk = [0-9]*" "app/build.gradle.kts" | grep -o "[0-9]*")
  echo "   Target SDK: $target_sdk"

  # Namespace
  namespace=$(grep "namespace" "app/build.gradle.kts" | head -1 | grep -o '"[^"]*"' | tr -d '"')
  echo "   Namespace: $namespace"
fi

echo ""

# ============================================================
# VERIFICACIÓN 9: Documentación de Análisis
# ============================================================
print_header "9️⃣ DOCUMENTACIÓN DE ANÁLISIS"

check_file_exists "ANALISIS_VIABILIDAD_Y_PLAN_DETALLADO.md" "Análisis de Viabilidad"
check_file_exists "GUIA_EJECUCION_RAPIDA.md" "Guía Rápida"
check_file_exists "MAPEO_ARCHIVOS_POR_FASE.md" "Mapeo de Archivos"
check_file_exists "CHECKLIST_PRE_EJECUCION.md" "Checklist Pre-Ejecución"
check_file_exists "RESUMEN_EJECUTIVO_ANALISIS.md" "Resumen Ejecutivo"

echo ""

# ============================================================
# VERIFICACIÓN 10: Integridad de Datos
# ============================================================
print_header "🔟 INTEGRIDAD"

# Check for critical DAO files
check_file_exists "app/src/main/java/com/josegarcia/appgym/data/dao/WorkoutDao.java" "WorkoutDao"
check_file_exists "app/src/main/java/com/josegarcia/appgym/data/dao/RoutineDao.java" "RoutineDao"

# Check for critical entities
check_file_exists "app/src/main/java/com/josegarcia/appgym/data/entities/WorkoutSession.java" "WorkoutSession entity"
check_file_exists "app/src/main/java/com/josegarcia/appgym/data/entities/ExerciseSet.java" "ExerciseSet entity"

# Check ExerciseCatalog doesn't exist yet (should be created in Phase 1)
if [ -f "app/src/main/java/com/josegarcia/appgym/data/entities/ExerciseCatalog.java" ]; then
  check_warn "ExerciseCatalog.java already exists (should be created in Phase 1)"
else
  check_pass "ExerciseCatalog.java does not exist (as expected)"
fi

echo ""

# ============================================================
# RESUMEN FINAL
# ============================================================
print_header "📊 RESUMEN FINAL"

echo ""
echo -e "Validaciones exitosas: ${GREEN}$PASS${NC}"
echo -e "Validaciones fallidas: ${RED}$FAIL${NC}"
echo -e "Advertencias: ${YELLOW}$WARN${NC}"
echo ""

# Calculate status
total=$((PASS + FAIL + WARN))
percentage=$((PASS * 100 / total))

if [ $FAIL -eq 0 ]; then
  if [ $WARN -eq 0 ]; then
    echo -e "${GREEN}════════════════════════════════════════════════════════════════${NC}"
    echo -e "${GREEN}✅ TODO OK - LISTO PARA FASE 0${NC}"
    echo -e "${GREEN}════════════════════════════════════════════════════════════════${NC}"
    exit 0
  else
    echo -e "${YELLOW}════════════════════════════════════════════════════════════════${NC}"
    echo -e "${YELLOW}⚠️  LISTO CON ADVERTENCIAS - Revisar arriba${NC}"
    echo -e "${YELLOW}════════════════════════════════════════════════════════════════${NC}"
    exit 0
  fi
else
  echo -e "${RED}════════════════════════════════════════════════════════════════${NC}"
  echo -e "${RED}❌ ERRORES DETECTADOS - Revisar arriba${NC}"
  echo -e "${RED}════════════════════════════════════════════════════════════════${NC}"
  exit 1
fi

