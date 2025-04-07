package com.dmitrysukhov.lifetracker.projects

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight.Companion.W700
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.dmitrysukhov.lifetracker.Project
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.H1
import com.dmitrysukhov.lifetracker.utils.H2
import com.dmitrysukhov.lifetracker.utils.InverseColor
import com.dmitrysukhov.lifetracker.utils.Montserrat
import com.dmitrysukhov.lifetracker.utils.PineColor
import com.dmitrysukhov.lifetracker.utils.TopBarState

@Composable
fun NewProjectScreen(setTopBarState: (TopBarState) -> Unit, navController: NavHostController) {
    val viewModel: ProjectsViewModel = hiltViewModel()
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(Unit) {
        setTopBarState(TopBarState("New Project") {
            IconButton(onClick = {
                viewModel.addProject(Project(title = title, color = generateRandomColor()))
                navController.navigateUp()
            }) {
                if (title.isNotBlank()) Icon(
                    painter = painterResource(R.drawable.tick),
                    contentDescription = null, tint = Color.White
                )
            }
        })
    }
    Column(
        modifier = Modifier
            .background(BgColor)
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        BasicTextField(
            value = title, onValueChange = { title = it },
            textStyle = H1.copy(color = InverseColor), decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (title.isEmpty())
                        Text("Заголовок", style = H1, color = PineColor.copy(0.5f))
                    innerTextField()
                }
            }, modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()

        BasicTextField(
            value = description,
            onValueChange = { description = it },
            textStyle = TextStyle(
                fontSize = 16.sp,
                fontWeight = W700,
                fontFamily = Montserrat,
                color = InverseColor
            ),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (description.isEmpty()) Text(
                        stringResource(R.string.description_hint),
                        style = H2, color = PineColor.copy(0.5f)
                    )
                    innerTextField()
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Column(Modifier.padding(top = 32.dp)) {
            Row () {
                Image(painter = painterResource(R.drawable.palette), contentDescription = "", Modifier.padding(end = 8.dp))
                Text(text = "Выберите цвет проекта", style = H2)
            }
            val colors = listOf(
                Color(0xFFFA3535),
                Color(0xFFFF582E),
                Color(0xFFFFA91F),
                Color(0xFFFFE030),
                Color(0xFFDBE204),
                Color(0xFFC1FF4D),
                Color(0xFF8FFF2E),
                Color(0xFF84E09E),
                Color(0xFF39E25D),
                Color(0xFF14C56D),
                Color(0xFF0ECC8A),
                Color(0xFF29B8D9),
                Color(0xFF669DE5),
                Color(0xFF737AFF),
                Color(0xFF7940FF),
                Color(0xFF983DC2),
                Color(0xFFC02A39),
                Color(0xFFED1F60),
                Color(0xFFE056CE),
                Color(0xFFF87687)
            )

            var selectedColor by remember { mutableStateOf(colors[0]) }

            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(10.dp)
            ) {
                colors.forEach { color ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { selectedColor = color }
                    ) {
                        if (selectedColor == color) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Выбран",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

const val NEW_PROJECT_SCREEN = "new_project_screen"