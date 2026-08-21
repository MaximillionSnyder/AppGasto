package com.example.appgasto.security

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TamperResponse @Inject constructor(
    @ApplicationContext private val context: Context
) : Application.ActivityLifecycleCallbacks {

    private val triggered = AtomicBoolean(false)
    private val _degraded = MutableStateFlow(false)
    val degraded: StateFlow<Boolean> = _degraded.asStateFlow()

    private var foregroundActivity: Activity? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    init {
        (context as? Application)?.registerActivityLifecycleCallbacks(this)
    }

    fun trigger(reason: Int) {
        if (!triggered.compareAndSet(false, true)) return

        _degraded.value = true

        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            delay(RANDOM.nextLong(MIN_EXIT_DELAY_MS, MAX_EXIT_DELAY_MS))
            mainHandler.post {
                val activity = foregroundActivity
                if (activity != null && !activity.isFinishing) {
                    activity.finishAffinity()
                    activity.window.decorView.postDelayed({ exitQuietly() }, 250L)
                } else {
                    exitQuietly()
                }
            }
        }
    }

    fun triggerIfCompromised(compromised: Boolean) {
        if (compromised) {
            trigger(REASON_INTEGRITY)
        }
    }

    private fun exitQuietly() {
        Runtime.getRuntime().exit(0)
    }

    override fun onActivityResumed(activity: Activity) {
        foregroundActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {
        if (foregroundActivity === activity) {
            foregroundActivity = null
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}

    companion object {
        val RANDOM = Random(System.nanoTime())
        const val REASON_INTEGRITY = 1
        const val MIN_EXIT_DELAY_MS = 2 * 60 * 1000L
        const val MAX_EXIT_DELAY_MS = 10 * 60 * 1000L
    }
}
