import java.io.File

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.baselineprofile)
}

val gitCommitCount = providers.exec {
    commandLine("git", "rev-list", "--count", "HEAD")
    workingDir = rootDir
    isIgnoreExitValue = true
}.standardOutput.asText.map { output ->
    try {
        output.trim().toInt()
    } catch (e: Exception) {
        1
    }
}.orElse(1)

val appKeystore = rootProject.file("keystore/appgasto.jks")

fun parseHexDigest(raw: String?): ByteArray? {
    if (raw.isNullOrBlank()) return null
    val cleaned = raw.replace(":", "").replace(" ", "").trim().uppercase()
    if (!Regex("^[0-9A-F]{64}$").matches(cleaned)) return null
    return cleaned.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}

fun extractKeystoreCertSha256(): ByteArray? = try {
    if (!appKeystore.exists() || providers.exec { commandLine("which", "keytool") }.standardOutput.asText.getOrElse("").isBlank()) {
        null
    } else {
        val storePass = project.findProperty("APPGASTO_STORE_PASSWORD")?.toString().orEmpty()
        if (storePass.isBlank()) {
            null
        } else {
            val output = providers.exec {
                commandLine(
                    "keytool", "-list", "-v",
                    "-keystore", appKeystore.absolutePath,
                    "-storepass", storePass
                )
                isIgnoreExitValue = true
            }.standardOutput.asText.getOrElse("")
            Regex("SHA256:\\s*([0-9A-Fa-f:]{95})").find(output)?.groupValues?.get(1)
                ?.let { parseHexDigest(it) }
        }
    }
} catch (t: Throwable) {
    null
}

fun xorEncode(bytes: ByteArray): List<Int> =
    bytes.mapIndexed { i, b -> (b.toInt() xor ((0x5A + i * 31) and 0xFF)) and 0xFF }

run {
    val entries = listOfNotNull(
        extractKeystoreCertSha256(),
        parseHexDigest(project.findProperty("PLAY_SIGNING_SHA256") as String?)
    )
    val headerText = buildString {
        appendLine("#pragma once")
        appendLine()
        appendLine("#include <stddef.h>")
        appendLine()
        if (entries.isEmpty()) {
            appendLine("#define SIG_WHITELIST_COUNT 0")
        } else {
            entries.forEachIndexed { idx, bytes ->
                append("#define SIG_ENTRY_${idx}_LEN ${bytes.size}")
                appendLine()
                append("static const unsigned char SIG_ENTRY_${idx}[${bytes.size}] = { ")
                append(xorEncode(bytes).joinToString(","))
                appendLine(" };")
            }
            appendLine()
            appendLine("#define SIG_WHITELIST_COUNT ${entries.size}")
            val names = entries.indices.joinToString(", ") { "SIG_ENTRY_$it" }
            appendLine("static const unsigned char* const SIG_WHITELIST[SIG_WHITELIST_COUNT] = { $names };")
        }
        appendLine()
    }

    val genDir = file("src/main/cpp/generated")
    genDir.mkdirs()
    val headerFile = File(genDir, "signature_whitelist.h")
    if (!headerFile.exists() || headerFile.readText() != headerText) {
        headerFile.writeText(headerText)
    }
}

android {
    namespace = "com.example.appgasto"
    compileSdk = 35
    ndkVersion = "27.0.12077973"

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    signingConfigs {
        if (appKeystore.exists()) {
            create("app") {
                storeFile = appKeystore
                storePassword = project.findProperty("APPGASTO_STORE_PASSWORD") as String?
                keyAlias = project.findProperty("APPGASTO_KEY_ALIAS") as String?
                keyPassword = project.findProperty("APPGASTO_KEY_PASSWORD") as String?
            }
        }
    }

    defaultConfig {
        applicationId = "com.example.appgasto"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = (project.findProperty("versionName") as String?) ?: "0.6"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        ndk {
            abiFilters += "arm64-v8a"
        }
    }

    flavorDimensions += "distribution"
    productFlavors {
        create("github") {
            dimension = "distribution"
        }
        create("play") {
            dimension = "distribution"
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
            isShrinkResources = true
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

androidComponents {
    onVariants(selector().all()) { variant ->
        variant.outputs.forEach { output ->
            output.versionCode.set(gitCommitCount)
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
    implementation(libs.sqlcipher.android)
    implementation(libs.androidx.appcompat)

    coreLibraryDesugaring(libs.desugar.jdk.libs)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
