package com.dmitrysukhov.lifetracker.todo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dmitrysukhov.lifetracker.R

@Composable
fun TimeInputDialog(
    initialHours: Long = 0,
    initialMinutes: Long = 0,
    onDismiss: () -> Unit,
    onTimeSet: (hours: Long, minutes: Long) -> Unit
) {
    var hoursText by remember { mutableStateOf(initialHours.toString()) }
    var minutesText by remember { mutableStateOf(initialMinutes.toString()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.time_for_task)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTimeField(
                        value = hoursText,
                        onValueChange = { hoursText = it },
                        label = stringResource(R.string.hours),
                        modifier = Modifier.weight(1f)
                    )
                    Text(":", fontSize = 20.sp, modifier = Modifier.padding(horizontal = 8.dp))
                    OutlinedTimeField(
                        value = minutesText,
                        onValueChange = { minutesText = it },
                        label = stringResource(R.string.minutes),
                        modifier = Modifier.weight(1f)
                    )
                }
                
                errorMessage?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                try {
                    val hours = hoursText.toLongOrNull() ?: 0
                    val minutes = minutesText.toLongOrNull() ?: 0
                    if (minutes >= 60) {
                        errorMessage = "Минуты должны быть меньше 60"
                        return@TextButton
                    }
                    onTimeSet(hours, minutes)
                } catch (_: Exception) {
                    errorMessage = "Неверный формат времени"
                }
            }) { Text(stringResource(R.string.ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
fun OutlinedTimeField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                onValueChange(newValue)
            }
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        ),
        modifier = modifier
    )
}