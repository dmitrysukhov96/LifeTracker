package com.dmitrysukhov.lifetracker.utils

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.dmitrysukhov.lifetracker.settings.ThemeMode

val Rosa = Color(0xFFFF0033)
val Red = Color(0xFFFA3535)
val DarkOrange = Color(0xFFFF582E)
val Orange = Color(0xFFFFA91F)
val Yellow = Color(0xFFEAB300)
val Olive = Color(0xFFA9AD0C)
val OliveGreen = Color(0xFF5D9208)
val LightGreen = Color(0xFF1AB115)
val Turquoise = Color(0xFF17997D)
val Blue = Color(0xFF29B8D9)
val SkyBlue = Color(0xFF669DE5)
val PeriwinkleBlue = Color(0xFF737AFF)
val BlueViolet = Color(0xFF7940FF)
val Purple = Color(0xFF71278B)
val Mauve = Color(0xFFC02A39)
val RedViolet = Color(0xFFED1F60)
val Magenta = Color(0xFF931886)
val Pink = Color(0xFFF16273)
val Brown = Color(0xFF86471D)
val PineColor = Color(0xFF34B979)
//val DarkerPine = Color(0xFF18593A)
val AccentColor = Color(0xFFCBF66E)
val WhitePine = Color(0xFFF1FCF5)
val BlackPine = Color(0xFF0A1711)
val BgColor @Composable get() = if (isDarkTheme()) BlackPine else WhitePine
val InverseColor @Composable get() = if (isDarkTheme()) WhitePine else BlackPine

@Composable
fun isDarkTheme(): Boolean {
    val themeMode by ThemeManager.themeMode.collectAsState()
    
    return when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
}