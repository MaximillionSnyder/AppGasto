package com.example.appgasto.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.example.appgasto.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IntegrityMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tamperResponse: TamperResponse
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val started = AtomicBoolean(false)

    fun start() {
        if (BuildConfig.DEBUG) return
        if (!started.compareAndSet(false, true)) return

        scope.launch {
            delay(RANDOM.nextLong(MIN_START_DELAY_MS, MAX_START_DELAY_MS))

            tamperResponse.triggerIfCompromised(SecurityBridge.isCompromised())
            SecurityBridge.startWatchdog()

            while (isActive) {
                val certs = try {
                    collectSigningCertificates()
                } catch (t: Throwable) {
                    emptyList()
                }
                if (certs.isNotEmpty()) {
                    val anyTrusted = certs.any { SecurityBridge.verifyCertificate(it) }
                    tamperResponse.triggerIfCompromised(!anyTrusted)
                }
                tamperResponse.triggerIfCompromised(SecurityBridge.isCompromised())

                delay(RANDOM.nextLong(MIN_CHECK_INTERVAL_MS, MAX_CHECK_INTERVAL_MS))
            }
        }
    }

    private fun collectSigningCertificates(): List<ByteArray> {
        val pm = context.packageManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val info = pm.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNING_CERTIFICATES
            )
            info.signingInfo?.apkContentsSigners?.map { it.toByteArray() } ?: emptyList()
        } else {
            @Suppress("DEPRECATION")
            val info = pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
            info.signatures?.map { it.toByteArray() } ?: emptyList()
        }
    }

    companion object {
        val RANDOM = TamperResponse.RANDOM
        const val MIN_START_DELAY_MS = 10_000L
        const val MAX_START_DELAY_MS = 30_000L
        const val MIN_CHECK_INTERVAL_MS = 45_000L
        const val MAX_CHECK_INTERVAL_MS = 150_000L
    }
}
