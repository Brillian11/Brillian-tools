package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.database.entity.DashboardWidgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DashboardDao {
    @Query("SELECT * FROM dashboard_widgets ORDER BY displayOrder ASC")
    fun getAllWidgets(): Flow<List<DashboardWidgetEntity>>

    @Query("SELECT * FROM dashboard_widgets WHERE isPinned = 1 ORDER BY displayOrder ASC")
    fun getPinnedWidgets(): Flow<List<DashboardWidgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWidgets(widgets: List<DashboardWidgetEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWidget(widget: DashboardWidgetEntity)

    @Update
    suspend fun updateWidget(widget: DashboardWidgetEntity)

    @Query("DELETE FROM dashboard_widgets WHERE id = :widgetId")
    suspend fun deleteWidget(widgetId: String)

    @Query("SELECT COUNT(*) FROM dashboard_widgets")
    suspend fun getWidgetCount(): Int
}
