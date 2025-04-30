package com.dmitrysukhov.lifetracker.notes

import com.dmitrysukhov.lifetracker.Note
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface NoteRepository {
    fun getAllNotes(): Flow<List<Note>>
    suspend fun getNoteById(id: Long): Note?
    suspend fun insertNote(note: Note): Long
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(note: Note)
}

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepository {
    
    override fun getAllNotes(): Flow<List<Note>> = 
        noteDao.getAllNotes()
    
    override suspend fun getNoteById(id: Long): Note? = 
        noteDao.getNoteById(id)
    
    override suspend fun insertNote(note: Note): Long = 
        noteDao.insertNote(note)
    
    override suspend fun updateNote(note: Note) = 
        noteDao.updateNote(note)
    
    override suspend fun deleteNote(note: Note) = 
        noteDao.deleteNote(note)
} 