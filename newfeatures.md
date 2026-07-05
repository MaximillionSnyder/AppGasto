# 🚀 NEWFEATURES - AppGasto

> **INSTRUCCIÓN PARA IA:** Cada nueva feature planeada se agrega como entrada con número de versión consecutivo, fecha y descripción. Cuando se implemente, cambiar `[ ]` por `[x]`. Siempre mantener este documento actualizado.

---

## Versión 1 — 2026-07-05

### 1.1 Receipt Scanning (Escanear recibos con cámara)

- **Estado:** `[ ]` Pendiente
- **Objetivo:** Permitir escanear un recibo físico con la cámara y auto-llenar los campos del formulario de gasto, evitando el ingreso manual.
- **Stack técnico:**
  - Google ML Kit Document Scanner API (`play-services-mlkit-document-scanner:16.0.0`) — UI de escaneo con detección de bordes, corrección de perspectiva, auto-capture
  - Google ML Kit Text Recognition v2 (`com.google.mlkit:text-recognition:16.0.0`) — OCR on-device para extraer texto del recibo
  - ReceiptParser propio — regex para extraer total, fecha, comercio y moneda
- **Qué extraer:**
  - `[ ]` **Total** del recibo (patrones: `TOTAL S/. 123.45`, `SUMA`, `IMPORTE`, `VUELTO`)
  - `[ ]` **Fecha** del recibo (formatos: dd/mm/aaaa, mm/dd/aaaa, yyyy-mm-dd, etc.)
  - `[ ]` **Comercio** (primeras líneas del texto OCR, se guarda como nota del gasto)
  - `[ ]` **Moneda** detectada (S/., $, €, R$, etc.)
- **Qué NO incluir (V1):**
  - `[x]` Items/líneas del recibo — descartado por complejidad de layouts variables
  - `[x]` Guardar imagen del recibo — descartado por impacto en almacenamiento, backup y migración DB
  - `[x]` Multi-moneda en totales/sumas — la UI principal sigue en S/. por ahora
- **Archivos a modificar/crear:**
  - `gradle/libs.versions.toml` — agregar versiones de dependencias ML Kit
  - `app/build.gradle.kts` — agregar dependencias
  - `data/ocr/ReceiptData.kt` — data class con resultado del OCR
  - `data/ocr/ReceiptParser.kt` — parser con regex para total, fecha, comercio, moneda
  - `data/ocr/ReceiptOcrService.kt` — interfaz del servicio
  - `data/ocr/MLKitReceiptOcrService.kt` — implementación con ML Kit Text Recognition
  - `di/OcrModule.kt` — módulo Hilt para proveer el servicio
  - `ui/add/AddEditScreen.kt` — botón "Escanear recibo" + lanzador Document Scanner
  - `ui/add/AddEditViewModel.kt` — manejar resultado del scan y auto-llenar state
  - `app/src/main/AndroidManifest.xml` — sin cambios (no requiere permiso CAMERA)

---

## Registro de Versiones

| Versión | Fecha | Cambios |
|:-------:|:-----:|:--------|
| 1 | 2026-07-05 | Receipt Scanning con ML Kit Document Scanner + Text Recognition + Parser |
