package com.dmitrysukhov.lifetracker.utils

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val PineColor = Color(0xFF34B979)
val DarkerPine = Color(0xFF18593A)
val AccentColor = Color(0xFFCBF66E)
val WhitePine = Color(0xFFF1FCF5)
val BlackPine = Color(0xFF0A1711)
val BgColor @Composable get() = if (isDarkTheme()) BlackPine else WhitePine
val InverseColor @Composable get() = if (isDarkTheme()) WhitePine else BlackPine

@Composable
fun isDarkTheme() = isSystemInDarkTheme()