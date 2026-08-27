package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.database.entity.EngineeringEstimateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EngineeringEstimateDao {
    @Query("SELECT * FROM engineering_estimates ORDER BY timestamp DESC")
    fun getAllEstimates(): Flow<List<EngineeringEstimateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEstimate(estimate: EngineeringEstimateEntity): Long

    @Query("DELETE FROM engineering_estimates WHERE id = :id")
    suspend fun deleteEstimateById(id: Long)
}
