package com.dmitrysukhov.lifetracker

import android.app.Application
import android.content.res.Configuration
import com.dmitrysukhov.lifetracker.settings.ThemeMode
import com.dmitrysukhov.lifetracker.utils.ThemeManager
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale

@HiltAndroidApp
class LifeTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        initializeTheme(sharedPref)
        initializeLanguage(sharedPref)
    }
    
    private fun initializeTheme(sharedPref: android.content.SharedPreferences) {
        val themeSetting = sharedPref.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        
        val themeMode = when (themeSetting) {
            ThemeMode.LIGHT.name -> ThemeMode.LIGHT
            ThemeMode.DARK.name -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
        
        ThemeManager.setThemeMode(themeMode)
    }
    
    private fun initializeLanguage(sharedPref: android.content.SharedPreferences) {
        val languageCode = sharedPref.getString("language", null)
        if (languageCode != null) {
            val locale = when (languageCode) {
                "ru" -> Locale("ru")
                "uk" -> Locale("uk")
                "en" -> Locale("en")
                else -> Locale.getDefault()
            }
            Locale.setDefault(locale)
            val configuration = Configuration(resources.configuration)
            configuration.setLocale(locale)
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
    }
}