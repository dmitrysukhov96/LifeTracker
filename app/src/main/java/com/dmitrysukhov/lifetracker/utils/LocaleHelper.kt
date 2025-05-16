package com.dmitrysukhov.lifetracker.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LocaleHelper {

    fun getLocaleFromLanguageCode(languageCode: String): Locale {
        return when (languageCode) {
            "ru" -> Locale("ru")
            "uk" -> Locale("uk")
            "de" -> Locale("de", "DE")
            "fr" -> Locale("fr")
            "es" -> Locale("es")
            "it" -> Locale("it")
            "bg" -> Locale("bg")
            "ja" -> Locale("ja")
            "ko" -> Locale("ko")
            "lt" -> Locale("lt")
            "sr" -> Locale("sr")
            "zh" -> Locale("zh")
            "he" -> Locale("he", "IL")
            "iw" -> Locale("he", "IL")
            "en" -> Locale("en")
            else -> Locale("en")
        }
    }

    fun applyLanguage(context: Context, languageCode: String): Context {
        val locale = getLocaleFromLanguageCode(languageCode)
        
        Locale.setDefault(locale)
        
        val configuration = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
        }
        
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
        
        return context.createConfigurationContext(configuration)
    }
    
    fun getLanguageFromPreferences(context: Context): String {
        val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("language", "en") ?: "en"
    }
} 