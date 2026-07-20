<div align="center">

# 💰 AppGasto

### Control de gastos diarios — inteligente, rápido y multi-moneda

<br>

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Compose-BOM_2024.12-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/develop/ui/compose)
[![Material 3](https://img.shields.io/badge/Material_3-You-FF6F00?style=for-the-badge&logo=materialdesign&logoColor=white)](https://m3.material.io)
[![API](https://img.shields.io/badge/API-26%2B-00C853?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/about/versions/oreo)
[![License](https://img.shields.io/badge/License-MIT-FBBC04?style=for-the-badge)](LICENSE)
[![Build](https://img.shields.io/badge/Build-Passing-00C853?style=for-the-badge&logo=githubactions&logoColor=white)](https://github.com/MaximillionSnyder/AppGasto/actions)

<br>

<picture>
  <source media="(prefers-color-scheme: dark)" srcset="https://img.shields.io/badge/3_Temas-00FF41?style=flat-square&logo=matrix&labelColor=0A0F0A">
  <img alt="Themes" src="https://img.shields.io/badge/3_Temas-FF6F00?style=flat-square&logo=materialdesign&logoColor=white">
</picture>
&nbsp;
<img alt="Screens" src="https://img.shields.io/badge/5_Pantallas-2196F3?style=flat-square">
&nbsp;
<img alt="Languages" src="https://img.shields.io/badge/8_Idiomas-FF5722?style=flat-square">
&nbsp;
<img alt="Currencies" src="https://img.shields.io/badge/Multi_Moneda-9C27B0?style=flat-square">
&nbsp;
<img alt="Receipt Scanner" src="https://img.shields.io/badge/Escanear_Recibos-00BCD4?style=flat-square">

<br><br>

**App Android nativa** para registrar gastos pequeños del día a día con widget, 5 temas (claro/oscuro/sistema/matrix/alto contraste), soporte multi-idioma, multi-moneda (25 divisas + onboarding de moneda base), tasas de cambio en tiempo real y escaneo inteligente de recibos.

<br>

[![Google Play](https://img.shields.io/badge/Google_Play-Disponible-414141?style=for-the-badge&logo=googleplay&logoColor=white)](https://github.com/MaximillionSnyder/AppGasto/releases)
[![APK Debug](https://img.shields.io/badge/APK_Debug-Descargar-FF6D00?style=for-the-badge&logo=android&logoColor=white)](https://github.com/MaximillionSnyder/AppGasto/releases)

</div>

---

## ⚡ Features

<div align="center">

| 📱 | 🌟 | 💰 | 🌍 |
|:--:|:--:|:--:|:--:|
| **5 Pantallas** | **5 Temas** | **Multi-Moneda** | **8 Idiomas** |
| Inicio • Lista • Agregar<br>Estadísticas • Ajustes | Claro • Oscuro • Sistema<br>🟢 Matrix • Alto contraste | 25 divisas + moneda base<br>Onboarding al primer uso | Español • English • Português • Italiano<br>Deutsch • 日本語 • 한국어 • Runasimi |

| 📸 | 💾 | 📊 | 🏷️ |
|:--:|:--:|:--:|:--:|
| **Escanear Recibos** | **Respaldo** | **Gráficas** | **7 Categorías** |
| ML Kit Document Scanner<br>+ OCR + auto-llenado | Export CSV • JSON<br>Auto Backup Drive | Donut chart por categoría<br>Periodos diario/semanal/mensual | Comida • Transporte • Ocio • Hogar<br>Salud • Ropa • Otros |

</div>
---

## 🛠️ Stack Técnico

<div align="center">

| Categoría | Tecnología | Versión |
|:---------:|:----------:|:-------:|
| 🎨 **UI** | Jetpack Compose + Material 3 | BOM 2024.12 |
| 🧠 **Lenguaje** | Kotlin | 2.1.0 |
| 🗄️ **Base de datos** | Room + KSP | 2.7.0 |
| ⚙️ **Preferencias** | DataStore Preferences | 1.1.1 |
| 💉 **DI** | Hilt + KSP | 2.54 |
| 📱 **Widget** | Glance | 1.1.1 |
| 🧭 **Navegación** | Navigation Compose | 2.8.4 |
| 📈 **Gráficas** | Canvas custom (Donut Chart) | — |
| 🌐 **Network** | Retrofit + OkHttp | 2.11.0 / 4.12.0 |
| 🤖 **ML Kit** | Document Scanner + Text Recognition | 16.0.0 / 16.0.1 |
| ⏰ **Background** | WorkManager | 2.10.0 |
| 🏗️ **Build** | Gradle + AGP | 8.7.3 / 8.7.3 |
| 💾 **Respaldo** | Auto Backup + JSON + CSV | — |

</div>

---

## 📸 Screenshots

<div align="center">

```
┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐
│  🏠 Inicio  │  │  📋 Lista  │  │  📊 Stats  │  │  ⚙️ Ajustes │
│──────────│  │──────────│  │──────────│  │──────────│
│ S/ 45.90 │  │ 🍔 Comida  │  │ 📈 Donut │  │ 🌙 Tema   │
│ Total hoy│  │ 🚗 Transp │  │ por cat. │  │ 💰 Moneda │
│  ───────  │  │   ...    │  │  ───────  │  │ 📸 Scan   │
│  3 gastos │  │  filters │  │ período  │  │           │
└──────────┘  └──────────┘  └──────────┘  └──────────┘
```

</div>

---

## 🚀 Empezar

<details open>
<summary><b>📦 Requisitos</b></summary>

- Android Studio Hedgehog (2023.1.1)+
- JDK 17 (incluido con Android Studio)
- Dispositivo/Emulador Android 8.0+ (API 26)
- Google Play Services (para ML Kit)

</details>

<details>
<summary><b>🔧 Instalación</b></summary>

```bash
# 1. Clonar
git clone https://github.com/MaximillionSnyder/AppGasto.git

# 2. Abrir en Android Studio
# File → Open → seleccionar carpeta AppGasto

# 3. Build & Run
./gradlew installDebug
```

</details>
---

## 🌐 Idiomas

<div align="center">

| Código | Idioma | | Código | Idioma |
|:------:|:------:|:-:|:------:|:------:|
| `es` | 🇪🇸 **Español** | Default | `de` | 🇩🇪 Deutsch |
| `en` | 🇬🇧 **English** | | `ja` | 🇯🇵 日本語 |
| `pt` | 🇧🇷 **Português** | | `ko` | 🇰🇷 한국어 |
| `it` | 🇮🇹 **Italiano** | | `qu` | Runasimi (Quechua) |

</div>
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
*Una app de MaximillionSnyder*

<br>

[![GitHub](https://img.shields.io/badge/GitHub-MaximillionSnyder-181717?style=for-the-badge&logo=github)](https://github.com/MaximillionSnyder)
[![Kotlin](https://img.shields.io/badge/made_with-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)

<br>

[⬆ Volver arriba](#-appgasto)

</div>
