plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.baselineprofile)
}

fun getAutoVersionCode(): Int {
    return try {
        val process = ProcessBuilder("git", "rev-list", "--count", "HEAD")
            .directory(rootDir)
            .redirectErrorStream(true)
            .start()
        val count = process.inputStream.bufferedReader().readText().trim().toInt()
        process.waitFor()
        count
    } catch (e: Exception) {
        1
    }
}

val appKeystore = rootProject.file("keystore/appgasto.jks")

android {
    namespace = "com.example.appgasto"
    compileSdk = 35

    signingConfigs {
        if (appKeystore.exists()) {
            create("app") {
                storeFile = appKeystore
                storePassword = (project.findProperty("APPGASTO_STORE_PASSWORD") as String?) ?: "appgasto123"
                keyAlias = (project.findProperty("APPGASTO_KEY_ALIAS") as String?) ?: "appgasto"
                keyPassword = (project.findProperty("APPGASTO_KEY_PASSWORD") as String?) ?: "appgasto123"
            }
        }
    }

    defaultConfig {
        applicationId = "com.example.appgasto"
        minSdk = 26
        targetSdk = 35
        versionCode = getAutoVersionCode()
        versionName = (project.findProperty("versionName") as String?) ?: "0.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            if (appKeystore.exists()) {
                signingConfig = signingConfigs.getByName("app")
            }
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (appKeystore.exists()) {
                signingConfig = signingConfigs.getByName("app")
            }
        }
    }

    baselineProfile {
        // Usa la configuración de release build type para generar perfiles
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.compose.foundation)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.datastore.preferences)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.gson)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)
    implementation(libs.mlkit.document.scanner)
    implementation(libs.mlkit.text.recognition)
    implementation(libs.androidx.appcompat)

    coreLibraryDesugaring(libs.desugar.jdk.libs)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
