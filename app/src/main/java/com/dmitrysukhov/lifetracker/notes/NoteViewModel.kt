package com.dmitrysukhov.lifetracker.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dmitrysukhov.lifetracker.Note
import com.dmitrysukhov.lifetracker.Project
import com.dmitrysukhov.lifetracker.projects.ProjectDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {
    
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()
    
    private val _selectedNote = MutableStateFlow<Note?>(null)
    val selectedNote: StateFlow<Note?> = _selectedNote.asStateFlow()
    
    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()
    
    @Inject
    lateinit var projectsDao: ProjectDao
    
    init {
        loadNotes()
        viewModelScope.launch {
            // Defer projects loading to ensure projectsDao is injected
            kotlinx.coroutines.delay(100)
            loadProjects()
        }
    }
    
    private fun loadProjects() {
        viewModelScope.launch {
            try {
                projectsDao.getAllProjects().collectLatest { list ->
                    _projects.value = list
                }
            } catch (e: Exception) {
                // Handle case when projectsDao is not yet initialized
                e.printStackTrace()
            }
        }
    }
    
    private fun loadNotes() {
        viewModelScope.launch {
            noteRepository.getAllNotes().collectLatest { notesList ->
                _notes.value = notesList
            }
        }
    }
    
    fun selectNote(note: Note?) {
        _selectedNote.value = note
    }
    
//    fun clearSelectedNote() {
//        _selectedNote.value = null
//    }
//
//    fun getNoteById(id: Long) {
//        viewModelScope.launch {
//            val note = noteRepository.getNoteById(id)
//            _selectedNote.value = note
//        }
//    }
    
    fun createNote(title: String, content: String, projectId: Long? = null) {
        viewModelScope.launch {
            val note = Note(
                title = title,
                content = content,
                projectId = projectId
            )
            noteRepository.insertNote(note)
        }
    }
    
    fun updateNote(note: Note) {
        viewModelScope.launch {
            val updatedNote = note.copy(updatedAt = System.currentTimeMillis())
            noteRepository.updateNote(updatedNote)
            
            // Update selected note if it's the one being edited
            if (_selectedNote.value?.id == note.id) {
                _selectedNote.value = updatedNote
            }
        }
    }
    
    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteRepository.deleteNote(note)
            
            // Clear selected note if it's the one being deleted
            if (_selectedNote.value?.id == note.id) {
                _selectedNote.value = null
            }
        }
    }
} 