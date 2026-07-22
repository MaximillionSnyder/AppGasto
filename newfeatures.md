# 🚀 NEWFEATURES - AppGasto

> **INSTRUCCIÓN PARA IA:** Cada nueva feature planeada se agrega como entrada con número de versión consecutivo, fecha y descripción. Cuando se implemente, cambiar `[ ]` por `[x]`. Siempre mantener este documento actualizado.

---

## Versión 1 — 2026-07-05

### 1.1 Receipt Scanning (Escanear recibos con cámara)

- **Estado:** `[x]` Implementado (2026-07-09)
- **Objetivo:** Permitir escanear un recibo físico con la cámara y auto-llenar los campos del formulario de gasto, evitando el ingreso manual.
- **Stack técnico:**
  - Google ML Kit Document Scanner API (`com.google.android.gms:play-services-mlkit-document-scanner:16.0.0`) — UI de escaneo con detección de bordes, corrección de perspectiva, auto-capture
    - **Paquete real:** `com.google.mlkit.vision.documentscanner.*` (NO `com.google.android.gms.mlkit.*`)
    - **Clase Options:** `GmsDocumentScannerOptions` (NO `GmsDocumentScanningOptions`)
  - Google ML Kit Text Recognition v2 (`com.google.mlkit:text-recognition:16.0.1`) — OCR on-device para extraer texto del recibo
    - **`TextRecognizerOptions`** está en `com.google.mlkit.vision.text.latin` (NO `com.google.mlkit.vision.text`)
  - ReceiptParser propio — regex para extraer total, fecha, comercio y moneda
- **Qué extraer:**
  - `[x]` **Total** del recibo (patrones: `TOTAL S/. 123.45`, `SUMA`, `IMPORTE`, `VUELTO`)
  - `[x]` **Fecha** del recibo (formatos: dd/mm/aaaa, mm/dd/aaaa, yyyy-mm-dd, etc.)
  - `[x]` **Comercio** (primeras líneas del texto OCR, se guarda como nota del gasto)
  - `[x]` **Moneda** detectada (S/., $, €, R$, etc.)
- **Qué NO incluir (V1):**
  - `[x]` Items/líneas del recibo — descartado por complejidad de layouts variables
  - `[x]` Guardar imagen del recibo — descartado por impacto en almacenamiento, backup y migración DB
  - `[x]` Multi-moneda en totales — pospuesto a V2 (ya en 2.1)
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
- **Comportamiento de auto-llenado:** El escaneo **sobrescribe** los campos detectados (monto, moneda, fecha, nota). Si la detección de un campo falla, se conserva el valor actual.

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

- **Estado:** `[x]` Implementado (2026-07-09, depende de 2.1)
- **Objetivo:** El parser del receipt scanner detecta la moneda del recibo y auto-selecciona en el dropdown de moneda.
- **Cambios:**
  - El parser reconoce símbolos: `S/.` → PEN, `$` → USD, `€` → EUR, `¥` → JPY, `£` → GBP, `R$` → BRL
  - Auto-llena el campo moneda en AddEditScreen según lo detectado

---

## Versión 3 — 2026-07-16

### 3.1 Accesibilidad (TalkBack, baja visión, fuentes ajustables)

- **Estado:** `[x]` Implementado (2026-07-19)
- **Objetivo:** Hacer la app usable para personas con discapacidad visual, motriz o cognitiva mediante content descriptions, semántica Compose, tamaño de fuente ajustable y modo alto contraste.

#### 3.1.1 Content descriptions en todos los iconos
- `[x]` Iconos en botones icon-only reemplazados por strings localizados: back (Settings, AddEdit), FAB agregar, editar/eliminar gasto, filtro, expandir/contraer (Home, AddEdit, Settings)
- `[x]` **Decisión (WCAG):** iconos decorativos junto a etiquetas de texto (filas de Ajustes, checkmarks de selección, iconos de categoría, empty states) mantienen `contentDescription = null` — describirlos duplicaría el anuncio de TalkBack
- `[x]` Strings nuevos en `strings.xml` (8 idiomas): `cd_back`, `cd_add_expense`, `cd_edit`, `cd_delete`, `cd_filter`, `cd_expand`, `cd_collapse`
- Archivos afectados: `ExpenseItem.kt`, `HomeScreen.kt`, `AddEditScreen.kt`, `ListScreen.kt`, `SettingsScreen.kt`, `MainPagerScreen.kt`

#### 3.1.2 Semántica para screen readers
- `[x]` `clickable(onClickLabel = ...)` en tarjetas de resumen del Home (mes/día/semana navegan a Estadísticas)
- `[x]` `semantics { heading() }` en títulos de sección (GENERAL, PERSONALIZACIÓN, DATOS, INFORMACIÓN) y encabezados de mes en Lista
- `[x]` `semantics { liveRegion = LiveRegionMode.Polite }` en indicadores de carga (Home, Lista) y SnackbarHost de Ajustes
- `[x]` **Decisión:** NO se aplicó `mergeDescendants` en `ExpenseItem` para no fusionar las acciones de los botones editar/eliminar en el nodo padre

#### 3.1.3 Tamaño de fuente ajustable
- `[x]` Nuevo enum `FontScale`: SMALL(0.85), NORMAL(1.0), LARGE(1.15), EXTRA_LARGE(1.3)
- `[x]` Nueva clave en `PreferencesRepository`: `font_scale` (String, default "NORMAL")
- `[x]` `SettingsScreen`: nueva fila "Tamaño de fuente" → `FontScaleDialog` con 4 opciones + preview ("Aa" escalado por opción)
- `[x]` **Decisión:** se aplica SOLO vía `CompositionLocalProvider(LocalDensity provides density.copy(fontScale))` — escalar también `Typography` duplicaría el factor (Typography usa `sp`, que la densidad ya escala)
- `[x]` Strings localizados para las 4 opciones en 8 idiomas

#### 3.1.4 Touch targets mínimos de 48dp
- `[x]` Botones editar/eliminar de `ExpenseItem` (28dp visuales) envueltos con `Modifier.minimumInteractiveComponentSize()` — área táctil de 48dp sin cambiar el tamaño visual

#### 3.1.5 Modo alto contraste (WCAG AAA)
- `[x]` Nuevo tema `HIGH_CONTRAST` en `ThemeMode` (basado en Light: fondo blanco puro, texto negro puro, acentos oscuros, outline visible)
- `[x]` Paleta de categorías `highContrastColors` en `CategoryColors.kt` (tonos oscuros sobre blanco)
- `[x]` `LocalIsHighContrast` CompositionLocal (en `Theme.kt`) para que los call sites de `CategoryColors.getById()` lean el modo sin enhebrar otro booleano por la navegación
- `[x]` Opción en `ThemeSettingsDialog` con ícono `Contrast`

### 3.2 Backup automático + recordatorios

- **Estado:** `[ ]` Pendiente
- **Objetivo:** Resguardar los datos automáticamente sin depender de que el usuario haga exportación manual. Incluye backup programado y recordatorios.
- **Stack técnico:** WorkManager (ya usado) + BackupManager (ya existente)

#### 3.2.1 Auto-backup programado
- `[ ]` `BackupWorker.kt` — `CoroutineWorker` que ejecuta `BackupManager.exportToJsonString()` cada 24h
- `[ ]` Guarda en `Documents/AppGasto/backups/appgasto_auto_YYYYMMDD_HHmmss.json`
- `[ ]` **CRÍTICO — Scoped Storage (`minSdk = 26`):** la escritura a `Documents/` depende de la API:
  - **API 26–28:** `File` API directa, requiere `WRITE_EXTERNAL_STORAGE` con `android:maxSdkVersion="28"` en el Manifest + check de permiso en runtime. Si el permiso es denegado, degradar a recordatorio de backup manual (3.2.3).
  - **API 29+:** `MediaStore.Files` con `RELATIVE_PATH = Documents/AppGasto/backups/` — sin permisos. Insertar entry, escribir vía `contentResolver.openOutputStream(uri)`, `IS_PENDING = 0` al terminar.
  - Lógica encapsulada en `AutoBackupStorage.kt` (nuevo): `fun writeBackup(json: String, fileName: String): Boolean`
- `[ ]` Refactor de `BackupManager`: extraer `exportToJsonString(): String` (lógica pura de serialización) del flujo SAF actual (`exportToJson(uri)`), para que el worker escriba sin UI. La exportación manual sigue usando SAF (`CreateDocument`).
- `[ ]` Limpieza automática: conservar últimos N backups (default 7) — query a MediaStore (API 29+) o listado de carpeta (API ≤28), borrar los más antiguos
- `[ ]` Schedule en `AppGastoApplication.onCreate()`: `PeriodicWorkRequest` 24h, unique `"auto_backup"` con `KEEP`
- `[ ]` Solo se ejecuta si el toggle está activado

#### 3.2.2 Toggle en Ajustes
- `[ ]` Nuevo `SettingsRow` con Switch: "Auto-backup diario"
- `[ ]` Nuevas claves DataStore: `auto_backup_enabled` (Boolean, default false), `max_backup_files` (Int, default 7)
- `[ ]` Fila informativa: "Se guarda en Documents/AppGasto/backups/"
- `[ ]` Strings localizados

#### 3.2.3 Recordatorio de backup manual
- `[ ]` Nueva clave DataStore: `last_manual_export_timestamp` (Long)
- `[ ]` `BackupReminderWorker.kt` — si pasaron 7+ días sin exportación manual, envía notificación
- `[ ]` Nuevo canal de notificación `backup_reminders` (separado del canal de presupuesto existente) para que el usuario pueda silenciarlos por separado
- `[ ]` Schedule 24h, se desactiva si auto-backup está activado
- `[ ]` Notificación con acción: "Hacer backup ahora"

### 3.3 Guardar imágenes de recibos escaneados

- **Estado:** `[ ]` Pendiente
- **Objetivo:** Al escanear un recibo, guardar la imagen JPEG en almacenamiento interno y asociarla al gasto para consulta futura.
- **Depende de:** 2.1 (multi-moneda) y 7.1 (receipt scanning) — ya implementadas

#### 3.3.1 Modelo de datos
- `[ ]` Nuevo campo en `Expense`: `receiptImagePath: String?` (nullable)
- `[ ]` Migración Room v2→v3:
  ```sql
  ALTER TABLE expenses ADD COLUMN receiptImagePath TEXT;
  ```
- `[ ]` `AppDatabase.version` → 3, agregar `MIGRATION_2_3`

#### 3.3.2 Almacenamiento de imágenes
- `[ ]` `ReceiptImageManager.kt` con:
  - `saveReceiptImage(sourceUri): String?` — copia JPEG a `files/receipts/{uuid}.jpg` y retorna el path. **CRÍTICO: el nombre es UUID, NO expenseId** — al escanear, el gasto aún no existe en DB (el ID se genera en el `insert()`), por lo que no se puede usar `expenseId` como nombre de archivo
  - Compresión al guardar: redimensionar a max 1920px (lado mayor), JPEG calidad 80, tope objetivo ~500KB por imagen
  - `getReceiptImage(path)` — carga Bitmap desde disco
  - `deleteReceiptImage(path)` — borra archivo
  - `deleteAllReceiptImages()` — borra carpeta completa
  - `getReceiptImageUri(path)` — FileProvider URI para compartir
- `[ ]` Proveer vía Hilt `@Singleton`

#### 3.3.3 Integración en flujo de escaneo
- `[ ]` `AddEditViewModel.handleScanResult()`: guardar imagen con `saveReceiptImage(sourceUri)` (nombre UUID) tras el scan y guardar el path en el state del formulario
- `[ ]` El path se persiste con el Expense al hacer `save()` (insert o update). Si es edición y no se re-escaneó, se mantiene el path existente
- `[ ]` Si OCR falla, la imagen igual se guarda (usuario puede rellenar manualmente)
- `[ ]` **Dependencia con 3.3.7:** si el usuario escanea pero NO guarda el gasto (cancela/cierra), la imagen queda huérfana — la borra el `ReceiptCleanupWorker` semanal. No borrar en `onCleared()` para no eliminar imágenes de gastos ya guardados

#### 3.3.4 Visualización en UI
- `[ ]` **Nueva dependencia Coil** (`io.coil-kt:coil-compose`) en `libs.versions.toml` + `app/build.gradle.kts` — cargar imágenes async con caché; cargar Bitmap directo en un `LazyColumn` causaría jank
- `[ ]` `ExpenseItem.kt`: thumbnail de 48dp si hay recibo, a la izquierda del ícono de categoría, con `rememberAsyncImagePainter` + placeholder
- `[ ]` `ReceiptViewerDialog.kt`: diálogo fullscreen con:
  - Imagen a máxima resolución + zoom con `Modifier.transformable`
  - Botón compartir (`FileProvider` + `Intent.ACTION_SEND`)
  - Botón eliminar recibo (borra archivo + limpia campo en DB)
- `[ ]` Thumbnail click → abre `ReceiptViewerDialog`
- `[ ]` `AddEditScreen.kt`: mostrar thumbnail existente + opción de re-escanear al editar

#### 3.3.5 Eliminación en cascada
- `[ ]` `ExpenseRepository.deleteExpense()`: leer `receiptImagePath` del gasto y borrar el archivo antes del registro
- `[ ]` `ExpenseRepository.deleteAllExpenses()`: borrar todas las imágenes
- `[ ]` `SettingsViewModel.clearAllData()`: hereda el comportamiento

#### 3.3.6 Backup con imágenes (formato ZIP)
- `[ ]` **CRÍTICO — NO usar Base64 en JSON:** infla el tamaño ~33% y parsear un JSON con varias imágenes en memoria (Gson) puede causar OOM en gama baja
- `[ ]` `BackupData.version` → 3 con nuevo formato: archivo **`.zip`** que contiene:
  - `backup.json` — mismo JSON que v2 (sin campos de imagen embebida; `receiptImagePath` se incluye como nombre de archivo relativo)
  - `receipts/{uuid}.jpg` — una entrada por imagen asociada a un gasto
- `[ ]` Exportar v3: streaming con `ZipOutputStream` (escribir JSON primero, luego cada imagen). Si "Incluir imágenes" está desactivado, exportar solo el JSON (formato v2-compatible)
- `[ ]` Importar: detectar formato por extensión/magic bytes — `.zip` (PK header) → flujo v3; JSON puro → flujo v1/v2 actual (sin cambios, `receiptImagePath = null`)
- `[ ]` Importar v3: streaming con `ZipInputStream` — parsear `backup.json`, extraer cada imagen a `files/receipts/` y mapear el path al gasto insertado
- `[ ]` Nueva opción en Ajustes: "Incluir imágenes en backup" (default: true)

#### 3.3.7 Limpieza de huérfanos
- `[ ]` `ReceiptCleanupWorker.kt`: WorkManager semanal que borra imágenes sin gasto asociado

#### 3.3.8 FileProvider
- `[ ]` `res/xml/file_paths.xml` — paths para `files/receipts/`
- `[ ]` `<provider>` en `AndroidManifest.xml` con `authorities="${applicationId}.fileprovider"`

### 3.4 Tests (nuevo — se tocan migraciones de DB y backup, alto riesgo de pérdida de datos)

- **Estado:** `[ ]` Pendiente
- `[ ]` `MigrationTest.kt` (androidTest, `MigrationTestHelper`) — verificar `MIGRATION_2_3`: crear DB v2 con datos, migrar a v3, validar esquema (`receiptImagePath` nullable) y que los datos se conservan
- `[ ]` `BackupV3Test.kt` — exportar gastos con imágenes a ZIP, importar en DB limpia, verificar round-trip (gastos + imágenes restauradas). Casos: backup sin imágenes, import v1/v2 (JSON) sigue funcionando
- `[ ]` `ReceiptImageManagerTest.kt` — save/get/delete de imagen, compresión (resultado ≤ 1920px), borrado de carpeta completa
- `[ ]` `AutoBackupStorageTest.kt` (o Robolectric) — escritura y limpieza de N backups mockeando ContentResolver

### 3.5 Onboarding de moneda base (primer inicio)

- **Estado:** `[x]` Implementado (2026-07-19)
- **Objetivo:** Obligar al usuario a elegir su moneda base la primera vez que abre la app, para que totales y conversiones no usen PEN por error.
- **Implementación:**
  - `[x]` Flag `onboardingCompleted` en `UserPreferences` + DataStore key `onboarding_completed`
  - `[x]` `PreferencesRepository.completeOnboarding(currency)` — guarda moneda base y marca onboarding
  - `[x]` `MainActivity` muestra `OnboardingScreen` si `!onboardingCompleted`, si no la app normal
  - `[x]` `ui/onboarding/OnboardingScreen.kt` — lista de 25 monedas + botón "Comenzar"
  - `[x]` Strings `welcome_title` / `welcome_description` / `welcome_confirm` en 8 idiomas
  - La moneda se puede cambiar después en Ajustes (sin cambios en ese flujo)

### Archivos a crear (estimado ~10 nuevos)
- `di/ReceiptModule.kt` — módulo Hilt para ReceiptImageManager
- `data/receipt/ReceiptImageManager.kt` — guardar/cargar/borrar imágenes de recibos
- `data/backup/AutoBackupStorage.kt` — escritura a Documents vía MediaStore (API 29+) / File API (API 26–28)
- `ui/components/ReceiptViewerDialog.kt` — visor fullscreen con zoom
- `ui/settings/FontScaleDialog.kt` — selector de tamaño de fuente
- `workers/BackupWorker.kt` — auto-backup programado
- `workers/BackupReminderWorker.kt` — recordatorio de backup manual
- `workers/ReceiptCleanupWorker.kt` — limpieza de imágenes huérfanas
- `res/xml/file_paths.xml` — FileProvider paths

### Archivos a modificar (estimado ~25)
- `data/local/Expense.kt` — +receiptImagePath
- `data/local/AppDatabase.kt` — version 3, MIGRATION_2_3
- `data/repository/ExpenseRepository.kt` — deleteExpense borra imagen
- `data/repository/PreferencesRepository.kt` — +font_scale, +auto_backup, +last_export
- `data/backup/BackupManager.kt` — extraer exportToJsonString(), BackupData v3 con ZIP (sin Base64)
- `domain/model/UserPreferences.kt` — +fontScale, +autoBackupEnabled
- `domain/model/ThemeMode.kt` — +HIGH_CONTRAST
- `ui/add/AddEditScreen.kt` — guardar imagen tras scan + thumbnail
- `ui/add/AddEditViewModel.kt` — handleScanResult guarda imagen (path UUID)
- `ui/components/ExpenseItem.kt` — thumbnail de recibo (Coil) + contentDescription
- `ui/components/CategorySelector.kt` — contentDescription
- `ui/home/HomeScreen.kt` — contentDescription + semantics
- `ui/list/ListScreen.kt` — contentDescription + semantics
- `ui/settings/SettingsScreen.kt` — auto-backup toggle, font scale, incluir imágenes
- `ui/settings/SettingsViewModel.kt` — nuevas propiedades
- `ui/settings/ThemeSettingsDialog.kt` — opción HIGH_CONTRAST + contentDescription
- `ui/settings/LanguageSettingsDialog.kt` — contentDescription
- `ui/theme/Color.kt` — paleta HIGH_CONTRAST
- `ui/theme/Theme.kt` — AppGastoTheme aplica fontScale (Typography + LocalDensity) + HIGH_CONTRAST
- `ui/theme/CategoryColors.kt` — colores HIGH_CONTRAST
- `ui/navigation/MainPagerScreen.kt` — contentDescription
- `AppGastoApplication.kt` — schedule BackupWorker + BackupReminderWorker + CleanupWorker + canal backup_reminders
- `app/src/main/AndroidManifest.xml` — FileProvider + WRITE_EXTERNAL_STORAGE (maxSdkVersion=28)
- `gradle/libs.versions.toml` — +coil-compose
- `app/build.gradle.kts` — +coil-compose
- Todos los `strings.xml` (8 idiomas) — ~30+ nuevas claves

---


---

## Versión 4 — 2026-07-22

### 4.1 Gráfico interactivo de presupuesto en Estadísticas

- **Estado:** `[x]` Implementado (2026-07-22)
- **Objetivo:** Gráfico interactivo del presupuesto mensual en Estadísticas (debajo del donut), con 4 estilos seleccionables desde Ajustes.
- **Archivos:** `BudgetChartStyle.kt`, `BudgetCharts.kt`, `BudgetChartStyleDialog.kt`, prefs, Stats/Settings VM+UI, strings 8 idiomas
- **Comportamiento:** visible si presupuesto activo; datos = mes actual; estilos CIRCULAR/BAR/SPEEDOMETER/COMPACT; tap alterna Gastado-Restante ↔ Disponible-%

## Registro de Versiones

| Versión | Fecha | Cambios |
|:-------:|:-----:|:--------|
| 1 | 2026-07-05 | Receipt Scanning con ML Kit Document Scanner + Text Recognition + Parser |
| 2 | 2026-07-05 | Multi-moneda con tasas de cambio + Receipt Scanning multi-moneda |
| 3 | 2026-07-16 | Accesibilidad (TalkBack, fontScale, alto contraste) + Auto-backup + Guardar recibos |
| 3-rev | 2026-07-19 | Revisión del plan V3: fix Scoped Storage en auto-backup (MediaStore/permiso legacy), imágenes con UUID (sin expenseId), backup v3 como ZIP (no Base64), Coil para thumbnails, canal backup_reminders, fontScale con LocalDensity, sección 3.4 Tests |
| 3.1 | 2026-07-19 | ✅ Accesibilidad implementada: content descriptions, semántica (heading/liveRegion/clickLabel), fontScale vía LocalDensity, touch targets 48dp, tema HIGH_CONTRAST con paleta de categorías propia + strings hardcodeados de Lista localizados (filter_by_month, this_month) |
| 4 | 2026-07-22 | ✅ Gráfico interactivo de presupuesto (4 estilos) en Stats + selector en Ajustes |
| 3.5 | 2026-07-19 | ✅ Onboarding de moneda base al primer inicio (`OnboardingScreen` + flag DataStore `onboarding_completed`) — evita totales en moneda incorrecta |
