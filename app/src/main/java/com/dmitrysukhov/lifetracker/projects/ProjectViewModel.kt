package com.dmitrysukhov.lifetracker.projects

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dmitrysukhov.lifetracker.Project
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ProjectsViewModel @Inject constructor(
    private val projectDao: ProjectDao
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
}