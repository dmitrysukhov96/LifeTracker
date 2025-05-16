package com.dmitrysukhov.lifetracker.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.H1
import com.dmitrysukhov.lifetracker.utils.InverseColor
import com.dmitrysukhov.lifetracker.utils.Montserrat
import com.dmitrysukhov.lifetracker.utils.PineColor
import com.dmitrysukhov.lifetracker.utils.SimpleText
import com.dmitrysukhov.lifetracker.utils.ThemeManager
import com.dmitrysukhov.lifetracker.utils.TopBarState
import java.util.Locale

const val SETTINGS_SCREEN = "settings_screen"

enum class ThemeMode(val stringRes: Int) {
    SYSTEM(R.string.theme_system), LIGHT(R.string.theme_light), DARK(R.string.theme_dark)
}

@Composable
fun SettingsScreen(setTopBarState: (TopBarState) -> Unit) {
    val context = LocalContext.current

    setTopBarState(TopBarState(context.getString(R.string.settings), screen = SETTINGS_SCREEN))

    val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    var currentUserName by remember { mutableStateOf(sharedPref.getString("user_name", "") ?: "") }
    var editedUserName by remember { mutableStateOf(currentUserName) }
    var languageExpanded by remember { mutableStateOf(false) }
    var selectedLanguage by remember {
        mutableStateOf(
            sharedPref.getString("language", "en") ?: "en"
        )
    }
    var themeExpanded by remember { mutableStateOf(false) }
    var selectedTheme by remember {
        val savedTheme =
            sharedPref.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        mutableStateOf(savedTheme)
    }

    fun applyTheme(theme: ThemeMode) {
        when (theme) {
            ThemeMode.LIGHT -> ThemeManager.setThemeMode(ThemeMode.LIGHT)
            ThemeMode.DARK -> ThemeManager.setThemeMode(ThemeMode.DARK)
            ThemeMode.SYSTEM -> ThemeManager.setThemeMode(ThemeMode.SYSTEM)
        }
    }

    fun applyLanguage(languageCode: String) {
        val locale = when (languageCode) {
            "ru" -> Locale("ru")
            "uk" -> Locale("uk")
            "de" -> Locale("de")
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
            else -> Locale("en")
        }
        Locale.setDefault(locale)
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
        setTopBarState(TopBarState(context.getString(R.string.settings), screen = SETTINGS_SCREEN))
        (context as? Activity)?.let {
            it.finish()
            it.startActivity(it.intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(R.string.username),
            color = InverseColor, style = SimpleText,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            BasicTextField(
                value = editedUserName, cursorBrush = SolidColor(PineColor),
                onValueChange = { editedUserName = it }, textStyle = H1.copy(color = InverseColor),
                modifier = Modifier.weight(1f)
            )
            if (editedUserName != currentUserName && editedUserName.isNotBlank()) {
                IconButton(
                    modifier = Modifier.size(20.dp), onClick = {
                        sharedPref.edit { putString("user_name", editedUserName.trim()) }
                        currentUserName = editedUserName
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.tick), contentDescription = null,
                        tint = PineColor, modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.theme), color = InverseColor, style = SimpleText,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .clickable { themeExpanded = true }
                    .fillMaxWidth()
                    .height(48.dp)
                    .border(
                        1.dp,
                        InverseColor.copy(0.5f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (selectedTheme) {
                        ThemeMode.SYSTEM.name -> stringResource(ThemeMode.SYSTEM.stringRes)
                        ThemeMode.LIGHT.name -> stringResource(ThemeMode.LIGHT.stringRes)
                        ThemeMode.DARK.name -> stringResource(ThemeMode.DARK.stringRes)
                        else -> stringResource(ThemeMode.SYSTEM.stringRes)
                    }, fontFamily = Montserrat, fontSize = 16.sp, fontWeight = FontWeight.Medium,
                    color = InverseColor, modifier = Modifier.weight(1f)
                )
                Icon(
                    painterResource(R.drawable.arrow_down), contentDescription = null,
                    tint = PineColor, modifier = Modifier
                        .padding(top = 1.dp)
                        .rotate(if (themeExpanded) 180f else 0f)
                )
            }

            DropdownMenu(
                expanded = themeExpanded,
                onDismissRequest = { themeExpanded = false },
                modifier = Modifier
                    .background(BgColor.copy(0.6f))
                    .padding(horizontal = 8.dp)
            ) {
                ThemeMode.entries.forEach { theme ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                stringResource(theme.stringRes),
                                fontFamily = Montserrat,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = InverseColor
                            )
                        },
                        onClick = {
                            if (selectedTheme != theme.name) {
                                selectedTheme = theme.name
                                sharedPref.edit {
                                    putString("theme_mode", theme.name)
                                }
                                themeExpanded = false
                                applyTheme(theme)
                            } else {
                                themeExpanded = false
                            }
                        }
                    )
                }
            }
        }

        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.language),
            color = InverseColor,
            style = SimpleText,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .clickable { languageExpanded = true }
                    .fillMaxWidth()
                    .height(48.dp)
                    .border(
                        1.dp,
                        InverseColor.copy(0.5f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (selectedLanguage) {
                        "en" -> "English"
                        "ru" -> "Русский"
                        "uk" -> "Українська"
                        "de" -> "Deutsch"
                        "fr" -> "Français"
                        "es" -> "Español"
                        "it" -> "Italiano"
                        "bg" -> "Български"
                        "ja" -> "日本語"
                        "ko" -> "한국어"
                        "lt" -> "Lietuvių"
                        "sr" -> "Српски"
                        "zh" -> "中文"
                        "he", "iw" -> "עברית"
                        else -> "English"
                    },
                    fontFamily = Montserrat,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = InverseColor,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    painterResource(R.drawable.arrow_down),
                    contentDescription = null,
                    tint = PineColor,
                    modifier = Modifier
                        .padding(top = 1.dp)
                        .rotate(if (languageExpanded) 180f else 0f)
                )
            }

            DropdownMenu(
                expanded = languageExpanded,
                onDismissRequest = { languageExpanded = false },
                modifier = Modifier
                    .background(BgColor.copy(0.6f))
                    .padding(horizontal = 8.dp)
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            "English",
                            fontFamily = Montserrat,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = InverseColor
                        )
                    },
                    onClick = {
                        if (selectedLanguage != "en") {
                            selectedLanguage = "en"
                            sharedPref.edit {
                                putString("language", "en")
                            }
                            languageExpanded = false

                            // Apply language immediately
                            applyLanguage("en")
                        } else {
                            languageExpanded = false
                        }
                    }
                )

                DropdownMenuItem(
                    text = {
                        Text(
                            "Русский",
                            fontFamily = Montserrat,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = InverseColor
                        )
                    },
                    onClick = {
                        if (selectedLanguage != "ru") {
                            selectedLanguage = "ru"
                            sharedPref.edit {
                                putString("language", "ru")
                            }
                            languageExpanded = false

                            // Apply language immediately
                            applyLanguage("ru")
                        } else {
                            languageExpanded = false
                        }
                    }
                )

                DropdownMenuItem(
                    text = {
                        Text(
                            "Українська",
                            fontFamily = Montserrat,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = InverseColor
                        )
                    },
                    onClick = {
                        if (selectedLanguage != "uk") {
                            selectedLanguage = "uk"
                            sharedPref.edit {
                                putString("language", "uk")
                            }
                            languageExpanded = false

                            // Apply language immediately
                            applyLanguage("uk")
                        } else {
                            languageExpanded = false
                        }
                    }
                )
                
                DropdownMenuItem(
                    text = {
                        Text(
                            "Deutsch",
                            fontFamily = Montserrat,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = InverseColor
                        )
                    },
                    onClick = {
                        if (selectedLanguage != "de") {
                            selectedLanguage = "de"
                            sharedPref.edit {
                                putString("language", "de")
                            }
                            languageExpanded = false
                            applyLanguage("de")
                        } else {
                            languageExpanded = false
                        }
                    }
                )
                
                DropdownMenuItem(
                    text = {
                        Text(
                            "Français",
                            fontFamily = Montserrat,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = InverseColor
                        )
                    },
                    onClick = {
                        if (selectedLanguage != "fr") {
                            selectedLanguage = "fr"
                            sharedPref.edit {
                                putString("language", "fr")
                            }
                            languageExpanded = false
                            applyLanguage("fr")
                        } else {
                            languageExpanded = false
                        }
                    }
                )
                
                DropdownMenuItem(
                    text = {
                        Text(
                            "Español",
                            fontFamily = Montserrat,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = InverseColor
                        )
                    },
                    onClick = {
                        if (selectedLanguage != "es") {
                            selectedLanguage = "es"
                            sharedPref.edit {
                                putString("language", "es")
                            }
                            languageExpanded = false
                            applyLanguage("es")
                        } else {
                            languageExpanded = false
                        }
                    }
                )
                
                DropdownMenuItem(
                    text = {
                        Text(
                            "Italiano",
                            fontFamily = Montserrat,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = InverseColor
                        )
                    },
                    onClick = {
                        if (selectedLanguage != "it") {
                            selectedLanguage = "it"
                            sharedPref.edit {
                                putString("language", "it")
                            }
                            languageExpanded = false
                            applyLanguage("it")
                        } else {
                            languageExpanded = false
                        }
                    }
                )
                
                DropdownMenuItem(
                    text = {
                        Text(
                            "Български",
                            fontFamily = Montserrat,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = InverseColor
                        )
                    },
                    onClick = {
                        if (selectedLanguage != "bg") {
                            selectedLanguage = "bg"
                            sharedPref.edit {
                                putString("language", "bg")
                            }
                            languageExpanded = false
                            applyLanguage("bg")
                        } else {
                            languageExpanded = false
                        }
                    }
                )
                
                DropdownMenuItem(
                    text = {
                        Text(
                            "日本語",
                            fontFamily = Montserrat,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = InverseColor
                        )
                    },
                    onClick = {
                        if (selectedLanguage != "ja") {
                            selectedLanguage = "ja"
                            sharedPref.edit {
                                putString("language", "ja")
                            }
                            languageExpanded = false
                            applyLanguage("ja")
                        } else {
                            languageExpanded = false
                        }
                    }
                )
                
                DropdownMenuItem(
                    text = {
                        Text(
                            "한국어",
                            fontFamily = Montserrat,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = InverseColor
                        )
                    },
                    onClick = {
                        if (selectedLanguage != "ko") {
                            selectedLanguage = "ko"
                            sharedPref.edit {
                                putString("language", "ko")
                            }
                            languageExpanded = false
                            applyLanguage("ko")
                        } else {
                            languageExpanded = false
                        }
                    }
                )
                
                DropdownMenuItem(
                    text = {
                        Text(
                            "Lietuvių",
                            fontFamily = Montserrat,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = InverseColor
                        )
                    },
                    onClick = {
                        if (selectedLanguage != "lt") {
                            selectedLanguage = "lt"
                            sharedPref.edit {
                                putString("language", "lt")
                            }
                            languageExpanded = false
                            applyLanguage("lt")
                        } else {
                            languageExpanded = false
                        }
                    }
                )
                
                DropdownMenuItem(
                    text = {
                        Text(
                            "Српски",
                            fontFamily = Montserrat,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = InverseColor
                        )
                    },
                    onClick = {
                        if (selectedLanguage != "sr") {
                            selectedLanguage = "sr"
                            sharedPref.edit {
                                putString("language", "sr")
                            }
                            languageExpanded = false
                            applyLanguage("sr")
                        } else {
                            languageExpanded = false
                        }
                    }
                )
                
                DropdownMenuItem(
                    text = {
                        Text(
                            "中文",
                            fontFamily = Montserrat,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = InverseColor
                        )
                    },
                    onClick = {
                        if (selectedLanguage != "zh") {
                            selectedLanguage = "zh"
                            sharedPref.edit {
                                putString("language", "zh")
                            }
                            languageExpanded = false
                            applyLanguage("zh")
                        } else {
                            languageExpanded = false
                        }
                    }
                )
                
                DropdownMenuItem(
                    text = {
                        Text(
                            "עברית",
                            fontFamily = Montserrat,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = InverseColor
                        )
                    },
                    onClick = {
                        if (selectedLanguage != "he") {
                            selectedLanguage = "he"
                            sharedPref.edit {
                                putString("language", "he")
                            }
                            languageExpanded = false
                            applyLanguage("he")
                        } else {
                            languageExpanded = false
                        }
                    }
                )
            }
        }
    }
}