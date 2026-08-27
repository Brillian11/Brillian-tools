package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.database.entity.SyncQueueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncQueueDao {
    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' ORDER BY timestamp ASC")
    fun getPendingQueue(): Flow<List<SyncQueueEntity>>

    @Query("SELECT COUNT(*) FROM sync_queue WHERE status = 'PENDING'")
    fun getPendingCount(): Flow<Int>

    @Query("SELECT * FROM sync_queue ORDER BY timestamp DESC LIMIT 50")
    fun getAllQueueLogs(): Flow<List<SyncQueueEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(item: SyncQueueEntity)

    @Update
    suspend fun updateQueueItem(item: SyncQueueEntity)

    @Query("UPDATE sync_queue SET status = 'COMPLETED' WHERE status = 'PENDING'")
    suspend fun markAllCompleted()

    @Query("DELETE FROM sync_queue WHERE status = 'COMPLETED'")
    suspend fun purgeCompleted()
}
