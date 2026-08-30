package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.database.dao.CutlistDao
import com.example.data.database.dao.DashboardDao
import com.example.data.database.dao.EngineeringEstimateDao
import com.example.data.database.dao.MaterialDao
import com.example.data.database.dao.QuickNoteDao
import com.example.data.database.dao.QuickTaskDao
import com.example.data.database.dao.SyncQueueDao
import com.example.data.database.dao.ToolLogDao
import com.example.data.database.entity.CutlistProjectEntity
import com.example.data.database.entity.DashboardWidgetEntity
import com.example.data.database.entity.EngineeringEstimateEntity
import com.example.data.database.entity.MaterialEntity
import com.example.data.database.entity.QuickNoteEntity
import com.example.data.database.entity.QuickTaskEntity
import com.example.data.database.entity.SyncQueueEntity
import com.example.data.database.entity.ToolLogEntity

@Database(
    entities = [
        DashboardWidgetEntity::class,
        QuickTaskEntity::class,
        QuickNoteEntity::class,
        ToolLogEntity::class,
        SyncQueueEntity::class,
        MaterialEntity::class,
        CutlistProjectEntity::class,
        EngineeringEstimateEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dashboardDao(): DashboardDao
    abstract fun quickTaskDao(): QuickTaskDao
    abstract fun quickNoteDao(): QuickNoteDao
    abstract fun toolLogDao(): ToolLogDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun materialDao(): MaterialDao
    abstract fun cutlistDao(): CutlistDao
    abstract fun engineeringEstimateDao(): EngineeringEstimateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "brillian_tools_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
