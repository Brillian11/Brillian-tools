package com.example.ui.screens.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.entity.QuickNoteEntity
import com.example.data.repository.NoteRepository
import com.example.ui.utils.NoteAttachmentHelper
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
    val isEditingMode: Boolean = false,
    val editingNoteId: Long? = null,
    val editorTitle: String = "",
    val editorContent: String = "",
    val editorTag: String = "Notes",
    val editorColorHex: String = "#3F51B5",
    val editorImagePaths: List<String> = emptyList(),
    val editorPdfPaths: List<String> = emptyList(),
    val isRichTextPreviewMode: Boolean = false
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

    fun openNewNoteEditor(
        initialTitle: String = "",
        initialContent: String = "",
        initialTag: String = "Notes"
    ) {
        _uiState.value = _uiState.value.copy(
            isEditingMode = true,
            editingNoteId = null,
            editorTitle = initialTitle,
            editorContent = initialContent,
            editorTag = initialTag,
            editorColorHex = if (initialTag == "Cutlist") "#2E7D32" else "#3F51B5",
            editorImagePaths = emptyList(),
            editorPdfPaths = emptyList(),
            isRichTextPreviewMode = false
        )
    }

    fun openEditNoteEditor(note: QuickNoteEntity) {
        _uiState.value = _uiState.value.copy(
            isEditingMode = true,
            editingNoteId = note.id,
            editorTitle = note.title,
            editorContent = note.content,
            editorTag = note.tag,
            editorColorHex = note.colorHex,
            editorImagePaths = NoteAttachmentHelper.parsePaths(note.imagePaths),
            editorPdfPaths = NoteAttachmentHelper.parsePaths(note.pdfPaths),
            isRichTextPreviewMode = false
        )
    }

    fun closeNoteEditor() {
        _uiState.value = _uiState.value.copy(isEditingMode = false)
    }

    fun updateEditorTitle(title: String) {
        _uiState.value = _uiState.value.copy(editorTitle = title)
    }

    fun updateEditorContent(content: String) {
        _uiState.value = _uiState.value.copy(editorContent = content)
    }

    fun updateEditorTag(tag: String) {
        _uiState.value = _uiState.value.copy(editorTag = tag)
    }

    fun updateEditorColorHex(colorHex: String) {
        _uiState.value = _uiState.value.copy(editorColorHex = colorHex)
    }

    fun togglePreviewMode() {
        _uiState.value = _uiState.value.copy(isRichTextPreviewMode = !_uiState.value.isRichTextPreviewMode)
    }

    fun addImageAttachment(filePath: String) {
        val current = _uiState.value.editorImagePaths.toMutableList()
        if (!current.contains(filePath)) {
            current.add(filePath)
            _uiState.value = _uiState.value.copy(editorImagePaths = current)
        }
    }

    fun removeImageAttachment(filePath: String) {
        val current = _uiState.value.editorImagePaths.toMutableList()
        current.remove(filePath)
        _uiState.value = _uiState.value.copy(editorImagePaths = current)
    }

    fun addPdfAttachment(filePath: String) {
        val current = _uiState.value.editorPdfPaths.toMutableList()
        if (!current.contains(filePath)) {
            current.add(filePath)
            _uiState.value = _uiState.value.copy(editorPdfPaths = current)
        }
    }

    fun removePdfAttachment(filePath: String) {
        val current = _uiState.value.editorPdfPaths.toMutableList()
        current.remove(filePath)
        _uiState.value = _uiState.value.copy(editorPdfPaths = current)
    }

    fun insertMarkdownFormatting(prefix: String, suffix: String = "") {
        val current = _uiState.value.editorContent
        val formattedSnippet = if (suffix.isNotEmpty()) {
            val placeholder = when (prefix) {
                "**" -> "bold text"
                "*" -> "italic text"
                "```\n" -> "code"
                else -> "text"
            }
            "$prefix$placeholder$suffix"
        } else {
            prefix
        }

        val newText = when {
            current.isEmpty() -> formattedSnippet
            current.endsWith("\n") -> "$current$formattedSnippet"
            else -> "$current\n$formattedSnippet"
        }
        _uiState.value = _uiState.value.copy(editorContent = newText)
    }

    fun saveCurrentNote() {
        val state = _uiState.value
        val title = state.editorTitle.ifBlank { "Untitled Note" }
        val content = state.editorContent
        val tag = state.editorTag
        val hexColor = state.editorColorHex
        val imagesStr = NoteAttachmentHelper.joinPaths(state.editorImagePaths)
        val pdfsStr = NoteAttachmentHelper.joinPaths(state.editorPdfPaths)

        viewModelScope.launch {
            if (state.editingNoteId == null) {
                noteRepository.addNote(
                    title = title,
                    content = content,
                    tag = tag,
                    colorHex = hexColor,
                    imagePaths = imagesStr,
                    pdfPaths = pdfsStr,
                    isMarkdown = true
                )
            } else {
                val existing = noteRepository.getNoteById(state.editingNoteId)
                if (existing != null) {
                    val updated = existing.copy(
                        title = title,
                        content = content,
                        tag = tag,
                        colorHex = hexColor,
                        imagePaths = imagesStr,
                        pdfPaths = pdfsStr,
                        isMarkdown = true,
                        updatedAt = System.currentTimeMillis()
                    )
                    noteRepository.updateNote(updated)
                }
            }
            closeNoteEditor()
        }
    }

    fun deleteCurrentNote() {
        val id = _uiState.value.editingNoteId
        if (id != null) {
            viewModelScope.launch {
                noteRepository.deleteNote(id)
                closeNoteEditor()
            }
        } else {
            closeNoteEditor()
        }
    }

    fun updateNoteContent(noteId: Long, newContent: String) {
        viewModelScope.launch {
            val existing = noteRepository.getNoteById(noteId)
            if (existing != null) {
                val updated = existing.copy(
                    content = newContent,
                    updatedAt = System.currentTimeMillis()
                )
                noteRepository.updateNote(updated)
            }
        }
    }

    fun addNote(title: String, content: String, tag: String, colorHex: String) {
        viewModelScope.launch {
            noteRepository.addNote(title, content, tag, colorHex)
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            noteRepository.deleteNote(noteId)
        }
    }

    fun saveCutlistAsNote(
        projectName: String,
        markdownContent: String,
        pdfPath: String? = null,
        csvPath: String? = null
    ) {
        viewModelScope.launch {
            val title = "Cutlist Project: $projectName"
            val pdfStr = if (!pdfPath.isNullOrBlank()) pdfPath else ""
            noteRepository.addNote(
                title = title,
                content = markdownContent,
                tag = "Cutlist",
                colorHex = "#2E7D32",
                imagePaths = "",
                pdfPaths = pdfStr,
                isMarkdown = true
            )
        }
    }
}
