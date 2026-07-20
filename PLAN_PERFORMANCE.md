# Plan de Mejoras de Rendimiento — AppGasto

## Fase 1 — Alto Impacto, Bajo Esfuerzo (Quick Wins)

| # | Mejora | Archivos | Impacto |
|---|---|---|---|
| 1.1 | **Agregar índice en `createdAt`** — todas las queries de agregación y filtrado por fecha hacen full table scan | `Expense.kt` → `indices = [Index("categoryId"), Index("createdAt")]` | 🔥 Crítico |
| 1.2 | **Agregar `insertAll` batch en DAOs** — BackupManager hace N transacciones individuales en loop | `ExpenseDao.kt`, `CategoryDao.kt` | Alto |
| 1.3 | **Activar R8 en release** — `isMinifyEnabled = false` → `true`. Actualizar proguard | `app/build.gradle.kts`, `proguard-rules.pro` | Alto |
| 1.4 | **Generar Baseline Profiles** — ya existe el generador, ejecutarlo | Ejecutar `./gradlew :app:generateBaselineProfile` | Alto |

## Fase 2 — Alto Impacto, Esfuerzo Medio

| # | Mejora | Archivos | Detalle |
|---|---|---|---|
| 2.1 | **Cache en memoria de ExchangeRateRepository** — `getRateToPen()` consulta DB en cada lookup | `ExchangeRateRepository.kt` | 🔥 Crítico |
| 2.2 | **Offload queries a IO en HomeViewModel** — 4 queries de agregación en main thread | `HomeViewModel.kt` | 🔥 Crítico |
| 2.3 | **Combinar queries de agregación en una sola SQL** — 4 SUM separados + 1 GROUP BY | `ExpenseDao.kt`, `ExpenseRepository.kt`, `HomeViewModel.kt` | Alto |
| 2.4 | **Filtrado en SQL en ListViewModel** — cargar toda la tabla en memoria y filtrar en Kotlin | `ExpenseDao.kt`, `ExpenseRepository.kt`, `ListViewModel.kt` | Alto |

## Fase 3 — Recomposición Compuse (Esfuerzo Medio)

| # | Mejora | Archivos | Detalle |
|---|---|---|---|
| 3.1 | **`remember` en lambdas de LazyColumn** — onEdit/onDelete se recrean en cada item | `HomeScreen.kt`, `ListScreen.kt`, `ExpenseItem.kt` | Medio |
| 3.2 | **Agregar `key` en LazyColumn items** — headers sin key pierden estado de scroll | `HomeScreen.kt`, `ListScreen.kt` | Medio |
| 3.3 | **`remember` en formateo de strings** — Currency.format(), DateTimeFormatter sin memoizar | `ExpenseItem.kt`, `HomeScreen.kt`, `StatsScreen.kt`, `AddEditScreen.kt` | Medio |
| 3.4 | **`derivedStateOf` para valores calculados** — ratio, progressColor, percentage | `SettingsScreen.kt`, `StatsScreen.kt` | Medio |
| 3.5 | **Eliminar parámetro `isDark` no usado en SettingsScreen** | `SettingsScreen.kt` | Medio |
| 3.6 | **Reducir alcance de recomposición en pantallas monolíticas** | `SettingsScreen.kt`, `AddEditScreen.kt`, `HomeScreen.kt` | Medio |
| 3.7 | **Estabilizar `pageTitles`/`pageIcons` con `remember`** | `MainPagerScreen.kt` | Bajo |
| 3.8 | **`LocalIsHighContrast` → colores resueltos en CompositionLocal** | `ExpenseItem.kt`, `CategorySelector.kt` | Medio |

## Fase 4 — Estructurales (Esfuerzo Mayor)

| # | Mejora | Archivos | Detalle |
|---|---|---|---|
| 4.1 | **BackupManager: streaming en lugar de cargar todo en memoria** | `BackupManager.kt` | Medio |
| 4.2 | **BackupManager: batch inserts con insertAll** | `BackupManager.kt` | Medio |
| 4.3 | **ReceiptParser: optimizar detección de moneda** — 31 contains() en serie → una regex | `ReceiptParser.kt` | Bajo |
| 4.4 | **Mover corrutina de composable a ViewModel en MainActivity** | `MainActivity.kt` | Bajo |

## Fase 5 — Herramientas y Monitoreo

| # | Mejora | Detalle |
|---|---|---|
| 5.1 | **Configurar Firebase Performance Monitoring** | `build.gradle.kts` |
| 5.2 | **Agregar macrobenchmarks** para las 4 pestañas principales | `benchmark/` |
| 5.3 | **Habilitar Compose Compiler Metrics** | `app/build.gradle.kts` |
| 5.4 | **Agregar `@Stable`/`@Immutable` en data classes de UI state** | ViewModels |
