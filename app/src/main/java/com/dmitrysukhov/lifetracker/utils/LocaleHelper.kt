package com.dmitrysukhov.lifetracker.utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {

    fun applyLanguage(context: Context, languageCode: String): Context {
        val locale = when (languageCode) {
            "ru" -> Locale("ru")
            "uk" -> Locale("uk")
            "en" -> Locale("en")
            else -> Locale.getDefault()
        }
        Locale.setDefault(locale)
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }
    
    fun getLanguageFromPreferences(context: Context): String {
        val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("language", "en") ?: "en"
    }
} 