package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.database.entity.QuickTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuickTaskDao {
    @Query("SELECT * FROM quick_tasks ORDER BY isCompleted ASC, updatedAt DESC")
    fun getAllTasks(): Flow<List<QuickTaskEntity>>

    @Query("SELECT * FROM quick_tasks WHERE isCompleted = 0 ORDER BY updatedAt DESC LIMIT :limit")
    fun getPendingTasks(limit: Int = 5): Flow<List<QuickTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: QuickTaskEntity): Long

    @Update
    suspend fun updateTask(task: QuickTaskEntity)

    @Query("DELETE FROM quick_tasks WHERE id = :taskId")
    suspend fun deleteTask(taskId: Long)

    @Query("SELECT COUNT(*) FROM quick_tasks WHERE syncStatus = 'PENDING_SYNC'")
    fun getPendingSyncCount(): Flow<Int>

    @Query("UPDATE quick_tasks SET syncStatus = 'SYNCED' WHERE syncStatus = 'PENDING_SYNC'")
    suspend fun markAllSynced()
}
