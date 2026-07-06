<div align="center">

# 💰 AppGasto

### Control de gastos diarios para Android

![Kotlin](https://img.shields.io/badge/Kotlin-100%25-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)
![API](https://img.shields.io/badge/API-26%2B-brightgreen)
![License](https://img.shields.io/badge/License-MIT-yellow)
![Build](https://img.shields.io/badge/Build-Passing-brightgreen)
![Themes](https://img.shields.io/badge/Themes-3-00FF41?logo=matrix)

<br>

App Android para registrar gastos pequeños del día a día, con widget, dark mode, soporte multi-idioma y gráficas.

</div>

---

## ✨ Características

<table>
<tr>
<td width="50%">

#### 📱 5 Pantallas
- **Inicio** — Resumen del día
- **Lista** — Historial filtrable
- **Agregar/Editar** — Registro rápido
- **Estadísticas** — Gráficas por categoría
- **Ajustes** — Configuración completa

</td>
<td width="50%">

#### 🌟 Funcionalidades
- 🌙 **3 temas** — Claro, Oscuro y Matrix (verde neón)
- 🏷️ **7 categorías** — Comida, Transporte, Ocio, Hogar, Salud, Ropa, Otros
- 💸 **Presupuesto mensual** — Alertas al 80% y 100%
- 📊 **Gráficas** — Resumen visual por categoría

</td>
</tr>
</table>

<table>
<tr>
<td width="50%">

#### 🌍 8 Idiomas
Español • English • Português • Italiano • Deutsch • 日本語 • 한국어 • Runasimi

</td>
<td width="50%">

#### 💾 Respaldo
- Exportar/Importar JSON
- Auto Backup a Google Drive

</td>
</tr>
</table>

---

## 📸 Capturas

<div align="center">

| Inicio | Lista | Estadísticas | Ajustes |
|:------:|:-----:|:------------:|:-------:|
| 🏠 | 📋 | 📊 | ⚙️ |

</div>

---

## 🛠️ Stack Técnico

<div align="center">

| Componente | Tecnología |
|:----------:|:----------:|
| 🎨 UI | Jetpack Compose + Material 3 |
| 🗄️ Base de datos | Room (SQLite) |
| ⚙️ Preferencias | DataStore |
| 💉 DI | Hilt |
| 📱 Widget | Glance |
| 🧭 Navegación | Navigation Compose |
| 📈 Gráficas | Canvas custom (donut chart) |
| ⏰ Background | WorkManager |
| 💾 Respaldo | Auto Backup + JSON |
| 🌍 Multi-idioma | Android Resources |

</div>

---

## 🚀 Cómo Compilar

### Requisitos
- Android Studio Hedgehog (2023.1.1) o más nuevo
- JDK 17 (incluido con Android Studio)
- Dispositivo Android 8.0+ (API 26) o emulador

### Instalación

```bash
# 1. Clonar el repositorio
git clone https://github.com/MaximillionSnyder/AppGasto.git

# 2. Abrir en Android Studio
# File → Open → seleccionar carpeta AppGasto

# 3. Ejecutar
./gradlew installDebug
```

---

## 📂 Estructura del Proyecto

```
app/src/main/java/com/example/appgasto/
│
├── 📁 data/
│   ├── local/        → Room: Expense, Category, DAOs, AppDatabase
│   ├── repository/   → ExpenseRepository, PreferencesRepository
│   └── backup/       → BackupManager, export/import JSON
│
├── 📁 domain/model/  → ThemeMode, AppLanguage, UserPreferences
│
├── 📁 ui/
│   ├── home/         → Inicio: resumen del día
│   ├── add/          → Agregar/Editar gasto
│   ├── list/         → Historial con filtros
│   ├── stats/        → Estadísticas y gráficas
│   ├── settings/     → Ajustes, tema, idioma, presupuesto
│   ├── navigation/   → Rutas del NavHost
│   ├── theme/        → Material 3 colores, tipografía
│   └── components/   → ExpenseItem, CategorySelector
│
├── 📁 widget/        → Glance widget
├── 📁 notifications/ → WorkManager para alertas
└── 📁 di/            → Módulos Hilt
```

---

## 🌐 Idiomas Soportados

| Código | Idioma | |
|:------:|:------:|:-:|
| `es` | 🇪🇸 Español | Default |
| `en` | 🇬🇧 English | |
| `pt` | 🇧🇷 Português | |
| `it` | 🇮🇹 Italiano | |
| `de` | 🇩🇪 Deutsch | |
| `ja` | 🇯🇵 日本語 | |
| `ko` | 🇰🇷 한국어 | |
| `qu` | Runasimi | Quechua |

> 💡 **¿Agregar un idioma?** Copia `values/strings.xml` a `values-XX/strings.xml` y traduce los textos.

---

## 📄 Licencia

```
MIT License

Copyright (c) 2026 MaximillionSnyder

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
```

---

<div align="center">

**Hecho con ❤️ para Android**

[![GitHub](https://img.shields.io/badge/GitHub-MaximillionSnyder-181717?logo=github)](https://github.com/MaximillionSnyder)

</div>
