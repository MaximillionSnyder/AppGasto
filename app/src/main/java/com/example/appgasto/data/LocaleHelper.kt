package com.example.appgasto.data

import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.appgasto.domain.model.AppLanguage
import java.util.Locale

object LocaleHelper {

    fun applyLanguage(context: Context, language: AppLanguage) {
        when (language) {
            AppLanguage.SPANISH -> setLocale("es")
            AppLanguage.ENGLISH -> setLocale("en")
            AppLanguage.PORTUGUESE -> setLocale("pt")
            AppLanguage.ITALIAN -> setLocale("it")
            AppLanguage.GERMAN -> setLocale("de")
            AppLanguage.JAPANESE -> setLocale("ja")
            AppLanguage.KOREAN -> setLocale("ko")
            AppLanguage.QUECHUA -> setLocale("qu")
            AppLanguage.SYSTEM -> setLocaleToSystem()
        }
    }

    private fun setLocale(languageCode: String) {
        val localeList = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    private fun setLocaleToSystem() {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
    }

    fun updateContext(context: Context, language: AppLanguage): Context {
        val locale = when (language) {
            AppLanguage.SPANISH -> Locale("es")
            AppLanguage.ENGLISH -> Locale("en")
            AppLanguage.PORTUGUESE -> Locale("pt")
            AppLanguage.ITALIAN -> Locale("it")
            AppLanguage.GERMAN -> Locale("de")
            AppLanguage.JAPANESE -> Locale("ja")
            AppLanguage.KOREAN -> Locale("ko")
            AppLanguage.QUECHUA -> Locale("qu")
            AppLanguage.SYSTEM -> return context
        }

        val config = context.resources.configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            config.setLocales(localeList)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        @Suppress("DEPRECATION")
        return context.createConfigurationContext(config)
    }
}
