# 📋 MEJORAS - AppGasto

> **INSTRUCCIÓN PARA IA:** Cuando se realice una mejora en el proyecto, agregar una nueva entrada en este archivo con el número de versión consecutivo, fecha y descripción. Siempre mantener este documento actualizado.

---

## Versión 1 — 2026-07-04

### 1.1 Fix: Declaración duplicada en AddEditViewModel.kt
- **Archivo:** `app/src/main/java/com/example/appgasto/ui/add/AddEditViewModel.kt`
- **Problema:** La propiedad `originalCreatedAt` estaba declarada 2 veces en la data class `AddEditUiState`, causando error de compilación "Conflicting declarations".
- **Solución:** Eliminada la declaración duplicada (línea 36) y el parámetro duplicado en el método `loadExpense()` (línea 67).

### 1.2 Fix: Referencia no resuelta LetterSpacing en Type.kt
- **Archivo:** `app/src/main/java/com/example/appgasto/ui/theme/Type.kt`
- **Problema:** Se usaba `LetterSpacing()` que no existe en Jetpack Compose, causando "Unresolved reference" en 7 lugares.
- **Solución:** Eliminado import de `androidx.compose.ui.text.style.LetterSpacing` y reemplazado `LetterSpacing(X.sp)` por `X.sp` directamente.

### 1.3 Fix: FilterChips desbordados en ListScreen.kt
- **Archivo:** `app/src/main/java/com/example/appgasto/ui/list/ListScreen.kt`
- **Problema:** Los chips de filtro de categoría estaban en un `Row` (sin wrap), causando que el último chip no se mostrara correctamente.
- **Solución:** Reemplazado `Row` por `FlowRow` para que los chips se envuelvan a la siguiente línea. Agregados imports `ExperimentalLayoutApi` y `FlowRow`.

### 1.4 Mejora: Rediseño del README.md
- **Archivo:** `README.md`
- **Problema:** El README era muy simple y no tenía elementos visuales atractivos.
- **Solución:** Rediseñado completamente con badges, tablas, emojis, estructura con columnas, syntax highlighting y footer con link al perfil.

---

## Versión 2 — 2026-07-04

### 2.1 Mejora: Tope de 4 decimales en campo de monto
- **Archivos:** `AddEditViewModel.kt`, `BudgetDialog.kt`
- **Problema:** El campo de monto no tenía límite de decimales y las comas no se convertían a puntos, causando errores al guardar.
- **Solución:** `updateAmount()` y `onValueChange` del BudgetDialog ahora:
  - Normalizan comas a puntos automáticamente
  - Rechazan múltiples puntos
  - Limitan la entrada a máximo 4 decimales
  - Validación silenciosa en tiempo real (sin mensajes de error)

---

## Versión 3 — 2026-07-04

### 3.1 Nuevo tema Matrix (Verde Neon Glow)
- **Archivos:** `ThemeMode.kt`, `Color.kt`, `Theme.kt`, `CategoryColors.kt`, `ThemeSettingsDialog.kt`, `strings.xml`, `MainActivity.kt`, `AppNavigation.kt`, `HomeScreen.kt`, `ListScreen.kt`, `StatsScreen.kt`, `AddEditScreen.kt`, `ExpenseItem.kt`, `CategorySelector.kt`, `SettingsScreen.kt`
- **Problema:** Solo existían temas claro y oscuro, sin opciones estilizadas.
- **Solución:** Agregado tema "Matrix" con paleta de verde neon sobre fondo negro:
  - Primary: `#00FF41` (verde neon), Secondary: `#00E676`, Tertiary: `#00FFFF` (cian)
  - Background: `#050A05` (negro puro), Surface: `#0A0F0A`
  - Gradientes de verde oscuro a verde neon
  - Colores de categorías en variantes neon para que destaquen sobre fondo oscuro
  - Icono `Terminal` en el selector de temas
  - El tema Matrix es siempre oscuro (`isDark = true`)
  - Se propagó `isMatrix` a través de la navegación y componentes para colores de categoría
- **Fix:** Agregado branch `MATRIX` faltante en `when` expression de `SettingsScreen.kt`

---

## Versión 4 — 2026-07-05

### 4.1 Limpieza de código muerto
- **Archivos:** `LocaleHelper.kt`, `AppModule.kt`, `ExpenseDao.kt`, `CategoryDao.kt`, `AppNavigation.kt`
- **Problema:** Varios archivos, métodos y funciones no se usaban en ningún lado, aumentando el mantenimiento sin beneficio.
- **Solución:**
  - Eliminado `LocaleHelper.kt` (código muerto, el cambio de idioma se hace inline en `SettingsViewModel`)
  - Eliminado `AppModule.kt` (módulo Hilt vacío sin `@Provides`)
  - Eliminados 4 métodos no usados de `ExpenseDao` (`getByCategory`, `getByCategoryAndDateRange`, `getDailyTotals`, `getExpensesSince`)
  - Eliminados 2 métodos no usados de `CategoryDao` (`update`, `delete`)
  - Eliminada función duplicada `Routes.editExpense()`, consolidada en `Routes.addExpense()`

---

## Versión 5 — 2026-07-08

### 5.1 Sección de notas desplegable (cerrada por defecto)
- **Archivos:** `AddEditScreen.kt`
- **Problema:** El campo de notas siempre estaba visible, ocupando espacio innecesario en el formulario.
- **Solución:** Convertido a sección colapsable con:
  - `AnimatedVisibility` para animar la expansión/colapso
  - `Row` clickeable con ícono `ExpandMore`/`ExpandLess` como indicador visual
  - Estado `noteExpanded` (default `false`) que controla la visibilidad
  - El campo de notas se oculta por defecto y se muestra al tocar "Notas"

### 5.2 Exportación CSV de gastos
- **Archivos:** `ExpenseCsvExporter.kt` (nuevo), `SettingsViewModel.kt`, `SettingsScreen.kt`, `strings.xml`, `strings.xml (en)`
- **Problema:** No existía forma de exportar gastos en formato CSV para análisis externo (Excel, Google Sheets, etc.).
- **Solución:**
  - Creado `ExpenseCsvExporter.kt` — Genera CSV con columnas: Fecha, Monto, Moneda, Monto_PEN, Categoría, Nota
  - Manejo de comillas y comas dentro de los campos para CSV válido
  - Botón "Exportar CSV" en sección de Respaldo de SettingsScreen
  - Nombre de archivo: `appgasto_YYYYMMDD_HHmmss.csv`
  - Strings localizadas en español e inglés

---

## Registro de Versiones

| Versión | Fecha | Cambios |
|:-------:|:-----:|:--------|
| 1 | 2026-07-04 | Fix declarations, LetterSpacing, FilterChips overflow, README redesign |
| 2 | 2026-07-04 | Tope de 4 decimales con soporte punto/coma |
| 3 | 2026-07-04 | Nuevo tema Matrix (verde neon glow) |
| 4 | 2026-07-05 | Limpieza de código muerto (LocaleHelper, AppModule, DAOs, Routes) |
| 5 | 2026-07-08 | Sección de notas desplegable + Exportación CSV de gastos |
