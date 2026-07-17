# 📦 ACTUALIZACIONES - AppGasto

> **INSTRUCCIÓN:** Cada vez que se actualice una dependencia, marcar `[ ]` → `[x]` y anotar la fecha. Mantener este documento sincronizado con `gradle/libs.versions.toml`.

---

## 🔴 Actualizaciones mayores (Kotlin + AGP + Gradle)

| Componente | Actual | Última estable | Archivo | Estado |
|-----------|--------|----------------|---------|:------:|
| Gradle | 8.11.1 | 8.14.5 | `gradle/wrapper/gradle-wrapper.properties` | `[ ]` |
| AGP | 8.7.3 | 8.13.2 | `gradle/libs.versions.toml` | `[ ]` |
| Kotlin | 2.1.0 | 2.4.10 | `gradle/libs.versions.toml` | `[ ]` |
| KSP | 2.1.0-1.0.29 | (emparejar con Kotlin) | `gradle/libs.versions.toml` | `[ ]` |
| Compose BOM | 2024.12.01 | Buscar 2025.x / 2026.x | `gradle/libs.versions.toml` | `[ ]` |

> **Nota Gradle:** 9.6.1 ya existe, pero AGP 8.13.2 requiere Gradle 8.x. Saltar a Gradle 9.x requiere AGP 9.x (aún en alpha). Mejor quedarse en 8.14.5 por ahora.

### Pasos para Kotlin + AGP + Gradle

1. Actualizar Gradle wrapper: `./gradlew wrapper --gradle-version 8.14.5`
2. Actualizar AGP en `libs.versions.toml`: `agp = "8.13.2"`
3. Actualizar Kotlin en `libs.versions.toml`: `kotlin = "2.4.10"`
4. Actualizar KSP en `libs.versions.toml`: buscar versión compatible con Kotlin 2.4.10
5. Actualizar Compose BOM en `libs.versions.toml`: buscar última 2025.x o 2026.x
6. Sincronizar Gradle, compilar, corregir deprecations

---

## 🟡 Actualizaciones medianas (AndroidX)

| Componente | Actual | Última estable | Estado |
|-----------|--------|----------------|:------:|
| Room | 2.7.0 | 2.8.4 | `[ ]` |
| Lifecycle | 2.8.7 | 2.11.0 | `[ ]` |
| Core KTX | 1.15.0 | 1.19.0 | `[ ]` |
| Activity Compose | 1.9.3 | 1.13.0 | `[ ]` |
| Work Manager | 2.10.0 | 2.11.2 | `[ ]` |
| Hilt Navigation Compose | 1.2.0 | 1.4.0 | `[ ]` |
| Hilt Work | 1.2.0 | 1.4.0 | `[ ]` |
| Navigation Compose | 2.8.4 | 2.9.x (verificar) | `[ ]` |

### Pasos para AndroidX

```toml
# En gradle/libs.versions.toml, reemplazar:
room = "2.8.4"
lifecycle = "2.11.0"
coreKtx = "1.19.0"
activityCompose = "1.13.0"
workRuntime = "2.11.2"
hiltNavigationCompose = "1.4.0"
hiltWork = "1.4.0"
navigationCompose = "2.9.2"   # verificar última estable
```

---

## 🟢 Actualizaciones menores

| Componente | Actual | Última estable | Estado |
|-----------|--------|----------------|:------:|
| Hilt | 2.54 | 2.60.1 | `[ ]` |
| JUnit | 4.13.2 | 4.13.2 | ✅ Actual |
| Espresso | 3.6.1 | 3.6.1 | ✅ Actual |
| AppCompat | 1.7.0 | 1.7.0 | ✅ Actual |
| MLKit Doc Scanner | 16.0.0 | 16.0.0 | ✅ Actual |
| MLKit Text Recognition | 16.0.1 | 16.0.1 | ✅ Actual |

### Ya están actualizadas (sin cambios)

| Componente | Versión |
|-----------|---------|
| DataStore Preferences | 1.1.1 |
| Glance (Widgets) | 1.1.1 |
| Gson | 2.11.0 |
| Retrofit | 2.11.0 |
| OkHttp | 4.12.0 |
| Desugaring JDK | 2.1.2 |
| Macrobenchmark | 1.3.2 |

---

## 🧹 Configuraciones obsoletas a limpiar

| Archivo | Línea | Flag | Motivo | Estado |
|---------|:-----:|------|--------|:------:|
| `gradle.properties` | 4 | `android.useAndroidX=true` | Default `true` desde AGP 7.0+. Innecesario. | `[ ]` |
| `gradle.properties` | 6 | `android.nonTransitiveRClass=true` | Default `true` desde AGP 8.0. Innecesario. | `[ ]` |
| `app/build.gradle.kts` | 23 | `vectorDrawables.useSupportLibrary = true` | Innecesario con `minSdk = 26`. | `[ ]` |

### Instrucciones de limpieza

```properties
# gradle.properties — ELIMINAR estas 2 líneas:
android.useAndroidX=true
android.nonTransitiveRClass=true
```

```kotlin
// app/build.gradle.kts — ELIMINAR esta línea:
vectorDrawables.useSupportLibrary = true
```

---

## 📋 Orden recomendado de aplicación

| Prioridad | Qué | Riesgo |
|:---------:|-----|:------:|
| 1 | 🧹 Limpiar flags obsoletos (3 líneas) | Nulo |
| 2 | Gradle 8.11.1 → 8.14.5 | Bajo |
| 3 | AGP 8.7.3 → 8.13.2 | Bajo |
| 4 | Kotlin 2.1.0 → 2.4.10 + KSP | Medio |
| 5 | Compose BOM 2024.12 → actual | Bajo |
| 6 | Room 2.7.0 → 2.8.4 | Bajo |
| 7 | AndroidX (lifecycle, core, activity, work, navigation, hilt-*) | Bajo |
| 8 | Hilt 2.54 → 2.60.1 | Bajo |

> **Regla de oro:** Actualizar de a uno, compilar, verificar que funciona, luego seguir con el siguiente. No todo junto.

---

*Generado: 2026-07-16*
