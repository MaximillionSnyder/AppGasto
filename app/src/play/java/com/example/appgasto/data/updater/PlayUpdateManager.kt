package com.example.appgasto.data.updater

import android.content.Context
import android.content.Intent
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayUpdateManager @Inject constructor(
    @ApplicationContext private val context: Context
) : UpdateManager {

    override val managesUpdatesInApp: Boolean = false

    override suspend fun fetchLatest(): Result<AppUpdate?> = Result.success(null)

    override suspend fun downloadAndInstall(update: AppUpdate, onComplete: (String) -> Unit) {
        openStoreListing()
        onComplete("")
    }

    override fun openStoreListing() {
        val packageName = context.packageName
        val playIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=$packageName")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(playIntent)
        } catch (e: Exception) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
