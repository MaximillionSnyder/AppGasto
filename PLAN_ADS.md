# Plan de Monetización con Anuncios — AppGasto

> Stack objetivo: **GMA Next-Gen SDK** (`com.google.android.libraries.ads.mobile.sdk:ads-mobile-sdk:1.4.0`).
> Alcance acordado: banner adaptativo en las 4 pantallas del pager + rewarded opt-in con 2 recompensas.
> Cuenta AdMob: aún no creada → se usan IDs de prueba oficiales + constantes centralizadas.

> **⚠️ NOTA (sep 2026, ver `PLAN_PRO.md`):** este plan quedó **postergado** en favor del pago único Pro.
> La recompensa rewarded "tema Matrix 24 h" (+ su migración de 30 días, sección 1 y Fases 4-5) quedó **descartada**:
> Matrix/HIGH_CONTRAST permanecen **gratis siempre** por decisión de `PLAN_PRO.md`. Si este plan se retoma,
> la rewarded solo aplicaría para "sin banner 24 h" y habría que resolver la convivencia con el gating Pro
> (features free vs desbloqueo por anuncio).

---

## 1) Resumen y decisiones

| Decisión | Valor elegido |
|---|---|
| Formatos a implementar | Banner adaptativo anclado + Rewarded |
| Formatos a **no** implementar | Interstitial, App Open, Ads en notificaciones/widget/lockscreen |
| Alcance del banner | Las 4 tabs del `MainPagerScreen.kt:48` vía `Scaffold.bottomBar` |
| Recompensa rewarded | **Ambas**: (a) sin banner 24 h + (b) tema Matrix 24 h — el usuario elige |
| Cuenta AdMob | Todavía no → usar IDs de prueba y dejar `AdsConfig.kt` listo |

Caveat Matrix: hoy el tema Matrix es gratis. Al pasar a recompensa quedará bloqueado por defecto
y se desbloquea 24 h viendo un anuncio. Mitigación: migración one-shot de 30 días de gracia para
usuarios que ya tienen `ThemeMode.MATRIX` seleccionado.

---

## 2) Reglas de Google Play (política Ads — 2026)

- **Los ads son parte de la app**: deben cumplir contenido restringido y rating.
- **Deceptive ads prohibidos**: no simular UI/notificaciones del sistema; obligación de etiquetar "Anuncio".
- **Disruptive ads**: interstitials full-screen inesperados prohibidos; no antes del splash;
  no al inicio de un segmento de contenido; deben cerrarse en ≤ 15 s (rewarded opt-in exento).
- **Made for Ads**: repetición de interstitials que impida usar la app → retiro/suspensión.
- **Lockscreen monetization**: prohibida (widget tampoco admite ads).
- **Ad fraud**: jamás clickear propios ads.
- **Families**: no aplica (app 13+; no declarar niños).

Fuente principal: `support.google.com/googleplay/android-developer/answer/9857753` (Better Ads Experiences).

### Lo que más causa rechazos — no implementar

1. Interstitial al abrir / antes del splash (incluye App Open en frío)
2. Interstitial durante flujo core (FAB agregar, OCR, Onboarding)
3. Interstitial de alta frecuencia (tras cada guardado)
4. Native sin etiqueta "Anuncio" o con cierre falso
5. Ads en notificaciones (`BudgetWorker`) / widget / lockscreen
6. Data safety / declaración "contiene anuncios" mal llenada

---

## 3) Arquitectura actual relevante

- `MainActivity.kt:28` → `AppNavigation.kt:26` → `MainPagerScreen.kt:56` (Scaffold + HorizontalPager 4 tabs) + `AddEditScreen.kt:318` (ruta separada)
- `AppGastoApplication.kt:22` (Hilt + WorkManager)
- `PreferencesRepository.kt:30` / `UserPreferences.kt` (DataStore)
- `settings.gradle.kts` ya incluye `google()` → Maven del SDK disponible
- `targetSdk 35`, `minSdk 26`, Kotlin 2.1 → requisitos SDK Next-Gen OK (24/35/1.9+)

---

## 4) Plan por fases

### Fase 1 — Infraestructura (Gradle + Manifest)

| Archivo | Cambio |
|---|---|
| `gradle/libs.versions.toml` | `adsMobileSdk = "1.4.0"` + `ads-mobile-sdk = { group="com.google.android.libraries.ads.mobile.sdk", name="ads-mobile-sdk" }` |
| `app/build.gradle.kts:104` | `implementation(libs.ads.mobile.sdk)` |
| `AndroidManifest.xml` | `<meta-data android:name="com.google.android.gms.ads.APPLICATION_ID" android:value="ca-app-pub-3940256099942544~3347511713" />` (muestra oficial, TODO reemplazar) |
| **nuevo** `ads/AdsConfig.kt` | `ADMOB_APP_ID`, `BANNER_AD_UNIT_ID`, `REWARDED_AD_UNIT_ID` — test vs real según `BuildConfig.DEBUG` |

> UMP SDK va incluido en el artefacto Next-Gen; si no resuelve `UserMessagingPlatform`, agregar `com.google.android.ump:user-messaging-platform:3.x`.

### Fase 2 — Consentimiento UMP + inicialización

**nuevo** `ads/AdsManager.kt` (`@Singleton` Hilt, patrón canónico `GoogleMobileAdsConsentManager`):

```kotlin
UserMessagingPlatform.getConsentInformation(ctx)
  → requestConsentInfoUpdate(activity, params)
  → loadAndShowConsentFormIfRequired(activity) { formError -> ... }
  → if (canRequestAds()) { MobileAds.initialize(ctx, InitializationConfig.Builder(appId).build()) }
```

- Se ejecuta en cada lanzamiento desde `MainActivity` (Launch **antes** de precargar ads — el SDK precarga al inicializar).
- Expone `StateFlow<AdsState>` + helpers `canShowRewarded`, `showRewarded(activity, onReward)`.
- Extras solo debug: registro de hashed device ID + `ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA`.
- Usa `RewardedAdPreloader.start(adUnitId, PreloadConfiguration(...))`; display con `pollAd()` + `ad.show(activity) { rewardItem -> }`.

**nuevo** `ads/AdsViewModel.kt` (wrapper observable para Compose).

Entrada: `MainActivity.kt` — `LaunchedEffect(Unit) { adsViewModel.gatherConsentAndInit(activity) }`.

### Fase 3 — Banner adaptativo anclado

**nuevo** `ads/BannerAdComposable.kt`:

```kotlin
val adSize = AdSize.getLargeAnchoredAdaptiveBannerAdSize(context, widthDp)
BannerAd.load(BannerAdRequest.Builder(AD_UNIT_ID, adSize).build()) // AdLoadResult.Success / Failure
AndroidView(factory = { bannerAd.getView(activity) })
DisposableEffect(Unit) { onDispose { bannerAdState?.destroy() } }
```

- `ui/navigation/MainPagerScreen.kt:56` → `Scaffold(bottomBar = { if (!adFree && adsReady) BannerAd() })`.
- Oculto mientras `adFreeUntil > now` (reward activo) o si aún no hay consent. Sin hueco si falla.
- No toca `AddEditScreen` / `Onboarding` (formulario sin interrupción → política segura).

IDs de prueba Next-Gen: banner `ca-app-pub-3940256099942544/9214589741`, rewarded `ca-app-pub-3940256099942544/5224354917`.

### Fase 4 — Preferencias de recompensas

| Archivo | Cambio |
|---|---|
| `domain/model/UserPreferences.kt` | `+ adFreeUntil: Long = 0L`, `matrixUnlockedUntil: Long = 0L` |
| `data/repository/PreferencesRepository.kt:30` | 2 `longPreferencesKey`, `grantAdFree24h()`, `grantMatrix24h()`, mapping en `preferencesFlow` |
| `AppGastoApplication.kt:26` | `applicationScope.launch { preferencesRepository.migrateMatrixUsers() }` — si `THEME_MODE==MATRIX` y primera vez → `matrixUnlockedUntil = now + 30d` + flag `MATRIX_MIGRATION_DONE` |

Helpers: `isAdFree(prefs)` / `isMatrixUnlocked(prefs)` / `effectiveThemeMode(prefs)` (MATRIX expirado → SYSTEM).

### Fase 5 — UI de recompensas

- `ui/settings/SettingsScreen.kt`: nueva sección "Recompensas" (2 filas con estado `Activo hasta %s`, botón `Ver anuncio` deshabilitado si `!RewardedAdPreloader.isAdAvailable()`). Fila condicional "Opciones de privacidad" si `privacyOptionsRequirementStatus == REQUIRED` → `UserMessagingPlatform.showPrivacyOptionsForm()`.
- `ui/settings/ThemeSettingsDialog.kt`: entrada Matrix con candado cuando bloqueada; tap → diálogo "desbloquear 24 h viendo un anuncio" → `AdsManager.showRewarded` → `grantMatrix24h()` + seleccionar tema.
- `MainActivity.kt`: `isDark/isMatrix` usan `effectiveThemeMode()`.

### Fase 6 — Strings (9 archivos)

Claves nuevas (~12) en `values/`, `values-en/`, `values-de/`, `values-it/`, `values-ja/`, `values-ko/`, `values-pt/`, `values-qu/` (+ default es):

`ads_section_title`, `reward_adfree_title/desc/status`, `reward_matrix_title/desc/status`,
`reward_watch_ad`, `reward_active_until`, `reward_ad_not_available`,
`matrix_locked_title/text/confirm/cancel`, `privacy_options`.

### Fase 7 — Verificación

```bash
./gradlew assembleDebug
./gradlew :app:assembleRelease  # R8 minify activo
```

Checklist manual:
- [ ] Banner test ad visible en 4 tabs, FAB no cubierto
- [ ] Banner oculto durante reward "sin anuncios" (24 h)
- [ ] Rewarded (Ajustes): otorga 24 h de cada recompensa; botón deshabilitado si no hay ad
- [ ] Matrix: bloqueado → tap → rewarded → desbloqueado 24 h; expiración vuelve a SYSTEM
- [ ] Formulario UMP con `DebugGeography_EEA` + test device

Test unitario opcional: lógica de expiración (`isAdFree`, `effectiveThemeMode`) — JUnit ya configurado.

### Fase 8 — Checklist administrativo (antes de publicar)

1. **AdMob**: crear cuenta → registrar app (App ID real) → 2 ad units (banner anclado, rewarded) → reemplazar 3 IDs en `AdsConfig.kt` + manifest.
2. **AdMob → Privacidad y mensajería**: configurar mensaje GDPR (EEA/UK/CH) y opcional US states.
3. **Play Console → App content → Ads**: `Contiene anuncios: Sí`, formatos banner+rewarded, max ad content rating `G`.
4. **Data safety** (según guía oficial AdMob): Device IDs, App activity, Diagnostics → propósito Advertising, cifrado en tránsito.
5. **Política de privacidad**: URL actualizada mencionando AdMob.
6. Limpiar debug UMP (test device/geography) antes del release.
7. Recomendado: cambiar `applicationId` de `com.example.appgasto` antes de linkear Play/AdMob.

---

## 5) Archivos tocados — resumen

| Nuevo | Modificado |
|---|---|
| `ads/AdsConfig.kt`, `ads/AdsManager.kt`, `ads/AdsViewModel.kt`, `ads/BannerAdComposable.kt` | `gradle/libs.versions.toml`, `app/build.gradle.kts`, `AndroidManifest.xml`, `AppGastoApplication.kt`, `MainActivity.kt`, `data/repository/PreferencesRepository.kt`, `domain/model/UserPreferences.kt`, `ui/navigation/MainPagerScreen.kt`, `ui/settings/SettingsScreen.kt`, `ui/settings/ThemeSettingsDialog.kt`, `res/values*/strings.xml` |

---

## 6) Alternativas descartadas

- Interstitial/App Open: alto riesgo de rechazo, no compensa en una app de utilidad.
- Native en lista: más complejo y riesgo "deceptive" si no se etiqueta perfecto.
- Bloquear Matrix sin gracia: se optó por 30 días para no penalizar usuarios actuales.

---

## 7) Referencias

- GMA Next-Gen quick-start / banner / rewarded / UMP: `developers.google.com/admob/android/next-gen/*`
- Ads policy Play: `support.google.com/googleplay/android-developer/answer/9857753`
- Test IDs Next-Gen: `.../9214589741` (banner), `.../5224354917` (rewarded), App ID `~3347511713`
