package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class WorkProject(
    val id: String,
    val name: String,
    val client: String = "",
    val hourlyRate: Double = 45.0,
    val colorHex: String = "#3F51B5",
    val createdAt: Long = System.currentTimeMillis()
)

data class WorkTask(
    val id: String,
    val projectId: String,
    val name: String,
    val isCompleted: Boolean = false,
    val reminderTime: Long? = null,
    val hasReminder: Boolean = false
)

data class WorkSubtask(
    val id: String,
    val taskId: String,
    val name: String,
    val isCompleted: Boolean = false
)

data class TimeLog(
    val id: String,
    val projectId: String,
    val durationSeconds: Long,
    val laborCost: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)

class WorkTrackingRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("work_tracking_prefs", Context.MODE_PRIVATE)

    fun saveProjects(projects: List<WorkProject>) {
        val array = JSONArray()
        projects.forEach { proj ->
            val obj = JSONObject().apply {
                put("id", proj.id)
                put("name", proj.name)
                put("client", proj.client)
                put("hourlyRate", proj.hourlyRate)
                put("colorHex", proj.colorHex)
                put("createdAt", proj.createdAt)
            }
            array.put(obj)
        }
        prefs.edit().putString("projects", array.toString()).apply()
    }

    fun loadProjects(): List<WorkProject> {
        val str = prefs.getString("projects", null) ?: return emptyList()
        val list = mutableListOf<WorkProject>()
        try {
            val array = JSONArray(str)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    WorkProject(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        client = obj.optString("client", ""),
                        hourlyRate = obj.optDouble("hourlyRate", 45.0),
                        colorHex = obj.optString("colorHex", "#3F51B5"),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun saveTasks(tasks: List<WorkTask>) {
        val array = JSONArray()
        tasks.forEach { t ->
            val obj = JSONObject().apply {
                put("id", t.id)
                put("projectId", t.projectId)
                put("name", t.name)
                put("isCompleted", t.isCompleted)
                if (t.reminderTime != null) put("reminderTime", t.reminderTime)
                put("hasReminder", t.hasReminder)
            }
            array.put(obj)
        }
        prefs.edit().putString("tasks", array.toString()).apply()
    }

    fun loadTasks(): List<WorkTask> {
        val str = prefs.getString("tasks", null) ?: return emptyList()
        val list = mutableListOf<WorkTask>()
        try {
            val array = JSONArray(str)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    WorkTask(
                        id = obj.getString("id"),
                        projectId = obj.getString("projectId"),
                        name = obj.getString("name"),
                        isCompleted = obj.optBoolean("isCompleted", false),
                        reminderTime = if (obj.has("reminderTime")) obj.getLong("reminderTime") else null,
                        hasReminder = obj.optBoolean("hasReminder", false)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun saveSubtasks(subtasks: List<WorkSubtask>) {
        val array = JSONArray()
        subtasks.forEach { s ->
            val obj = JSONObject().apply {
                put("id", s.id)
                put("taskId", s.taskId)
                put("name", s.name)
                put("isCompleted", s.isCompleted)
            }
            array.put(obj)
        }
        prefs.edit().putString("subtasks", array.toString()).apply()
    }

    fun loadSubtasks(): List<WorkSubtask> {
        val str = prefs.getString("subtasks", null) ?: return emptyList()
        val list = mutableListOf<WorkSubtask>()
        try {
            val array = JSONArray(str)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    WorkSubtask(
                        id = obj.getString("id"),
                        taskId = obj.getString("taskId"),
                        name = obj.getString("name"),
                        isCompleted = obj.optBoolean("isCompleted", false)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun saveTimeLogs(logs: List<TimeLog>) {
        val array = JSONArray()
        logs.forEach { log ->
            val obj = JSONObject().apply {
                put("id", log.id)
                put("projectId", log.projectId)
                put("durationSeconds", log.durationSeconds)
                put("laborCost", log.laborCost)
                put("timestamp", log.timestamp)
                put("note", log.note)
            }
            array.put(obj)
        }
        prefs.edit().putString("time_logs", array.toString()).apply()
    }

    fun loadTimeLogs(): List<TimeLog> {
        val str = prefs.getString("time_logs", null) ?: return emptyList()
        val list = mutableListOf<TimeLog>()
        try {
            val array = JSONArray(str)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    TimeLog(
                        id = obj.getString("id"),
                        projectId = obj.getString("projectId"),
                        durationSeconds = obj.getLong("durationSeconds"),
                        laborCost = obj.getDouble("laborCost"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        note = obj.optString("note", "")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}
