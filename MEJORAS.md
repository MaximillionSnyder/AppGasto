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

## Registro de Versiones

| Versión | Fecha | Cambios |
|:-------:|:-----:|:--------|
| 1 | 2026-07-04 | Fix declarations, LetterSpacing, FilterChips overflow, README redesign |
