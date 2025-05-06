package com.dmitrysukhov.lifetracker.common.ui

import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.Medium
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dmitrysukhov.lifetracker.utils.Blue
import com.dmitrysukhov.lifetracker.utils.BlueViolet
import com.dmitrysukhov.lifetracker.utils.DarkOrange
import com.dmitrysukhov.lifetracker.utils.Rosa
import com.dmitrysukhov.lifetracker.utils.H2
import com.dmitrysukhov.lifetracker.utils.InverseColor
import com.dmitrysukhov.lifetracker.utils.LightGreen
import com.dmitrysukhov.lifetracker.utils.Magenta
import com.dmitrysukhov.lifetracker.utils.Mauve
import com.dmitrysukhov.lifetracker.utils.Olive
import com.dmitrysukhov.lifetracker.utils.OliveGreen
import com.dmitrysukhov.lifetracker.utils.Orange
import com.dmitrysukhov.lifetracker.utils.PeriwinkleBlue
import com.dmitrysukhov.lifetracker.utils.PineColor
import com.dmitrysukhov.lifetracker.utils.Pink
import com.dmitrysukhov.lifetracker.utils.Purple
import com.dmitrysukhov.lifetracker.utils.Red
import com.dmitrysukhov.lifetracker.utils.RedViolet
import com.dmitrysukhov.lifetracker.utils.SimpleText
import com.dmitrysukhov.lifetracker.utils.SkyBlue
import com.dmitrysukhov.lifetracker.utils.Small
import com.dmitrysukhov.lifetracker.utils.Brown
import com.dmitrysukhov.lifetracker.utils.Turquoise
import com.dmitrysukhov.lifetracker.utils.WhitePine
import com.dmitrysukhov.lifetracker.utils.Yellow

@Composable
fun ThreeButtonsSelector(
    selected: Int, first: String, second: String, third: String, onSelect: (Int) -> Unit
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            modifier = Modifier
                .weight(1f)
                .alpha(if (selected == 0) 1f else 0.4f),
            colors = buttonColors(containerColor = PineColor),
            onClick = { onSelect(0) }
        ) {
            Text(
                first, style = SimpleText, fontWeight = if (selected == 0) Bold else Medium,
                color = Color.White
            )
        }
        Button(
            modifier = Modifier
                .weight(1f)
                .alpha(if (selected == 1) 1f else 0.4f),
            colors = buttonColors(containerColor = PineColor), onClick = { onSelect(1) }
        ) {
            Text(
                second, style = SimpleText, fontWeight = if (selected == 1) Bold else Medium,
                color = Color.White
            )
        }
        Button(
            modifier = Modifier
                .weight(1f)
                .alpha(if (selected == 2) 1f else 0.4f),
            colors = buttonColors(containerColor = PineColor), onClick = { onSelect(2) }
        ) {
            Text(
                third, style = SimpleText, fontWeight = if (selected == 2) Bold else Medium,
                color = Color.White
            )
        }
    }
}

@Composable
fun ToggleSelector(
    leftText: String, rightText: String, selectedIndex: Int, onSelectedChange: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = leftText, style = H2, modifier = Modifier.padding(8.dp),
            color = if (selectedIndex == 0) PineColor else InverseColor,
        )
        var checked by rememberSaveable { mutableStateOf(selectedIndex == 1) }
        val transition = updateTransition(targetState = checked, label = "switch")
        val knobOffset by transition.animateDp(label = "knobOffset") { if (it) 20.dp else 0.dp }
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(28.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(PineColor)
                .clickable {
                    checked = !checked
                    onSelectedChange(if (checked) 1 else 0)
                }
                .padding(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .offset(x = knobOffset)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(WhitePine)
            )
        }
        Text(
            text = rightText,
            style = H2,
            color = if (selectedIndex == 1) PineColor else InverseColor,
            modifier = Modifier.padding(8.dp)
        )
    }
}


@Composable
fun ColorPicker(
    selectedColorInt: Int, onColorSelected: (Int) -> Unit
) {
    val colors = listOf(Rosa, Red, DarkOrange, Orange, Yellow, Olive, OliveGreen, LightGreen, PineColor,
        Turquoise, Blue, SkyBlue, PeriwinkleBlue, BlueViolet, Purple,
        Magenta, Pink, RedViolet, Mauve, Brown
    )
    Column(Modifier.padding(start = 24.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            colors.take(10).forEachIndexed { index, color ->
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .shadow(2.dp, CircleShape)
                        .clip(CircleShape)
                        .background(color)
                        .clickable { onColorSelected(color.toArgb()) }
                ) {
                    if (selectedColorInt == color.toArgb()) {
                        Icon(
                            imageVector = Icons.Default.Check, contentDescription = null,
                            tint = Color.White, modifier = Modifier
                                .align(Alignment.Center)
                                .size(16.dp)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            colors.drop(10).forEachIndexed { idx, color ->
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .shadow(2.dp, CircleShape)
                        .clip(CircleShape)
                        .background(color)
                        .clickable { onColorSelected(color.toArgb()) }
                ) {
                    if (selectedColorInt == color.toArgb()) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(16.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun SubtitleWithIcon(textRes: Int, iconRes: Int, iconColor: Color) {
    Row(Modifier.padding(bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null, tint = iconColor,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(text = stringResource(textRes), style = H2)
    }
}

@Composable
fun ProjectTag(text: String, color: Color) {
    Box(
        Modifier
            .widthIn(max = 114.dp)
            .background(color, shape = RoundedCornerShape(52))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) { Text(text = text, color = Color.White, style = Small, maxLines = 1, overflow = TextOverflow.Ellipsis) }
}

@Composable
fun EmptyPlaceholder(titleRes: Int, textRes: Int) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp), contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(titleRes), style = H2,
                color = PineColor, modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = stringResource(textRes), style = Small,
                color = InverseColor.copy(alpha = 0.7f)
            )
        }
    }
}