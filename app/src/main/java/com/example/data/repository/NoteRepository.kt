package com.example.data.repository

import com.example.data.database.dao.QuickNoteDao
import com.example.data.database.entity.QuickNoteEntity
import kotlinx.coroutines.flow.Flow

class NoteRepository(
    private val noteDao: QuickNoteDao
) {
    val allNotes: Flow<List<QuickNoteEntity>> = noteDao.getAllNotes()

    suspend fun getNoteById(noteId: Long): QuickNoteEntity? = noteDao.getNoteById(noteId)

    suspend fun addNote(
        title: String,
        content: String,
        tag: String = "Work",
        colorHex: String = "#3F51B5",
        imagePaths: String = "",
        pdfPaths: String = "",
        isMarkdown: Boolean = true
    ): Long {
        val note = QuickNoteEntity(
            title = title,
            content = content,
            tag = tag,
            colorHex = colorHex,
            imagePaths = imagePaths,
            pdfPaths = pdfPaths,
            isMarkdown = isMarkdown,
            updatedAt = System.currentTimeMillis(),
            syncStatus = "SYNCED"
        )
        return noteDao.insertNote(note)
    }

    suspend fun updateNote(note: QuickNoteEntity) {
        val updated = note.copy(
            updatedAt = System.currentTimeMillis(),
            syncStatus = "SYNCED"
        )
        noteDao.updateNote(updated)
    }

    suspend fun deleteNote(noteId: Long) {
        noteDao.deleteNote(noteId)
    }
}
