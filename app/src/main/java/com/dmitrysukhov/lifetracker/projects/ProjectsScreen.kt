package com.dmitrysukhov.lifetracker.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dmitrysukhov.lifetracker.utils.BgColor
import com.dmitrysukhov.lifetracker.utils.Blue
import com.dmitrysukhov.lifetracker.utils.BlueViolet
import com.dmitrysukhov.lifetracker.utils.DarkOrange
import com.dmitrysukhov.lifetracker.utils.ForestGreen
import com.dmitrysukhov.lifetracker.utils.Green
import com.dmitrysukhov.lifetracker.utils.LightGreen
import com.dmitrysukhov.lifetracker.utils.Magenta
import com.dmitrysukhov.lifetracker.utils.Mauve
import com.dmitrysukhov.lifetracker.utils.Montserrat
import com.dmitrysukhov.lifetracker.utils.Olive
import com.dmitrysukhov.lifetracker.utils.OliveGreen
import com.dmitrysukhov.lifetracker.utils.Orange
import com.dmitrysukhov.lifetracker.utils.PeriwinkleBlue
import com.dmitrysukhov.lifetracker.utils.Pink
import com.dmitrysukhov.lifetracker.utils.Purple
import com.dmitrysukhov.lifetracker.utils.Red
import com.dmitrysukhov.lifetracker.utils.RedViolet
import com.dmitrysukhov.lifetracker.utils.SkyBlue
import com.dmitrysukhov.lifetracker.utils.Teal
import com.dmitrysukhov.lifetracker.utils.Turquoise
import com.dmitrysukhov.lifetracker.utils.Yellow

@Composable
fun ProjectsScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
            .padding(24.dp)
    ) {
        items(projects) { project ->
            Item(
                title = project.title,
                progress = "${project.completed}/${project.total} выполнено",
                deadline = "до ${project.deadline}",
                gradient = generateGradient(project.color)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun Item(title: String, progress: String, deadline: String, gradient: Brush) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(gradient)
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(text = title, fontSize = 14.sp, fontFamily = Montserrat, fontWeight = FontWeight.Medium, color = Color.White)
        Spacer(modifier = Modifier.weight(1f))
        Row {
            Text(text = progress, fontSize = 12.sp, fontFamily = Montserrat, fontWeight = FontWeight.Medium,color = Color.White.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.weight(1f))
            Text(text = deadline, fontSize = 12.sp, fontFamily = Montserrat, fontWeight = FontWeight.Medium, color = Color.White.copy(alpha = 0.8f))
        }
    }
}


val projects = listOf(
    Project("Программирование", 4, 10, "25.10.2025", Red),
    Project("Лайф", 4, 10, "25.10.2025", DarkOrange),
    Project("Покупки", 2, 9, "12.03.2025", Orange),
    Project("Работа", 0, 100, "23.02.2025", Yellow),
    Project("Спорт", 10, 10, "12.03.2025", Olive),
    Project("Дизайн LifeTracker", 6, 13, "15.01.2025", OliveGreen),
    Project("Подготовиться к молодёжке", 2, 3, "10.01.2025", LightGreen),
    Project("Чтение книг", 3, 7, "20.02.2025", Green),
    Project("Учёба", 5, 12, "01.04.2025", Teal),
    Project("Отдых", 8, 15, "07.06.2025", ForestGreen),
    Project("Музыка", 6, 9, "14.03.2025", Turquoise),
    Project("Разработка приложения", 9, 20, "30.09.2025", Blue),
    Project("Фитнес", 4, 8, "10.05.2025", SkyBlue),
    Project("Кулинария", 3, 5, "18.07.2025", PeriwinkleBlue),
    Project("Волейбол", 7, 14, "05.11.2025", BlueViolet),
    Project("Игра на гитаре", 2, 6, "03.08.2025", Purple),
    Project("Художественное искусство", 5, 9, "21.12.2025", Mauve),
    Project("Подготовка к экзаменам", 8, 17, "15.04.2025", RedViolet),
    Project("Путешествия", 3, 7, "22.06.2025", Magenta),
    Project("Фотография", 4, 10, "12.09.2025", Pink)
)

data class Project(val title: String, val completed: Int, val total: Int, val deadline: String, val color: Color)


const val PROJECTS_SCREEN = "Projects"

fun generateGradient(baseColor: Color): Brush {
    val hsv = FloatArray(3)
    android.graphics.Color.RGBToHSV(
        (baseColor.red * 255).toInt(), (baseColor.green * 255).toInt(),
        (baseColor.blue * 255).toInt(), hsv
    )
    hsv[2] *= 0.7f
    return Brush.verticalGradient(listOf(baseColor, Color(android.graphics.Color.HSVToColor(hsv))))
}
