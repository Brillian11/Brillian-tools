package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.database.entity.CutlistProjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CutlistDao {
    @Query("SELECT * FROM cutlist_projects ORDER BY createdAt DESC")
    fun getAllCutlistProjects(): Flow<List<CutlistProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: CutlistProjectEntity): Long

    @Query("DELETE FROM cutlist_projects WHERE id = :id")
    suspend fun deleteProjectById(id: Long)
}
