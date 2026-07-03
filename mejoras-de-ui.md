# Mejoras de UI - AppGasto

## Resumen general

Rediseño completo de la interfaz visual de AppGasto con un estilo **colorido y vibrante**, reemplazando la paleta naranja/marrón original por violeta (#6C63FF), coral (#FF6584) y teal (#00BFA6). Se mejoraron las 5 pantallas principales, los componentes reutilizables, el tema, los diálogos, el widget y el icono de la app.

---

## 1. Tema y paleta de colores

### Color.kt
- Paleta light: primario violeta (#6C63FF), secundario coral (#FF6584), terciario teal (#00BFA6)
- Paleta dark: variantes adaptadas con tonos pastel (#B4AFFF, #FFB2C0, #64FFDA)
- Nuevos colores de gradiente: `GradientStart`, `GradientEnd`, `GradientTertiary`
- Se agregaron slots `tertiary`, `onTertiary`, `tertiaryContainer`, `onTertiaryContainer` y `outline`

### Type.kt
- Letter spacing negativo en displayLarge y headlineLarge para tipografía más ajustada
- Nuevos estilos: `displayMedium`, `headlineMedium`, `titleSmall`, `bodySmall`, `labelSmall`
- `displayLarge` ahora usa `FontWeight.ExtraBold`

### Theme.kt
- `AppShapes` personalizadas: extraSmall (8dp), small (12dp), medium (16dp), large (24dp), extraLarge (28dp)
- Se inyectan los colores tertiary y outline en ambos color schemes

### CategoryColors.kt
- Colores más saturados: food (#FF6D00), transport (#2979FF), leisure (#AA00FF), home (#00C853), health (#FF1744), clothing (#FFAB00)
- Variantes dark más luminosas

---

## 2. Componentes reutilizables

### ExpenseItem.kt
- **Avatar circular** con icono representativo por categoría (Fastfood, DirectionsCar, SportsEsports, Home, Favorite, Checkroom, MoreHoriz) sobre fondo con color de categoría al 15% de opacidad
- Card con `shadowElevation` y `tonalElevation` para profundidad
- Monto con `FontWeight.Bold` y estilo `titleMedium`
- Iconos de editar (color primario) y eliminar (color error) más compactos (28dp)
- Nota del gasto con `maxLines = 1` para evitar desbordes

### CategorySelector.kt
- Cada chip ahora muestra un **icono circular** de la categoría (28dp) con fondo de color
- Animaciones con `animateColorAsState` y `animateDpAsState` (spring) para transiciones suaves al seleccionar
- Borde de 2dp animado al seleccionar
- Padding aumentado (14dp horizontal, 10dp vertical)

---

## 3. HomeScreen

- **Hero card** con gradiente horizontal violeta→coral mostrando el total del mes en displayMedium blanco
- **Mini-cards** para "Total hoy" y "Esta semana" con iconos circulares (Today, DateRange) sobre fondos de color
- `LargeFloatingActionButton` circular para agregar gastos
- **Empty state** con icono CalendarToday (40dp) y mensaje centrado
- TopAppBar con `titleLarge` y `FontWeight.Bold`
- Spacing entre items aumentado a 12dp

---

## 4. StatsScreen

- **Total card** con gradiente violeta→coral y monto en `displayLarge` blanco
- **Gráfico de dona** implementado con `Canvas` y `drawArc` (180dp), mostrando distribución por categoría con colores vivos
- **Leyenda** con dots circulares de color, porcentaje y monto por categoría
- **Barras de progreso** con gradiente horizontal por categoría (color sólido → color al 60% de opacidad)
- `FilterChip` con colores personalizados: seleccionado = primario con texto blanco
- Todo contenido en una Card con `shape = large`

---

## 5. ListScreen

- `FilterChip` con `FilterChipDefaults.filterChipColors` usando primario como fondo seleccionado
- **Empty state** con icono ReceiptLong (48dp) al 40% de opacidad
- Spacing entre items aumentado a 10dp
- TopAppBar con `titleLarge` y `FontWeight.Bold`

---

## 6. AddEditScreen

- **Campo de monto hero**: Box con gradiente violeta→coral (borderRadius 20dp), texto en `displayMedium` blanco centrado, bordes del TextField blancos
- Selector de categoría con más espacio (20dp de separación)
- Campo de nota con `shape = medium`
- Botón de fecha con icono CalendarMonth
- **Botón de guardar**: 56dp de altura, `titleMedium`, color primario
- CircularProgressIndicator con color `onPrimary` durante guardado

---

## 7. SettingsScreen

- Cada sección tiene un **icono circular coloreado** en el header (primario para presupuesto, secundario para apariencia, teal para backup)
- `SettingsRow` reutilizable con icono circular (36dp) + título + subtítulo
- `HorizontalDivider` entre filas de una misma sección
- `Switch` con `SwitchDefaults` coloreado (thumb blanco, track primario)
- Cards con `shape = large` y fondo `surfaceVariant` al 30% de opacidad
- Sección "Acerca de" con icono Info en círculo

---

## 8. Diálogos

### ThemeSettingsDialog
- Cada opción muestra un icono coloreado (LightMode amarillo, DarkMode violeta, SettingsBrightness teal) en un círculo
- Opción seleccionada con fondo primario al 8% + icono de check
- Diálogo con `shape = large`

### LanguageSettingsDialog
- **Banderas emoji** por idioma (🇪🇸 Español, 🇬🇧 English, 🇧🇷 Português, etc.)
- Opción seleccionada con fondo primario al 8% + icono de check
- Diálogo con `shape = large`

### BudgetDialog
- Descripción con `bodyMedium` y color `onSurfaceVariant`
- TextField con `shape = medium`
- Diálogo con `shape = large`

---

## 9. Widget

- **widget_background.xml**: Gradiente violeta→coral con radio 24dp
- **widget_layout.xml**: Título con letter spacing 0.05, monto en 32sp bold, padding 16dp

---

## 10. Recursos XML

- **themes.xml**: Status bar y navigation bar actualizados a #F8F7FC (light) y #131316 (dark)
- **colors.xml**: Colores de categoría sincronizados con CategoryColors.kt
- **ic_launcher_foreground.xml**: Rediseñado como signo de dólar ($) blanco dentro de círculo violeta (#6C63FF)

---

## 11. Bug fix

- Renombrado `R.string.import` → `R.string.import_action` en 8 archivos de localización (values, values-en, values-de, values-it, values-ja, values-ko, values-pt, values-qu). El nombre `import` es una palabra reservada de Java y causaba fallo en AAPT2.

---

## Archivos modificados

| Archivo | Tipo de cambio |
|---|---|
| `ui/theme/Color.kt` | Reescrito |
| `ui/theme/Type.kt` | Reescrito |
| `ui/theme/Theme.kt` | Reescrito |
| `ui/theme/CategoryColors.kt` | Reescrito |
| `ui/components/ExpenseItem.kt` | Reescrito |
| `ui/components/CategorySelector.kt` | Reescrito |
| `ui/home/HomeScreen.kt` | Reescrito |
| `ui/stats/StatsScreen.kt` | Reescrito |
| `ui/list/ListScreen.kt` | Reescrito |
| `ui/add/AddEditScreen.kt` | Reescrito |
| `ui/settings/SettingsScreen.kt` | Reescrito |
| `ui/settings/ThemeSettingsDialog.kt` | Reescrito |
| `ui/settings/LanguageSettingsDialog.kt` | Reescrito |
| `ui/settings/BudgetDialog.kt` | Reescrito |
| `res/drawable/widget_background.xml` | Modificado |
| `res/layout/widget_layout.xml` | Modificado |
| `res/values/themes.xml` | Modificado |
| `res/values-night/themes.xml` | Modificado |
| `res/values/colors.xml` | Modificado |
| `res/values-night/colors.xml` | Modificado |
| `res/drawable/ic_launcher_foreground.xml` | Reescrito |
| `res/values*/strings.xml` (x8) | Bug fix |
