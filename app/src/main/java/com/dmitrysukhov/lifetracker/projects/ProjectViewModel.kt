package com.dmitrysukhov.lifetracker.projects

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dmitrysukhov.lifetracker.Project
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProjectsViewModel @Inject constructor(
    private val projectDao: ProjectDao
) : ViewModel() {

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
            projectDao.insert(project)
        }
    }
}