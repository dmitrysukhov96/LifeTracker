package com.dmitrysukhov.lifetracker

import android.app.Application
import android.content.Context
import android.os.Build
import com.dmitrysukhov.lifetracker.settings.ThemeMode
import com.dmitrysukhov.lifetracker.utils.LocaleHelper
import com.dmitrysukhov.lifetracker.utils.ThemeManager
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale

@HiltAndroidApp
class LifeTrackerApp : Application() {
    
    override fun attachBaseContext(base: Context) {
        val languageCode = LocaleHelper.getLanguageFromPreferences(base)
        super.attachBaseContext(LocaleHelper.applyLanguage(base, languageCode))
    }
    
    override fun onCreate() {
        super.onCreate()
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        
        // Инициализация темы
        initializeTheme(sharedPref)
        
        // Повторно применяем локализацию
        initializeLanguage()
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
    
    private fun initializeLanguage() {
        val languageCode = LocaleHelper.getLanguageFromPreferences(this)
        val locale = LocaleHelper.getLocaleFromLanguageCode(languageCode)
        
        // Устанавливаем локаль по умолчанию
        Locale.setDefault(locale)
        
        // Обновляем конфигурацию ресурсов
        val configuration = resources.configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
        }
        
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }
}