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

## Versión 6 — 2026-07-09

### 6.1 Fix: Import ordering en perf commits
- **Archivos:** `ExpenseItem.kt`, `SettingsScreen.kt`
- **Problema:** Los commits `6a68d52` y `349cef5` colocaron declaraciones `private val` antes de los `import` en Kotlin, lo cual rompe la compilación (imports deben ir antes de cualquier declaración top-level). También había un import duplicado de `DateTimeFormatter`.
- **Solución:** Movidos todos los imports antes de las declaraciones en ambos archivos. Eliminado import duplicado.

### 6.2 Fix: Captura obsoleta de lambda en remember
- **Archivos:** `HomeScreen.kt`, `ListScreen.kt`
- **Problema:** `remember(expense.id) { { lambda } }` capturaba el objeto `expense` de la primera composición; si el gasto se actualizaba (mismo id, nuevo objeto), `onDelete` borraba el snapshot viejo.
- **Solución:** Revertido a lambdas inline sin `remember`. Eliminado import `remember` no utilizado en `HomeScreen.kt`.

### 6.3 Tarjetas de resumen del Home conectadas a Estadísticas
- **Archivos:** `HomeScreen.kt`, `AppNavigation.kt`, `StatsViewModel.kt`
- **Problema:** Las tarjetas "Total hoy", "Esta semana" y "Este mes" no tenían navegación a Estadísticas con el periodo preseleccionado.
- **Solución:**
  - Añadido argumento de navegación `period` a la ruta de StatsScreen (default `MONTHLY`)
  - `StatsViewModel` usa `SavedStateHandle` para leer el periodo inicial del nav arg
  - `HomeScreen`: `MiniSummaryCard` y tarjeta de mes ahora son clickeables
  - Mapeo: "Total hoy" → `DAILY`, "Esta semana" → `WEEKLY`, "Este mes" → `MONTHLY`
  - Helper `Routes.stats(period)` para navegación parametrizada

### 6.4 Desglose por moneda colapsable
- **Archivo:** `HomeScreen.kt`
- **Problema:** El bloque de "Desglose por moneda" con `FlowRow` de chips ocupaba mucho espacio en la pantalla de inicio.
- **Solución:** Convertido a sección colapsable con:
  - `AnimatedVisibility` para animar expansión/colapso
  - `Row` clickeable con ícono `ExpandMore`/`ExpandLess`
  - Estado `currencyExpanded` (default `false`) — colapsado por defecto
  - Mismo patrón que la sección de notas en `AddEditScreen.kt`

---

## Versión 7 — 2026-07-09

### 7.1 Escaneo de recibos (Receipt Scanning)
- **Archivos:** `gradle/libs.versions.toml`, `app/build.gradle.kts`, `data/ocr/ReceiptData.kt`, `data/ocr/ReceiptParser.kt`, `data/ocr/ReceiptOcrService.kt`, `data/ocr/MLKitReceiptOcrService.kt`, `di/OcrModule.kt`, `ui/add/AddEditScreen.kt`, `ui/add/AddEditViewModel.kt`, `strings.xml` (+en)
- **Objetivo:** Escanear un recibo físico y auto-llenar los campos del formulario de gasto.
- **Solución:**
  - Dependencias ML Kit: `play-services-mlkit-document-scanner` + `text-recognition` (versiones `16.0.0`)
  - `GmsDocumentScanning` lanza la UI de escaneo (sin permiso CAMERA) y devuelve URI de imagen JPEG
  - `MLKitReceiptOcrService` carga el bitmap y corre Text Recognition v2 (on-device)
  - `ReceiptParser` extrae con regex: **Total** (líneas TOTAL/SUMA/IMPORTE, ignora VUELTO), **Fecha** (4 formatos), **Comercio** (1ª línea) y **Moneda** (símbolos → PEN/USD/EUR/JPY/GBP/BRL)
  - `AddEditScreen`: botón "Escanear recibo" + `rememberLauncherForActivityResult(StartIntentSenderForResult)`; indicador de carga `isScanning`
  - `AddEditViewModel.handleScanResult(uri)` auto-rellena monto/moneda/fecha/nota (sobrescribe lo detectado; conserva valor actual si un campo no se detecta)
  - `OcrModule` (Hilt) provee `ReceiptOcrService` como singleton
  - Strings `scan_receipt` / `scan_error` en español e inglés
- **Nota:** Requiere Google Play Services con módulo ML Kit; OCR es heurístico y puede necesitar ajustes.

---

## Versión 8 — 2026-07-14

### 8.1 Fix: Paquetes incorrectos en imports de ML Kit
- **Archivos:** `AddEditScreen.kt`, `MLKitReceiptOcrService.kt`, `gradle/libs.versions.toml`
- **Problema:** Los imports de ML Kit usaban paquetes y nombres de clase que no existen en los AAR reales descargados desde Google Maven, causando múltiples errores "Unresolved reference" al compilar.
- **Solución** (verificado extrayendo los AAR directamente de `dl.google.com`):
  - **Package document scanner:** `com.google.mlkit.vision.documentscanner.*` — el código usaba `com.google.android.gms.mlkit.vision.documentscanner.*` (no existe)
  - **Clase Options:** `GmsDocumentScannerOptions` (con "Scanner") — el código usaba `GmsDocumentScanningOptions` (con "Scanning")
  - **Constante FORMAT_JPEG:** está en `GmsDocumentScannerOptions.RESULT_FORMAT_JPEG` — el código la buscaba en `GmsDocumentScanningResult.FORMAT_JPEG`
  - **TextRecognizerOptions:** está en `com.google.mlkit.vision.text.latin` — el código importaba desde `com.google.mlkit.vision.text` (sin `.latin`)
- **Adicional:** 
  - Eliminado import `com.google.android.gms.tasks.addOnSuccessListener` (top-level extension no disponible en `play-services-tasks:18.2.0`); se usa la función miembro de `Task` vía SAM conversion
  - Agregado import faltante `androidx.activity.compose.rememberLauncherForActivityResult`
  - Bump `mlkitTextRecognition` de `16.0.0` a `16.0.1`

---

## Versión 10 — 2026-07-15

### 10.1 Rediseño completo de la pantalla de Ajustes
- **Archivos:** `SettingsScreen.kt`, `strings.xml` (+en)
- **Objetivo:** Mejorar la apariencia visual, usabilidad y organización de la pantalla de Ajustes.
- **Cambios:**
  - **Encabezados de sección agrupados:** Se agregaron títulos GENERAL, PERSONALIZACIÓN, DATOS e INFORMACIÓN sobre las tarjetas, con texto en mayúsculas y color primario, para organizar visualmente los ajustes.
  - **Flecha `>` en SettingsRow:** Cada fila clickable ahora muestra un ícono `KeyboardArrowRight` al final, indicando que es interactiva (estilo Ajustes de Android/iOS). Las filas informativas lo ocultan.
  - **Versión dinámica:** Reemplazado el texto hardcodeado "Versión 0.2" por `BuildConfig.VERSION_NAME` (formato `v{X}`).
  - **Sección de Moneda rediseñada:** Ahora incluye una fila informativa "Moneda base → PEN — Soles peruanos" y el botón "Actualizar tasas" con ícono `Refresh`.
  - **Sección Acerca de mejorada:** Agregado ícono de la app con fondo gradiente (`GradientStart → GradientEnd`) en un `RoundedCornerShape(14.dp)`, nombre de la app en `SemiBold`, versión dinámica en color primario, y texto "Hecho con ❤️ en Kotlin".
  - **Animación en presupuesto:** El bloque de "Cambiar monto" / "Definir presupuesto" ahora se expande/contrae con `AnimatedVisibility` usando `expandVertically()`/`shrinkVertically()` al activar/desactivar el Switch.
  - **subtitle opcional:** El parámetro `subtitle` de `SettingsRow` ahora tiene default `""` y solo se muestra si tiene contenido.
  - **Spacer horizontal consistente:** Reemplazado `Spacer(Modifier.padding(horizontal = 8.dp))` por `Spacer(Modifier.width(12.dp))` en SettingsRow y SettingsSection.

---

## Versión 11 — 2026-07-15

### 11.1 Barra de progreso del presupuesto mensual

### 11.1 Barra de progreso del presupuesto mensual
- **Archivos:** `SettingsViewModel.kt`, `SettingsScreen.kt`, `strings.xml` (+en)
- **Objetivo:** Mostrar visualmente qué porcentaje del presupuesto mensual se ha consumido.
- **Solución:**
  - Nuevo campo `monthlyExpenseTotal` en `SettingsUiState` — se calcula en `init` del ViewModel usando `expenseRepository.getCurrentMonthTotal()`
  - `LinearProgressIndicator` con altura de 8dp y bordes redondeados debajo del Switch de presupuesto
  - Color dinámico: verde/primary (< 80%), naranja (80–99%), rojo/error (≥ 100%)
  - Texto informativo: "S/. {gastado} de S/. {presupuesto}" o "¡Presupuesto excedido!"
  - Animado con `AnimatedVisibility` junto al resto del contenido

### 11.2 Botón "Restablecer datos"
- **Archivos:** `SettingsViewModel.kt`, `SettingsScreen.kt`, `strings.xml` (+en)
- **Objetivo:** Permitir borrar todos los gastos y reiniciar el presupuesto desde Ajustes.
- **Solución:**
  - Nueva fila `SettingsRow` con ícono `DeleteForever` y color error al final de la sección DATOS
  - `AlertDialog` de confirmación con texto descriptivo y botón "Confirmar" en rojo
  - Método `clearAllData()` en ViewModel que llama a `expenseRepository.deleteAllExpenses()` y resetea el presupuesto
  - Snackbar "Datos restablecidos" al completar

### 11.3 Preview visual de temas y banderas en círculo
- **Archivos:** `ThemeSettingsDialog.kt`, `LanguageSettingsDialog.kt`
- **Objetivo:** Mejorar la apariencia de los diálogos de selección con previsualizaciones visuales.
- **Solución:**
  - **ThemeSettingsDialog:** Cada opción ahora muestra un círculo de color de 24dp al final con el color primario del tema (morado para Light, lila para Dark, teal para System, verde neón para Matrix). Al estar seleccionado, el checkmark se muestra dentro del círculo en blanco.
  - **LanguageSettingsDialog:** La bandera de cada idioma ahora va dentro de un círculo de 40dp con fondo `surfaceVariant` (o primary con alpha si está seleccionado), consistente con el estilo de ThemeOption.

---

## Versión 12 — 2026-07-19

### 12.1 Accesibilidad completa (plan V3, sección 3.1)
- **Archivos:** `FontScale.kt` (nuevo), `FontScaleDialog.kt` (nuevo), `ThemeMode.kt`, `UserPreferences.kt`, `PreferencesRepository.kt`, `Theme.kt`, `Color.kt`, `CategoryColors.kt`, `MainActivity.kt`, `SettingsScreen.kt`, `SettingsViewModel.kt`, `ThemeSettingsDialog.kt`, `MainPagerScreen.kt`, `HomeScreen.kt`, `ListScreen.kt`, `AddEditScreen.kt`, `ExpenseItem.kt`, `CategorySelector.kt`, `StatsScreen.kt`, todos los `strings.xml` (8 idiomas)
- **Objetivo:** Hacer la app usable con TalkBack, baja visión y necesidades de fuente grande/alto contraste.
- **Cambios:**
  - **Content descriptions:** botones icon-only ahora anuncian su acción (volver, agregar gasto, editar/eliminar gasto, filtros, expandir/contraer). Los iconos decorativos junto a texto mantienen `null` (práctica WCAG — evita anuncios duplicados).
  - **Semántica:** `heading()` en encabezados de sección y de mes; `liveRegion = Polite` en indicadores de carga y snackbar; `clickable(onClickLabel = …)` en tarjetas del Home que navegan a Estadísticas.
  - **Tamaño de fuente:** nuevo enum `FontScale` (0.85/1.0/1.15/1.3) + clave DataStore `font_scale` + diálogo con preview "Aa". Se aplica SOLO vía `LocalDensity` (escalar también `Typography` duplicaría el factor, pues los `sp` ya los escala la densidad).
  - **Touch targets:** botones de 28dp en `ExpenseItem` con `minimumInteractiveComponentSize()` (48dp táctil, mismo tamaño visual).
  - **Tema HIGH_CONTRAST:** 5ª opción de tema (WCAG AAA: blanco puro/negro puro, acentos oscuros). Paleta de categorías propia y `LocalIsHighContrast` CompositionLocal para no enhebrar otro booleano por la navegación.
- **Extra (i18n):** strings hardcodeados "Por mes"/"Este mes" de `ListScreen` reemplazados por `filter_by_month`/`this_month` en los 8 idiomas.

---

## Registro de Versiones

| Versión | Fecha | Cambios |
|:-------:|:-----:|:--------|
| 12 | 2026-07-19 | Accesibilidad: content descriptions, semántica, fontScale, touch targets 48dp, tema alto contraste + fix strings hardcodeados de Lista |
| 11 | 2026-07-15 | Barra progreso presupuesto, restablecer datos, preview visual temas/banderas |
| 10 | 2026-07-15 | Rediseño Ajustes: secciones agrupadas, flechas >, versión dinámica, moneda como row, About mejorado, animación presupuesto |
| 9 | 2026-07-15 | Fix desfase temporal en DatePicker |
| 1 | 2026-07-04 | Fix declarations, LetterSpacing, FilterChips overflow, README redesign |
| 2 | 2026-07-04 | Tope de 4 decimales con soporte punto/coma |
| 3 | 2026-07-04 | Nuevo tema Matrix (verde neon glow) |
| 4 | 2026-07-05 | Limpieza de código muerto (LocaleHelper, AppModule, DAOs, Routes) |
| 5 | 2026-07-08 | Sección de notas desplegable + Exportación CSV de gastos |
| 6 | 2026-07-09 | Fix imports, tarjetas Home conectadas a Stats, desglose moneda colapsable |
| 7 | 2026-07-09 | Escaneo de recibos con ML Kit Document Scanner + Text Recognition + parser |
| 8 | 2026-07-14 | Fix paquetes/nombres imports ML Kit, bump text-recognition 16.0.1, import faltante |
