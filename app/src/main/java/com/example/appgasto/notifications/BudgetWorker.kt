package com.example.appgasto.notifications

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.appgasto.R
import com.example.appgasto.data.repository.ExpenseRepository
import com.example.appgasto.data.repository.PreferencesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate

@HiltWorker
class BudgetWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val expenseRepository: ExpenseRepository,
    private val preferencesRepository: PreferencesRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val NOTIFICATION_ID_80 = 1001
        const val NOTIFICATION_ID_100 = 1002
    }

    override suspend fun doWork(): Result {
        return try {
            val preferences = preferencesRepository.preferencesFlow.first()
            if (!preferences.budgetEnabled || preferences.monthlyBudget <= 0) {
                return Result.success()
            }

            val monthStart = LocalDate.now().withDayOfMonth(1)
            val currentTotal = expenseRepository.getTotalForPeriod(
                monthStart, LocalDate.now()
            )
            val percentage = (currentTotal / preferences.monthlyBudget) * 100

            when {
                percentage >= 100 -> {
                    if (!preferencesRepository.wasBudgetAlertSentThisMonth(100)) {
                        sendNotification(
                            NOTIFICATION_ID_100,
                            applicationContext.getString(R.string.budget_alert_title_100),
                            applicationContext.getString(R.string.budget_alert_100)
                        )
                        preferencesRepository.setBudgetAlertSent(100)
                    }
                }
                percentage >= 80 -> {
                    if (!preferencesRepository.wasBudgetAlertSentThisMonth(80)) {
                        sendNotification(
                            NOTIFICATION_ID_80,
                            applicationContext.getString(R.string.budget_alert_title_80),
                            applicationContext.getString(R.string.budget_alert_80)
                        )
                        preferencesRepository.setBudgetAlertSent(80)
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun sendNotification(notificationId: Int, title: String, message: String) {
        val notificationManager = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(applicationContext, "budget_alerts")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}
