package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.database.entity.QuickNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuickNoteDao {
    @Query("SELECT * FROM quick_notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<QuickNoteEntity>>

    @Query("SELECT * FROM quick_notes WHERE id = :noteId LIMIT 1")
    suspend fun getNoteById(noteId: Long): QuickNoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: QuickNoteEntity): Long

    @Update
    suspend fun updateNote(note: QuickNoteEntity)

    @Query("DELETE FROM quick_notes WHERE id = :noteId")
    suspend fun deleteNote(noteId: Long)

    @Query("SELECT COUNT(*) FROM quick_notes WHERE syncStatus = 'PENDING_SYNC'")
    fun getPendingSyncCount(): Flow<Int>

    @Query("UPDATE quick_notes SET syncStatus = 'SYNCED' WHERE syncStatus = 'PENDING_SYNC'")
    suspend fun markAllSynced()
}
