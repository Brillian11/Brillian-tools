package com.example.ui.screens.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.entity.QuickNoteEntity
import com.example.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class QuickNotesUiState(
    val searchQuery: String = "",
    val selectedTag: String = "ALL",
    val showAddDialog: Boolean = false
)

class QuickNotesViewModel(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuickNotesUiState())
    val uiState: StateFlow<QuickNotesUiState> = _uiState.asStateFlow()

    val notes: StateFlow<List<QuickNoteEntity>> = combine(
        noteRepository.allNotes,
        _uiState
    ) { allNotes, state ->
        allNotes.filter { note ->
            val matchesQuery = note.title.contains(state.searchQuery, ignoreCase = true) ||
                    note.content.contains(state.searchQuery, ignoreCase = true)
            val matchesTag = state.selectedTag == "ALL" || note.tag.equals(state.selectedTag, ignoreCase = true)
            matchesQuery && matchesTag
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setSelectedTag(tag: String) {
        _uiState.value = _uiState.value.copy(selectedTag = tag)
    }

    fun setShowAddDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showAddDialog = show)
    }

    fun addNote(title: String, content: String, tag: String, colorHex: String) {
        viewModelScope.launch {
            noteRepository.addNote(title, content, tag, colorHex)
            setShowAddDialog(false)
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            noteRepository.deleteNote(noteId)
        }
    }
}
