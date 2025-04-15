package com.dmitrysukhov.lifetracker.projects

import com.dmitrysukhov.lifetracker.Project
import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
    fun getAllProjects(): Flow<List<Project>>
    suspend fun insertProject(project: Project)
    suspend fun updateProject(project: Project)
    suspend fun deleteProject(project: Project)
} 