package com.dmitrysukhov.lifetracker.projects

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dmitrysukhov.lifetracker.Project
import com.dmitrysukhov.lifetracker.TodoItem
import com.dmitrysukhov.lifetracker.todo.TodoDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ProjectsViewModel @Inject constructor(
    private val projectDao: ProjectDao,
    private val todoDao: TodoDao
) : ViewModel() {
    var selectedProject: Project? = null
    private val _projects = mutableStateListOf<Project>()
    val projects: List<Project> get() = _projects

    init {
        viewModelScope.launch {
            projectDao.getAllProjects().collectLatest { list ->
                _projects.clear()
                _projects.addAll(list)
            }
        }
        
        // Subscribe to task changes to update project stats
        viewModelScope.launch {
            updateProjectsWithTaskStats(todoDao.getAllTasks())
        }
    }

    fun addProject(project: Project) {
        viewModelScope.launch {
            project.imagePath?.let { path ->
                projectDao.insert(project.copy(imagePath = path))
            } ?: run {
                projectDao.insert(project)
            }
        }
    }
    
    fun deleteProject(projectId: Long) {
        viewModelScope.launch {
            projectDao.deleteProject(projectId)
        }
    }
    
    fun updateProject(project: Project) {
        viewModelScope.launch {
            val oldProject = projectDao.getProjectById(project.projectId)
            oldProject?.imagePath?.takeIf { it.isNotEmpty() }?.let { oldPath ->
                try {
                    File(oldPath).delete()
                } catch (_: Exception) {
                    // Handle file deletion error
                }
            }
            projectDao.update(project)
        }
    }
    
    private fun updateProjectsWithTaskStats(tasks: List<TodoItem>) {
        val updatedProjects = _projects.map { project ->
            val projectTasks = tasks.filter { it.projectId == project.projectId }
            project.copy(
                totalTasks = projectTasks.size,
                completedTasks = projectTasks.count { it.isDone }
            )
        }
        _projects.clear()
        _projects.addAll(updatedProjects)
    }
}