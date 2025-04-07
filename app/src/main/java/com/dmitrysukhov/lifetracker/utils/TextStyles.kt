package com.dmitrysukhov.lifetracker.utils

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.Medium
import androidx.compose.ui.unit.sp

val H1 = TextStyle(
    fontSize = 18.sp, fontFamily = Montserrat, fontWeight = Bold, letterSpacing = 0.sp
)

val H2 = TextStyle(
    fontSize = 16.sp, fontFamily = Montserrat, fontWeight = Medium,
    letterSpacing = 0.sp, lineHeight = 24.sp
)

val SimpleText = TextStyle(
    fontSize = 14.sp, fontFamily = Montserrat, fontWeight = Medium,
    letterSpacing = 0.sp, lineHeight = 18.sp
)

val BoldText = TextStyle(
    fontSize = 14.sp, fontFamily = Montserrat, fontWeight = Bold,
    letterSpacing = 0.sp, lineHeight = 18.sp
)

val Small = TextStyle(
    fontSize = 12.sp, fontFamily = Montserrat, fontWeight = Medium,
    letterSpacing = 0.sp, lineHeight = 18.sp
)
