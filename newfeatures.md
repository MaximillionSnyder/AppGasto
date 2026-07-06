# 🚀 NEWFEATURES - AppGasto

> **INSTRUCCIÓN PARA IA:** Cada nueva feature planeada se agrega como entrada con número de versión consecutivo, fecha y descripción. Cuando se implemente, cambiar `[ ]` por `[x]`. Siempre mantener este documento actualizado.

---

## Versión 1 — 2026-07-05

### 1.1 Receipt Scanning (Escanear recibos con cámara)

- **Estado:** `[ ]` Pendiente
- **Objetivo:** Permitir escanear un recibo físico con la cámara y auto-llenar los campos del formulario de gasto, evitando el ingreso manual.
- **Stack técnico:**
  - Google ML Kit Document Scanner API (`play-services-mlkit-document-scanner:16.0.0`) — UI de escaneo con detección de bordes, corrección de perspectiva, auto-capture
  - Google ML Kit Text Recognition v2 (`com.google.mlkit:text-recognition:16.0.0`) — OCR on-device para extraer texto del recibo
  - ReceiptParser propio — regex para extraer total, fecha, comercio y moneda
- **Qué extraer:**
  - `[ ]` **Total** del recibo (patrones: `TOTAL S/. 123.45`, `SUMA`, `IMPORTE`, `VUELTO`)
  - `[ ]` **Fecha** del recibo (formatos: dd/mm/aaaa, mm/dd/aaaa, yyyy-mm-dd, etc.)
  - `[ ]` **Comercio** (primeras líneas del texto OCR, se guarda como nota del gasto)
  - `[ ]` **Moneda** detectada (S/., $, €, R$, etc.)
- **Qué NO incluir (V1):**
  - `[x]` Items/líneas del recibo — descartado por complejidad de layouts variables
  - `[x]` Guardar imagen del recibo — descartado por impacto en almacenamiento, backup y migración DB
  - `[x]` Multi-moneda en totales — pospuesto a V2
- **Archivos a modificar/crear:**
  - `gradle/libs.versions.toml` — agregar versiones de dependencias ML Kit
  - `app/build.gradle.kts` — agregar dependencias
  - `data/ocr/ReceiptData.kt` — data class con resultado del OCR
  - `data/ocr/ReceiptParser.kt` — parser con regex para total, fecha, comercio, moneda
  - `data/ocr/ReceiptOcrService.kt` — interfaz del servicio
  - `data/ocr/MLKitReceiptOcrService.kt` — implementación con ML Kit Text Recognition
  - `di/OcrModule.kt` — módulo Hilt para proveer el servicio
  - `ui/add/AddEditScreen.kt` — botón "Escanear recibo" + lanzador Document Scanner
  - `ui/add/AddEditViewModel.kt` — manejar resultado del scan y auto-llenar state
  - `app/src/main/AndroidManifest.xml` — sin cambios (no requiere permiso CAMERA)

---

## Versión 2 — 2026-07-05

### 2.1 Multi-moneda con tasas de cambio

- **Estado:** `[x]` Implementado
- **Objetivo:** Soportar múltiples monedas en los gastos (USD, JPY, EUR, etc.) con conversión automática a PEN usando tasas de cambio en tiempo real.
- **API de tasas:** `fawazahmed0/currency-api` — gratis, sin API key, sin rate limits, 200+ monedas, actualizado diario, servido por CDN jsDelivr
- **Stack técnico:**
  - Retrofit + OkHttp para llamar a la API
  - Room para cachear tasas localmente (`ExchangeRateEntity`)
  - WorkManager para refrescar tasas cada 24h automáticamente
  - Botón manual "Actualizar tasas" en Settings
- **Modelo (`Expense.kt`):**
  - `[x]` Nuevo campo `currency: String` (código ISO 4217, default `"PEN"`)
  - `[x]` Nuevo campo `amountInPEN: Double` (convertido automáticamente al guardar)
  - `[x]` Nuevo campo `exchangeRateUsed: Double` (tasa usada al momento de guardar)
  - `[x]` **REGLA:** `amountInPEN` e `exchangeRateUsed` son **INMUTABLES** una vez guardados. Tasas futuras NO afectan gastos pasados.
  - `[x]` Migración Room v1→v2:
    ```sql
    ALTER TABLE expenses ADD COLUMN currency TEXT NOT NULL DEFAULT 'PEN';
    ALTER TABLE expenses ADD COLUMN amountInPEN REAL NOT NULL DEFAULT 0;
    ALTER TABLE expenses ADD COLUMN exchangeRateUsed REAL NOT NULL DEFAULT 1.0;
    ```
- **Conversión:**
  - `[x]` `ExchangeRateApi.kt` — Retrofit a `pen.json` del CDN
  - `[x]` `ExchangeRateEntity.kt` + `ExchangeRateDao.kt` — cache local
  - `[x]` `ExchangeRateRepository.kt` — lógica de refresh (24h) + fetch
  - `[x]` `CurrencyConverter.kt` — convierte cualquier monto a PEN usando rates cacheados
  - `[x]` `CurrencyModule.kt` — Hilt module para proveer servicios
  - `[x]` `ExchangeRateWorker.kt` — WorkManager para refrescar tasas cada 24h
  - **REGLA CRÍTICA:** Al guardar un gasto, se calcula `amountInPEN = amount / currentRate` (donde `currentRate` es "1 PEN = X [moneda]" devuelto por la API) y `exchangeRateUsed = currentRate`. Ambos campos se guardan y **NUNCA se recalculan** posteriormente. Tasas futuras NO afectan gastos históricos.
- **UI:**
  - `[x]` `AddEditScreen.kt` — dropdown selector de moneda al lado del monto + campo monto original
  - `[x]` `ExpenseItem.kt` — mostrar símbolo de moneda (ej: `$100.00`, `¥10,000`)
  - `[x]` `HomeScreen.kt` — total del mes convertido a PEN + desglose por moneda
  - `[x]` `StatsScreen.kt` — montos con moneda y total convertido
  - `[x]` `SettingsScreen.kt` — botón "Actualizar tasas" + timestamp última actualización
- **Queries DAO actualizadas (CRÍTICO):**
  - `[x]` `getTotalForPeriod()` — `SUM(amountInPEN)` en lugar de `SUM(amount)`
  - `[x]` `getTotalSince()` — `SUM(amountInPEN)` en lugar de `SUM(amount)`
  - `[x]` `getTotalByCategorySince()` — `SUM(amountInPEN)` en lugar de `SUM(amount)`
  - `[x]` Nueva query: `getTotalByCurrencySince()` para desglose por moneda
- **Presupuesto:**
  - `[x]` Compara contra `amountInPEN` de TODOS los gastos (incluye convertidos)
- **Migración de Backups:**
  - `[x]` `BackupData.version` se incrementa a `2`
  - `[x]` Al importar backup v1 (sin currency/amountInPEN): asignar currency="PEN", amountInPEN=amount, exchangeRateUsed=1.0
  - `[x]` Al importar backup v2 (con campos): validar y insertar directamente
  - `[x]` No exportar ExchangeRateEntity (se reconstruye con refresh)
- **Archivos a crear:**
  - `data/currency/ExchangeRateApi.kt`
  - `data/currency/ExchangeRateRepository.kt`
  - `data/currency/CurrencyConverter.kt`
  - `data/local/ExchangeRateEntity.kt`
  - `data/local/ExchangeRateDao.kt`
  - `di/CurrencyModule.kt`
- **Archivos a modificar:**
  - `data/local/Expense.kt` — +currency, +amountInPEN, +exchangeRateUsed
  - `data/local/ExpenseDao.kt` — queries SUM(amount) → SUM(amountInPEN)
  - `data/local/AppDatabase.kt` — +ExchangeRateEntity, +migration v1→v2, +exchangeRateDao(), exportSchema=true
  - `data/repository/ExpenseRepository.kt` — métodos de totales usan amountInPEN
  - `data/backup/BackupManager.kt` — exportar currency, amountInPEN, exchangeRateUsed + versionar backup a v2
  - `di/DatabaseModule.kt` — +ExchangeRateDao
  - `ui/add/AddEditScreen.kt` — dropdown selector de moneda
  - `ui/add/AddEditViewModel.kt` — +currency state, +conversión
  - `ui/home/HomeScreen.kt` — total mes en PEN + desglose por moneda
  - `ui/home/HomeViewModel.kt` — usa amountInPEN
  - `ui/stats/StatsScreen.kt` — montos con moneda
  - `ui/stats/StatsViewModel.kt` — usa amountInPEN (línea 74 hardcodeada)
  - `ui/components/ExpenseItem.kt` — símbolo de moneda
  - `ui/settings/SettingsScreen.kt` — botón "Actualizar tasas"
  - `ui/settings/SettingsViewModel.kt` — +ExchangeRateRepository
  - `notifications/BudgetWorker.kt` — compara contra amountInPEN
  - `widget/ExpenseWidget.kt` — usa amountInPEN
  - `app/build.gradle.kts` — +Retrofit +OkHttp
  - `gradle/libs.versions.toml` — +retrofit +okhttp
  - `app/src/main/AndroidManifest.xml` — +INTERNET
- **Archivos NO mencionados en el plan (AGREGADOS):**
  - `widget/ExpenseWidget.kt` — usa SUM(amount), debe cambiar a SUM(amountInPEN)
  - `notifications/BudgetWorker.kt` — compara contra SUM(amount), debe usar SUM(amountInPEN)
  - `ui/stats/StatsViewModel.kt` — suma it.amount en memoria, debe usar amountInPEN
  - `data/repository/ExpenseRepository.kt` — 6 métodos que retornan totales usando SUM(amount)

### 2.2 Receipt Scanning — Integración con multi-moneda

- **Estado:** `[ ]` Pendiente (depende de 2.1)
- **Objetivo:** El parser del receipt scanner detecta la moneda del recibo y auto-selecciona en el dropdown de moneda.
- **Cambios:**
  - El parser reconoce símbolos: `S/.` → PEN, `$` → USD, `€` → EUR, `¥` → JPY, `£` → GBP, `R$` → BRL
  - Auto-llena el campo moneda en AddEditScreen según lo detectado

---

## Registro de Versiones

| Versión | Fecha | Cambios |
|:-------:|:-----:|:--------|
| 1 | 2026-07-05 | Receipt Scanning con ML Kit Document Scanner + Text Recognition + Parser |
| 2 | 2026-07-05 | Multi-moneda con tasas de cambio + Receipt Scanning multi-moneda |
