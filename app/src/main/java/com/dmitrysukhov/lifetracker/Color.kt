package com.dmitrysukhov.lifetracker

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val WhitePine = Color(0xFFF1FCF5)
val PineColor = Color(0xFFC2EBD6)
val AccentColor = Color(0xFFCBF66E)
val DarkerPine = Color(0xFF18593A)
val BlackPine = Color(0xFF0E281C)
val Yellow = Color(0xFFFFEA00)
val BgColor @Composable get() = if (isDarkTheme()) BlackPine else WhitePine
val InverseColor @Composable get() = if (isDarkTheme()) WhitePine else BlackPine

@Composable
fun isDarkTheme() = isSystemInDarkTheme()