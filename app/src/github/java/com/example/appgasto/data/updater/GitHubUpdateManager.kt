package com.example.appgasto.data.updater

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.appgasto.BuildConfig
import com.example.appgasto.security.TamperResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GitHubUpdateManager @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val tamperResponse: TamperResponse,
    @ApplicationContext private val context: Context
) : UpdateManager {

    private val gson = Gson()

    override val managesUpdatesInApp: Boolean = true

    override suspend fun fetchLatest(): Result<AppUpdate?> = withContext(Dispatchers.IO) {
        if (tamperResponse.degraded.value) {
            return@withContext Result.success(null)
        }
        try {
            val request = Request.Builder()
                .url("https://api.github.com/repos/MaximillionSnyder/AppGasto/releases/latest")
                .header("Accept", "application/vnd.github.v3+json")
                .build()
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext if (response.code == 404) {
                        Result.success(null)
                    } else {
                        Result.failure(Exception("GitHub API error: ${response.code}"))
                    }
                }
                val release = gson.fromJson(response.body?.string(), GitHubRelease::class.java)
                    ?: return@withContext Result.success(null)

                val remoteVersion = release.tagName.removePrefix("v").trim()
                val localVersion = BuildConfig.VERSION_NAME.trim()
                if (compareVersions(remoteVersion, localVersion) <= 0) {
                    return@withContext Result.success(null)
                }
                val apkAsset = release.assets?.firstOrNull { it.name.endsWith(".apk") }
                Result.success(
                    AppUpdate(
                        tagName = release.tagName,
                        changelog = release.body,
                        downloadUrl = apkAsset?.browserDownloadUrl
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun downloadAndInstall(update: AppUpdate, onComplete: (String) -> Unit) {
        val url = update.downloadUrl
        if (url.isNullOrEmpty()) {
            onComplete("No APK found in release")
            return
        }
        val result = withContext(Dispatchers.IO) { downloadApk(url) }
        if (result.isSuccess) {
            installApk(result.getOrNull()!!)
            onComplete("")
        } else {
            onComplete(result.exceptionOrNull()?.localizedMessage ?: "Download failed")
        }
    }

    override fun openStoreListing() {
        openUrl("https://github.com/MaximillionSnyder/AppGasto/releases/latest")
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private suspend fun downloadApk(url: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).build()
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Download error: ${response.code}"))
                }
                val file = File(context.cacheDir, "update.apk")
                response.body?.byteStream()?.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
                Result.success(file)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun installApk(file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(intent)
    }

    private fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = v2.split(".").map { it.toIntOrNull() ?: 0 }
        val maxLen = maxOf(parts1.size, parts2.size)
        for (i in 0 until maxLen) {
            val a = parts1.getOrElse(i) { 0 }
            val b = parts2.getOrElse(i) { 0 }
            if (a > b) return 1
            if (a < b) return -1
        }
        return 0
    }
}
