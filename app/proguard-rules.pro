# Add project specific ProGuard rules here.
-keep class com.example.appgasto.data.local.** { *; }

# Gson / Backup
-keep class com.example.appgasto.data.backup.BackupManager$BackupData { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keepclassmembers class * extends com.google.gson.reflect.TypeToken { *; }

