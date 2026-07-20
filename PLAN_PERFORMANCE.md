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

## Fase 3 — Recomposición Compose

| # | Mejora | Archivos | Estado |
|---|---|---|---|
| 3.1 | **`remember` en `CategoryColors.getById` + `LocalIsHighContrast`** en ExpenseItem | `ExpenseItem.kt:72-74` | ✅ |
| 3.2 | **`remember` en formateo de strings** — `localizedName()`, dateFormat, currencyFormat | `ExpenseItem.kt:108,121,129` | ✅ |
| 3.3 | **Extraer secciones de SettingsScreen a composables independientes** para aislar recomposición por sección | `SettingsScreen.kt` | ✅ |
| 3.4 | **`remember` en `CategoryColors.getById` + animaciones** en CategorySelector | `CategorySelector.kt:73-82` | ✅ |
| 3.5 | **Extraer `AmountCurrencyBox`** para aislar recomposición por tipeo en AddEditScreen | `AddEditScreen.kt` | ✅ |
| 3.6 | **`remember` en formateo de totales** en HomeScreen (monthTotal, currencyBreakdown) | `HomeScreen.kt:122,169` | ✅ |
| 3.7 | **Reusar `donutSlices`** en StatsScreen para evitar doble llamado a `CategoryColors.getById` | `StatsScreen.kt:185-188` | ✅ |

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
