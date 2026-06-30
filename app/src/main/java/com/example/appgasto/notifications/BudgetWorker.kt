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
        const val NOTIFICATION_ID = 1001
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
                percentage >= 100 -> sendNotification(
                    "Presupuesto agotado",
                    "Has alcanzado el límite de tu presupuesto mensual"
                )
                percentage >= 80 -> sendNotification(
                    "Presupuesto al 80%",
                    "Has alcanzado el 80% de tu presupuesto mensual"
                )
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun sendNotification(title: String, message: String) {
        val notificationManager = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(applicationContext, "budget_alerts")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
