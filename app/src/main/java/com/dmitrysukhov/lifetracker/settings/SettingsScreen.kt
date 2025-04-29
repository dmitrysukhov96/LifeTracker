package com.dmitrysukhov.lifetracker.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.H1
import com.dmitrysukhov.lifetracker.utils.InverseColor
import com.dmitrysukhov.lifetracker.utils.PineColor
import com.dmitrysukhov.lifetracker.utils.TopBarState
import androidx.core.content.edit

const val SETTINGS_SCREEN = "settings_screen"

@Composable
fun SettingsScreen(
    navController: NavHostController, setTopBarState: (TopBarState) -> Unit
) {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
    var currentUserName by remember { mutableStateOf(sharedPref.getString("user_name", "") ?: "") }
    var editedUserName by remember { mutableStateOf(currentUserName) }
    
    LaunchedEffect(Unit) {
        setTopBarState(TopBarState(title = "Settings"))
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.username),
            color = InverseColor,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            BasicTextField(
                value = editedUserName,
                onValueChange = { editedUserName = it },
                textStyle = H1.copy(color = InverseColor),
                modifier = Modifier.weight(1f)
            )
            if (editedUserName != currentUserName && editedUserName.isNotBlank()) {
                IconButton(modifier = Modifier.size(20.dp), onClick = {
                    sharedPref.edit { putString("user_name", editedUserName.trim()) }
                    currentUserName = editedUserName
                }) {
                    Icon(
                        painter = painterResource(R.drawable.tick),
                        contentDescription = null,
                        tint = PineColor, modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}