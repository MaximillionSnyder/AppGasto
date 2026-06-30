# AppGasto

App Android para registrar gastos pequeños del día a día, con widget, dark mode, soporte multi-idioma y gráficas.

## Características

- **5 pantallas**: Inicio (resumen del día), Lista (historial filtrable), Agregar/Editar gasto, Estadísticas, Ajustes
- **Widget**: Total del día y acceso rápido desde la pantalla principal
- **8 idiomas**: Español, English, Português, Italiano, Deutsch, 日本語, 한국어, Runasimi
- **Dark mode**: Claro, Oscuro o seguir al sistema
- **7 categorías** predefinidas (editables en la DB): Comida, Transporte, Ocio, Hogar, Salud, Ropa, Otros
- **Presupuesto mensual** con alertas al 80% y 100%
- **Gráficas**: Resumen por categoría con barras de progreso
- **Respaldo**: Exportar/Importar datos en JSON + Auto Backup a Google Drive (sin config extra)
- **Material 3**: Diseño moderno con paleta naranja

## Requisitos

- Android Studio Hedgehog (2023.1.1) o más nuevo
- JDK 17 (incluido con Android Studio)
- Dispositivo Android 8.0+ (API 26) o emulador

## Cómo compilar e instalar

1. Clona el repo:
   ```bash
   git clone https://github.com/MaximillionSnyder/AppGasto.git
   ```

2. Abre la carpeta `AppGasto` en Android Studio:
   - File → Open → seleccionar la carpeta `AppGasto`
   - Esperar a que Gradle sincronice dependencias (la primera vez tarda unos minutos)

3. Conecta tu Android por USB con depuración USB activada, o inicia un emulador

4. Dale a Run (▶) o ejecuta:
   ```bash
   ./gradlew installDebug
   ```

## Stack técnico

| Capa | Tecnología |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Base de datos | Room (SQLite) |
| Preferencias | DataStore |
| DI | Hilt |
| Widget | Glance |
| Navegación | Navigation Compose |
| Gráficas | Vico |
| Background | WorkManager |
| Respaldo | Auto Backup (Google Drive) + JSON manual |
| Multi-idioma | Android resources + AppCompatDelegate |

## Estructura del proyecto

```
app/src/main/java/com/example/appgasto/
├── data/
│   ├── local/        (Room: Expense, Category, DAOs, AppDatabase)
│   ├── repository/   (ExpenseRepository, PreferencesRepository)
│   └── backup/       (BackupManager, export/import JSON)
├── domain/model/     (ThemeMode, AppLanguage, UserPreferences)
├── ui/
│   ├── home/         (Inicio: resumen del día)
│   ├── add/          (Agregar/Editar gasto)
│   ├── list/         (Historial con filtros)
│   ├── stats/        (Estadísticas y gráficas)
│   ├── settings/     (Ajustes, tema, idioma, presupuesto, backup)
│   ├── navigation/   (Rutas del NavHost)
│   ├── theme/        (Material 3 colores, tipografía, dark mode)
│   └── components/   (ExpenseItem, CategorySelector)
├── widget/           (Glance widget)
├── notifications/    (WorkManager para alertas de presupuesto)
└── di/               (Módulos Hilt)
```

## Idiomas soportados

| Código | Idioma |
|---|---|
| `es` (default) | Español |
| `en` | English |
| `pt` | Português |
| `it` | Italiano |
| `de` | Deutsch |
| `ja` | 日本語 |
| `ko` | 한국어 |
| `qu` | Runasimi (Quechua) |

Para agregar un nuevo idioma: copia `values/strings.xml` a `values-XX/strings.xml` y traduce los textos.

## Licencia

MIT
