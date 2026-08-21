package com.example.appgasto.data.updater

import java.io.File

data class AppUpdate(
    val tagName: String,
    val changelog: String?,
    val downloadUrl: String? = null
)

interface UpdateManager {
    val managesUpdatesInApp: Boolean
    suspend fun fetchLatest(): Result<AppUpdate?>
    suspend fun downloadAndInstall(update: AppUpdate, onComplete: (String) -> Unit)
    fun openStoreListing()
}
