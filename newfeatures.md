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

- **Estado:** `[ ]` Pendiente
- **Objetivo:** Soportar múltiples monedas en los gastos (USD, JPY, EUR, etc.) con conversión automática a PEN usando tasas de cambio en tiempo real.
- **API de tasas:** `fawazahmed0/currency-api` — gratis, sin API key, sin rate limits, 200+ monedas, actualizado diario, servido por CDN jsDelivr
- **Stack técnico:**
  - Retrofit + OkHttp para llamar a la API
  - Room para cachear tasas localmente (`ExchangeRateEntity`)
  - WorkManager para refrescar tasas cada 24h automáticamente
  - Botón manual "Actualizar tasas" en Settings
- **Modelo (`Expense.kt`):**
  - `[ ]` Nuevo campo `currency: String` (código ISO 4217, default `"PEN"`)
  - `[ ]` Nuevo campo `amountInPEN: Double` (convertido automáticamente al guardar)
  - `[ ]` Nuevo campo `exchangeRateUsed: Double` (tasa usada al momento de guardar)
  - `[ ]` **REGLA:** `amountInPEN` e `exchangeRateUsed` son **INMUTABLES** una vez guardados. Tasas futuras NO afectan gastos pasados.
  - `[ ]` Migración Room v1→v2:
    ```sql
    ALTER TABLE expenses ADD COLUMN currency TEXT NOT NULL DEFAULT 'PEN';
    ALTER TABLE expenses ADD COLUMN amountInPEN REAL NOT NULL DEFAULT 0;
    ALTER TABLE expenses ADD COLUMN exchangeRateUsed REAL NOT NULL DEFAULT 1.0;
    ```
- **Conversión:**
  - `[ ]` `ExchangeRateApi.kt` — Retrofit a `pen.json` del CDN
  - `[ ]` `ExchangeRateEntity.kt` + `ExchangeRateDao.kt` — cache local
  - `[ ]` `ExchangeRateRepository.kt` — lógica de refresh (24h) + fetch
  - `[ ]` `CurrencyConverter.kt` — convierte cualquier monto a PEN usando rates cacheados
  - `[ ]` `CurrencyModule.kt` — Hilt module para proveer servicios
  - **REGLA CRÍTICA:** Al guardar un gasto, se calcula `amountInPEN = amount * currentRate` y `exchangeRateUsed = currentRate`. Ambos campos se guardan y **NUNCA se recalculan** posteriormente. Tasas futuras NO afectan gastos históricos.
- **UI:**
  - `[ ]` `AddEditScreen.kt` — dropdown selector de moneda al lado del monto + campo monto original
  - `[ ]` `ExpenseItem.kt` — mostrar símbolo de moneda (ej: `$100.00`, `¥10,000`)
  - `[ ]` `HomeScreen.kt` — total del mes convertido a PEN + desglose por moneda
  - `[ ]` `StatsScreen.kt` — montos con moneda y total convertido
  - `[ ]` `SettingsScreen.kt` — botón "Actualizar tasas" + timestamp última actualización
- **Queries DAO actualizadas (CRÍTICO):**
  - `[ ]` `getTotalForPeriod()` — `SUM(amountInPEN)` en lugar de `SUM(amount)`
  - `[ ]` `getTotalSince()` — `SUM(amountInPEN)` en lugar de `SUM(amount)`
  - `[ ]` `getTotalByCategorySince()` — `SUM(amountInPEN)` en lugar de `SUM(amount)`
  - `[ ]` Nueva query: `getTotalByCurrencySince()` para desglose por moneda
- **Presupuesto:**
  - `[ ]` Compara contra `amountInPEN` de TODOS los gastos (incluye convertidos)
- **Migración de Backups:**
  - `[ ]` `BackupData.version` se incrementa a `2`
  - `[ ]` Al importar backup v1 (sin currency/amountInPEN): asignar currency="PEN", amountInPEN=amount, exchangeRateUsed=1.0
  - `[ ]` Al importar backup v2 (con campos): validar y insertar directamente
  - `[ ]` No exportar ExchangeRateEntity (se reconstruye con refresh)
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

## Versión 3 — 2026-07-05

### 3.1 Transiciones y animaciones entre pantallas

- **Estado:** `[ ]` Pendiente
- **Objetivo:** Mejorar la experiencia visual al navegar entre pantallas con animaciones fluidas.
- **Cambios:**
  - `[ ]` Agregar `enterTransition` / `exitTransition` en `NavHost` (fade + slide)
  - `[ ]` Animaciones de entrada/salida en `TopAppBar` al cambiar de pantalla
  - `[ ]` Animación de expansión/contracción en el panel de filtros de `ListScreen`
  - `[ ]` Animación de carga del `DonutChart` en `StatsScreen` (dibujar arcos progresivamente)
  - `[ ]` Transición suave del FAB en `HomeScreen` al hacer scroll

### 3.2 Confirmación antes de eliminar gasto

- **Estado:** `[ ]` Pendiente
- **Objetivo:** Evitar eliminaciones accidentales mostrando un diálogo de confirmación.
- **Cambios:**
  - `[ ]` `ExpenseItem.kt` — al presionar el ícono de eliminar, mostrar `AlertDialog` con:
    - Título: "Eliminar gasto"
    - Mensaje: "¿Estás seguro de que deseas eliminar este gasto?"
    - Botones: "Cancelar" (dismiss) + "Eliminar" (confirmar, color rojo)
  - `[ ]` Animación de eliminación del item de la lista (slide out + fade)

### 3.3 Pull-to-refresh en listas

- **Estado:** `[ ]` Pendiente
- **Objetivo:** Permitir actualizar los datos con un gesto de deslizar hacia abajo.
- **Cambios:**
  - `[ ]` `HomeScreen.kt` — pull-to-refresh para recalcular totales
  - `[ ]` `ListScreen.kt` — pull-to-refresh para recargar la lista de gastos
  - `[ ]` `StatsScreen.kt` — pull-to-refresh para recalcular estadísticas
  - `[ ]` Indicador de progreso circular con animación Material 3

### 3.4 Empty states mejorados

- **Estado:** `[ ]` Pendiente
- **Objetivo:** Mostrar estados vacíos más informativos y atractivos con acciones sugeridas.
- **Cambios:**
  - `[ ]` `HomeScreen.kt` — empty state con ilustración + botón "Agregar primer gasto"
  - `[ ]` `ListScreen.kt` — empty state diferenciado: sin gastos vs. sin resultados de filtro
  - `[ ]` `StatsScreen.kt` — empty state cuando no hay datos para el período seleccionado
  - `[ ]` Agregar íconos ilustrativos más grandes y coloridos en cada empty state

### 3.5 Feedback visual mejorado

- **Estado:** `[ ]` Pendiente
- **Objetivo:** Dar retroalimentación clara al usuario en cada acción.
- **Cambios:**
  - `[ ]` `AddEditScreen.kt` — animación de éxito al guardar (checkmark animado + color verde temporal)
  - `[ ]` `ExpenseItem.kt` — feedback visual al deslizar para eliminar (fondo rojo渐进)
  - `[ ]` `SettingsScreen.kt` — snackackbar mejorado con icono al exportar/importar backup
  - `[ ]` Agregar `HapticFeedback` en acciones clave (guardar, eliminar)
  - `[ ]` `BudgetWorker.kt` — notificación mejorada con progress bar al enviar alerta de presupuesto

### 3.6 Mejoras de accesibilidad

- **Estado:** `[ ]` Pendiente
- **Objetivo:** Hacer la app usable por personas con discapacidades visuales y motoras.
- **Cambios:**
  - `[ ]` Agregar `contentDescription` a todos los `Icon` composables que no lo tengan
  - `[ ]` Tamaño mínimo de toque 48dp en todos los botones e íconos interactivos
  - `[ ]` `ExpenseItem.kt` — expandir área de toque del ícono de editar/eliminar
  - `[ ]` `CategorySelector.kt` — asegurar contraste mínimo 4.5:1 en todos los chips
  - `[ ]` Navegación completa por TalkBack (orden lógico de lectura)
  - `[ ]` `OutlinedTextField` — labels persistentes y hints descriptivos

### 3.7 Navegación mejorada (Bottom Navigation Bar)

- **Estado:** `[ ]` Pendiente
- **Objetivo:** Mejorar la navegación principal con una barra inferior fija.
- **Cambios:**
  - `[ ]` `AppNavigation.kt` — agregar `NavigationBar` con 4 destinos:
    - Home (ícono Home, ruta principal)
    - Lista (ícono List, historial)
    - Estadísticas (ícono BarChart, stats)
    - Ajustes (ícono Settings, configuración)
  - `[ ]` `HomeScreen.kt` — remover `TopAppBar` acciones de navegación (se mueven a bottom nav)
  - `[ ]` Animación de selección con `NavigationBarItem` indicator
  - `[ ]` Ocultar/mostrar bottom nav con animación al hacer scroll (hide on scroll)
  - `[ ]` `AddEditScreen` — bottom nav oculto (es una pantalla temporal)

### 3.8 Formularios mejorados

- **Estado:** `[ ]` Pendiente
- **Objetivo:** Mejorar la experiencia de ingreso de datos en el formulario de gastos.
- **Cambios:**
  - `[ ]` `AddEditScreen.kt` — validación en tiempo real con mensajes de error inline (debajo del campo)
  - `[ ]` Autocompletado de notas con gastos anteriores (sugerencias basadas en historial)
  - `[ ]` `CategorySelector.kt` — recordar última categoría seleccionada
  - `[ ]` Animación de teclado al abrir/cerrar el campo de monto
  - `[ ]` Formateo automático del monto con separador de miles al perder foco
  - `[ ]` Botón de limpiar (X) en el campo de nota cuando tiene texto

### 3.9 Mejoras en el gráfico DonutChart

- **Estado:** `[ ]` Pendiente
- **Objetivo:** Hacer el gráfico de dona más interactivo e informativo.
- **Cambios:**
  - `[ ]` Animación de entrada: arcos se dibujan progresivamente desde 0°
  - `[ ]` Interacción táctil: tocar un segmento resalta la categoría y muestra tooltip
  - `[ ]` Leyenda interactiva: tocar una categoría en la lista resalta el segmento del gráfico
  - `[ ]` Centro del donut muestra el total con animación de conteo (count-up)
  - `[ ]` Soporte para tema Matrix con colores neon en el gráfico

---

## Registro de Versiones

| Versión | Fecha | Cambios |
|:-------:|:-----:|:--------|
| 1 | 2026-07-05 | Receipt Scanning con ML Kit Document Scanner + Text Recognition + Parser |
| 2 | 2026-07-05 | Multi-moneda con tasas de cambio + Receipt Scanning multi-moneda |
| 3 | 2026-07-05 | UX: Transiciones, confirmación eliminar, pull-to-refresh, empty states, feedback visual, accesibilidad, bottom nav, formularios, DonutChart interactivo |
