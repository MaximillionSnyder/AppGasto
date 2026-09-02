# Plan Pro (pago único) — AppGasto

> Alcance acordado: 1 producto `inapp` no-consumible `pro_unlock` (~$3.99 USD base).
> Decisiones cerradas: CSV → Pro, Matrix/HIGH_CONTRAST → gratis siempre, updater GitHub se elimina en track Play (conflicto política self-update).
> Cuenta Play: aún no creada → usar Billing Library + DataStore `is_pro`, IDs reales al publicar.

---

## 1) Precio: mínimo y recomendado

Mínimos oficiales Play (`play.google.com/supported-locations`):

- USA: **$0.99 USD**
- Perú: **S/.0.99 PEN**
- No existe $0.49. No se puede publicar por debajo del mínimo por país.

Comisión 2026: **15% hasta $1M/año** (30% después). Neto de $3.99 ≈ $3.39.

| Precio base (USD) | Conversión auto PE | Neto aprox | Veredicto |
|---|---|---|---|
| $1.99 | ~S/.7.5 | ~$1.69 | Barato, devalúa Pro |
| **$3.99** | **~S/.14.9** | **~$3.39** | **Recomendado: sweet spot utilidad offline** |
| $4.99 | ~S/.18.9 | ~$4.24 | Ok si incluye auto-backup + fotos |

Pasos en Play Console: `Monetize with Play > Products > One-time products > pro_unlock`
→ precio base **$3.99 USD** → dejar autoconversión → ajustar PE a `.99` psicológico.
Optimización posterior sin código vía `Price experiments`.

---

## 2) Matriz Free vs Pro final

| Feature (archivo actual) | Free | Pro `pro_unlock` |
|---|---|---|
| Registro ilimitado, 5 pantallas, multi-moneda 25 divisas, stats donut, 1 presupuesto mensual, export JSON manual | ✅ | ✅ |
| Temas `LIGHT/DARK/SYSTEM/MATRIX/HIGH_CONTRAST` (`ui/settings/ThemeSettingsDialog.kt:78-93`, `MainActivity.kt:42-50`) | ✅ todos gratis, sin gating, sin migración | — |
| Presupuesto avanzado por categorías (`ui/advancedbudget/AdvancedBudgetScreen.kt:51`, toggle `ui/settings/SettingsScreen.kt:289`, 5° tab `ui/navigation/MainPagerScreen.kt:41`) | ❌ oculto | ✅ |
| Escaneo recibos (`ui/add/AddEditScreen.kt:193`, `AddEditViewModel.handleScanResult():121`) | 10/mes | ilimitado |
| Export CSV (`ui/settings/SettingsScreen.kt:323`, `SettingsViewModel.exportCsv():332`) | ❌ paywall | ✅ ilimitado |
| Auto-backup diario + fotos recibos ZIP (futuro `newfeatures.md` 3.2/3.3) | ❌ | ✅ cuando existan |

Sin `proGraceUntil` para temas. Solo gracia 30 días para quien ya activó `advancedBudgetEnabled=true`.

---

## 3) Arquitectura nueva

```
billing/
  BillingConfig.kt      // PRODUCT_ID="pro_unlock", constantes
  BillingManager.kt     // @Singleton Hilt: BillingClient, query, launch, acknowledge
  BillingViewModel.kt   // wrapper StateFlow<isPro> para Compose
di/
  BillingModule.kt      // provee BillingClient singleton con enablePendingPurchases()
ui/settings/
  ProPaywallDialog.kt   // beneficios + precio + Comprar + Restaurar
  ProSettingsRow.kt     // fila "AppGasto Pro" con badge PRO / Comprado (integrada en SettingsScreen)
```

Flujo canónico PBL 8:

```kotlin
BillingClient.newBuilder(ctx).enablePendingPurchases().build()
  → startConnection(BillingClientStateListener)
  → queryPurchasesAsync(QueryPurchasesParams(INAPP))
  → isPro = purchases.any { it.purchaseState == PURCHASED && (it.isAcknowledged || acknowledge()) }
  → launchBillingFlow(activity, BillingFlowParams(productDetails))
  → onPurchasesUpdated → acknowledgePurchase() → prefs.setPro(true)
```

Sin backend: `acknowledge` obligatorio en <3 días o Google reembolsa solo.
Fuente de verdad: `Play queryPurchasesAsync()` en cada arranque + espejo `is_pro` en DataStore para offline.

---

## 4) Plan por fases

### Fase 0 — Pre-Play (0.5 día, bloqueante)

1. Cambiar `applicationId` en `app/build.gradle.kts:40` de `com.example.appgasto` → ej. `com.appgasto.app`. Sin esto no se pueden crear productos.
2. Play Console: cuenta dev ($25 único) + perfil merchant + app nueva + producto `pro_unlock` inapp no-consumible activo.
3. `app/proguard-rules.pro`: agregar `-keep class com.android.billingclient.**`.

### Fase 1 — Persistencia Pro (0.5 día)

| Archivo | Cambio |
|---|---|
| `domain/model/UserPreferences.kt:3` | `+ isPro: Boolean = false`, `+ scanCountMonth: String = ""` (ej. `"2026-09"`), `+ scanCount: Int = 0`, `+ advancedGraceUntil: Long = 0L` |
| `data/repository/PreferencesRepository.kt:31` | 4 keys `is_pro`, `scan_month`, `scan_count`, `advanced_grace_until` + `setPro()`, `tryConsumeScanSlot(): Boolean` (resetea si cambia mes), `migrateLegacyAdvancedUsers()` (si `ADVANCED_BUDGET_ENABLED==true` → `advancedGraceUntil = now + 30d`) |

Helper: `isProEffective(prefs) = prefs.isPro || now < prefs.advancedGraceUntil`.

### Fase 2 — Billing core (1.5-2 días)

| Archivo | Cambio |
|---|---|
| `gradle/libs.versions.toml` | `billingKtx = "8.0.0"` + `billing-ktx = { group = "com.android.billingclient", name = "billing-ktx" }` |
| `app/build.gradle.kts:104` | `implementation(libs.billing.ktx)` |
| **nuevo** `billing/BillingConfig.kt` | `const val PRODUCT_ID = "pro_unlock"` |
| **nuevo** `billing/BillingManager.kt` | `@Singleton @Inject`, `StateFlow<BillingState(isPro, ProductDetails?)`, `connectAndQuery()`, `launchProPurchase(activity)`, `restore()` (= `queryPro()`), manejo `PENDING`, `USER_CANCELED`, `ITEM_ALREADY_OWNED` |
| **nuevo** `di/BillingModule.kt` | provee `BillingClient` singleton |
| **nuevo** `billing/BillingViewModel.kt` | expone `isPro`, `priceText`, `launch/restore` para Compose |
| `AppGastoApplication.kt:26` | `applicationScope.launch { billingManager.connectAndQuery() }` antes de cualquier gating |

### Fase 3 — Paywall UI (1 día)

| Archivo | Cambio |
|---|---|
| **nuevo** `ui/settings/ProPaywallDialog.kt` | Hero Pro, lista 5 beneficios, precio desde `ProductDetails.oneTimePurchaseOfferDetails.formattedPrice`, botones Comprar/Restaurar/Cerrar, estado cargando |
| `ui/settings/SettingsScreen.kt:262` | Nueva `ProSettingsSection` arriba de `AppearanceSettingsSection` (icono `WorkspacePremium`, badge). `showProDialog` state |
| `ui/settings/SettingsViewModel.kt:49` | `+ isPro, proPriceText` en `SettingsUiState`, colecta `preferencesFlow` + `billingManager` |
| Strings | ~14 claves x 8 locales (`values, -en, -de, -it, -ja, -ko, -pt, -qu`): `pro_title/desc/benefits_advanced/benefits_scan/benefits_csv/benefits_backup/benefits_support/buy/restore/owned/grace/scan_limit/scan_count` |

### Fase 4 — Gating (1 día)

1. **Presupuesto avanzado:** `SettingsScreen.kt:289 onAdvancedBudgetToggle` → si `!isProEffective` abre paywall en vez de activar. `MainPagerScreen.kt:41 pageCount` usa `advancedBudgetEnabled && isProEffective`. `AdvancedBudgetScreen.kt:51` muestra `ProLockedPlaceholder` si entra por deep-link.
2. **Scan cuota:** `AddEditViewModel.handleScanResult():121` → `if (!isPro && !prefs.tryConsumeScanSlot()) { error = scan_limit_pro; return }`. `AddEditScreen.kt:193` botón muestra `"Escanear (3/10)"` y abre paywall al agotar.
3. **CSV:** `SettingsScreen.kt:323 onCsvExportClick` → si `!isPro` paywall.
4. **Temas:** sin cambios (Matrix/HC gratis por decisión).

### Fase 5 — Eliminar updater GitHub en track Play (0.5 día, crítico)

Motivo: `REQUEST_INSTALL_PACKAGES` + descarga APK vía `FileProvider` viola política Play `Self-updating apps` si el build viene de Play.

**Borrar:**

- `data/updater/GitHubRelease.kt:5` entero (`GitHubRelease` + `GitHubAsset`).
- En `ui/settings/SettingsViewModel.kt`: `UpdateEvent:43`, campos `updateRelease/isCheckingUpdate/isDownloading:61-63`, `checkForUpdate():109`, `fetchLatestRelease():131`, `downloadAndInstall():160`, `downloadApk/installApk/compareVersions`, `dismissUpdateDialog`. Inyección `okHttpClient:72` + `gson` solo usadas por updater (tasas usan `ExchangeRateRepository`, no tocar `di/CurrencyModule.kt`).
- En `ui/settings/SettingsScreen.kt`: `showUpdateDialog:120`, `LaunchedEffect updateEvent:122`, `InfoSettingsSection onCheckUpdateClick:333`, bloque `AlertDialog update:440-508`, estado `updateRelease` en UI.
- En `app/proguard-rules.pro:17-18` keeps de `GitHubRelease/GitHubAsset`.
- Strings `update_*`, `checking_update`, `current_version`, `later`, `update_available_title`, `update_downloading`, `update_error`, `update_no_changelog`, `update_action` en los 8 `values*/strings.xml`.

**Modificar (no borrar):**

- `app/src/main/AndroidManifest.xml:7` quitar `REQUEST_INSTALL_PACKAGES`, `:9-13` quitar `<queries> INSTALL_PACKAGE`. **Mantener** `FileProvider:63-71` + `res/xml/file_paths.xml` (se reutiliza para compartir fotos de recibos Pro).
- `.github/workflows/release.yml:41-56` — ya no `assembleRelease APK + softprops/action-gh-release`. Cambiar a `bundleRelease` + subida a `internal testing` (o archivar si releases solo desde Play Console). `build.yml` sin cambios.

### Fase 6 — Verificación

```bash
./gradlew assembleDebug
./gradlew :app:bundleRelease  # R8 minify activo
```

Checklist manual:

- [ ] Compra test con tester de licencia (tarjeta `Se aprueba siempre`) otorga Pro y persiste tras reinicio
- [ ] `ITEM_ALREADY_OWNED` → Restaurar otorga Pro sin cobrar de nuevo
- [ ] Modo avión: `isPro` cacheado en DataStore sigue desbloqueando
- [ ] R8 release: Billing no crashea (regla keep)
- [ ] Scan free: 10/10 → 11° abre paywall, al cambiar de mes resetea a 0
- [ ] CSV free → paywall; CSV Pro → exporta
- [ ] `advancedBudgetEnabled` legacy → gracia 30d, luego paywall
- [ ] Sin rastro updater: `rg -i "checkForUpdate|downloadAndInstall|REQUEST_INSTALL_PACKAGES|GitHubRelease"` → 0 hits
- [ ] Strings 8 idiomas compilan (`aapt` sin `missing translation` bloqueante)

Test unitario opcional: `tryConsumeScanSlot()` cambio de mes, `isProEffective()` gracia (JUnit ya configurado).

### Fase 7 — Checklist administrativo (antes de publicar)

1. Producto `pro_unlock` **activo** (no Draft), 1 `buy` purchase option marcada backwards-compatible, precio + impuestos por país.
2. Play Console → Contenido app → Compras integradas: declarado. `Contiene anuncios: No`.
3. Data Safety: `Compras integradas: Sí`. Sin `Device IDs` (no hay ads).
4. Política de privacidad URL (menciona Google Play Billing, no recolectas tarjetas).
5. Cuenta dev personal nueva → closed test 12 testers / 14 días antes de producción. Si es cuenta organización con D-U-N-S, sin ese requisito.
6. Cambiar `applicationId` antes de subir el primer `.aab` (no se puede cambiar después sin app nueva).

---

## 5) Archivos tocados — resumen

| Nuevos | Modificados |
|---|---|
| `billing/BillingConfig.kt`, `billing/BillingManager.kt`, `billing/BillingViewModel.kt`, `di/BillingModule.kt`, `ui/settings/ProPaywallDialog.kt` | `gradle/libs.versions.toml`, `app/build.gradle.kts`, `app/proguard-rules.pro`, `app/src/main/AndroidManifest.xml`, `AppGastoApplication.kt`, `data/repository/PreferencesRepository.kt`, `domain/model/UserPreferences.kt`, `ui/settings/SettingsScreen.kt`, `ui/settings/SettingsViewModel.kt`, `ui/navigation/MainPagerScreen.kt`, `ui/advancedbudget/AdvancedBudgetScreen.kt`, `ui/add/AddEditScreen.kt`, `ui/add/AddEditViewModel.kt`, `data/updater/GitHubRelease.kt` (eliminado), `.github/workflows/release.yml`, `res/values*/strings.xml` (8 locales), `res/xml/file_paths.xml` (revisión futura para receipts) |

Estimación total: **4-5 días**. 60% es gating + strings + limpieza updater, no Billing en sí.

---

## 6) Alternativas descartadas

- Suscripción mensual/anual: sin costo servidor recurrente, fricción alta para utilidad offline LATAM.
- RevenueCat u otro wrapper: overkill sin backend, dependencia + fee innecesaria.
- Mantener updater GitHub en paralelo con flavor `play/github`: válido a futuro, pero para v1 Play se elimina total para pasar revisión sin fricción.
- Gatear Matrix/HC: descartado por decisión (quedan gratis, evita migración y backlash).
- Ads como monetización principal: eCPM LATAM ~$1-2, sesiones 30s, daña UX finanzas. Rewarded opt-in queda como opción futura, ver `PLAN_ADS.md`.

---

## 7) Referencias

- One-time products PBL: `developer.android.com/google/play/billing/one-time-products`
- Crear inapp: `support.google.com/googleplay/android-developer/answer/1153481`
- Precios y rangos por país: `play.google.com/supported-locations` / `support.google.com/googleplay/android-developer/answer/10532353`
- Mínimos históricos: US $0.99, PE S/.0.99 (`android-developers.googleblog.com/2015/11/minimum-purchase-price...`)
- Service fee 15%/30%: `support.google.com/googleplay/android-developer/answer/112622`
- Price experiments: `play.google.com/console/about/price-experiments`
