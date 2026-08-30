package com.example.data.repository

import com.example.data.database.dao.QuickNoteDao
import com.example.data.database.dao.SyncQueueDao
import com.example.data.database.entity.QuickNoteEntity
import com.example.data.database.entity.SyncQueueEntity
import kotlinx.coroutines.flow.Flow

class NoteRepository(
    private val noteDao: QuickNoteDao,
    private val syncQueueDao: SyncQueueDao
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
            syncStatus = "PENDING_SYNC"
        )
        val id = noteDao.insertNote(note)
        syncQueueDao.enqueue(
            SyncQueueEntity(
                entityType = "NOTE",
                entityId = id.toString(),
                action = "CREATE",
                payload = "Note: $title [$tag]",
                status = "PENDING"
            )
        )
        return id
    }

    suspend fun updateNote(note: QuickNoteEntity) {
        val updated = note.copy(
            updatedAt = System.currentTimeMillis(),
            syncStatus = "PENDING_SYNC"
        )
        noteDao.updateNote(updated)
        syncQueueDao.enqueue(
            SyncQueueEntity(
                entityType = "NOTE",
                entityId = note.id.toString(),
                action = "UPDATE",
                payload = "Updated Note: ${note.title}",
                status = "PENDING"
            )
        )
    }

    suspend fun deleteNote(noteId: Long) {
        noteDao.deleteNote(noteId)
        syncQueueDao.enqueue(
            SyncQueueEntity(
                entityType = "NOTE",
                entityId = noteId.toString(),
                action = "DELETE",
                payload = "Deleted Note #$noteId",
                status = "PENDING"
            )
        )
    }
}
