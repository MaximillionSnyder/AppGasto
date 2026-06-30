# Análisis del repositorio `AppGasto`

> Análisis exhaustivo del estado actual del código (commit `d365274`, rama `main`).
> Incluye discrepancias entre el README y la implementación, bugs detectados,
> dead code y prioridades de corrección.

App Android (Kotlin, 100%) para registrar gastos diarios. 1 solo commit, ~3.119
LOC Kotlin en 38 archivos. Sin releases, sin tests.

---

## 1. Stack técnico: README vs realidad

| Capa | README | Realidad | Comentario |
|---|---|---|---|
| UI | Jetpack Compose + Material 3 | OK | Compose + M3 |
| DB | Room | OK | Room 2.6.1 — **pero ver bug #1 (no compila)** |
| Prefs | DataStore | OK | DataStore Preferences |
| DI | Hilt | OK | Hilt 2.50 |
| Widget | **Glance** | **FALSO** | `ExpenseWidget.kt:12` extiende `AppWidgetProvider` clásico y usa `R.layout.widget_layout` (RemoteViews). Glance está en dependencias pero **no se usa**. |
| Gráficas | **Vico** | **FALSO** | `StatsScreen.kt:177-191` dibuja barras con `Box(...).fillMaxWidth(percentage)` a mano. Vico declarado en `build.gradle.kts:101-102` pero **0 referencias** en código. |
| Background | WorkManager | OK | `BudgetWorker` |
| Respaldo | **Auto Backup Google Drive (sin config)** + JSON | **Parcialmente falso** | Solo existe export/import JSON manual a `Downloads`. Lo de "Drive" es solo el Auto Backup genérico de Android (`allowBackup=true` + `backup_rules.xml`), que **no es "sin config extra"** (requiere cuenta Google activa) y solo respalda DB/prefs. |
| Multi-idioma | Android resources + AppCompatDelegate | OK en teoría — ver §6 | Los `strings.xml` existen pero **no se cablean** en la UI |

**Dependencias muertas**: `glance`, `glance-appwidget`, `vico:core`, `vico:compose-m3`
se declaran pero no se referencedian. Aumentan el APK sin beneficio.

---

## 2. Arquitectura

Estructura limpia por capas, inyección con Hilt, ViewModels con `StateFlow`,
navegación con Navigation Compose.

```
data/local      Room (Expense, Category, DAOs, AppDatabase)
data/repository ExpenseRepository, PreferencesRepository
data/backup     BackupManager (JSON)
domain/model    ThemeMode, AppLanguage, UserPreferences
ui/{home,add,list,stats,settings}  pantalla + ViewModel cada una
ui/components   ExpenseItem, CategorySelector
ui/theme        Color/Type/Theme/CategoryColors
widget          ExpenseWidget (RemoteViews)
notifications   BudgetWorker (WorkManager)
di              DatabaseModule (+ AppModule vacío)
```

5 pantallas (Home, Add/Edit, List, Stats, Settings) + 1 widget. Sin
`BottomNavigation`: se navega por iconos del `TopAppBar` y FAB.

---

## 3. Bugs críticos (el proyecto probablemente no compila)

### Bug #1 — Falta `TypeConverter` para `LocalDateTime` (build break)
`Expense.kt:26` declara `createdAt: LocalDateTime`. Room 2.6.1 **no** convierte
`java.time.LocalDateTime` automáticamente (y no hay `coreLibraryDesugaring` en
`compileOptions`). No existe ningún `@TypeConverter` en todo el repo (verificado
con grep). El procesamiento KSP de Room fallará con:

> *Cannot figure out how to save this field into the database. You can consider
> adding a type converter for it.*

**Esto significa que el commit actual no compila.**

**Fix**: añadir un `Converters` con
`@TypeConverter fun fromLocalDateTime(ldt) = ldt.toString()` y registrarlo en
`@Database(typeConverters = [Converters::class])`, o serializar a epoch millis.

### Bug #2 — El widget siempre muestra "0.00"
`ExpenseWidget.kt:24-42` construye `RemoteViews` desde `widget_layout.xml` (que
tiene `widget_total` con texto hardcode `"0.00"`) pero **nunca llama
`setTextViewText(R.id.widget_total, ...)`** con el total real. Tampoco consulta
la DB ni se actualiza al insertar gastos. Solo setea el `PendingIntent` de click.
El README dice "Widget: Total del día" → **falso**, siempre muestra 0.00.

### Bug #3 — Exportación JSON rota en Android 10+
`BackupManager.kt:45-49` escribe en
`Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS)`. El permiso
`WRITE_EXTERNAL_STORAGE` está limitado a `maxSdkVersion="28"`
(`AndroidManifest.xml:6-7`). En API 29+ (targetSdk 34) con scoped storage, esta
llamada **falla/lanza excepción**. Debería usarse `MediaStore.Downloads` o SAF
(`CreateDocument`). La exportación "Guardar copia en Descargas" no funcionará en
dispositivos modernos.

---

## 4. Bugs funcionales importantes

### Edición silenciosa del timestamp
`AddEditViewModel.kt:100`: al editar,
`createdAt = LocalDateTime.of(state.date, LocalTime.now())` → **se pierde la hora
original** del gasto y se reemplaza por la hora actual. Al editar un gasto viejo
su timestamp cambia al "ahora".

### Las pantallas no refrescan al volver
Los ViewModels cargan datos en `init {}` y los DAO son `suspend fun` que devuelven
`List` (no `Flow`). No hay `onResume`/`LifecycleEventEffect` que recargue.
Consecuencia: tras **agregar** un gasto en `AddEditScreen` y volver a Home, **no
se ve el nuevo gasto** hasta reiniciar la app (Home solo recarga en
`deleteExpense`, `HomeViewModel.kt:61-66`). Mismo problema en List y Stats.

**Fix**: devolver `Flow<List<Expense>>` desde los DAO y recolectar, o recargar en
`onResume`.

### Importación destructiva y no transaccional
`BackupManager.kt:67-77`: borra todas las categorías y gastos y reinserta **sin
transacción**. Si falla a mitad, se pierden todos los datos. Además
`deleteAllCategories()` con `CASCADE` ya borra los gastos, así que
`deleteAllExpenses()` es redundante. Debe envolverse en `db.withTransaction { }`.

### `fallbackToDestructiveMigration()`
`AppDatabase.kt:32`: cualquier cambio de schema **borra todos los datos del
usuario**. Inaceptable para una app que guarda historial de gastos. No hay
migraciones definidas ni `exportSchema`.

### WorkManager spam de notificaciones
`BudgetWorker` corre cada 6 h (`AppGastoApplication.kt:54`) y notifica si el gasto
mensual ≥ 80% o ≥ 100%. No guarda estado de "ya notificado", así que **lanza la
misma notificación cada 6 horas** mientras estés por encima del umbral. Además usa
un único `NOTIFICATION_ID = 1001` para ambos umbrales, así que se sobreescriben
entre sí. Solo notifica al checkear, no al cruzar el umbral real.

### Filtros de fecha incompletos
`ListScreen.kt` solo expone `FilterChip` de categoría. El código de
`ListViewModel.applyFilters(..., startDate, endDate)` y los strings
`filter_date`/`from`/`to`/`apply` existen, pero **no hay UI** para filtrar por
fecha. Feature a medias.

---

## 5. Inconsistencias y dead code

- **`Category.colorHex` e `iconName` son columnas muertas**: se guardan en el
  seed (`AppDatabase.kt:41`) pero la UI nunca los lee. Los colores se hardcoded
  por `id` en `CategoryColors.kt:27-44` (mapa `1L→food … 7L→other`). Cualquier
  categoría nueva (id ≥ 8) cae al gris "other" sin importar su `colorHex`.
  Además no existe UI para editar categorías (el README dice "editables en la DB").
- **`LocaleHelper.kt` es código muerto**: verificado con grep, **no se referencia
  en ningún lado**. `SettingsViewModel.setLanguage` hace el cambio de locale
  inline con `AppCompatDelegate` (`SettingsViewModel.kt:56-75`). Los métodos
  `applyLanguage`/`updateContext` no se invocan.
- **`AppModule.kt`** es un `object` Hilt vacío que no provee nada.
- **`DatabaseModule` provee `ExpenseDao`/`CategoryDao`** pero nadie los inyecta
  directamente: `ExpenseRepository` recibe `AppDatabase` y llama
  `database.expenseDao()`. Proveedores inútiles.
- **`Routes.editExpense`** es idéntica a `Routes.addExpense(expenseId)`
  (`AppNavigation.kt:22-25`).
- **`PreferencesRepository`** tiene `safeValueOf<T>` genérico **y**
  `safeValueOfLanguage` duplicado (`PreferencesRepository.kt:66-80`).
- Varios métodos del DAO (`getDailyTotals`, `getTotalSince`, `getByCategory`...)
  no se usan desde el repo.

---

## 6. i18n: soporte real vs elclaimed

- **8 locales completos** (es, en, pt, it, de, ja, ko, qu) — verificado que **las
  101 claves coinciden** entre el default y cada variante.
- **Las categorías no se traducen**: el seed inserta nombres en español
  hardcodeados (`AppDatabase.kt:39-49`: "Comida", "Transporte"...). Aunque existan
  `cat_food` etc. en `strings.xml`, la UI muestra `category.name` desde la DB → un
  usuario en inglés verá "Comida", "Ocio". Bug de i18n real.
- **Notificaciones y canal hardcodeados en español**: `BudgetWorker.kt:44-49`
  ("Presupuesto agotado"), `AppGastoApplication.kt:39` ("Alertas de presupuesto").
  No usan `strings.xml` aunque ya existen `budget_alert_80/100`.
- **Textos UI literales en español**: `HomeScreen.kt` ("AppGasto", "Hoy",
  "Semana", "Mes", "Gastos de hoy", "Sin gastos hoy"), `ListScreen.kt`
  ("Historial", "Todas", "Limpiar filtros"), `StatsScreen.kt` ("Hoy"/"Semana"/
  "Mes", "Total", "Por categoría"), `AddEditScreen.kt` ("Agregar Gasto", "Monto",
  "Categoría", "Nota", "Fecha", "Guardar"), `SettingsScreen.kt` y diálogos.
  **Ningún `stringResource(R.string.*)`** se usa en las pantallas. Los
  `strings.xml` están completos pero **no cableados** → el multi-idioma de la UI
  en realidad **no funciona**; la app siempre muestra español salvo el nombre de
  la app.
- `AppCompatDelegate.setApplicationLocales` se usa para cambiar locale, pero como
  la UI no usa `stringResource`, cambiar de idioma no produce casi ningún cambio
  visible (solo formatos de fecha/Sistema). Inconsistencia con el XML theme
  `Theme.AppGasto` que hereda de `android:Theme.Material.Light.NoActionBar`
  (framework Material 1, no AppCompat), lo que además puede interferir con
  `AppCompatDelegate`.

---

## 7. Otros detalles

- **0 tests**: no existen directorios `test/` ni `androidTest/` pese a declarar
  JUnit/Espresso/Compose UI test. Cobertura nula.
- **`proguard-rules.pro`** solo keep `data.local.**` — ok para Room, pero falta
  `-keep` para Gson `BackupData` y reflection de `TypeToken`; con
  `isMinifyEnabled=false` ahora no importa, pero al activar R8 el import/export
  podría romper.
- **`themes.xml`** hereda de `Theme.Material.Light.NoActionBar` (Material 1 del
  framework), no de `Theme.Material3` ni AppCompat — contradice el "Material 3"
  del README a nivel XML (en Compose sí es M3).
- **MainActivity** llama `enableEdgeToEdge()` antes de `super.onCreate()` y
  `setDecorFitsSystemWindows(false)` — correcto, pero `WindowCompat` se importa y
  usa mientras `enableEdgeToEdge` ya lo maneja; redundante.
- **Cast inseguro** `Theme.kt:69` `(view.context as Activity).window` — frágil si
  el composable se usa fuera de Activity.
- **`<string name="import">`** (`strings.xml:27`) usa palabra reservada; el R
  generado lo renombrará, pero como no se referencia, es latente.
- **Moneda hardcodeada `S/.`** (`SettingsScreen.kt:121`) — soles peruanos fijos,
  sin locale ni preferencia de moneda.

---

## 8. Resumen ejecutivo

**Lo bueno**: arquitectura limpia y modular, Hilt + Room + Compose bien
organizados, 8 locales traducidos completos en `strings.xml`, paleta M3
consistente, uso correcto de WorkManager + DataStore, README claro.

**Lo grave**:
1. **No compila** por el `LocalDateTime` sin `TypeConverter` (bug #1).
2. **El widget no muestra datos** reales (bug #2).
3. **La exportación falla en Android 10+** por scoped storage (bug #3).
4. **El multi-idioma de la UI no funciona**: las pantallas usan strings literales
   en español, ignorando los 8 `strings.xml` traducidos.
5. **Edición altera el timestamp**, las pantallas no refrescan al volver, import
   no transaccional, migraciones destructivas.

**Veracidad del README**: inflada. Glance, Vico y "Auto Backup a Google Drive sin
config" no se cumplen; las categorías no son editables ni traducibles como se
sugiere.

### Prioridad de fixes
1. TypeConverters (que compile).
2. Cablear `stringResource` en todas las pantallas + seed de categorías
   localizado o por key.
3. Arreglar widget para leer y mostrar el total real.
4. Migrar export a `MediaStore`/SAF.
5. DAOs reactivos con `Flow` para refresco automático.
6. Transacción en import + migraciones reales.
7. Eliminar dependencias dead (Glance/Vico) o usarlas de verdad.
8. Tests.
