package com.example.appgasto.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.appgasto.MainActivity
import com.example.appgasto.R
import com.example.appgasto.data.local.AppDatabase
import com.example.appgasto.domain.model.Currency
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

// Same DataStore name as PreferencesRepository: both delegates share the file.
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class ExpenseWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val (total, baseCurrency) = runBlocking(Dispatchers.IO) { getTodayTotalInBase(context) }
        val totalText = baseCurrency.format(total)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val views = RemoteViews(context.packageName, R.layout.widget_layout).apply {
            setTextViewText(R.id.widget_total, totalText)
            setOnClickPendingIntent(R.id.widget_container, pendingIntent)
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private suspend fun getTodayTotalInBase(context: Context): Pair<Double, Currency> {
        return try {
            val database = AppDatabase.create(context.applicationContext)
            val start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN)
            val totalPEN = database.expenseDao().getTotalSince(start) ?: 0.0

            val baseCode = context.dataStore.data.first()[BASE_CURRENCY_KEY] ?: Currency.PEN.code
            val baseCurrency = Currency.fromCode(baseCode)
            val rateToBase = if (baseCurrency == Currency.PEN) 1.0
                else database.exchangeRateDao().getByCode(baseCurrency.code)?.rateToPen ?: 1.0

            Pair(totalPEN * rateToBase, baseCurrency)
        } catch (e: Exception) {
            Pair(0.0, Currency.PEN)
        }
    }

    companion object {
        private val BASE_CURRENCY_KEY = stringPreferencesKey("base_currency")

        fun updateAll(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, ExpenseWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds.isNotEmpty()) {
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_total)
                val intent = Intent(context, ExpenseWidget::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                context.sendBroadcast(intent)
            }
        }
    }
}
