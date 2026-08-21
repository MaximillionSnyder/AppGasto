package com.example.appgasto.security

import com.example.appgasto.BuildConfig

object SecurityBridge {

    private val nativeAvailable: Boolean = !BuildConfig.DEBUG && runCatching {
        System.loadLibrary("gastosec")
        true
    }.getOrDefault(false)

    fun verifyCertificate(certDer: ByteArray): Boolean =
        !nativeAvailable || nativeVerifyCertificate(certDer)

    fun startWatchdog() {
        if (nativeAvailable) {
            nativeStartWatchdog()
        }
    }

    fun isCompromised(): Boolean =
        nativeAvailable && nativeIsCompromised()

    private external fun nativeVerifyCertificate(certDer: ByteArray): Boolean

    private external fun nativeStartWatchdog()

    private external fun nativeIsCompromised(): Boolean
}
