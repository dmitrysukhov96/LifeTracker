import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TodoViewModel : ViewModel() {
    // Состояние для списка задач
    private val _todoList = MutableStateFlow<List<TodoItem>>(emptyList())
    val todoList: StateFlow<List<TodoItem>> get() = _todoList

    // Состояние для выбранной задачи
    private val _selectedTask = MutableStateFlow<TodoItem?>(null)
    val selectedTask: StateFlow<TodoItem?> get() = _selectedTask

    // Установка выбранной задачи
    fun setSelectedTask(todoItem: TodoItem) {
        _selectedTask.value = todoItem
    }

    // Добавление задачи
    fun addTask(taskTitle: String) {
        val newId = _todoList.value.maxOfOrNull { it.id }?.plus(1) ?: 1
        val newTask = TodoItem(id = newId, text = taskTitle, description = "", dueDate = "")
        _todoList.value = _todoList.value + newTask
    }

    // Удаление задачи
    fun removeTask(taskId: Int) {
        _todoList.value = _todoList.value.filterNot { it.id == taskId }
    }

    // Редактирование задачи
    fun updateTask(task: TodoItem) {
        _todoList.value = _todoList.value.map {
            if (it.id == task.id) task else it
        }
    }
}

data class TodoItem(
    val id: Int,
    val text: String,
    val description: String? = null,
    val dueDate: String? = null
)


//    fun deleteTask(item: TodoItem) {
//        viewModelScope.launch {
//            todoDao.deleteTask(item)
//            loadTasks()
//        }
//    }

