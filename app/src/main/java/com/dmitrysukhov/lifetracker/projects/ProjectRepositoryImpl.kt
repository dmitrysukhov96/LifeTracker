package com.dmitrysukhov.lifetracker.projects

import com.dmitrysukhov.lifetracker.Project
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectRepositoryImpl @Inject constructor(
    private val projectDao: ProjectDao
) : ProjectRepository {
    override fun getAllProjects(): Flow<List<Project>> {
        return projectDao.getAllProjects()
    }

    override suspend fun insertProject(project: Project) {
        projectDao.insert(project)
    }

    override suspend fun updateProject(project: Project) {
        projectDao.update(project)
    }

    override suspend fun deleteProject(project: Project) {
        projectDao.delete(project)
    }
} 