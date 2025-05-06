package com.dmitrysukhov.lifetracker.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dmitrysukhov.lifetracker.R
import com.dmitrysukhov.lifetracker.TodoItem
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.BoldText
import com.dmitrysukhov.lifetracker.utils.H2
import com.dmitrysukhov.lifetracker.utils.InverseColor
import com.dmitrysukhov.lifetracker.utils.SimpleText
import com.dmitrysukhov.lifetracker.utils.TopBarState

@Composable
fun ViewProjectScreen(
    setTopBarState: (TopBarState) -> Unit, viewModel: ProjectsViewModel,
    navController: NavHostController
) {
    val project = viewModel.selectedProject
    val projectColor = project?.let { Color(it.color) } ?: Color(0xFF669DE5)
    setTopBarState(
        TopBarState(
            title = project?.title ?: "",
            color = projectColor,
            imagePath = if (project?.imagePath.isNullOrEmpty()) null else project?.imagePath,
            topBarActions = {
                IconButton(onClick = {
                    // Keep the selected project and navigate to edit screen
                    navController.navigate(NEW_PROJECT_SCREEN)
                }) {
                    Icon(Icons.Filled.Edit, contentDescription = null, tint = Color.White)
                }
            }
        )
    )

    Column(
        modifier = Modifier
            .background(BgColor)
            .fillMaxSize()
            .padding(horizontal = 24.dp),
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(stringResource(R.string.description_colon), style = BoldText, color = projectColor)
        Text(
            project?.description ?: "",
            style = SimpleText,
            color = InverseColor
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Text(stringResource(R.string.goal_colon), style = BoldText, color = projectColor)
            Spacer(modifier = Modifier.width(8.dp))
            Text(project?.goal ?: "Нет цели", style = SimpleText, color = InverseColor)
        }
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = projectColor.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.task),
                contentDescription = null, tint = projectColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.tasks_colon), style = H2, color = InverseColor)
            Spacer(modifier = Modifier.weight(1f))
            IconButton(modifier = Modifier.size(16.dp), onClick = {}) {
                Icon(
                    painter = painterResource(R.drawable.plus),
                    contentDescription = stringResource(R.string.add_task),
                    tint = projectColor
                )
            }
            Spacer(Modifier.width(20.dp))
        }
        TodoItem( //todo real tasks that belong to this project
            0, "", "", null,
            System.currentTimeMillis(), "", 456, false
        )
        Spacer(Modifier.height(28.dp))
        HorizontalDivider(color = projectColor.copy(alpha = 0.5f))
    }
}

const val VIEW_PROJECT_SCREEN = "view_project_screen"