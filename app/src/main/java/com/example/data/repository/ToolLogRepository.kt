package com.example.data.repository

import com.example.data.database.dao.ToolLogDao
import com.example.data.database.entity.ToolLogEntity
import kotlinx.coroutines.flow.Flow

class ToolLogRepository(
    private val toolLogDao: ToolLogDao,
    private val noteRepository: NoteRepository? = null
) {
    val recentLogs: Flow<List<ToolLogEntity>> = toolLogDao.getRecentLogs(30)

    suspend fun logToolActivity(toolType: String, title: String, summary: String, value: Double = 0.0) {
        toolLogDao.insertLog(
            ToolLogEntity(
                toolType = toolType,
                title = title,
                summary = summary,
                value = value,
                timestamp = System.currentTimeMillis()
            )
        )
        // Write all logs inside quick notes as well
        noteRepository?.addNote(
            title = "Log: $title",
            content = summary,
            tag = "Log",
            colorHex = "#FF5722" // Orange color for logs
        )
    }

    suspend fun clearLogs() {
        toolLogDao.clearLogs()
    }
}
