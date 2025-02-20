package com.dmitrysukhov.lifetracker.utils

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.dmitrysukhov.lifetracker.R

val Montserrat = FontFamily(
    Font(R.font.montserrat_light, FontWeight.Light),
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_medium, FontWeight.Medium),
    Font(R.font.montserrat_semibold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold, FontWeight.Bold),
    Font(R.font.montserrat_extrabold, FontWeight.ExtraBold),
    Font(R.font.montserrat_black, FontWeight.Black),
    Font(R.font.montserrat_italic, FontWeight.Normal, style = FontStyle.Italic),
    Font(R.font.montserrat_bolditalic, FontWeight.Bold, style = FontStyle.Italic),
    Font(R.font.montserrat_extrabolditalic, FontWeight.ExtraBold, style = FontStyle.Italic)
)