# Add project specific ProGuard rules here.
-keep class com.example.appgasto.data.local.** { *; }

# Gson rules for BackupData serialization
-keep class com.example.appgasto.data.backup.BackupManager$BackupData { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Preserve generic signatures for TypeToken
-keepattributes Signature
-keepattributes *Annotation*
