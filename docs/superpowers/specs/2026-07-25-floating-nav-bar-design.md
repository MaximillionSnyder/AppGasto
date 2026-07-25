# Diseño: Barra de navegación flotante (M3 Expressive)

- **Fecha:** 2026-07-25
- **Rama:** `dev`
- **Estado:** Aprobado por el usuario

## Contexto

Tras el merge de `master` (commit `8f95151`), la navegación principal es una `NavigationBar` estándar de Material 3, pegada al borde inferior, con 4 destinos (Home, Lista, Estadísticas, Ajustes) y un FAB circular separado para agregar gastos. El usuario la percibe "muy simple" y quiere mejorar su apariencia.

## Objetivo

Rediseñar la barra inferior con estética **M3 Expressive**: una píldora flotante, despegada de los bordes, con animaciones e iconografía con más "vida".

## Decisiones aprobadas

| Decisión | Elección del usuario |
|---|---|
| Estilo | Píldora flotante (márgenes laterales, esquinas totalmente redondeadas, sombra) |
| Botón "+" | FAB separado, flotando sobre la píldora; visible solo en Home y Lista (comportamiento actual, sin cambios) |
| Etiquetas | Solo visibles en el ítem activo, dentro del indicador expandido |
| Iconos | Outlined (inactivo) → Filled (activo), con transición `Crossfade` |
| Enfoque de implementación | **A**: reestilizar componentes M3 oficiales (`NavigationBar` / `NavigationBarItem`), no componente custom |

## Alcance

### Se toca

1. **`ui/navigation/FloatingNavBar.kt`** (archivo nuevo): composable `FloatingNavBar(selectedIndex: Int, onItemSelected: (Int) -> Unit)` con responsabilidad única de renderizar la píldora. Contiene internamente la definición de los 4 destinos (recurso de título, icono filled, icono outlined). Incluye un `@Preview`.
2. **`ui/navigation/MainPagerScreen.kt`**: únicamente el bloque `bottomBar` del `Scaffold`, reemplazando el `NavigationBar` actual por `FloatingNavBar(...)`.

### No se toca (garantizado)

- Pantallas Home, Lista, Estadísticas, Ajustes.
- FAB: mismo botón, misma posición, misma visibilidad (solo Home y Lista).
- Pager y gesto de swipe.
- Navegación, ViewModels, capa de datos.
- `Theme.kt` / `Color.kt`: cero cambios; la píldora consume tokens del `colorScheme` vigente.
- Dependencias: ninguna nueva (`material-icons-extended` ya existe).
- Tests unitarios existentes.

## Diseño detallado

### Componente `FloatingNavBar`

`NavigationBar` oficial con:

- `Modifier.padding(horizontal = 16.dp, vertical = 8.dp).navigationBarsPadding().shadow(12.dp, CircleShape).clip(CircleShape).height(64.dp)`
  - Altura inicial 64dp; si la etiqueta del ítem activo se corta visualmente, se ajusta a 72dp (decisión de implementación, sin cambio de diseño).
- `windowInsets = WindowInsets(0)` para que el inset de la barra de sistema no infle la píldora (el espaciado se maneja con `navigationBarsPadding()` en el modifier externo).
- `containerColor = MaterialTheme.colorScheme.surfaceContainerHigh` (flota visualmente sobre el fondo en los 4 temas).
- 4 × `NavigationBarItem(alwaysShowLabel = false)`:
  - Slot `icon`: `Crossfade(targetState = selected, animationSpec = tween(200))` que alterna entre el icono outlined y el filled.
  - Slot `label`: `Text(stringResource(titulo), style = labelSmall)`.
  - Indicador: default M3 (píldora animada incluida).

### Destinos e iconos

| Destino | String | Outlined | Filled |
|---|---|---|---|
| Home | `R.string.home_title` | `Icons.Outlined.Home` | `Icons.Filled.Home` |
| Lista | `R.string.list_title` | `Icons.Outlined.FormatListBulleted` | `Icons.Filled.FormatListBulleted` |
| Estadísticas | `R.string.stats_title` | `Icons.Outlined.BarChart` | `Icons.Filled.BarChart` |
| Ajustes | `R.string.settings_title` | `Icons.Outlined.Settings` | `Icons.Filled.Settings` |

### Comportamiento y animaciones

- Selección: misma lógica actual (`pagerState.animateScrollToPage(index)`); el swipe del pager sigue sincronizando la selección.
- El indicador píldora animado y la aparición de la etiqueta en el ítem activo vienen incluidos en `NavigationBarItem`.
- Háptica: `LocalHapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)` al pulsar un destino.

### Temas

Cero colores hardcodeados. Contenedor `surfaceContainerHigh`; indicador, iconos y etiquetas con los defaults M3 (`secondaryContainer`, `onSurface`, `onSurfaceVariant`). Los 4 esquemas (claro, oscuro, Matrix, alto contraste) funcionan sin cambios adicionales.

### Manejo de errores y accesibilidad

- Componente puramente visual, sin estados de error.
- Accesibilidad preservada por usar componentes oficiales: semántica de selección (`selected`), `contentDescription` en iconos con el título del destino, targets táctiles ≥ 48dp garantizados por `NavigationBarItem`.

### Testing

- No se agregan tests de Compose UI (el proyecto no tiene infraestructura de tests de UI; se mantiene así).
- `@Preview` de `FloatingNavBar` para verificación visual rápida en Android Studio.
- Checklist manual post-implementación:
  - [ ] 4 temas (claro, oscuro, Matrix, alto contraste)
  - [ ] Tap en cada destino + swipe entre páginas (sincronización de selección)
  - [ ] FAB visible solo en Home y Lista, sin solaparse con la píldora
  - [ ] Rotación a landscape
  - [ ] TalkBack: anuncia destino y estado seleccionado

## Referencias

- Implementación actual: `app/src/main/java/com/example/appgasto/ui/navigation/MainPagerScreen.kt`
- Tokens de espaciado reutilizados: `ui/theme/Dimens.kt`
