package com.dmitrysukhov.lifetracker

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.W600

val poppinsFontFamily = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold),
    Font(R.font.poppins_extrabold, FontWeight.ExtraBold),
    Font(R.font.poppins_black, FontWeight.Black)
)

val PoppinsBold = TextStyle(
    fontFamily = poppinsFontFamily,
    fontWeight = W600,
)

//todo тут надо сделать то же самое для других шрифтов чтобы поэкспериментировать с ними
//в шрифте Involve есть кучу вариаций, Oblique означает Курсив. можете спросить чатГпт как добавить курсив в FontFamily
//а у шрифта Pusia наоборот только один шрифт Bold. тем не менее ему тоже надо создать FontFamily
//после создания Involve и Pusia шрифтов надо подменить тут попинс на другой шрифт чтоб везде в приложении высветился другой шрифт
//запустить, сделать скриншоты чтоб посмотртеь как все выглядит - и отправить мне
    //дальше следующий шрифт - то же самое.
//и когда я одобрю - скажу что дальше сделать