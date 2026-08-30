package com.example.ui.screens.ai

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ui.navigation.ScreenRoutes
import com.example.domain.agent.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

enum class AiMessageSender {
    USER,
    COPILOT
}

data class AiChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: AiMessageSender,
    val text: String,
    val timestamp: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
    val suggestedToolRoute: String? = null,
    val suggestedToolTitle: String? = null,
    val isStreaming: Boolean = false,
    val executedTool: ToolModule? = null,
    val toolInitialState: ToolState? = null,
    val rawArguments: Map<String, Any?> = emptyMap(),
    val attachedImageBitmap: android.graphics.Bitmap? = null,
    val imagePath: String? = null
)

data class ChatSession(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Project Consultation",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val messages: List<AiChatMessage> = emptyList()
)

data class RemoteAiModel(
    val id: String,
    val displayName: String,
    val description: String = "",
    val isCloud: Boolean = true,
    val isCustom: Boolean = false
)

suspend fun fetchModelsFromServer(
    aiProvider: String,
    apiKey: String
): Result<List<RemoteAiModel>> = withContext(Dispatchers.IO) {
    val key = apiKey.trim().ifEmpty {
        val buildKey = BuildConfig.GEMINI_API_KEY.trim()
        if (buildKey.isNotEmpty() && buildKey != "MY_GEMINI_API_KEY") buildKey else ""
    }
    if (key.isBlank()) {
        return@withContext Result.success(emptyList())
    }

    val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    try {
        if (aiProvider.equals("DeepSeek", ignoreCase = true)) {
            val request = Request.Builder()
                .url("https://api.deepseek.com/models")
                .addHeader("Authorization", "Bearer $key")
                .get()
                .build()
            val response = client.newCall(request).execute()
            val respBody = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                val err = try {
                    JSONObject(respBody).optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}: ${response.message}"
                } catch (e: Exception) {
                    "HTTP ${response.code}: ${response.message}"
                }
                return@withContext Result.failure(Exception(err))
            }
            val json = JSONObject(respBody)
            val data = json.optJSONArray("data") ?: JSONArray()
            val list = mutableListOf<RemoteAiModel>()
            for (i in 0 until data.length()) {
                val item = data.getJSONObject(i)
                val id = item.optString("id")
                if (id.isNotEmpty()) {
                    list.add(
                        RemoteAiModel(
                            id = id,
                            displayName = id,
                            description = "Server Model (${item.optString("owned_by", "deepseek")})",
                            isCloud = true
                        )
                    )
                }
            }
            Result.success(list)
        } else {
            // Google Gemini API - Live Models Endpoint
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models?key=$key")
                .get()
                .build()
            val response = client.newCall(request).execute()
            val respBody = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                val err = try {
                    JSONObject(respBody).optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}: ${response.message}"
                } catch (e: Exception) {
                    "HTTP ${response.code}: ${response.message}"
                }
                return@withContext Result.failure(Exception(err))
            }
            val json = JSONObject(respBody)
            val models = json.optJSONArray("models") ?: JSONArray()
            val list = mutableListOf<RemoteAiModel>()
            for (i in 0 until models.length()) {
                val item = models.getJSONObject(i)
                val rawName = item.optString("name")
                val id = rawName.removePrefix("models/")
                val displayName = item.optString("displayName").ifEmpty { id }
                val description = item.optString("description")
                val methods = item.optJSONArray("supportedGenerationMethods")
                var canGenerateContent = false
                if (methods != null) {
                    for (m in 0 until methods.length()) {
                        if (methods.getString(m) == "generateContent") {
                            canGenerateContent = true
                            break
                        }
                    }
                } else {
                    canGenerateContent = true
                }

                if (canGenerateContent && id.isNotEmpty()) {
                    list.add(
                        RemoteAiModel(
                            id = id,
                            displayName = displayName,
                            description = description,
                            isCloud = true
                        )
                    )
                }
            }
            Result.success(list)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

fun saveMessageImageToInternalStorage(context: Context, id: String, bitmap: android.graphics.Bitmap): String? {
    return try {
        val file = java.io.File(context.filesDir, "chat_img_${id}.jpg")
        java.io.FileOutputStream(file).use { out ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, out)
        }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun saveChatSessions(context: Context, sessions: List<ChatSession>, activeSessionId: String) {
    val prefs = context.getSharedPreferences("brillian_chat_pref", Context.MODE_PRIVATE)
    val sessionsArray = JSONArray()
    for (session in sessions) {
        val sessionObj = JSONObject().apply {
            put("id", session.id)
            put("title", session.title)
            put("createdAt", session.createdAt)
            put("updatedAt", session.updatedAt)

            val msgArray = JSONArray()
            for (msg in session.messages) {
                val obj = JSONObject().apply {
                    put("id", msg.id)
                    put("sender", msg.sender.name)
                    put("text", msg.text)
                    put("timestamp", msg.timestamp)
                    put("suggestedToolRoute", msg.suggestedToolRoute ?: "")
                    put("suggestedToolTitle", msg.suggestedToolTitle ?: "")
                    put("executedToolId", msg.executedTool?.metadata?.id ?: "")
                    put("imagePath", msg.imagePath ?: "")

                    val argsObj = JSONObject()
                    msg.rawArguments.forEach { (k, v) ->
                        argsObj.put(k, v)
                    }
                    put("rawArguments", argsObj)

                    if (msg.toolInitialState is DynamicToolState) {
                        val stateObj = JSONObject()
                        msg.toolInitialState.parameterValues.forEach { (k, v) ->
                            stateObj.put(k, v)
                        }
                        put("parameterValues", stateObj)
                    }
                }
                msgArray.put(obj)
            }
            put("messages", msgArray)
        }
        sessionsArray.put(sessionObj)
    }
    prefs.edit()
        .putString("chat_sessions_v3", sessionsArray.toString())
        .putString("active_session_id_v3", activeSessionId)
        .apply()
}

fun loadChatSessions(context: Context, toolRegistry: ToolRegistry, defaultWelcomeText: String): Pair<List<ChatSession>, String> {
    val prefs = context.getSharedPreferences("brillian_chat_pref", Context.MODE_PRIVATE)
    val sessionsJson = prefs.getString("chat_sessions_v3", null)
    val activeId = prefs.getString("active_session_id_v3", null)

    if (!sessionsJson.isNullOrBlank()) {
        val sessions = mutableListOf<ChatSession>()
        try {
            val sessionsArray = JSONArray(sessionsJson)
            for (s in 0 until sessionsArray.length()) {
                val sObj = sessionsArray.getJSONObject(s)
                val sId = sObj.optString("id", UUID.randomUUID().toString())
                val sTitle = sObj.optString("title", "Project Consultation")
                val sCreated = sObj.optLong("createdAt", System.currentTimeMillis())
                val sUpdated = sObj.optLong("updatedAt", System.currentTimeMillis())
                val msgsList = mutableListOf<AiChatMessage>()

                val msgArray = sObj.optJSONArray("messages")
                if (msgArray != null) {
                    for (i in 0 until msgArray.length()) {
                        val obj = msgArray.getJSONObject(i)
                        val id = obj.optString("id", UUID.randomUUID().toString())
                        val senderStr = obj.optString("sender", "COPILOT")
                        val sender = if (senderStr == "USER") AiMessageSender.USER else AiMessageSender.COPILOT
                        val text = obj.optString("text", "")
                        val timestamp = obj.optString("timestamp", "")
                        val sRoute = obj.optString("suggestedToolRoute").takeIf { it.isNotEmpty() }
                        val sTitleMsg = obj.optString("suggestedToolTitle").takeIf { it.isNotEmpty() }
                        val toolId = obj.optString("executedToolId").takeIf { it.isNotEmpty() }
                        val imgPath = obj.optString("imagePath").takeIf { it.isNotEmpty() }

                        var loadedBitmap: android.graphics.Bitmap? = null
                        if (imgPath != null) {
                            try {
                                val f = java.io.File(imgPath)
                                if (f.exists()) {
                                    loadedBitmap = android.graphics.BitmapFactory.decodeFile(f.absolutePath)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        val rawArgs = mutableMapOf<String, Any?>()
                        val argsObj = obj.optJSONObject("rawArguments")
                        if (argsObj != null) {
                            val keys = argsObj.keys()
                            while (keys.hasNext()) {
                                val k = keys.next()
                                rawArgs[k] = argsObj.get(k)
                            }
                        }

                        var toolState: ToolState? = null
                        var toolModule: ToolModule? = null

                        if (toolId != null) {
                            toolModule = toolRegistry.getToolById(toolId)
                            if (toolModule is DynamicToolModule) {
                                val stateObj = obj.optJSONObject("parameterValues")
                                val paramsMap = mutableMapOf<String, Double>()
                                if (stateObj != null) {
                                    val keys = stateObj.keys()
                                    while (keys.hasNext()) {
                                        val k = keys.next()
                                        paramsMap[k] = stateObj.optDouble(k, 0.0)
                                    }
                                } else {
                                    toolModule.definition.parameters.forEach { param ->
                                        paramsMap[param.key] = param.defaultValue
                                    }
                                }
                                toolState = DynamicToolState(
                                    toolId = toolId,
                                    title = toolModule.definition.title,
                                    parameterValues = paramsMap
                                )
                            }
                        }

                        msgsList.add(
                            AiChatMessage(
                                id = id,
                                sender = sender,
                                text = text,
                                timestamp = timestamp,
                                suggestedToolRoute = sRoute,
                                suggestedToolTitle = sTitleMsg,
                                isStreaming = false,
                                executedTool = toolModule,
                                toolInitialState = toolState,
                                rawArguments = rawArgs,
                                attachedImageBitmap = loadedBitmap,
                                imagePath = imgPath
                            )
                        )
                    }
                }

                sessions.add(
                    ChatSession(
                        id = sId,
                        title = sTitle,
                        createdAt = sCreated,
                        updatedAt = sUpdated,
                        messages = msgsList
                    )
                )
            }

            if (sessions.isNotEmpty()) {
                val effectiveActiveId = if (activeId != null && sessions.any { it.id == activeId }) activeId else sessions.first().id
                return Pair(sessions, effectiveActiveId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Check legacy single-list chat history
    val legacy = loadChatHistory(context, toolRegistry)
    if (legacy.isNotEmpty()) {
        val initialSession = ChatSession(
            id = UUID.randomUUID().toString(),
            title = "Project Consultation",
            messages = legacy
        )
        saveChatSessions(context, listOf(initialSession), initialSession.id)
        return Pair(listOf(initialSession), initialSession.id)
    }

    // Default first session
    val defaultSession = ChatSession(
        id = UUID.randomUUID().toString(),
        title = "New Trade Session",
        messages = listOf(
            AiChatMessage(
                sender = AiMessageSender.COPILOT,
                text = defaultWelcomeText,
                suggestedToolRoute = "widget_calculator",
                suggestedToolTitle = "Standard Calculator"
            )
        )
    )
    saveChatSessions(context, listOf(defaultSession), defaultSession.id)
    return Pair(listOf(defaultSession), defaultSession.id)
}

fun saveChatHistory(context: Context, messages: List<AiChatMessage>) {
    val prefs = context.getSharedPreferences("brillian_chat_pref", Context.MODE_PRIVATE)
    val array = JSONArray()
    for (msg in messages) {
        val obj = JSONObject().apply {
            put("id", msg.id)
            put("sender", msg.sender.name)
            put("text", msg.text)
            put("timestamp", msg.timestamp)
            put("suggestedToolRoute", msg.suggestedToolRoute ?: "")
            put("suggestedToolTitle", msg.suggestedToolTitle ?: "")
            put("executedToolId", msg.executedTool?.metadata?.id ?: "")
            put("imagePath", msg.imagePath ?: "")
            
            val argsObj = JSONObject()
            msg.rawArguments.forEach { (k, v) ->
                argsObj.put(k, v)
            }
            put("rawArguments", argsObj)

            if (msg.toolInitialState is DynamicToolState) {
                val stateObj = JSONObject()
                msg.toolInitialState.parameterValues.forEach { (k, v) ->
                    stateObj.put(k, v)
                }
                put("parameterValues", stateObj)
            }
        }
        array.put(obj)
    }
    prefs.edit().putString("chat_history_v2", array.toString()).apply()
}

fun loadChatHistory(context: Context, toolRegistry: ToolRegistry): List<AiChatMessage> {
    val prefs = context.getSharedPreferences("brillian_chat_pref", Context.MODE_PRIVATE)
    val str = prefs.getString("chat_history_v2", null) ?: return emptyList()
    val list = mutableListOf<AiChatMessage>()
    try {
        val array = JSONArray(str)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val id = obj.optString("id", UUID.randomUUID().toString())
            val senderStr = obj.optString("sender", "COPILOT")
            val sender = if (senderStr == "USER") AiMessageSender.USER else AiMessageSender.COPILOT
            val text = obj.optString("text", "")
            val timestamp = obj.optString("timestamp", "")
            val sRoute = obj.optString("suggestedToolRoute").takeIf { it.isNotEmpty() }
            val sTitle = obj.optString("suggestedToolTitle").takeIf { it.isNotEmpty() }
            val toolId = obj.optString("executedToolId").takeIf { it.isNotEmpty() }
            val imgPath = obj.optString("imagePath").takeIf { it.isNotEmpty() }

            var loadedBitmap: android.graphics.Bitmap? = null
            if (imgPath != null) {
                try {
                    val f = java.io.File(imgPath)
                    if (f.exists()) {
                        loadedBitmap = android.graphics.BitmapFactory.decodeFile(f.absolutePath)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val rawArgs = mutableMapOf<String, Any?>()
            val argsObj = obj.optJSONObject("rawArguments")
            if (argsObj != null) {
                val keys = argsObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    rawArgs[k] = argsObj.get(k)
                }
            }

            var toolState: ToolState? = null
            var toolModule: ToolModule? = null

            if (toolId != null) {
                toolModule = toolRegistry.getToolById(toolId)
                if (toolModule is DynamicToolModule) {
                    val stateObj = obj.optJSONObject("parameterValues")
                    val paramsMap = mutableMapOf<String, Double>()
                    if (stateObj != null) {
                        val keys = stateObj.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            paramsMap[k] = stateObj.optDouble(k, 0.0)
                        }
                    } else {
                        toolModule.definition.parameters.forEach { param ->
                            paramsMap[param.key] = param.defaultValue
                        }
                    }
                    toolState = DynamicToolState(
                        toolId = toolId,
                        title = toolModule.definition.title,
                        parameterValues = paramsMap
                    )
                }
            }

            list.add(
                AiChatMessage(
                    id = id,
                    sender = sender,
                    text = text,
                    timestamp = timestamp,
                    suggestedToolRoute = sRoute,
                    suggestedToolTitle = sTitle,
                    isStreaming = false,
                    executedTool = toolModule,
                    toolInitialState = toolState,
                    rawArguments = rawArgs,
                    attachedImageBitmap = loadedBitmap,
                    imagePath = imgPath
                )
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return list
}

fun parseMarkdownToAnnotatedString(text: String): AnnotatedString {
    return buildAnnotatedString {
        val lines = text.split("\n")
        lines.forEachIndexed { lineIdx, line ->
            val trimmedLine = line.trimStart()
            when {
                // H1 Header
                trimmedLine.startsWith("# ") -> {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)) {
                        appendInlineMarkdown(this, trimmedLine.removePrefix("# ").trim())
                    }
                }
                // H2 Header
                trimmedLine.startsWith("## ") -> {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                        appendInlineMarkdown(this, trimmedLine.removePrefix("## ").trim())
                    }
                }
                // H3 Header (User explicitly requested rich formatting for ###)
                trimmedLine.startsWith("### ") -> {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp)) {
                        appendInlineMarkdown(this, trimmedLine.removePrefix("### ").trim())
                    }
                }
                // H4 Header
                trimmedLine.startsWith("#### ") -> {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp)) {
                        appendInlineMarkdown(this, trimmedLine.removePrefix("#### ").trim())
                    }
                }
                // Bullet list item
                trimmedLine.startsWith("- ") || trimmedLine.startsWith("* ") -> {
                    val content = if (trimmedLine.startsWith("- ")) trimmedLine.removePrefix("- ") else trimmedLine.removePrefix("* ")
                    append("  • ")
                    appendInlineMarkdown(this, content)
                }
                // Numbered list item like 1. 2.
                trimmedLine.matches(Regex("""^\d+\.\s+.*""")) -> {
                    val dotIdx = trimmedLine.indexOf('.')
                    val num = trimmedLine.substring(0, dotIdx + 1)
                    val rest = trimmedLine.substring(dotIdx + 1).trimStart()
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(num + " ")
                    }
                    appendInlineMarkdown(this, rest)
                }
                // Blockquote
                trimmedLine.startsWith("> ") -> {
                    withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                        append("▎ ")
                        appendInlineMarkdown(this, trimmedLine.removePrefix("> ").trim())
                    }
                }
                // Standard text line
                else -> {
                    appendInlineMarkdown(this, line)
                }
            }
            if (lineIdx < lines.size - 1) {
                append("\n")
            }
        }
    }
}

private fun appendInlineMarkdown(builder: AnnotatedString.Builder, text: String) {
    var i = 0
    val n = text.length
    while (i < n) {
        // Inline code `...`
        if (text[i] == '`' && !text.startsWith("```", i)) {
            val endIdx = text.indexOf('`', i + 1)
            if (endIdx != -1) {
                builder.withStyle(style = SpanStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)) {
                    append(text.substring(i + 1, endIdx))
                }
                i = endIdx + 1
                continue
            }
        }

        // Bold **...**
        if (i + 1 < n && text[i] == '*' && text[i + 1] == '*') {
            val endIdx = text.indexOf("**", i + 2)
            if (endIdx != -1) {
                builder.withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(text.substring(i + 2, endIdx))
                }
                i = endIdx + 2
                continue
            }
        }

        // Italic *...*
        if (text[i] == '*') {
            val endIdx = text.indexOf('*', i + 1)
            if (endIdx != -1) {
                builder.withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(text.substring(i + 1, endIdx))
                }
                i = endIdx + 1
                continue
            }
        }

        // Italic _..._
        if (text[i] == '_') {
            val endIdx = text.indexOf('_', i + 1)
            if (endIdx != -1) {
                builder.withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(text.substring(i + 1, endIdx))
                }
                i = endIdx + 1
                continue
            }
        }

        builder.append(text[i])
        i++
    }
}

data class ChatColorPalette(
    val name: String,
    val baseName: String,
    val baseHex: String,
    val accentName: String,
    val accentHex: String,
    val score: Int,
    val brand: String,
    val category: String,
    val description: String
)

fun parseInlineColorPalettes(text: String): List<ChatColorPalette> {
    val list = mutableListOf<ChatColorPalette>()
    try {
        val pattern = java.util.regex.Pattern.compile("\\[COLOR:\\s*([^|]+)\\s*\\|\\s*([^|]+)\\s*\\|\\s*([^|]+)\\s*\\|\\s*([^|]+)\\s*\\|\\s*([^|]+)\\s*\\|\\s*([^|]+)\\s*\\|\\s*([^|]+)\\s*\\|\\s*([^|]+)\\s*\\|\\s*([^\\]]+)\\s*\\]")
        val matcher = pattern.matcher(text)
        while (matcher.find()) {
            val name = matcher.group(1).trim()
            val baseName = matcher.group(2).trim()
            val baseHex = matcher.group(3).trim()
            val accentName = matcher.group(4).trim()
            val accentHex = matcher.group(5).trim()
            val score = matcher.group(6).trim().toIntOrNull() ?: 95
            val brand = matcher.group(7).trim()
            val category = matcher.group(8).trim()
            val description = matcher.group(9).trim()
            list.add(
                ChatColorPalette(name, baseName, baseHex, accentName, accentHex, score, brand, category, description)
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return list
}

fun cleanMessageTextOfColorTags(text: String): String {
    return try {
        val pattern = java.util.regex.Pattern.compile("\\[COLOR:[^\\]]+\\]")
        pattern.matcher(text).replaceAll("").trim()
    } catch (e: Exception) {
        text
    }
}

data class ChatRecommendedToolsList(
    val tools: List<Pair<String, String>>
)

fun parseInlineRecommendedTools(text: String): ChatRecommendedToolsList? {
    try {
        val pattern = java.util.regex.Pattern.compile("\\[RECOMMEND_TOOLS:\\s*([^\\]]+)\\s*\\]")
        val matcher = pattern.matcher(text)
        if (matcher.find()) {
            val content = matcher.group(1).trim()
            val toolsList = mutableListOf<Pair<String, String>>()
            content.split(",").forEach { item ->
                if (item.trim().isNotEmpty()) {
                    val parts = item.split("|")
                    if (parts.size >= 2) {
                        val id = parts[0].trim()
                        val name = parts[1].trim()
                        toolsList.add(Pair(id, name))
                    }
                }
            }
            if (toolsList.isNotEmpty()) {
                return ChatRecommendedToolsList(toolsList)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

fun cleanMessageTextOfRecommendTags(text: String): String {
    return try {
        val pattern = java.util.regex.Pattern.compile("\\[RECOMMEND_TOOLS:[^\\]]+\\]")
        pattern.matcher(text).replaceAll("").trim()
    } catch (e: Exception) {
        text
    }
}

@Composable
fun ChatRecommendedToolsCard(
    recommended: ChatRecommendedToolsList,
    onNavigateToTool: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Recommended Copilot Tools",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "These specialized tools can help you complete your calculations:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                recommended.tools.forEach { (toolId, displayName) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .clickable { onNavigateToTool(toolId) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.Launch,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Open Tool",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

data class ChatWoodworkDraft(
    val projectName: String,
    val dimensions: String,
    val pieces: List<com.example.domain.math.DraftPiece>,
    val projectNotes: String = ""
)

fun parseInlineProjectNotes(text: String): String {
    try {
        val pattern = java.util.regex.Pattern.compile("\\[PROJECT_NOTES:\\s*([^\\]]+)\\s*\\]")
        val matcher = pattern.matcher(text)
        if (matcher.find()) {
            return matcher.group(1).trim()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return ""
}

fun cleanMessageTextOfNotesTags(text: String): String {
    return try {
        val pattern = java.util.regex.Pattern.compile("\\[PROJECT_NOTES:[^\\]]+\\]")
        pattern.matcher(text).replaceAll("").trim()
    } catch (e: Exception) {
        text
    }
}

fun parseInlineWoodworkDrafts(text: String): ChatWoodworkDraft? {
    try {
        val pattern = java.util.regex.Pattern.compile("\\[WOODWORK_DRAFT:\\s*([^|]+)\\s*\\|\\s*([^|]+)\\s*\\|\\s*([^\\]]+)\\s*\\]")
        val matcher = pattern.matcher(text)
        if (matcher.find()) {
            val projectName = matcher.group(1).trim()
            val dimensions = matcher.group(2).trim()
            val piecesStr = matcher.group(3).trim()
            
            val pieces = mutableListOf<com.example.domain.math.DraftPiece>()
            piecesStr.split(";").forEach { item ->
                if (item.trim().isNotEmpty()) {
                    val parts = item.split(",")
                    if (parts.size >= 5) {
                        val label = parts[0].trim()
                        val len = parts[1].trim().toDoubleOrNull() ?: 0.0
                        val wid = parts[2].trim().toDoubleOrNull() ?: 0.0
                        val thick = parts[3].trim().toDoubleOrNull() ?: 20.0
                        val qty = parts[4].trim().toIntOrNull() ?: 1
                        pieces.add(com.example.domain.math.DraftPiece(label, len, wid, thick, qty))
                    }
                }
            }
            if (pieces.isNotEmpty()) {
                val notes = parseInlineProjectNotes(text)
                com.example.domain.math.CutlistDraftStore.setDraft(projectName, dimensions, pieces, notes)
                return ChatWoodworkDraft(projectName, dimensions, pieces, notes)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

fun cleanMessageTextOfDraftTags(text: String): String {
    return try {
        val pattern = java.util.regex.Pattern.compile("\\[WOODWORK_DRAFT:[^\\]]+\\]")
        pattern.matcher(text).replaceAll("").trim()
    } catch (e: Exception) {
        text
    }
}

@Composable
fun ChatWoodworkDraftCard(
    draft: ChatWoodworkDraft,
    onNavigateToTool: (String) -> Unit,
    onReviseDraft: ((ChatWoodworkDraft) -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = draft.projectName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Target Dimensions: ${draft.dimensions}",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = "Calculated Cuts & Thicknesses:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                draft.pieces.forEach { piece ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${piece.quantity}x ${piece.label}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            modifier = Modifier.weight(1f)
                        )
                        val dimsText = if (piece.widthMm > 0) {
                            "${piece.lengthMm.toInt()}×${piece.widthMm.toInt()}×${piece.thicknessMm.toInt()} mm"
                        } else {
                            "${piece.lengthMm.toInt()} mm (${piece.thicknessMm.toInt()}mm thick)"
                        }
                        Text(
                            text = dimsText,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            if (draft.projectNotes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Non-Cut Materials & Accessories:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    draft.projectNotes.split(";").forEach { accessory ->
                        if (accessory.trim().isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingBag,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = accessory.trim(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        com.example.domain.math.CutlistDraftStore.setDraft(draft.projectName, draft.dimensions, draft.pieces, draft.projectNotes)
                        onNavigateToTool("widget_cutlist_optimizer")
                    },
                    modifier = Modifier.weight(1f).height(38.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Launch,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("3D Cutlist", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                }

                val context = LocalContext.current
                OutlinedButton(
                    onClick = {
                        val db = com.example.data.database.AppDatabase.getInstance(context)
                        val noteRepo = com.example.data.repository.NoteRepository(db.quickNoteDao(), db.syncQueueDao())

                        val markdown = buildString {
                            append("# 🪚 AI Cutlist Draft: ${draft.projectName}\n\n")
                            append("> Dimensions: ${draft.dimensions}\n\n")
                            append("## 📋 Cut Pieces Breakdown\n")
                            append("| Piece Label | Length (mm) | Width (mm) | Thick (mm) | Qty |\n")
                            append("|---|---|---|---|---|\n")
                            draft.pieces.forEach { p ->
                                append("| ${p.label} | ${p.lengthMm} | ${p.widthMm} | ${p.thicknessMm} | ${p.quantity} |\n")
                            }
                            append("\n")
                            if (draft.projectNotes.isNotBlank()) {
                                append("## 📝 Hardware & BOM Notes\n")
                                append("- ${draft.projectNotes.replace(";", "\n- ")}\n")
                            }
                        }

                        kotlinx.coroutines.GlobalScope.launch {
                            noteRepo.addNote(
                                title = "AI Cutlist: ${draft.projectName}",
                                content = markdown,
                                tag = "Cutlist",
                                colorHex = "#2E7D32",
                                imagePaths = "",
                                pdfPaths = "",
                                isMarkdown = true
                            )
                        }
                        Toast.makeText(context, "Saved to Field Notes as Markdown!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.StickyNote2,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF2E7D32)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save to Notes", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                }

                if (onReviseDraft != null) {
                    OutlinedButton(
                        onClick = { onReviseDraft(draft) },
                        modifier = Modifier.weight(1f).height(38.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Revise", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BrillianAiScreen(
    currentRoute: String = "dashboard",
    isOnline: Boolean = true,
    aiProvider: String = "Gemini",
    apiKey: String = "",
    selectedModel: String = "gemini-2.0-flash",
    onModelSelected: (String) -> Unit = {},
    onNavigateToTool: (String) -> Unit = {},
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()

    val toolRegistry = remember { ToolRegistry() }
    val agentService = remember(toolRegistry) { GeminiAgentService(toolRegistry) }

    LaunchedEffect(Unit) {
        toolRegistry.indexAllTools()
    }

    var inputText by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }
    var isIndonesian by remember { mutableStateOf(false) }

    val effectiveApiKey = remember(apiKey) {
        apiKey.trim().ifEmpty {
            val buildKey = BuildConfig.GEMINI_API_KEY.trim()
            if (buildKey.isNotEmpty() && buildKey != "MY_GEMINI_API_KEY") buildKey else ""
        }
    }
    val hasKey = effectiveApiKey.isNotEmpty()

    var serverModels by remember { mutableStateOf<List<RemoteAiModel>>(emptyList()) }
    var isFetchingModels by remember { mutableStateOf(false) }
    var fetchErrorMessage by remember { mutableStateOf<String?>(null) }
    var modelMenuExpanded by remember { mutableStateOf(false) }
    var showCustomModelDialog by remember { mutableStateOf(false) }
    var customModelInput by remember { mutableStateOf("") }

    var activeModelId by remember(selectedModel) {
        mutableStateOf(selectedModel.ifEmpty { if (hasKey) "gemini-2.0-flash" else "built_in_engine" })
    }

    fun loadModelsFromServer() {
        if (!hasKey || effectiveApiKey.isBlank()) {
            isFetchingModels = false
            serverModels = emptyList()
            fetchErrorMessage = null
            return
        }
        isFetchingModels = true
        fetchErrorMessage = null
        coroutineScope.launch {
            val result = fetchModelsFromServer(aiProvider, effectiveApiKey)
            isFetchingModels = false
            result.onSuccess { models ->
                serverModels = models
                fetchErrorMessage = null
                // If active model is not in server list and not built_in_engine, set to first server model if empty
                if (models.isNotEmpty() && (activeModelId.isBlank() || activeModelId == "built_in_engine")) {
                    activeModelId = models.first().id
                    onModelSelected(activeModelId)
                }
            }.onFailure { error ->
                fetchErrorMessage = error.message ?: "Failed to fetch model list from server."
            }
        }
    }

    LaunchedEffect(aiProvider, effectiveApiKey) {
        if (hasKey) {
            loadModelsFromServer()
        } else {
            isFetchingModels = false
            serverModels = emptyList()
            fetchErrorMessage = null
            activeModelId = "built_in_engine"
            onModelSelected("built_in_engine")
        }
    }

    val activeModelDisplayName = remember(activeModelId, serverModels) {
        if (activeModelId == "built_in_engine") {
            "Built-in Trade Engine (Offline)"
        } else {
            val found = serverModels.find { it.id == activeModelId }
            found?.displayName ?: activeModelId
        }
    }

    val initialWelcomeText = remember(isIndonesian) {
        if (isIndonesian) {
            "Halo! Saya Brillian Copilot, asisten cerdas teknisi & pertukangan Anda. Tanyakan perhitungan tangga, beton, drop tegangan, hukum ohm, optimasi potong kayu, cat, atau rumus konstruksi lainnya."
        } else {
            "Hello! I am Brillian Copilot, your intelligent trade and engineering assistant. Ask me anything about stair layouts, concrete volume, voltage drop, Ohm's law, cutlist optimization, paint coverage, or structural formulas."
        }
    }

    val context = LocalContext.current

    var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var selectedImageBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var replyingToDraft by remember { mutableStateOf<ChatWoodworkDraft?>(null) }
    var replyingToMessage by remember { mutableStateOf<AiChatMessage?>(null) }

    val cameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview()
    ) { bitmap: android.graphics.Bitmap? ->
        if (bitmap != null) {
            val stream = java.io.ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, stream)
            selectedImageBytes = stream.toByteArray()
            selectedImageBitmap = bitmap
        }
    }

    val galleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bytes = stream.readBytes()
                    selectedImageBytes = bytes
                    selectedImageBitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val sessions = remember { mutableStateListOf<ChatSession>() }
    var activeSessionId by remember { mutableStateOf("") }
    var sessionToRename by remember { mutableStateOf<ChatSession?>(null) }
    var renameTitleInput by remember { mutableStateOf("") }
    var sessionToDelete by remember { mutableStateOf<ChatSession?>(null) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    val chatMessages = remember {
        mutableStateListOf<AiChatMessage>()
    }

    LaunchedEffect(isIndonesian) {
        val (loadedSessions, activeId) = loadChatSessions(context, toolRegistry, initialWelcomeText)
        sessions.clear()
        sessions.addAll(loadedSessions)
        activeSessionId = activeId
        val activeSession = sessions.find { it.id == activeSessionId } ?: sessions.firstOrNull()
        if (activeSession != null) {
            chatMessages.clear()
            chatMessages.addAll(activeSession.messages)
        }
    }

    fun syncAndSaveSession(messages: List<AiChatMessage>, customTitle: String? = null) {
        val idx = sessions.indexOfFirst { it.id == activeSessionId }
        if (idx != -1) {
            val cur = sessions[idx]
            val effectiveTitle = customTitle ?: cur.title
            val updated = cur.copy(
                title = effectiveTitle,
                updatedAt = System.currentTimeMillis(),
                messages = messages.toList()
            )
            sessions[idx] = updated
            saveChatSessions(context, sessions, activeSessionId)
        }
    }

    fun createNewSession() {
        val newSession = ChatSession(
            id = UUID.randomUUID().toString(),
            title = if (isIndonesian) "Sesi Proyek Baru" else "New Trade Session",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            messages = listOf(
                AiChatMessage(
                    sender = AiMessageSender.COPILOT,
                    text = initialWelcomeText,
                    suggestedToolRoute = "widget_calculator",
                    suggestedToolTitle = "Standard Calculator"
                )
            )
        )
        sessions.add(0, newSession)
        activeSessionId = newSession.id
        chatMessages.clear()
        chatMessages.addAll(newSession.messages)
        saveChatSessions(context, sessions, activeSessionId)
    }

    val quickPrompts = remember(isIndonesian) {
        if (isIndonesian) {
            listOf(
                "Hitung tinggi anak tangga & jumlah undakan untuk tinggi 280 cm",
                "Hitung volume beton untuk plat lantai 6m x 4m tebal 12cm",
                "Berapa batas drop tegangan 220V kabel tembaga 50 meter?",
                "Rumus perhitungan kemiringan atap genteng & panjang kaso",
                "Estimasi kebutuhan cat dinding ruangan 4x5 meter tinggi 3m",
                "Tabel konversi AWG ke mm² dan kapasitas arus kabel"
            )
        } else {
            listOf(
                "Calculate stair risers & treads for total rise 108 inches",
                "Concrete volume required for a 20ft x 12ft slab at 4in depth",
                "Voltage drop for 100ft copper wire 12 AWG at 15A 120V",
                "Compound miter saw angles for 52/38 crown molding",
                "Paint & primer estimator for a 12x15ft room with 9ft ceiling",
                "Rebar grid spacing and tonnage for foundation footing"
            )
        }
    }

    // Auto-scroll on new messages
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    // Auto-scroll when keyboard opens
    val isImeVisible = WindowInsets.isImeVisible
    LaunchedEffect(isImeVisible) {
        if (isImeVisible && chatMessages.isNotEmpty()) {
            delay(100)
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    fun handleSend(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty() && selectedImageBytes == null || isThinking) return

        val currentImageBytes = selectedImageBytes
        val currentImageBitmap = selectedImageBitmap

        val replyPrefix = when {
            replyingToDraft != null -> {
                val d = replyingToDraft!!
                val pieceSummary = d.pieces.joinToString("; ") { "${it.quantity}x ${it.label} (${it.lengthMm.toInt()}x${it.widthMm.toInt()}x${it.thicknessMm.toInt()}mm)" }
                "[REVISE DRAFT PLAN \"${d.projectName}\" (${d.dimensions}) - Current cuts: $pieceSummary | BOM: ${d.projectNotes}]\n"
            }
            replyingToMessage != null -> {
                "[REPLYING TO AI MESSAGE: \"${replyingToMessage?.text?.take(150)}\"]\n"
            }
            else -> ""
        }

        // Clear reply state immediately
        replyingToDraft = null
        replyingToMessage = null

        val promptForAgent = replyPrefix + (trimmed.ifEmpty { 
            if (isIndonesian) "Analisis gambar yang diunggah ini secara detail dan berikan rincian kebutuhan materialnya." 
            else "Analyze this uploaded image and detail its specifications/requirements." 
        })

        // Clear the state immediately for next input
        selectedImageBytes = null
        selectedImageBitmap = null

        val messageId = UUID.randomUUID().toString()
        val savedImgPath = if (currentImageBitmap != null) {
            saveMessageImageToInternalStorage(context, messageId, currentImageBitmap)
        } else null

        val userMsg = AiChatMessage(
            id = messageId,
            sender = AiMessageSender.USER,
            text = trimmed.ifEmpty { if (isIndonesian) "[Analisis Gambar]" else "[Image Analysis]" },
            attachedImageBitmap = currentImageBitmap,
            imagePath = savedImgPath
        )
        chatMessages.add(userMsg)

        // Check if session title should be auto-updated from default
        val curSession = sessions.find { it.id == activeSessionId }
        val newTitle = if (curSession != null && (curSession.title.startsWith("New Trade") || curSession.title.startsWith("Sesi Proyek") || curSession.title.startsWith("Project Consultation"))) {
            trimmed.ifEmpty { if (isIndonesian) "Analisis Gambar" else "Image Inspection" }.take(35)
        } else null

        syncAndSaveSession(chatMessages, newTitle)
        inputText = ""
        isThinking = true
        keyboardController?.hide()

        coroutineScope.launch {
            try {
                // Build conversational history from last 15 messages (excluding the new user message)
                val historyList = chatMessages.dropLast(1).takeLast(15).map { msg ->
                    com.example.domain.agent.AgentHistoryMessage(
                        role = if (msg.sender == AiMessageSender.USER) "user" else "model",
                        text = msg.text
                    )
                }

                val agentResponse = agentService.processUserMessage(
                    userMessage = promptForAgent,
                    apiKey = effectiveApiKey,
                    modelId = activeModelId,
                    isIndonesian = isIndonesian,
                    history = historyList,
                    imageBytes = currentImageBytes,
                    imageMimeType = if (currentImageBytes != null) "image/jpeg" else null,
                    context = context
                )
                
                val chatMsg = when (agentResponse) {
                    is AgentResponse.TextMessage -> {
                        AiChatMessage(
                            sender = AiMessageSender.COPILOT,
                            text = agentResponse.text
                        )
                    }
                    is AgentResponse.ToolExecution -> {
                        val initialSt = agentResponse.tool.createInitialState(agentResponse.arguments)
                        AiChatMessage(
                            sender = AiMessageSender.COPILOT,
                            text = agentResponse.aiExplanation,
                            executedTool = agentResponse.tool,
                            toolInitialState = initialSt,
                            rawArguments = agentResponse.arguments
                        )
                    }
                }
                chatMessages.add(chatMsg)
            } catch (e: Exception) {
                chatMessages.add(AiChatMessage(
                    sender = AiMessageSender.COPILOT,
                    text = "⚠️ Error: ${e.localizedMessage}"
                ))
            } finally {
                isThinking = false
                syncAndSaveSession(chatMessages)
                delay(100)
                if (chatMessages.isNotEmpty()) {
                    listState.animateScrollToItem(chatMessages.size - 1)
                }
            }
        }
    }

    // Observe and consume pending AI consultation requests from tools
    val pendingAiSession by com.example.domain.agent.AiSessionBridge.pendingSession.collectAsState()
    LaunchedEffect(pendingAiSession) {
        val req = com.example.domain.agent.AiSessionBridge.consumePendingSession()
        if (req != null) {
            val newSession = ChatSession(
                id = UUID.randomUUID().toString(),
                title = req.title.ifEmpty { if (isIndonesian) "Sesi Proyek Baru" else "New Trade Session" },
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                messages = emptyList()
            )
            sessions.add(0, newSession)
            activeSessionId = newSession.id
            chatMessages.clear()
            saveChatSessions(context, sessions, activeSessionId)

            if (req.autoSend && req.initialPrompt.isNotBlank()) {
                handleSend(req.initialPrompt)
            } else if (req.initialPrompt.isNotBlank()) {
                inputText = req.initialPrompt
            }
        }
    }

    // Rename Session Dialog
    if (sessionToRename != null) {
        AlertDialog(
            onDismissRequest = { sessionToRename = null },
            title = {
                Text(
                    text = if (isIndonesian) "Ubah Judul Percakapan" else "Rename Chat Title",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isIndonesian) "Masukkan nama atau label proyek untuk riwayat chat ini:" else "Enter a project name or topic label for this chat history:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = renameTitleInput,
                        onValueChange = { renameTitleInput = it },
                        singleLine = true,
                        placeholder = { Text("e.g. Kitchen Cabinet 3m Project") },
                        modifier = Modifier.fillMaxWidth().testTag("input_rename_chat_title"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val s = sessionToRename
                        if (s != null && renameTitleInput.isNotBlank()) {
                            val idx = sessions.indexOfFirst { it.id == s.id }
                            if (idx != -1) {
                                sessions[idx] = sessions[idx].copy(title = renameTitleInput.trim(), updatedAt = System.currentTimeMillis())
                                saveChatSessions(context, sessions, activeSessionId)
                            }
                        }
                        sessionToRename = null
                    },
                    modifier = Modifier.testTag("button_confirm_rename_chat")
                ) {
                    Text(if (isIndonesian) "Simpan" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToRename = null }) {
                    Text(if (isIndonesian) "Batal" else "Cancel")
                }
            }
        )
    }

    // Delete Single Session Confirmation Dialog
    if (sessionToDelete != null) {
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            title = {
                Text(
                    text = if (isIndonesian) "Hapus Percakapan?" else "Delete Conversation?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = if (isIndonesian) 
                        "Apakah Anda yakin ingin menghapus \"${sessionToDelete?.title}\"? Tindakan ini tidak dapat dibatalkan."
                    else 
                        "Are you sure you want to delete \"${sessionToDelete?.title}\"? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = sessionToDelete
                        if (target != null) {
                            val removingActive = target.id == activeSessionId
                            sessions.removeAll { it.id == target.id }
                            if (sessions.isEmpty()) {
                                createNewSession()
                            } else if (removingActive) {
                                val nextSession = sessions.first()
                                activeSessionId = nextSession.id
                                chatMessages.clear()
                                chatMessages.addAll(nextSession.messages)
                                saveChatSessions(context, sessions, activeSessionId)
                            } else {
                                saveChatSessions(context, sessions, activeSessionId)
                            }
                        }
                        sessionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("button_confirm_delete_chat")
                ) {
                    Text(if (isIndonesian) "Hapus" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToDelete = null }) {
                    Text(if (isIndonesian) "Batal" else "Cancel")
                }
            }
        )
    }

    // Clear All Sessions Confirmation Dialog
    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = {
                Text(
                    text = if (isIndonesian) "Hapus Semua Riwayat?" else "Clear All Chat History?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = if (isIndonesian)
                        "Semua riwayat percakapan dan proyek AI Copilot akan dihapus permanen."
                    else
                        "All Copilot project consultations and chat histories will be permanently deleted.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        sessions.clear()
                        createNewSession()
                        showClearAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("button_confirm_clear_all_chats")
                ) {
                    Text(if (isIndonesian) "Hapus Semua" else "Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text(if (isIndonesian) "Batal" else "Cancel")
                }
            }
        )
    }

    // Custom Model Input Dialog
    if (showCustomModelDialog) {
        AlertDialog(
            onDismissRequest = { showCustomModelDialog = false },
            title = {
                Text(
                    text = "Custom Server Model ID",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Enter any new, experimental, or custom model name supported by your $aiProvider server endpoint (e.g. gemini-2.5-pro, gemini-2.0-flash-thinking, deepseek-r1):",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = customModelInput,
                        onValueChange = { customModelInput = it },
                        placeholder = { Text("e.g. gemini-2.0-flash") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_custom_model_id"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = customModelInput.trim()
                        if (trimmed.isNotEmpty()) {
                            activeModelId = trimmed
                            onModelSelected(trimmed)
                        }
                        showCustomModelDialog = false
                    },
                    modifier = Modifier.testTag("button_confirm_custom_model")
                ) {
                    Text("Apply Model")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomModelDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .width(320.dp)
                    .fillMaxHeight(),
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isIndonesian) "Riwayat & Proyek" else "Chats & Projects",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (isIndonesian) "${sessions.size} sesi tersimpan" else "${sessions.size} saved sessions",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { coroutineScope.launch { drawerState.close() } }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Drawer",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // New Chat Button
                    Button(
                        onClick = {
                            createNewSession()
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("button_drawer_new_chat"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isIndonesian) "Mulai Chat Baru" else "New Chat Session",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Sessions List
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sessions, key = { it.id }) { session ->
                            val isActive = session.id == activeSessionId
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isActive) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                },
                                border = if (isActive) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        activeSessionId = session.id
                                        chatMessages.clear()
                                        chatMessages.addAll(session.messages)
                                        saveChatSessions(context, sessions, activeSessionId)
                                        coroutineScope.launch { drawerState.close() }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isActive) Icons.Default.ChatBubble else Icons.Default.ChatBubbleOutline,
                                        contentDescription = null,
                                        tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = session.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                                            ),
                                            color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${session.messages.size} msgs • ${SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(session.updatedAt))}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Rename button
                                    IconButton(
                                        onClick = {
                                            renameTitleInput = session.title
                                            sessionToRename = session
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Rename",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }

                                    // Delete button
                                    IconButton(
                                        onClick = { sessionToDelete = session },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = { showClearAllDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isIndonesian) "Hapus Semua Riwayat" else "Clear All Chat History", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .testTag("ai_copilot_screen"),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .navigationBarsPadding()
            ) {
                // Fixed Top Header (Safe from notch / status bar, never scrolls off)
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Drawer Toggle
                            IconButton(
                                onClick = { coroutineScope.launch { drawerState.open() } },
                                modifier = Modifier.size(38.dp).testTag("button_open_chat_history")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Open Chat History",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = "AI Copilot",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Brillian Copilot",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                val currentSession = sessions.find { it.id == activeSessionId }
                                Text(
                                    text = currentSession?.title ?: if (isIndonesian) "Asisten Kalkulasi & Lapangan" else "Trade & Engineering Assistant",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // New Chat Button
                            IconButton(
                                onClick = { createNewSession() },
                                modifier = Modifier.size(36.dp).testTag("button_new_chat_header")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddComment,
                                    contentDescription = "New Chat",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Language switch
                            FilledTonalButton(
                                onClick = { isIndonesian = !isIndonesian },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(if (isIndonesian) "ID" else "EN", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Close
                            IconButton(
                                onClick = onClose,
                                modifier = Modifier.size(36.dp).testTag("ai_close_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Live Server Model Selector Dropdown Menu
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Surface(
                                onClick = { modelMenuExpanded = true },
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("button_select_ai_model")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (activeModelId == "built_in_engine") Icons.Default.Memory else Icons.Default.Cloud,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Model: $activeModelDisplayName",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = if (activeModelId == "built_in_engine") "Offline" else if (serverModels.any { it.id == activeModelId }) "Server Live" else "Custom",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Change Model",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        DropdownMenu(
                            expanded = modelMenuExpanded,
                            onDismissRequest = { modelMenuExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            // Header with Refresh Server Action
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (hasKey) "Server Models ($aiProvider Live)" else "Model Selection (Offline)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (hasKey) {
                                    IconButton(
                                        onClick = { loadModelsFromServer() },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Refresh Models from Server",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            HorizontalDivider()

                            // Loading state
                            if (isFetchingModels) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Querying $aiProvider server for available models...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Error state
                            if (fetchErrorMessage != null && !isFetchingModels) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = fetchErrorMessage ?: "",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            TextButton(
                                                onClick = { loadModelsFromServer() },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text("Retry", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            // Server Models List (Dynamic from server response)
                            if (serverModels.isNotEmpty()) {
                                serverModels.forEach { modelOption ->
                                    val isCurrent = modelOption.id == activeModelId
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = modelOption.displayName,
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                                        ),
                                                        color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                                    ) {
                                                        Text(
                                                            text = modelOption.id,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                                if (modelOption.description.isNotBlank()) {
                                                    Text(
                                                        text = modelOption.description,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        maxLines = 2
                                                    )
                                                }
                                            }
                                        },
                                        trailingIcon = if (isCurrent) {
                                            {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        } else null,
                                        onClick = {
                                            activeModelId = modelOption.id
                                            onModelSelected(modelOption.id)
                                            modelMenuExpanded = false
                                        }
                                    )
                                }
                            } else if (!hasKey) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Key,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "No API Key Connected",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Text(
                                            text = "Connect your $aiProvider API key in Settings to load live models dynamically from the server.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                            TextButton(
                                                onClick = {
                                                    modelMenuExpanded = false
                                                    onNavigateToTool(ScreenRoutes.Settings.route)
                                                },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text("Open Settings", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            } else if (!isFetchingModels && fetchErrorMessage == null) {
                                Text(
                                    text = "No models retrieved from server. Enter a custom model below.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            // Custom Model Blank Canvas Entry
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "+ Custom Model ID (Blank Canvas)...",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "Type any server model ID directly without limits",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    customModelInput = if (activeModelId != "built_in_engine") activeModelId else ""
                                    modelMenuExpanded = false
                                    showCustomModelDialog = true
                                }
                            )

                            // Built-in Offline Engine Fallback
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Memory,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Built-in Trade Engine",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (activeModelId == "built_in_engine") FontWeight.Bold else FontWeight.Normal
                                                )
                                            )
                                            Text(
                                                text = "Deterministic formulas (100% Offline)",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                trailingIcon = if (activeModelId == "built_in_engine") {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                } else null,
                                onClick = {
                                    activeModelId = "built_in_engine"
                                    onModelSelected("built_in_engine")
                                    modelMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Message List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("ai_message_list"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                itemsIndexed(chatMessages, key = { _, msg -> msg.id }) { _, message ->
                    ChatMessageBubble(
                        message = message,
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(message.text))
                        },
                        onNavigateToTool = onNavigateToTool,
                        onStateChange = { newState ->
                            val idx = chatMessages.indexOfFirst { it.id == message.id }
                            if (idx != -1) {
                                chatMessages[idx] = chatMessages[idx].copy(toolInitialState = newState)
                                saveChatHistory(context, chatMessages)
                            }
                        },
                        onReviseDraft = { draft ->
                            replyingToDraft = draft
                            replyingToMessage = null
                        },
                        onReplyMessage = { msg ->
                            replyingToMessage = msg
                            replyingToDraft = null
                        },
                        isIndonesian = isIndonesian
                    )
                }

                if (isThinking) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isIndonesian) "Copilot sedang memproses via $activeModelDisplayName..." else "Copilot is calculating via $activeModelDisplayName...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Quick Prompt Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickPrompts.forEach { prompt ->
                    SuggestionChip(
                        onClick = { handleSend(prompt) },
                        label = {
                            Text(
                                text = prompt,
                                maxLines = 1,
                                fontSize = 12.sp
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            // Dropdown menu for "/" tool mentions
            val showSlashDropdown = inputText.contains("/")
            val lastSlashIdx = if (showSlashDropdown) inputText.lastIndexOf('/') else -1
            val slashQuery = if (lastSlashIdx != -1) {
                val afterSlash = inputText.substring(lastSlashIdx + 1)
                if (afterSlash.contains(" ")) "" else afterSlash
            } else {
                ""
            }

            val matchedTools = remember(inputText, slashQuery, showSlashDropdown) {
                if (showSlashDropdown && lastSlashIdx != -1) {
                    val allDefs = com.example.domain.model.ToolDefinition.ALL_TOOLS
                    if (slashQuery.isBlank()) {
                        allDefs.take(15)
                    } else {
                        allDefs.filter { tool ->
                            tool.title.contains(slashQuery, ignoreCase = true) ||
                            tool.category.contains(slashQuery, ignoreCase = true) ||
                            tool.keywords.any { it.contains(slashQuery, ignoreCase = true) }
                        }.take(10)
                    }
                } else {
                    emptyList()
                }
            }

            if (showSlashDropdown && matchedTools.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .heightIn(max = 220.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isIndonesian) "PILIH ALAT YANG INGIN ANDA SEBUT" else "MENTION / PICK A TOOL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                        }
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(matchedTools) { tool ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val prefix = if (lastSlashIdx >= 0) inputText.substring(0, lastSlashIdx) else ""
                                            inputText = "${prefix}Calculate using ${tool.title}: "
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = tool.category.take(1),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = tool.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = tool.description,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = tool.category,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Bottom Input Bar with clean spacing above keyboard edge
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Reply/Revise Banner preview if active
                    if (replyingToDraft != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isIndonesian) "Revisi Plan: ${replyingToDraft?.projectName}" else "Revising Plan: ${replyingToDraft?.projectName}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Dims: ${replyingToDraft?.dimensions} (${replyingToDraft?.pieces?.size ?: 0} cut parts)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                                IconButton(
                                    onClick = { replyingToDraft = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cancel Reply",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    } else if (replyingToMessage != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Reply,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = replyingToMessage?.text?.take(80) ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { replyingToMessage = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cancel Reply",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Image preview thumbnail if selected
                    selectedImageBitmap?.let { bmp ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Attached Image",
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(6.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isIndonesian) "Gambar terlampir" else "Image attached",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (isIndonesian) "Siap dianalisis oleh Copilot" else "Ready for Copilot analysis",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = {
                                    selectedImageBytes = null
                                    selectedImageBitmap = null
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove Image",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 12.dp, top = 8.dp, bottom = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Camera and Gallery buttons
                        IconButton(
                            onClick = { cameraLauncher.launch(null) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Take Photo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        
                        IconButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Photo,
                                contentDescription = "Pick Image",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ai_chat_input"),
                            placeholder = {
                                Text(
                                    text = if (isIndonesian) "Tanyakan rumus, dimensi, material..." else "Ask formulas, trade math, tools...",
                                    fontSize = 14.sp
                                )
                            },
                            maxLines = 4,
                            shape = RoundedCornerShape(24.dp),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Send
                            ),
                            keyboardActions = KeyboardActions(
                                onSend = { handleSend(inputText) }
                            ),
                            trailingIcon = {
                                if (inputText.isNotBlank()) {
                                    IconButton(onClick = { inputText = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear Text",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        FilledIconButton(
                            onClick = { handleSend(inputText) },
                            enabled = (inputText.isNotBlank() || selectedImageBytes != null) && !isThinking,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("ai_send_button"),
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send Message",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun ChatMessageBubble(
    message: AiChatMessage,
    onCopy: () -> Unit,
    onNavigateToTool: (String) -> Unit,
    onStateChange: (ToolState) -> Unit = {},
    onReviseDraft: ((ChatWoodworkDraft) -> Unit)? = null,
    onReplyMessage: ((AiChatMessage) -> Unit)? = null,
    isIndonesian: Boolean = false
) {
    val isUser = message.sender == AiMessageSender.USER

    if (isUser) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 4.dp
                ),
                color = MaterialTheme.colorScheme.primary,
                tonalElevation = 0.dp,
                shadowElevation = 1.dp,
                modifier = Modifier.widthIn(max = 320.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    // Render attached user image if present (persisted or live)
                    val userBmp = message.attachedImageBitmap
                    if (userBmp != null) {
                        Image(
                            bitmap = userBmp.asImageBitmap(),
                            contentDescription = "User Attachment",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 220.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .padding(bottom = 8.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    if (message.text.isNotEmpty()) {
                        SelectionContainer {
                            Text(
                                text = message.text,
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 21.sp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = message.timestamp,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    } else {
        // AI / Copilot response: Full width, NO avatar icon, generous and clean Gemini layout!
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    val baseCleanText = cleanMessageTextOfColorTags(message.text)
                    val draftCleanText = cleanMessageTextOfDraftTags(baseCleanText)
                    val notesCleanText = cleanMessageTextOfNotesTags(draftCleanText)
                    val cleanText = cleanMessageTextOfRecommendTags(notesCleanText)
                    val palettes = parseInlineColorPalettes(message.text)
                    val woodworkDraft = parseInlineWoodworkDrafts(message.text)
                    val recommendedTools = parseInlineRecommendedTools(message.text)

                    if (cleanText.isNotEmpty()) {
                        SelectionContainer {
                            Text(
                                text = parseMarkdownToAnnotatedString(cleanText),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    lineHeight = 22.sp,
                                    fontFamily = if (cleanText.contains("=")) FontFamily.Monospace else FontFamily.Default
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    if (palettes.isNotEmpty()) {
                        palettes.forEach { palette ->
                            ChatPaletteCard(palette = palette)
                        }
                    }

                    if (woodworkDraft != null) {
                        ChatWoodworkDraftCard(
                            draft = woodworkDraft,
                            onNavigateToTool = onNavigateToTool,
                            onReviseDraft = onReviseDraft
                        )
                    }

                    if (recommendedTools != null) {
                        ChatRecommendedToolsCard(recommended = recommendedTools, onNavigateToTool = onNavigateToTool)
                    }

                    // Suggested Tool Shortcut
                    if (message.suggestedToolRoute != null && message.suggestedToolTitle != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        FilledTonalButton(
                            onClick = {
                                val target = when (message.suggestedToolRoute) {
                                    "tool_calculator" -> "widget_calculator"
                                    "tool_stair_layout" -> "widget_stair_layout"
                                    "tool_concrete_volume" -> "widget_concrete_volume"
                                    "tool_voltage_drop" -> "widget_voltage_drop"
                                    "tool_compound_miter" -> "widget_compound_miter"
                                    "tool_paint_coverage" -> "widget_paint_coverage"
                                    else -> message.suggestedToolRoute
                                }
                                onNavigateToTool(target)
                            },
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f, fill = false),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Build,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Open ${message.suggestedToolTitle}",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            lineHeight = 16.sp
                                        ),
                                        fontWeight = FontWeight.Bold,
                                        softWrap = true,
                                        maxLines = 2
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Dynamic Interactive Tool Canvas
                    if (message.executedTool != null && message.toolInitialState != null) {
                        var toolState by remember(message.id) { mutableStateOf(message.toolInitialState) }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Render the tool's custom Composable view
                            val view = message.executedTool.getComposableView(toolState) { newState ->
                                toolState = newState
                                onStateChange(newState)
                            }
                            view()

                            Spacer(modifier = Modifier.height(10.dp))

                            // Deep-Link Navigation Launcher Button
                            Button(
                                onClick = { onNavigateToTool(message.executedTool.metadata.id) },
                                modifier = Modifier.fillMaxWidth().height(38.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF1F5F9),
                                    contentColor = Color(0xFF0F172A)
                                ),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Launch,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Open Full Screen ${message.executedTool.metadata.displayName}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF0F172A)
                                )
                            }
                        }
                    }

                    // Footer with Timestamp and Copy
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { onCopy() }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy response",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Copy",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        if (woodworkDraft != null && onReviseDraft != null) {
                                            onReviseDraft(woodworkDraft)
                                        } else if (onReplyMessage != null) {
                                            onReplyMessage(message)
                                        }
                                    }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = if (woodworkDraft != null) Icons.Default.Edit else Icons.Default.Reply,
                                    contentDescription = "Reply or revise plan",
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (woodworkDraft != null) (if (isIndonesian) "Revisi Plan" else "Revise Plan") else (if (isIndonesian) "Balas" else "Reply"),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                                )
                            }
                        }

                        Text(
                            text = message.timestamp,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

fun mapToolIdToRoute(toolId: String): String {
    return when (toolId) {
        "brick_wall_calculator", "widget_masonry_mortar" -> ScreenRoutes.MasonryMortar.route
        "concrete_volume_calculator", "widget_concrete_volume" -> ScreenRoutes.ConcreteVolume.route
        "ohms_law_calculator", "widget_ohms_law" -> ScreenRoutes.OhmsLaw.route
        "paint_coverage_calculator", "widget_paint_coverage" -> ScreenRoutes.PaintCoverage.route
        "stair_layout_calculator", "widget_stair_layout" -> ScreenRoutes.StairLayout.route
        
        // Woodworking
        "widget_kerf_bending" -> ScreenRoutes.KerfBending.route
        "widget_cutlist_optimizer" -> ScreenRoutes.CutlistOptimizer.route
        "widget_dado_step_over" -> ScreenRoutes.DadoStepOver.route
        "widget_board_footage" -> ScreenRoutes.BoardFootage.route
        "widget_rafter_calculator" -> ScreenRoutes.RafterCalculator.route
        "widget_compound_miter" -> ScreenRoutes.CompoundMiter.route
        "widget_wood_moisture" -> ScreenRoutes.WoodMoisture.route
        "widget_joinery_spacing" -> ScreenRoutes.JoinerySpacing.route
        "widget_segmented_turning" -> ScreenRoutes.SegmentedTurning.route
        "widget_sagulator" -> ScreenRoutes.Sagulator.route
        "widget_grain_matching" -> ScreenRoutes.GrainMatching.route
        "widget_drill_tap_chart" -> ScreenRoutes.DrillTapChart.route
        "widget_blade_speed" -> ScreenRoutes.BladeSpeed.route

        // Electrical
        "widget_voltage_drop" -> ScreenRoutes.VoltageDrop.route
        "widget_conduit_fill" -> ScreenRoutes.ConduitFill.route
        "widget_conduit_bender" -> ScreenRoutes.ConduitBender.route
        "widget_resistor_color_code" -> ScreenRoutes.ResistorColorCode.route
        "widget_breaker_panel" -> ScreenRoutes.BreakerPanel.route
        "widget_led_driver" -> ScreenRoutes.LedDriver.route
        "widget_box_fill_capacity" -> ScreenRoutes.BoxFillCapacity.route
        "widget_industrial_motor_fla" -> ScreenRoutes.IndustrialMotorFla.route
        "widget_solar_battery_sizer" -> ScreenRoutes.SolarBatterySizer.route
        "widget_current_loop_scaling" -> ScreenRoutes.CurrentLoopScaling.route
        "widget_power_factor_correction" -> ScreenRoutes.PowerFactorCorrection.route

        // Plumbing & HVAC
        "widget_pipe_sizing" -> ScreenRoutes.PipeSizing.route
        "widget_hvac_load" -> ScreenRoutes.HvacLoad.route
        "widget_tile_grout" -> ScreenRoutes.TileGrout.route
        "widget_drywall_stud" -> ScreenRoutes.DrywallStud.route

        // Civil & Masonry
        "widget_rebar_estimator" -> ScreenRoutes.RebarEstimator.route
        "widget_cut_fill_earthwork" -> ScreenRoutes.CutFillEarthwork.route
        "widget_slope_drainage" -> ScreenRoutes.SlopeDrainage.route
        "widget_soil_asphalt" -> ScreenRoutes.SoilAsphalt.route
        "widget_beam_deflection" -> ScreenRoutes.BeamDeflection.route
        "widget_earthwork_grade" -> ScreenRoutes.EarthworkGrade.route
        "widget_framing_roofing" -> ScreenRoutes.FramingRoofing.route
        "widget_meteorology" -> ScreenRoutes.Meteorology.route
        "widget_parabolic_focus" -> ScreenRoutes.ParabolicFocus.route
        "widget_stationing_cogo" -> ScreenRoutes.StationingCogo.route
        "widget_retaining_wall_sizer" -> ScreenRoutes.RetainingWallSizer.route
        "widget_aggregate_sieve" -> ScreenRoutes.AggregateSieve.route
        "widget_equipment_hauling" -> ScreenRoutes.EquipmentHauling.route
        "widget_stormwater_rational" -> ScreenRoutes.StormwaterRational.route

        // Hardware & Sensors
        "widget_digital_level" -> ScreenRoutes.DigitalLevel.route
        "widget_compass" -> ScreenRoutes.Compass.route
        "widget_decibel_meter" -> ScreenRoutes.DecibelMeter.route
        "widget_strobe_tachometer" -> ScreenRoutes.StrobeTachometer.route
        "widget_thermal_camera" -> ScreenRoutes.ThermalCamera.route
        "widget_usb_endoscope" -> ScreenRoutes.UsbEndoscope.route
        "widget_vibration_analyzer" -> ScreenRoutes.VibrationAnalyzer.route
        "widget_ble_multimeter" -> ScreenRoutes.BleMultimeter.route
        "widget_sun_path_tracker" -> ScreenRoutes.SunPathTracker.route
        "widget_barometric_altimeter" -> ScreenRoutes.BarometricAltimeter.route
        "widget_lux_meter" -> ScreenRoutes.LuxMeter.route
        "widget_fractional_calc" -> ScreenRoutes.FractionalCalculator.route
        "widget_ar_measurement" -> ScreenRoutes.ArMeasurement.route
        "widget_ar_area_calculator" -> ScreenRoutes.ArAreaCalculator.route
        "widget_plumb_bob" -> ScreenRoutes.PlumbBob.route
        "widget_stud_detector" -> ScreenRoutes.StudDetector.route
        "widget_laser_measure" -> ScreenRoutes.LaserMeasure.route
        "widget_jobsite_ir_remote" -> ScreenRoutes.JobsiteIrRemote.route

        "widget_psychrometric" -> ScreenRoutes.Psychrometric.route
        "widget_refrigerant" -> ScreenRoutes.Refrigerant.route
        "widget_duct_sizer" -> ScreenRoutes.DuctSizer.route
        "widget_expansion_tank" -> ScreenRoutes.ExpansionTank.route
        "widget_sling_angle" -> ScreenRoutes.SlingAngle.route

        else -> {
            val cleanId = toolId.removePrefix("widget_")
            "tool_$cleanId"
        }
    }
}

/**
 * Intelligent domain engine for offline and online trade calculations & recommendations.
 */
fun generateCopilotResponse(query: String, isIndonesian: Boolean): AiChatMessage {
    val q = query.lowercase(Locale.ROOT)

    // 1. Stair / Tangga calculation
    if (q.contains("stair") || q.contains("riser") || q.contains("tread") || q.contains("tangga") || q.contains("undakan")) {
        val totalRiseMatch = Regex("""(\d+(\.\d+)?)""").find(q)?.value?.toDoubleOrNull() ?: if (isIndonesian) 280.0 else 108.0
        val isMetric = isIndonesian || q.contains("cm") || q.contains("meter") || q.contains("mm")

        val targetRiser = if (isMetric) 17.5 else 7.5
        val numberOfRisers = kotlin.math.round(totalRiseMatch / targetRiser).toInt().coerceAtLeast(1)
        val exactRiserHeight = totalRiseMatch / numberOfRisers
        val numberOfTreads = (numberOfRisers - 1).coerceAtLeast(1)
        val treadDepth = if (isMetric) 28.0 else 10.5
        val totalRun = numberOfTreads * treadDepth

        val unit = if (isMetric) "cm" else "in"

        val responseText = if (isIndonesian) {
            """
            📐 **Hasil Perhitungan Tangga Pro:**
            • Total Ketinggian (Total Rise): ${String.format("%.1f", totalRiseMatch)} $unit
            • Jumlah Undakan / Riser: $numberOfRisers anak tangga
            • Tinggi Riser Presisi: ${String.format("%.2f", exactRiserHeight)} $unit (Standar ideal: 16-18 cm)
            • Jumlah Pijakan (Tread): $numberOfTreads pijakan
            • Kedalaman Pijakan (Tread Depth): ${String.format("%.1f", treadDepth)} $unit
            • Total Bentang Horizontal (Total Run): ${String.format("%.1f", totalRun)} $unit

            💡 **Aturan Keselamatan Konstruksi (Blondel 2R + T):**
            2 × (${String.format("%.2f", exactRiserHeight)}) + ${String.format("%.1f", treadDepth)} = ${String.format("%.1f", 2 * exactRiserHeight + treadDepth)} cm (Ideal: 60 - 64 cm).
            """.trimIndent()
        } else {
            """
            📐 **Stair Layout Calculation:**
            • Total Rise: ${String.format("%.1f", totalRiseMatch)} $unit
            • Number of Risers: $numberOfRisers
            • Exact Riser Height: ${String.format("%.3f", exactRiserHeight)} $unit (Target: 7" to 7-3/4")
            • Number of Treads: $numberOfTreads
            • Standard Tread Depth: ${String.format("%.1f", treadDepth)} $unit
            • Total Run: ${String.format("%.1f", totalRun)} $unit (${String.format("%.1f", totalRun / 12.0)} ft)

            💡 **IRC Safety Formula Check (2R + T):**
            2 × (${String.format("%.2f", exactRiserHeight)}) + ${String.format("%.1f", treadDepth)} = ${String.format("%.2f", 2 * exactRiserHeight + treadDepth)}" (Standard range: 24" - 25").
            """.trimIndent()
        }

        return AiChatMessage(
            sender = AiMessageSender.COPILOT,
            text = responseText,
            suggestedToolRoute = ScreenRoutes.StairLayout.route,
            suggestedToolTitle = "Stair Layout Calculator"
        )
    }

    // 2. Concrete / Beton calculation
    if (q.contains("concrete") || q.contains("slab") || q.contains("beton") || q.contains("cor") || q.contains("semen")) {
        val responseText = if (isIndonesian) {
            """
            🏗️ **Panduan Estimasi Pengecoran Beton:**
            • **Rumus Volume:** Panjang (m) × Lebar (m) × Tebal (m) = Total m³
            • **Faktor Kehilangan (Wastage):** Tambahkan 5% - 10% untuk pemadatan dan tumpahan.
            • **Komposisi Campuran Standar K-250 (per 1 m³):**
              - Semen: ~384 kg (sekitar 8 sak @ 50kg)
              - Pasir Beton: ~692 kg (0.49 m³)
              - Kerikil/Split: ~1039 kg (0.77 m³)
              - Air: ~215 Liter (W/C ~ 0.56)

            Contoh untuk plat 6m × 4m tebal 12cm = 2.88 m³ (+10% safety = 3.17 m³ / ~26 sak semen).
            """.trimIndent()
        } else {
            """
            🏗️ **Concrete Volume & Mix Estimator:**
            • **Formula:** Length (ft) × Width (ft) × (Thickness (in) / 12) ÷ 27 = Cubic Yards (yd³)
            • **Wastage Factor:** Add +5% to +10% margin for over-excavation and spillage.
            • **Standard 60 lb / 80 lb Pre-mix Bags:**
              - 1 yd³ requires 45 bags of 80lb mix (or 60 bags of 60lb mix).

            Example: 20ft × 12ft patio @ 4" thick = 2.96 yd³ (~3.25 yd³ with 10% buffer / 146 bags of 80lb).
            """.trimIndent()
        }

        return AiChatMessage(
            sender = AiMessageSender.COPILOT,
            text = responseText,
            suggestedToolRoute = ScreenRoutes.ConcreteVolume.route,
            suggestedToolTitle = "Concrete Volume Calculator"
        )
    }

    // 3. Electrical / Voltage drop / Ohm's Law
    if (q.contains("voltage") || q.contains("wire") || q.contains("amp") || q.contains("ohm") || q.contains("tegangan") || q.contains("kabel") || q.contains("listrik") || q.contains("awg")) {
        val responseText = if (isIndonesian) {
            """
            ⚡ **Pedoman Kelistrikan & Penurunan Tegangan (PUIL / NEC):**
            • **Hukum Ohm Dasar:** V = I × R | P = V × I | I = P / V
            • **Penurunan Tegangan Maksimal:**
              - Rangkaian cabang (Branch circuit): Maksimum 3%
              - Total pengumpan + cabang (Feeder + Branch): Maksimum 5%
            • **Rumus Drop Tegangan 1-Fasa (Tembaga):**
              VD = (2 × K × L × I) / CM
              (K = 12.9 ohm-cmil/ft untuk tembaga; L = jarak; I = arus ampere; CM = circular mil luas kabel).
            • **Ukuran Standar Kabel Tembaga NYM/NYY:**
              - 1.5 mm²: Pemutus 10A (Lampu & Penerangan)
              - 2.5 mm²: Pemutus 16A (Stop Kontak & Beban Standar)
              - 4.0 mm²: Pemutus 25A (AC Besar / Water Heater)
            """.trimIndent()
        } else {
            """
            ⚡ **Electrical & Voltage Drop Engineering Reference:**
            • **Ohm's Law Core Equations:** V = I × R | P = V × I | R = V² / P
            • **NEC Max Voltage Drop Rule (NEC 210.19(A)):**
              - Recommended max drop: 3% on branch circuits, 5% overall.
            • **Single-Phase Copper Voltage Drop Formula:**
              VD = (2 × K × L × I) / Circular_Mils
              (K ≈ 12.9 Ω·cmil/ft for Copper; L = One-way length in feet; I = Load current in Amperes).
            • **Standard Ampacity Table (75°C Copper THHN):**
              - 14 AWG: 15 Amperes
              - 12 AWG: 20 Amperes
              - 10 AWG: 30 Amperes
              - 8 AWG: 50 Amperes
            """.trimIndent()
        }

        return AiChatMessage(
            sender = AiMessageSender.COPILOT,
            text = responseText,
            suggestedToolRoute = ScreenRoutes.VoltageDrop.route,
            suggestedToolTitle = "Voltage Drop Calculator"
        )
    }

    // 4. Cutlist Optimizer & Furniture Drafting
    if (q.contains("cutlist") || q.contains("optimizer") || q.contains("table") || q.contains("meja") || q.contains("desk") || q.contains("cabinet") || q.contains("lemari") || q.contains("shelf") || q.contains("rak") || q.contains("furniture") || q.contains("furnitur") || q.contains("potong kayu")) {
        val responseText = if (isIndonesian) {
            """
            🪵 **Rancangan & Optimasi Pemotongan Kayu Pro (Multi-Stock 1D & 2D):**
            • **Sistem Inventori Multi-Stock:** Mendukung pemilahan bahan mentah dalam Tab terpisah (Plywood Lembaran 244×122cm, Kayu Papan Solid 200×20×2cm, dan Balok Rafter/Kaki 2×4).
            • **Alokasi Tebal Presisi:** Komponen otomatis dicocokkan dengan tab stok yang memiliki ketebalan sesuai.
            • **Perhitungan Kerf & Trim:** Pisau potong standar 3.2 mm diperhitungkan otomatis agar ukuran akhir presisi.

            Berikut adalah rancangan draft potongan siap impor untuk proyek furnitur:
            [WOODWORK_DRAFT: Meja Kopi Workshop | 100x60x45 cm | Table Top Planks,1000,200,20,3; Leg Posts (2x4 Timber),450,40,60,4; Apron Support Rails,800,80,20,2; Side Stretchers,440,80,20,2]
            [PROJECT_NOTES: 1 Kotak Sekrup Pocket Hole 30mm; 4x Kaki Karet Pelindung Lantai; 1 Kaleng Wood Stain Walnut; 1 Kaleng Clear Coat Polyurethane; Lem Kayu PVA Titebond]
            [RECOMMEND_TOOLS: widget_cutlist_optimizer|Cut List Optimizer, widget_board_footage|Board Footage Calculator, widget_kerf_bending|Kerf Bending Studio, widget_wood_moisture|Wood Moisture Meter]
            """.trimIndent()
        } else {
            """
            🪵 **Woodworking Cut List & Multi-Stock Layout Optimizer:**
            • **Tabbed Multi-Stock Inventory:** Organizes sheet goods (e.g. 4x8 Plywood 18mm) and dimensional lumber (e.g. 2x4 studs, 1x8 planks) in dedicated tabs with individual multipliers.
            • **Automatic Thickness Matching:** Cut pieces are strictly routed to stock tabs sharing identical thickness specs.
            • **Kerf & Trim Margins:** 1/8" blade allowance and perimeter clean-up margins calculated automatically for maximum yield.

            Here is a drafted project layout ready for single-click import:
            [WOODWORK_DRAFT: Workshop Coffee Table | 100x60x45 cm | Table Top Planks,1000,200,20,3; Leg Posts (2x4 Timber),450,40,60,4; Apron Support Rails,800,80,20,2; Side Stretchers,440,80,20,2]
            [PROJECT_NOTES: 1 Box 1-1/4" Pocket Screws; 4x Heavy-Duty Rubber Feet; 1 Can Walnut Wood Stain; 1 Can Clear Satin Polyurethane; PVA Wood Glue]
            [RECOMMEND_TOOLS: widget_cutlist_optimizer|Cut List Optimizer, widget_board_footage|Board Footage Calculator, widget_kerf_bending|Kerf Bending Studio, widget_wood_moisture|Wood Moisture Meter]
            """.trimIndent()
        }

        return AiChatMessage(
            sender = AiMessageSender.COPILOT,
            text = responseText,
            suggestedToolRoute = ScreenRoutes.CutlistOptimizer.route,
            suggestedToolTitle = "Cut List Optimizer (1D & 2D)"
        )
    }

    // 5. Crown Molding / Compound Miter
    if (q.contains("miter") || q.contains("crown") || q.contains("molding") || q.contains("sudut")) {
        val responseText = if (isIndonesian) {
            """
            🪵 **Panduan Pemotongan Sudut Miter & Crown Molding:**
            • **Sudut Profil Crown Molding 52/38 (Dinding 90° Siku):**
              - Sudut Miter Gergaji (Miter Angle): 31.62°
              - Sudut Kemiringan Pisau (Bevel Angle): 33.86°
            • **Sudut Profil 45/45 (Dinding 90° Siku):**
              - Sudut Miter: 35.26° | Bevel Angle: 30.00°
            """.trimIndent()
        } else {
            """
            🪵 **Compound Miter & Crown Molding Angle Reference:**
            • **Standard 52/38 Crown Molding for 90° Corner:**
              - Miter Angle Setting: 31.62°
              - Bevel Angle Setting: 33.86°
            • **Standard 45/45 Crown Molding for 90° Corner:**
              - Miter Angle Setting: 35.26°
              - Bevel Angle Setting: 30.00°
            """.trimIndent()
        }

        return AiChatMessage(
            sender = AiMessageSender.COPILOT,
            text = responseText,
            suggestedToolRoute = ScreenRoutes.CompoundMiter.route,
            suggestedToolTitle = "Compound Miter Calculator"
        )
    }

    // 5. Paint / Dinding / Cat
    if (q.contains("paint") || q.contains("cat") || q.contains("dinding") || q.contains("primer") || q.contains("kuas") || q.contains("wall")) {
        val responseText = if (isIndonesian) {
            """
            🎨 **Estimasi Kebutuhan Cat & Primer:**
            • **Rumus Luas Bersih:** (Keliling Ruangan × Tinggi Dinding) - (Luas Pintu & Jendela)
            • **Daya Sebar Rata-rata:**
              - Cat Tembok Interior: 10 - 12 m² per liter per lapis
              - Cat Eksterior Weathercoat: 8 - 10 m² per liter per lapis
              - Cat Primer / Sealer Alkali: 9 - 11 m² per liter
            • **Rekomendasi Pelapisan:** 1 lapis cat dasar (alkali sealer) + 2 lapis cat penutup utama.
            """.trimIndent()
        } else {
            """
            🎨 **Wall Area & Paint Coverage Estimator:**
            • **Net Surface Area:** (Perimeter × Height) - (Doors ~21 sq ft + Windows ~15 sq ft)
            • **Standard Paint Coverage:**
              - 1 Gallon covers approximately 350 - 400 sq ft (single coat).
              - Primer: ~300 - 350 sq ft per gallon on porous new drywall.
            • **Recommended Application:** 1 coat dedicated primer + 2 topcoats for uniform sheen & durability.
            """.trimIndent()
        }

        return AiChatMessage(
            sender = AiMessageSender.COPILOT,
            text = responseText,
            suggestedToolRoute = ScreenRoutes.PaintCoverage.route,
            suggestedToolTitle = "Paint Coverage Calculator"
        )
    }

    // Default General Response
    val defaultText = if (isIndonesian) {
        """
        🛠️ **Brillian Engineering Assistant:**
        Saya siap membantu Anda dengan berbagai perhitungan teknis di lapangan:
        • **Struktur & Sipil:** Volume beton, galian/urukan tanah, rebar, balok lentur.
        • **Kayu & Konstruksi:** Dimensi tangga, kaso atap, sudut miter, optimasi potong.
        • **Kelistrikan:** Drop tegangan, kapasitas kabel AWG/mm², hukum ohm, beban motor.
        • **Finishing:** Kebutuhan cat, luas dinding, keramik & nat, ketebalan drywall.

        Ketik pertanyaan spesifik atau nilai angka yang ingin dihitung!
        """.trimIndent()
    } else {
        """
        🛠️ **Brillian Engineering Assistant:**
        I am ready to assist with any trade formulas and on-site math:
        • **Civil & Structural:** Concrete volume, cut/fill earthwork, rebar grids, beam deflection.
        • **Carpentry & Framing:** Stair layout, roof rafters, compound miters, cutlist optimization.
        • **Electrical:** Voltage drop, wire ampacity, conduit fill, Ohm's law, motor FLA.
        • **Finishing & HVAC:** Paint coverage, tile grout, duct sizing, psychrometric dew point.

        Type a specific calculation query or trade dimension to get started!
        """.trimIndent()
    }

    return AiChatMessage(
        sender = AiMessageSender.COPILOT,
        text = defaultText,
        suggestedToolRoute = ScreenRoutes.ToolCatalog.route,
        suggestedToolTitle = "Tool Catalog"
    )
}

data class HardwareSpecs(
    val totalRamGb: Double,
    val memoryTier: String,
    val socVendor: String,
    val supportsVulkan: Boolean
)

data class ModelProfile(
    val id: String,
    val name: String,
    val size: String,
    val sizeBytes: Long,
    val targetTokensPerSec: String,
    val description: String,
    val minRamGb: Double
)

fun detectHardware(context: Context): HardwareSpecs {
    val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    val memoryInfo = ActivityManager.MemoryInfo()
    actManager?.getMemoryInfo(memoryInfo)
    val totalBytes = memoryInfo.totalMem
    val ramGb = totalBytes.toDouble() / (1024.0 * 1024.0 * 1024.0)

    val memoryTier = when {
        ramGb >= 11.5 -> "Flagship High-Memory (12GB+)"
        ramGb >= 7.5 -> "Performance Tier (8GB+)"
        ramGb >= 5.5 -> "Standard Tier (6GB)"
        ramGb >= 3.5 -> "Balanced Tier (4GB)"
        else -> "Entry Level (<4GB)"
    }

    val vendor = Build.HARDWARE.ifBlank { Build.MANUFACTURER }
    val supportsVulkan = context.packageManager.hasSystemFeature(PackageManager.FEATURE_VULKAN_HARDWARE_LEVEL, 1) ||
            context.packageManager.hasSystemFeature("android.hardware.vulkan.level")

    return HardwareSpecs(
        totalRamGb = ramGb,
        memoryTier = memoryTier,
        socVendor = "$vendor (${Build.BOARD})",
        supportsVulkan = supportsVulkan
    )
}

fun getModelsList(): List<ModelProfile> {
    return listOf(
        ModelProfile(
            id = "smollm2_360m",
            name = "SmolLM2 360M Instruct (GGUF Q4_K_M)",
            size = "230 MB",
            sizeBytes = 241172480L,
            targetTokensPerSec = "38-55 tok/s",
            description = "Ultra-lightweight embedded trade calculator. Instant zero-latency responses even on entry-level Android devices.",
            minRamGb = 3.0
        ),
        ModelProfile(
            id = "qwen25_15b",
            name = "Qwen 2.5 1.5B Trade Assistant (GGUF Q4_K_M)",
            size = "980 MB",
            sizeBytes = 1027604480L,
            targetTokensPerSec = "18-28 tok/s",
            description = "Balanced deep reasoning for complex structural engineering, NEC electrical code lookups, and multi-step formulas.",
            minRamGb = 6.0
        ),
        ModelProfile(
            id = "llama32_3b",
            name = "Llama 3.2 3B Engineering (GGUF Q4_K_M)",
            size = "1.85 GB",
            sizeBytes = 1986000000L,
            targetTokensPerSec = "10-18 tok/s",
            description = "High-precision expert model for full construction estimation and building code synthesis. Requires 8GB+ RAM.",
            minRamGb = 7.5
        )
    )
}

fun getRecommendedModel(specs: HardwareSpecs): String {
    return when {
        specs.totalRamGb >= 7.5 -> "llama32_3b"
        specs.totalRamGb >= 5.5 -> "qwen25_15b"
        else -> "smollm2_360m"
    }
}

@Composable
fun ChatPaletteCard(palette: ChatColorPalette) {
    fun parseColorHex(hex: String): Color {
        return try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (e: Exception) {
            Color.LightGray
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = palette.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${palette.brand} • ${palette.category}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Match Score
                Box(
                    modifier = Modifier
                        .background(
                            color = if (palette.score >= 95) Color(0xFFDCFCE7) else Color(0xFFFEF9C3),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${palette.score}% Match",
                        color = if (palette.score >= 95) Color(0xFF15803D) else Color(0xFF854D0E),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Swatches
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Base Color Swatch
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(45.dp)
                            .background(parseColorHex(palette.baseHex), shape = RoundedCornerShape(8.dp))
                            .border(1.dp, Color.Gray.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = palette.baseName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    Text(
                        text = palette.baseHex,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Accent Color Swatch
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(45.dp)
                            .background(parseColorHex(palette.accentHex), shape = RoundedCornerShape(8.dp))
                            .border(1.dp, Color.Gray.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = palette.accentName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    Text(
                        text = palette.accentHex,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Description
            Text(
                text = palette.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp
            )
        }
    }
}

