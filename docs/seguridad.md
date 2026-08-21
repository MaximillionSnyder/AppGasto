# Seguridad y endurecimiento de GastoApp

Este documento describe la arquitectura de protección de la app, cómo funciona
cada capa y los pasos de configuración posteriores (clave de firma de Google Play).

## Visión general

| Capa | Componente | Archivos |
|---|---|---|
| Ofuscación JVM | R8 + eliminación de logs | `app/proguard-rules.pro` |
| Verificación de firma | `libgastosec.so` | `app/src/main/cpp/integrity.cpp` |
| Anti-debugging | `libgastosec.so` | `app/src/main/cpp/antidebug.cpp` |
| Cadenas ofuscadas | XOR compile-time + wipe en RAM | `app/src/main/cpp/secstr.h` |
| Orquestación | Monitor de integridad | `security/IntegrityMonitor.kt`, `security/TamperResponse.kt` |
| Datos en reposo | SQLCipher sobre Room | `data/local/AppDatabase.kt`, `data/local/DbKeyStore.kt`, `data/local/SqlCipherMigration.kt` |

## Canales de distribución (flavors)

- **github**: APK firmado con el keystore propio; actualizador integrado que
  descarga e instala releases desde GitHub (`REQUEST_INSTALL_PACKAGES`).
- **play**: AAB para Google Play; sin permiso de instalación y las comprobaciones
  de "buscar actualización" abren la ficha de Play Store.

## Verificación de firma

1. En tiempo de compilación, `app/build.gradle.kts` extrae con `keytool` el
   SHA-256 del certificado de `keystore/appgasto.jks`.
2. El digest se cifra con XOR y se genera
   `app/src/main/cpp/generated/signature_whitelist.h` (ignorado por git).
3. En runtime, `SecurityBridge.verifyCertificate()` pasa los bytes DER del
   certificado al código nativo, que calcula SHA-256 dentro de la librería y lo
   compara en tiempo constante contra la lista blanca.

Si el APK es modificado y re-firmado por un tercero, la comparación falla.

### Configurar el hash de firma de Google Play

Google re-firma la app con su propia clave (Play App Signing). Tras crear la
aplicación en Play Console:

1. Ir a *Setup > App signing* y copiar el **SHA-256 del App signing key**.
2. Pasarlo como propiedad de Gradle en CI (o `gradle.properties` local):

```bash
./gradlew bundlePlayRelease -PPLAY_SIGNING_SHA256=AA:BB:CC:...:99
```

La lista blanca acepta simultáneamente el certificado local (canal GitHub) y el
de Play. Si ninguna fuente está disponible en build time, la lista queda vacía y
la verificación opera en modo permisivo para no bloquear builds de desarrollo;
los builds de producción de CI siempre incluyen keystore.

## Anti-debugging

- `ptrace(PTRACE_TRACEME)` invocado directamente con `svc #0` en ensamblador
  ARM64 (sin pasar por `libc`, donde suelen colocarse hooks).
- Lectura por syscalls crudas de `/proc/self/status` para auditar `TracerPid`.
- Hilo guardián nativo con intervalos aleatorios de 20–90 s.
- Solo compila para `arm64-v8a`; en otras arquitecturas los chequeos son no-op.

## Respuesta graduada

`IntegrityMonitor` verifica firma + estado anti-debug al arrancar (con jitter de
10–30 s) y luego cada 45–150 s. Ante un fallo:

1. **Silencio inmediato**: se desactivan el actualizador (`GitHubUpdateManager`)
   y el escaneo OCR de recibos (`AddEditViewModel`). Sin logs ni diálogos.
2. **Salida diferida**: entre 2 y 10 minutos después (aleatorio), la app cierra
   todas sus actividades y finaliza el proceso.

Los chequeos están desactivados por completo en builds debug
(`BuildConfig.DEBUG`) y ante errores internos del bridge JNI la app falla
abierto (no castiga a usuarios legítimos).

## Base de datos cifrada (SQLCipher)

- Room abre la base mediante `SupportOpenHelperFactory` (`sqlcipher-android`).
- La contraseña aleatoria (32 bytes) se genera en el primer arranque, se cifra
  con AES-GCM usando una clave de Android Keystore y se guarda en
  `files/gastosec_db.key`.
- **Migración automática**: si existe una BD antigua en texto plano, se copia
  con `sqlcipher_export` a una BD cifrada temporal, se verifica y se hace el
  swap atómico conservando datos y versión de esquema.
- La BD y su archivo de clave están **excluidos** de backups automáticos de
  Android: la clave de Keystore no viaja entre dispositivos y restaurar ambos
  dejaría la BD irrecuperable. Para migrar de dispositivo usar el backup JSON
  integrado (Ajustes > Copia de seguridad).

## Limitaciones conocidas

- Ninguna técnica aquí es infalible frente a un analista experto con el código
  fuente público; elevan el coste del ataque binario (reempaquetado, hooks).
- El ensamblador anti-debug solo cubre arm64-v8a.
- Un atacante que compile desde su propio fork puede eliminar toda la
  protección; la lista blanca protege contra manipulación del binario oficial.
