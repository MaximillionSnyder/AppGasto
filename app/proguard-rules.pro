# Add project specific ProGuard rules here.

# --- Hardening: eliminar trazas de consola en release ---
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
    public static int wtf(...);
}
-assumenosideeffects class java.io.PrintStream {
    public void println(...);
}
-assumenosideeffects interface okhttp3.logging.HttpLoggingInterceptor$Logger {
    public void log(...);
}

# --- Room ---
-keep class com.example.appgasto.data.local.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# --- Hilt / Dagger ---
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# --- Gson (reflection) ---
-keep class com.example.appgasto.data.backup.BackupManager$BackupData { *; }
-keep class com.example.appgasto.data.backup.LocalDateTimeTypeAdapter
-keep class com.example.appgasto.data.updater.GitHubRelease { *; }
-keep class com.example.appgasto.data.updater.GitHubAsset { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keepclassmembers class * extends com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.TypeAdapter { *; }
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# --- Retrofit / OkHttp ---
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# --- ML Kit ---
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# --- DataStore ---
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite { *; }

