package com.dmitrysukhov.lifetracker

import android.content.Context
import androidx.room.Room
import com.dmitrysukhov.lifetracker.notes.NoteDao
import com.dmitrysukhov.lifetracker.notes.NoteRepository
import com.dmitrysukhov.lifetracker.notes.NoteRepositoryImpl
import com.dmitrysukhov.lifetracker.projects.ProjectDao
import com.dmitrysukhov.lifetracker.projects.ProjectRepository
import com.dmitrysukhov.lifetracker.projects.ProjectRepositoryImpl
import com.dmitrysukhov.lifetracker.todo.TodoDao
import com.dmitrysukhov.lifetracker.tracker.EventDao
import com.dmitrysukhov.lifetracker.tracker.EventRepository
import com.dmitrysukhov.lifetracker.habits.HabitDao
import com.dmitrysukhov.lifetracker.habits.HabitEventDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext, AppDatabase::class.java, "lifetracker_database"
        )
        .build()
    }

    @Provides
    fun provideTodoDao(database: AppDatabase): TodoDao {
        return database.todoDao()
    }

    @Provides
    fun provideHabitDao(database: AppDatabase): HabitDao = database.habitDao()

    @Provides
    fun provideHabitEventDao(database: AppDatabase): HabitEventDao = database.habitEventDao()

    @Provides
    fun provideProjectDao(database: AppDatabase): ProjectDao {
        return database.projectsDao()
    }

    @Provides
    @Singleton
    fun provideEventDao(database: AppDatabase): EventDao {
        return database.eventDao()
    }

    @Provides
    @Singleton
    fun provideNoteDao(database: AppDatabase): NoteDao {
        return database.noteDao()
    }

    @Provides
    @Singleton
    fun provideEventRepository(eventDao: EventDao): EventRepository {
        return EventRepository(eventDao)
    }

    @Provides
    @Singleton
    fun provideProjectRepository(projectDao: ProjectDao): ProjectRepository {
        return ProjectRepositoryImpl(projectDao)
    }
    
    @Provides
    @Singleton
    fun provideNoteRepository(noteDao: NoteDao): NoteRepository {
        return NoteRepositoryImpl(noteDao)
    }
}
