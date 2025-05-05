package com.dmitrysukhov.lifetracker.utils

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity

abstract class LocaleBaseActivity : ComponentActivity() {
    
    override fun attachBaseContext(newBase: Context) {
        val languageCode = LocaleHelper.getLanguageFromPreferences(newBase)
        super.attachBaseContext(LocaleHelper.applyLanguage(newBase, languageCode))
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val languageCode = LocaleHelper.getLanguageFromPreferences(this)
        LocaleHelper.applyLanguage(this, languageCode)
    }
    
    override fun onResume() {
        super.onResume()
        val languageCode = LocaleHelper.getLanguageFromPreferences(this)
        LocaleHelper.applyLanguage(this, languageCode)
    }
} 