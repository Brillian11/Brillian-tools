package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.database.entity.ToolLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ToolLogDao {
    @Query("SELECT * FROM tool_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 20): Flow<List<ToolLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ToolLogEntity)

    @Query("DELETE FROM tool_logs")
    suspend fun clearLogs()
}
