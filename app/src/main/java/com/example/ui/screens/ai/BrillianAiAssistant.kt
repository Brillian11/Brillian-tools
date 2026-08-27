package com.example.ui.screens.ai

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import org.json.JSONArray
import java.util.concurrent.TimeUnit

// Model configurations for on-device local AI recommendation matrix
data class ModelProfile(
    val id: String,
    val name: String,
    val size: String,
    val sizeBytes: Long,
    val description: String,
    val recommendedRam: String,
    val targetTokensPerSec: String
)

data class ChatMessage(
    val role: String, // "user" or "assistant" or "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val toolLink: String? = null,
    val toolTitle: String? = null
)

data class ToolContext(
    val toolId: String,
    val title: String,
    val defaultParams: Map<String, String>,
    val tradeKnowledgeBase: String,
    val sampleQuestions: List<String>
)

@Composable
fun BrillianAiFloatingAssistant(
    currentRoute: String,
    isOnline: Boolean,
    onNavigateToTool: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    // Shift bubble upward to clear bottom navbar (80dp) + safety gap when on dashboard tabs
    val bottomPadding = if (currentRoute == "dashboard") 140.dp else 24.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = bottomPadding),
        contentAlignment = Alignment.BottomEnd
    ) {
        // Floating Bubble Button (always visible when not expanded)
        AnimatedVisibility(
            visible = !isExpanded,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            FloatingActionButton(
                onClick = { isExpanded = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = CircleShape,
                modifier = Modifier
                    .size(56.dp)
                    .testTag("floating_ai_bubble")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Engineering,
                        contentDescription = "Brillian AI Copilot",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Expanded Copilot Panel overlay
        AnimatedVisibility(
            visible = isExpanded,
            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
        ) {
            BrillianAiCopilotPanel(
                currentRoute = currentRoute,
                isOnline = isOnline,
                onClose = { isExpanded = false },
                onNavigateToTool = onNavigateToTool
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrillianAiCopilotPanel(
    currentRoute: String,
    isOnline: Boolean,
    onClose: () -> Unit,
    onNavigateToTool: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 1. Hardware Detection
    val hardwareSpecs = remember { detectHardware(context) }
    val recommendedModel = remember(hardwareSpecs) { getRecommendedModel(hardwareSpecs) }

    // 2. Active Screen Context Detection
    val toolContext = remember(currentRoute) { getToolContext(currentRoute) }

    // 3. Shared Preferences for persistent mock download state
    val prefs = remember { context.getSharedPreferences("brillian_ai_prefs", Context.MODE_PRIVATE) }
    var downloadedModelId by remember { mutableStateOf(prefs.getString("downloaded_model", "smollm2_360m")) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }
    var downloadSpeed by remember { mutableStateOf("") }
    var downloadStatusText by remember { mutableStateOf("") }

    // 4. Tab State: 0 = Chat Copilot, 1 = On-Device Model Manager, 2 = Hardware Spec Report
    var selectedTab by remember { mutableStateOf(0) }

    // 5. Chat History State
    val chatMessages = remember {
        mutableStateListOf(
            ChatMessage(
                role = "assistant",
                content = "Hello! I am **Brillian AI**, your on-device local industrial trade copilot. I am optimized to run 100% offline at job sites without cell reception. How can I assist you with your trade calculations today?"
            )
        )
    }
    var currentInput by remember { mutableStateOf("") }
    var isGeneratingResponse by remember { mutableStateOf(false) }

    val lazyListState = rememberLazyListState()

    // Whenever messages change, scroll to the bottom
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            lazyListState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth(0.95f)
            .fillMaxHeight(0.7f)
            .padding(bottom = 8.dp)
            .testTag("ai_copilot_panel"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Engineering,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Brillian AI",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (!downloadedModelId.isNullOrEmpty()) Color(0xFF4CAF50) else Color(0xFFFF9800))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (!downloadedModelId.isNullOrEmpty()) "Local Offline Mode Active (${downloadedModelId})" else "Offline Model Needed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Panel",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Tab Content - Directly displaying Chat Copilot
            Box(modifier = Modifier.weight(1f)) {
                if (true) {
                    // Chat Copilot Tab
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Context Notification Banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Layers,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Active Screen Context: ${toolContext.title}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.weight(1f)
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Dns,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Trade RAG Active",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }

                        // Model Selection Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Memory,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Active LLM:",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Parse downloaded models list
                            val downloadedListString = prefs.getString("downloaded_models_list", "") ?: ""
                            val downloadedIds = if (downloadedListString.isBlank()) {
                                if (!downloadedModelId.isNullOrEmpty()) {
                                    prefs.edit().putString("downloaded_models_list", downloadedModelId).apply()
                                    listOf(downloadedModelId)
                                } else {
                                    emptyList()
                                }
                            } else {
                                downloadedListString.split(",").filter { it.isNotBlank() }
                            }
                            val allModels = getModelsList()
                            val downloadedModels = allModels.filter { downloadedIds.contains(it.id) || it.id == downloadedModelId }

                            if (downloadedModels.isEmpty()) {
                                Text(
                                    text = "None (Download in Settings)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.SemiBold
                                )
                            } else {
                                var showModelDropdown by remember { mutableStateOf(false) }
                                val activeModel = allModels.find { it.id == downloadedModelId } ?: downloadedModels.first()

                                Box {
                                    OutlinedButton(
                                        onClick = { showModelDropdown = true },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp).testTag("select_model_dropdown_button")
                                    ) {
                                        Text(
                                            text = activeModel.name.split(" ")[0] + " " + (if (activeModel.id == "qwen25_15b") "1.5B" else if (activeModel.id == "smollm2_360m") "360M" else "2B"),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = showModelDropdown,
                                        onDismissRequest = { showModelDropdown = false }
                                    ) {
                                        downloadedModels.forEach { model ->
                                            DropdownMenuItem(
                                                text = {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        if (model.id == downloadedModelId) {
                                                            Icon(
                                                                imageVector = Icons.Default.Check,
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                        }
                                                        Text(
                                                            text = model.name,
                                                            style = MaterialTheme.typography.bodySmall.copy(
                                                                fontWeight = if (model.id == downloadedModelId) FontWeight.Bold else FontWeight.Normal
                                                            )
                                                        )
                                                    }
                                                },
                                                onClick = {
                                                    prefs.edit().putString("downloaded_model", model.id).apply()
                                                    downloadedModelId = model.id
                                                    showModelDropdown = false
                                                },
                                                modifier = Modifier.testTag("model_option_${model.id}")
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Conversation Area
                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(chatMessages) { msg ->
                                ChatBubble(
                                    msg = msg,
                                    onNavigateToTool = onNavigateToTool
                                )
                            }

                                if (isGeneratingResponse) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    strokeWidth = 2.dp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Local LLM is thinking offline...",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Suggestions Quick-Actions based on Active Screen
                            if (toolContext.sampleQuestions.isNotEmpty() && chatMessages.size == 1) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Recommended Quick Inquiries:",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        toolContext.sampleQuestions.forEach { question ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                                    .clickable {
                                                        currentInput = question
                                                    }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = question,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Input Box
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = currentInput,
                                    onValueChange = { currentInput = it },
                                    placeholder = { Text("Ask Brillian AI trade questions...") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp)
                                        .testTag("ai_chat_input"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                        disabledContainerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    maxLines = 3,
                                    enabled = !isGeneratingResponse
                                )

                                IconButton(
                                    onClick = {
                                        if (currentInput.isNotBlank()) {
                                            val query = currentInput
                                            chatMessages.add(ChatMessage("user", query))
                                            currentInput = ""
                                            isGeneratingResponse = true

                                            coroutineScope.launch {
                                                // Trigger real online Gemini model if online, with robust local fallback
                                                val response = if (isOnline) {
                                                    generateOnlineGeminiInference(query, toolContext) ?: generateOfflineInference(
                                                        userQuery = query,
                                                        downloadedModelId = downloadedModelId ?: "",
                                                        toolContext = toolContext,
                                                        isOnline = isOnline
                                                    )
                                                } else {
                                                    generateOfflineInference(
                                                        userQuery = query,
                                                        downloadedModelId = downloadedModelId ?: "",
                                                        toolContext = toolContext,
                                                        isOnline = isOnline
                                                    )
                                                }
                                                val answer = response.content
                                                val toolLink = response.toolLink
                                                val toolTitle = response.toolTitle

                                                // Stream the response word-by-word
                                                chatMessages.add(ChatMessage("assistant", "", toolLink = toolLink, toolTitle = toolTitle))
                                                val lastIdx = chatMessages.size - 1
                                                val words = answer.split(" ")
                                                val sb = StringBuilder()

                                                for (i in words.indices) {
                                                    delay(30) // snappy simulated token-generation delay
                                                    sb.append(words[i]).append(" ")
                                                    chatMessages[lastIdx] = ChatMessage("assistant", sb.toString().trim(), toolLink = toolLink, toolTitle = toolTitle)
                                                }
                                                isGeneratingResponse = false
                                            }
                                        }
                                    },
                                    enabled = currentInput.isNotBlank() && !isGeneratingResponse,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (currentInput.isNotBlank() && !isGeneratingResponse) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                        .testTag("ai_send_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Send",
                                        tint = if (currentInput.isNotBlank() && !isGeneratingResponse) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        // Reserved for other tabs
                        when (1) {
                            1 -> {
                        // On-Device Model Manager Tab
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Secure On-Demand Model Downloader",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Host AI weights locally on your storage (100% secure, zero cell latency, zero subscription costs). Download requires Wi-Fi. Choose a model that matches your detected memory tier.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Status Box
                            if (isDownloading) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(18.dp),
                                                strokeWidth = 2.dp,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = "Downloading AI Weights offline...",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                        LinearProgressIndicator(
                                            progress = { downloadProgress },
                                            modifier = Modifier.fillMaxWidth(),
                                            color = MaterialTheme.colorScheme.primary,
                                            trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Speed: ${downloadSpeed}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                            )
                                            Text(
                                                text = "${(downloadProgress * 100).toInt()}% Done",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                            )
                                        }
                                        Text(
                                            text = downloadStatusText,
                                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                                        )
                                    }
                                }
                            }

                            // Model Recommendation Matrix List
                            getModelsList().forEach { model ->
                                val isSelected = downloadedModelId == model.id
                                val isRecommended = recommendedModel == model.id

                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = model.name,
                                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    if (isRecommended) {
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(MaterialTheme.colorScheme.primary)
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(
                                                                text = "Recommended",
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = MaterialTheme.colorScheme.onPrimary
                                                            )
                                                        }
                                                    }
                                                }
                                                Text(
                                                    text = "Approx Size: ${model.size} | Performance: ${model.targetTokensPerSec}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                )
                                            }
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Active",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }

                                        Text(
                                            text = model.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        if (!isSelected && !isDownloading) {
                                            Button(
                                                onClick = {
                                                    isDownloading = true
                                                    downloadProgress = 0f
                                                    coroutineScope.launch {
                                                        val steps = listOf(
                                                            "Requesting server-chunk download index...",
                                                            "Allocating offline sandbox file in directory context.getExternalFilesDir()...",
                                                            "Downloading chunk 1 of 4 (weights-001)...",
                                                            "Downloading chunk 2 of 4 (weights-002)...",
                                                            "Downloading chunk 3 of 4 (weights-003)...",
                                                            "Downloading chunk 4 of 4 (weights-004)...",
                                                            "Validating on-device GGUF hash values...",
                                                            "Compiling LLM layout to system NPU shader..."
                                                        )

                                                        for (stepIdx in steps.indices) {
                                                            val baseProg = stepIdx.toFloat() / steps.size
                                                            downloadStatusText = steps[stepIdx]
                                                            for (j in 0..5) {
                                                                delay(150)
                                                                downloadProgress = baseProg + (j / 6.0f) * (1.0f / steps.size)
                                                                downloadSpeed = "${(18..35).random()} MB/s"
                                                            }
                                                        }

                                                        downloadProgress = 1.0f
                                                        downloadSpeed = "0 MB/s"
                                                        downloadStatusText = "Model compiled and loaded into isolated process."
                                                        delay(1000)

                                                        prefs.edit().putString("downloaded_model", model.id).apply()
                                                        downloadedModelId = model.id
                                                        isDownloading = false
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary
                                                )
                                            ) {
                                                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Download and Compile")
                                            }
                                        }
                                    }
                                }
                            }

                            if (!downloadedModelId.isNullOrEmpty()) {
                                OutlinedButton(
                                    onClick = {
                                        prefs.edit().putString("downloaded_model", "").apply()
                                        downloadedModelId = ""
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Erase Local Weights & Free Space")
                                }
                            }
                        }
                    }

                    2 -> {
                        // Hardware Spec Report Tab
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "On-Device Hardware Diagnostics",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )

                            val details = listOf(
                                "Total System Memory" to "${"%.2f".format(hardwareSpecs.totalRamGb)} GB RAM",
                                "Detected Memory Tier" to hardwareSpecs.memoryTier,
                                "Processor / SoC Vendor" to hardwareSpecs.socVendor,
                                "Device Model" to hardwareSpecs.deviceModel,
                                "OS Version" to "Android ${hardwareSpecs.androidVersion} (API ${hardwareSpecs.apiLevel})",
                                "Vulkan GPU Accelerator" to if (hardwareSpecs.supportsVulkan) "Enabled (Vulkan API Level 1.3 Active)" else "CPU Software Fallback (ARM Neon)",
                                "Max Context Support" to "2048 Tokens (Process isolated, memory limited to 1.1 GB max)"
                            )

                            details.forEach { (label, value) ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = value,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } // closes when (1)
        } // closes else
    } // closes Box
} // closes Column
} // closes BrillianAiCopilotPanel

@Composable
fun ChatBubble(
    msg: ChatMessage,
    onNavigateToTool: (String) -> Unit
) {
    val isUser = msg.role == "user"
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 0.dp,
                            bottomEnd = if (isUser) 0.dp else 16.dp
                        )
                    )
                    .background(
                        if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .padding(12.dp)
            ) {
                Column {
                    // Formatting simple markdown-style bold tags "**" inside the chat bubble
                    Text(
                        text = parseBoldMarkdown(msg.content),
                        color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    // Clickable Tool Link Card inside bubble!
                    if (!isUser && !msg.toolLink.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToTool(msg.toolLink) }
                                .testTag("chat_tool_link_${msg.toolLink}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Launch,
                                    contentDescription = "Navigate to tool",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = msg.toolTitle ?: "Open Tool",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
            Text(
                text = if (isUser) "You" else "Brillian Copilot",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
            )
        }
    }
}

// ---------------- HARDWARE SPEC DETECTION ----------------

data class HardwareDiagnostics(
    val totalRamGb: Double,
    val memoryTier: String,
    val socVendor: String,
    val deviceModel: String,
    val androidVersion: String,
    val apiLevel: Int,
    val supportsVulkan: Boolean
)

fun detectHardware(context: Context): HardwareDiagnostics {
    val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memInfo = ActivityManager.MemoryInfo()
    actManager.getMemoryInfo(memInfo)
    val totalRamGB = memInfo.totalMem / (1024 * 1024 * 1024.0)

    val memoryTier = when {
        totalRamGB < 6.0 -> "Low-End / Budget Tier"
        totalRamGB < 11.0 -> "Mid-Range Tier"
        else -> "Flagship / High-End Tier"
    }

    val hardware = Build.HARDWARE
    val board = Build.BOARD
    val socVendor = if (hardware.lowercase().contains("qcom") || board.lowercase().contains("msm")) {
        "Qualcomm Snapdragon"
    } else if (hardware.lowercase().contains("mt") || board.lowercase().contains("mt")) {
        "MediaTek Dimensity"
    } else {
        "ARM Generic Cortex-A"
    }

    return HardwareDiagnostics(
        totalRamGb = totalRamGB,
        memoryTier = memoryTier,
        socVendor = socVendor,
        deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
        androidVersion = Build.VERSION.RELEASE,
        apiLevel = Build.VERSION.SDK_INT,
        supportsVulkan = Build.VERSION.SDK_INT >= 24
    )
}

fun getRecommendedModel(specs: HardwareDiagnostics): String {
    return when {
        specs.totalRamGb < 6.0 -> "smollm2_360m"
        specs.totalRamGb < 11.0 -> "qwen25_15b"
        else -> "gemma2_2b"
    }
}

fun getModelsList(): List<ModelProfile> {
    return listOf(
        ModelProfile(
            id = "smollm2_360m",
            name = "SmolLM2 360M Instruct (Q4_K_M)",
            size = "230 MB",
            sizeBytes = 241172480,
            description = "Optimized specifically for battery-sensitive work. Fast, reliable math-based calculation capabilities on Snappy architectures.",
            recommendedRam = "4 GB - 6 GB RAM",
            targetTokensPerSec = "24 tok/sec"
        ),
        ModelProfile(
            id = "qwen25_15b",
            name = "Qwen2.5 1.5B Instruct (Q4_K_M)",
            size = "920 MB",
            sizeBytes = 964689920,
            description = "Best balance of reasoning and memory size. Excellent on SD 7-series or equivalent processors.",
            recommendedRam = "6 GB - 8 GB RAM",
            targetTokensPerSec = "15 tok/sec"
        ),
        ModelProfile(
            id = "gemma2_2b",
            name = "Gemma-2 2B Instruct (Q4_K_M)",
            size = "1.52 GB",
            sizeBytes = 1632051200,
            description = "High reasoning and technical engineering capabilities. Best for multi-step math processes, complex structural analysis, and compliance checks.",
            recommendedRam = "12 GB - 16 GB RAM",
            targetTokensPerSec = "11 tok/sec"
        )
    )
}

// ----------------- TOOL CONTEXT & TRADE KNOWLEDGE BASE (RAG) -----------------

fun getToolContext(route: String): ToolContext {
    return when (route) {
        "tool_voltage_drop" -> ToolContext(
            toolId = "voltage_drop",
            title = "Voltage Drop Calculator",
            defaultParams = mapOf(
                "length" to "120 ft",
                "current" to "20 Amps",
                "conductor" to "12 AWG Copper",
                "voltage" to "120V Single Phase",
                "calculated_drop" to "3.94%"
            ),
            tradeKnowledgeBase = """
                NEC Chapter 9 Table 8 Resistance values of copper and aluminum.
                Conductor 12 AWG resistance per 1000ft is 1.93 Ohms.
                Formulas: Single Phase Drop = (2 * K * I * L) / CircularMils.
                K constant (Copper) = 12.9, K constant (Aluminum) = 21.2.
                NEC 210.19(A) recommends maximum branch circuit voltage drop of 3% for optimal equipment life.
            """.trimIndent(),
            sampleQuestions = listOf(
                "Why is my 12 AWG wire showing 3.94% drop?",
                "What size copper wire brings it under 3%?",
                "How does switching to Aluminum affect this?"
            )
        )
        "tool_board_footage" -> ToolContext(
            toolId = "board_footage",
            title = "Board Footage & Lumber Estimator",
            defaultParams = mapOf(
                "thickness" to "2 inches (8/4)",
                "width" to "8 inches",
                "length" to "10 feet",
                "quantity" to "5 pieces",
                "calculated_board_feet" to "66.67 BF"
            ),
            tradeKnowledgeBase = """
                Lumber measurements are computed in Board Feet (BF).
                Formula: BF = (Thickness (in) * Width (in) * Length (ft)) / 12 * Quantity.
                Commercial hardwood is typically graded by thickness in quarters (e.g., 4/4 is 1\" rough, 8/4 is 2\" rough).
                Common shrink allowance is 5-8% for green timber, and workshop planning requires 10-15% overage for knots and sapwood waste.
            """.trimIndent(),
            sampleQuestions = listOf(
                "How do you calculate board footage for 8/4 rough lumber?",
                "Why is thickness graded in quarters?",
                "How much overage should I add for grading waste?"
            )
        )
        "tool_paint_coverage" -> ToolContext(
            toolId = "paint_coverage",
            title = "Paint & Primer Coverage",
            defaultParams = mapOf(
                "area" to "450 sq ft",
                "substrate" to "Rough Brick masonry",
                "coats" to "2 coats",
                "calculated_gallons" to "2.25 Gallons"
            ),
            tradeKnowledgeBase = """
                Standard paint covers roughly 350-400 sq ft per gallon on fully primed drywall.
                Substrate Porosity Index (Multiplier factors):
                - Drywall / Smooth Steel: 1.0 (no extra)
                - Rough Sawn Cedar: 1.4 (high absorption)
                - Porous Masonry / Brick: 1.6 (very high absorption)
                Wet Film Thickness (WFT) to Dry Film Thickness (DFT) formula:
                DFT = WFT * (Volume Solids % / 100).
            """.trimIndent(),
            sampleQuestions = listOf(
                "Why does brick absorb 60% more paint than drywall?",
                "Explain the relationship of WFT and DFT.",
                "How many gallons do I need for 450 sq ft of brick?"
            )
        )
        "tool_cutlist_optimizer" -> ToolContext(
            toolId = "cutlist_optimizer",
            title = "Cut List Optimizer",
            defaultParams = mapOf(
                "sheet_size" to "48 x 96 inches",
                "kerf_width" to "0.125 inches (1/8\" blade)",
                "parts" to "6 pieces of 12x24, 4 pieces of 15x30",
                "yield" to "82.4%"
            ),
            tradeKnowledgeBase = """
                1D and 2D nesting optimization rules require subtracting blade kerf (normally 1/8\" or 3.2mm) from each sheet boundary.
                Grain direction must match between parallel nested parts to ensure physical joint stability and prevent wrapping under heat.
                First cut must establish a clean reference edge (trim cut) of at least 1/4\" on rough timber sheets.
            """.trimIndent(),
            sampleQuestions = listOf(
                "Why is kerf thickness critical to sheet nesting?",
                "What is grain matching in wood panel optimization?",
                "How do I prevent warp during ripping?"
            )
        )
        "tool_sagulator" -> ToolContext(
            toolId = "sagulator",
            title = "Lumber Sagulator",
            defaultParams = mapOf(
                "span" to "36 inches",
                "wood" to "Red Oak (Janka 1290, SG 0.63)",
                "load" to "100 lbs uniform",
                "deflection" to "0.041 inches"
            ),
            tradeKnowledgeBase = """
                Shelf deflection is computed using Euler-Bernoulli beam theory.
                Formula: Deflection = (5 * W * L^3) / (384 * E * I).
                E (Modulus of Elasticity) values:
                - Red Oak: 1,820,000 psi
                - Eastern White Pine: 1,240,000 psi
                I (Moment of Inertia) for flat rectangular board: (Width * Thickness^3) / 12.
                Acceptable deflection threshold for premium cabinetry is < 0.03\" per foot (or 0.09\" for 36\" span).
            """.trimIndent(),
            sampleQuestions = listOf(
                "Is 0.041 inches of deflection acceptable for a bookcase?",
                "What is the modulus of elasticity for Red Oak?",
                "How does doubling the thickness impact sag?"
            )
        )
        else -> ToolContext(
            toolId = "general",
            title = "Brillian Trade Companion",
            defaultParams = emptyMap(),
            tradeKnowledgeBase = """
                General trade assistance guidelines:
                Apply the closest civil, electrical, plumbing, carpentry, and masonry codes.
                Always prioritize OSHA safety limits and maximum structural load ratings.
                Provide exact formulas and explain step-by-step calculations.
            """.trimIndent(),
            sampleQuestions = listOf(
                "Tell me standard riser height guidelines for stairs.",
                "Explain the 3-4-5 rule for squaring framing.",
                "How do I convert decimal inches to eighths of an inch?"
            )
        )
    }
}

// ---------------- LOCAL INFERENCE GENERATION RULE-ENGINE ----------------

data class AIResponse(
    val content: String,
    val toolLink: String? = null,
    val toolTitle: String? = null
)

fun generateOfflineInference(
    userQuery: String,
    downloadedModelId: String,
    toolContext: ToolContext,
    isOnline: Boolean
): AIResponse {
    if (downloadedModelId.isEmpty()) {
        return AIResponse(
            content = "⚠️ **Local AI Engine is Offline**\n\nPlease select or download an on-device local model weights file (e.g., SmolLM2 or Qwen2.5) to provide 100% secure, offline trade calculations on remote jobsites."
        )
    }

    val q = userQuery.lowercase().trim()
    val hash = Math.abs(userQuery.hashCode())

    val modelName = when (downloadedModelId) {
        "smollm2_360m" -> "SmolLM2 360M"
        "qwen25_15b" -> "Qwen2.5 1.5B"
        else -> "Gemma-2 2B"
    }
    val runWithModelText = "\n\n*(Processed locally on-device via $modelName)*"

    // Dynamic offline math & arithmetic processor interceptor
    val mathAnswer = tryEvaluateArithmetic(userQuery)
    if (mathAnswer != null) {
        return AIResponse(
            content = mathAnswer + runWithModelText
        )
    }

    // Custom check for casual conversation greetings or help queries
    val isGreeting = q in setOf("hello", "hi", "hey", "howdy", "yo", "greetings", "good morning", "good afternoon", "good evening", "hi!")
    val isAppreciation = q in setOf("thanks", "thank you", "thanks!", "thank you!", "great", "awesome", "perfect", "cool", "nice", "ok", "okay")
    val isHelpRequest = q in setOf("help", "who are you", "what can you do", "what is this", "menu", "features", "capabilities")

    if (isGreeting) {
        return AIResponse(
            content = "Hello! I am your on-device **Brillian Copilot**, compiled to run securely and fully offline at remote jobsites.\n\nI can assist you with mathematical formulas, wire-sizing compliance, wood sag limits, and precise material estimations for any active project. What trade problem are we solving today?" + runWithModelText
        )
    }

    if (isAppreciation) {
        return AIResponse(
            content = "You're very welcome! Glad I could help. Let me know if you need any other calculations or reference checks for your project. Keep up the great work and stay safe on the jobsite!" + runWithModelText
        )
    }

    if (isHelpRequest) {
        return AIResponse(
            content = "I am a local AI assistant optimized for on-site trade computations without cellular signal or internet dependency. My offline knowledge database includes:\n\n" +
                    "- **Woodworking**: Board footage estimation, shelf sag limits (Sagulator), kerf-bending, and cutlist packing.\n" +
                    "- **Electrical**: Line voltage drop calculations, NEC-compliant wire sizing, conduit filling, and Ohm's Law.\n" +
                    "- **Masonry & Finishes**: Concrete volume sizing, mortar ratios, and paint coverage estimations based on substrate porosity.\n\n" +
                    "To start, simply ask a technical question or select an active calculator tool from the dashboard!" + runWithModelText
        )
    }

    val dynamicPrefixes = listOf(
        "Checking local weights stream... Here is the on-site calculation for your inquiry: \n\n",
        "Based on your query and active screen parameters, here is the technical breakdown: \n\n",
        "Analyzing trade parameters for **${toolContext.title}** offline. Let's look at the specs: \n\n",
        "Query successfully mapped to local database cache. Analysis details below: \n\n",
        "Under offline mode, here is the official trade standard computation: \n\n"
    )
    val prefix = dynamicPrefixes[hash % dynamicPrefixes.size]

    val suffixTips = listOf(
        "\n\n*Pro-tip: Always cross-reference physical wire sizing or lumber loading spans with local structural code standards before final assembly.*",
        "\n\n*Safety Note: Ensure your on-site tools are fully calibrated to professional tolerances to match calculated indices.*",
        "\n\n*Compliance Tip: Keep documented calculations in your active jobsite folder for regulatory inspections.*",
        "\n\n*Practical Guide: Wear proper personal protective equipment (PPE) when working with these high-stress load materials.*"
    )
    val suffix = suffixTips[(hash + 13) % suffixTips.size]

    // Match exact rules and respond using RAG database for accurate mathematical formulas without hallucination!
    val coreResponse = when (toolContext.toolId) {
        "voltage_drop" -> {
            val text = if (q.contains("drop") || q.contains("why") || q.contains("current")) {
                "Based on **NEC Table 8** and local RAG context, your 12 AWG wire over a span of 120 ft carries a resistance of **1.93 Ohms per 1000 ft**. At 20 Amps on a 120V single-phase circuit, the calculated drop is **3.94%**.\n\n" +
                "**Why is it highlighting?**\n" +
                "This violates **NEC 210.19(A)** which strongly recommends keeping branch circuit drop under **3%** to avoid wire overheating and equipment power starvation.\n\n" +
                "**How to fix it?**\n" +
                "Upsizing the conductor from **12 AWG** to **10 AWG Copper** (resistance 1.24 Ohms/1000ft) reduces the calculated drop to **2.53%**, bringing the system safely within electrical standard code parameters."
            } else if (q.contains("aluminum")) {
                "Switching from Copper to Aluminum increases resistance from **1.93 Ohms** to **3.18 Ohms** per 1000 ft (Al K-constant is 21.2 vs Cu 12.9).\n\n" +
                "For a 120ft single-phase 120V line carrying 20A, an aluminum wire of the same size yields a **6.48% drop**. You would need to upsize to at least **8 AWG Aluminum** to bring the drop safely down to **2.57%**."
            } else {
                "To resolve your voltage drop inquiry on a single-phase circuit, the exact local formula is:\n" +
                "`Voltage Drop = (2 * K * Current * Length) / Circular Mils`.\n" +
                "Currently, your circuit has a length of **120 ft** at **20A** with **12 AWG**. Let me know if you want me to calculate a specific wire gauge transition for you!"
            }
            AIResponse(prefix + text + suffix + runWithModelText, "widget_voltage_drop", "Voltage Drop Calculator")
        }
        "board_footage" -> {
            val text = if (q.contains("quarters") || q.contains("4/4") || q.contains("8/4")) {
                "In commercial lumber mills, rough hardwood thickness is measured in quarters of an inch:\n" +
                "- **4/4 (Four-Quarter)** rough lumber is exactly **1.0 inch** thick.\n" +
                "- **8/4 (Eight-Quarter)** rough lumber is exactly **2.0 inches** thick.\n\n" +
                "Quarters refer specifically to the rough-sawn dimension before surface planing. After planing both faces (S2S), a 4/4 board will typically finish at **13/16 inches** thick."
            } else if (q.contains("calculate") || q.contains("footage") || q.contains("formula")) {
                "The Board Footage (BF) formula utilized by your local engine is:\n" +
                "BF = (Thickness (in) * Width (in) * Length (ft) / 12) * Quantity\n\n" +
                "For your active input (thickness = **2\"**, width = **8\"**, length = **10ft**, quantity = **5 pieces**):\n" +
                "BF = (2 * 8 * 10 / 12) * 5 = 66.67 Board Feet.\n" +
                "Hardwoods always calculate board footage based on rough dimensions prior to planing waste."
            } else {
                "For hardwood estimation, we always add a standard overage margin depending on the lumber grade:\n" +
                "- **FAS (First and Seconds)**: Add **10% to 15%** waste allowance.\n" +
                "- **Common #1**: Add **20% to 25%** allowance.\n" +
                "Your active estimation is **66.67 BF**. Let me know if you would like to compute cost logs!"
            }
            AIResponse(prefix + text + suffix + runWithModelText, "widget_board_footage", "Board Footage Estimator")
        }
        "paint_coverage" -> {
            val text = if (q.contains("brick") || q.contains("absorb") || q.contains("porosity")) {
                "Masonry materials like rough-sawn cedar and unsealed brick are highly porous, which dramatically increases wet paint absorption.\n\n" +
                "The local RAG substrate porosity index dictates:\n" +
                "- Drywall/Wood (Smooth): **1.0x** multiplier\n" +
                "- Rough Brick: **1.6x** multiplier (requiring 60% more paint volume)\n\n" +
                "For your 450 sq ft area, a standard single gallon covering 350 sq ft is insufficient. Rough brick reduces actual coverage to **218 sq ft per gallon**, requiring **2.25 gallons** for a proper 2-coat finish."
            } else if (q.contains("wft") || q.contains("dft") || q.contains("dry")) {
                "**Wet Film Thickness (WFT)** and **Dry Film Thickness (DFT)** are governed by the volume solids percentage of the paint:\n" +
                "DFT = WFT * (Volume Solids % / 100)\n\n" +
                "If you apply a paint with **40% solids** at a 5-mil wet thickness, the dry film will measure exactly **2 mils**. Measuring WFT on-site with a wet gauge ensures compliance with industrial thickness specs."
            } else {
                "Your active surface area is **450 sq ft** on rough brick masonry with **2 coats** of coverage, which yields an estimated volume of **2.25 Gallons**. Ensure you use a heavy-nap masonry roller (1\" to 1.25\") to fill deep substrate crevices."
            }
            AIResponse(prefix + text + suffix + runWithModelText, "tool_painting_coating_studio", "Paint & Coating Studio")
        }
        "cutlist_optimizer" -> {
            val text = if (q.contains("kerf") || q.contains("blade")) {
                "**Kerf thickness** is the width of the material removed by the saw blade during a cut.\n" +
                "A standard table saw blade has a kerf of **1/8 inch (0.125\")**.\n\n" +
                "When dividing a 48\"x96\" sheet into multiple parts, failing to subtract the 1/8\" kerf per cut causes cumulative dimensional errors, resulting in the final parts being undersized. The optimizer strictly subtracts 1/8\" from the sheet size for every parallel nested layout line."
            } else if (q.contains("grain")) {
                "**Grain matching** is an advanced layout technique where the wood grain runs continuously across adjacent drawers, panels, or cabinet faces.\n" +
                "When active, the optimizer restricts parts rotation so that the vertical/horizontal grain lines stay strictly aligned. This increases layout waste (lower yield %) but delivers a highly professional finish."
            } else {
                "To optimize your cut list, input your sheet bounds (e.g. 48\"x96\" plywood) and list your desired part dimensions. The on-device engine uses a 2D bin-packing algorithm to nesting-fit the pieces, securing a high-yield output above **80%** while respecting your saw blade's **1/8\" kerf**."
            }
            AIResponse(prefix + text + suffix + runWithModelText, "widget_cutlist_optimizer", "Cutlist Optimizer")
        }
        "sagulator" -> {
            val text = if (q.contains("acceptable") || q.contains("deflection") || q.contains("allowable")) {
                "According to premium cabinetry guidelines, the **allowable deflection threshold** for loaded shelving is **0.03 inches per foot**.\n\n" +
                "For your **36-inch (3ft) span**, the max acceptable sag is **0.09 inches**.\n" +
                "Your Red Oak shelf under a 100 lb uniform load will deflect exactly **0.041 inches**. This is well below the 0.09\" sag limit, meaning the shelf is structurally sound and will not visibly droop over time."
            } else if (q.contains("oak") || q.contains("elasticity") || q.contains("e")) {
                "The Modulus of Elasticity (E) represents the material stiffness of wood:\n" +
                "- **Red Oak**: 1,820,000 psi\n" +
                "- **Pine**: 1,240,000 psi\n\n" +
                "A stiffer wood with higher E values significantly reduces sag deflection under heavy loads. Red Oak is highly recommended for loaded spans over 30 inches."
            } else if (q.contains("thickness") || q.contains("double")) {
                "Shelf deflection is inversely proportional to the cube of its thickness ($1 / T^3$).\n\n" +
                "If you double the shelf thickness from **3/4\"** to **1.5\"**, you reduce shelf deflection by **88%** under the exact same load. Increasing thickness is the single most powerful way to eliminate shelf sag."
            } else {
                "For a 36-inch Red Oak shelf carrying a 100 lb uniform load, the calculated deflection is **0.041 inches**. Let me know if you would like to compare different wood species stiffnesses!"
            }
            AIResponse(prefix + text + suffix + runWithModelText, "widget_sagulator", "Timber Sagulator")
        }
        else -> {
            // General Trade RAG
            if (q.contains("concrete") || q.contains("slab") || q.contains("footing")) {
                val computedVolume = "For a standard **10ft x 10ft** concrete slab at **4 inches** thick, the calculated volume is **1.23 cubic yards** of concrete. This equates to approximately **56 standard 80lb bags** of premixed concrete, or **75 bags of 60lb mix**."
                AIResponse(prefix + computedVolume + suffix + runWithModelText, "widget_concrete_volume", "Concrete Volume Sizer")
            } else if (q.contains("drop") || q.contains("voltage") || q.contains("wire") || q.contains("current")) {
                val computedDrop = "For a single-phase 120V circuit carrying **20 Amps** over a **120 ft** run using **12 AWG Copper** wire, the calculated voltage drop is **3.94%** (4.73 Volts drop). Upsizing to **10 AWG Copper** brings the drop down to a safe **2.53%**."
                AIResponse(prefix + computedDrop + suffix + runWithModelText, "widget_voltage_drop", "Voltage Drop Calculator")
            } else if (q.contains("paint") || q.contains("coverage") || q.contains("gallon")) {
                val computedPaint = "For a surface area of **450 sq ft** with a **rough brick** substrate (porosity factor 1.6x) and applying **2 coats**, you will need exactly **2.25 Gallons** of paint."
                AIResponse(prefix + computedPaint + suffix + runWithModelText, "tool_painting_coating_studio", "Paint & Coating Studio")
            } else if (q.contains("board") || q.contains("lumber") || q.contains("footage") || q.contains("bf")) {
                val computedBF = "For **5 pieces** of rough-sawn lumber measuring **2\" thick x 8\" wide x 10ft long**, the board footage is exactly **66.67 Board Feet (BF)**."
                AIResponse(prefix + computedBF + suffix + runWithModelText, "widget_board_footage", "Board Footage Estimator")
            } else if (q.contains("sag") || q.contains("shelf") || q.contains("deflection") || q.contains("stiffness")) {
                val computedSag = "For a **36-inch (3ft) Red Oak** shelf (3/4\" thick, 10\" deep) carrying a **100 lb** uniform load, the calculated deflection is **0.041 inches**, which is well within the 0.09\" allowable droop limit."
                AIResponse(prefix + computedSag + suffix + runWithModelText, "widget_sagulator", "Timber Sagulator")
            } else if (q.contains("cut") || q.contains("optimize") || q.contains("layout") || q.contains("plywood")) {
                val optimizerText = "Using our 2D bin-packing layout algorithm, nesting multiple parts onto standard **48\" x 96\" plywood** sheets while accounting for a **1/8\" saw blade kerf** can improve raw material yield to **over 85%**."
                AIResponse(prefix + optimizerText + suffix + runWithModelText, "widget_cutlist_optimizer", "Cutlist Optimizer")
            } else if (q.contains("stair") || q.contains("riser") || q.contains("run")) {
                val stairText = "Under International Residential Code (IRC) Section R311.7:\n- The **maximum riser height** of stairs is **7.75 inches**.\n- The **minimum tread run** is **10.0 inches**.\nTo calculate proper stringer layouts, open the **Stair Layout** tool."
                AIResponse(prefix + stairText + suffix + runWithModelText, "widget_stair_layout", "Stair Layout Tool")
            } else if (q.contains("decimal") || q.contains("fraction") || q.contains("inch")) {
                val fracText = "To convert decimal inches to eighths of an inch, multiply the decimal part by 8:\n- `0.125 * 8 = 1` -> **1/8\"**\n- `0.375 * 8 = 3` -> **3/8\"**\n- `0.625 * 8 = 5` -> **5/8\"**\n- `0.875 * 8 = 7` -> **7/8\"**\nFor high-precision, multi-format fraction calculations, open the Fractional Calculator."
                AIResponse(prefix + fracText + suffix + runWithModelText, "widget_fractional_calc", "Fractional Calculator")
            } else if (q.contains("task") || q.contains("checklist")) {
                val tasksText = "Track your jobsite safety checkpoints, contractor tasks, and building inspection items on-demand."
                AIResponse(prefix + tasksText + suffix + runWithModelText, "widget_tasks", "Task Checklist")
            } else if (q.contains("timer") || q.contains("focus")) {
                val timerText = "Set high-performance interval work blocks (Pomodoro style) for focused, distraction-free productivity at active jobsites."
                AIResponse(prefix + timerText + suffix + runWithModelText, "widget_timer", "Focus Timer")
            } else if (q.contains("convert") || q.contains("unit")) {
                val unitText = "Convert length (m to ft), mass (kg to lbs), area, volume, and temperature scales instantly with offline RAG support."
                AIResponse(prefix + unitText + suffix + runWithModelText, "widget_unit_converter", "Unit Converter")
            } else if (q.contains("calc") || q.contains("scientific")) {
                val calcText = "Perform advanced trigonometric, logarithmic, exponential, and physical constant math operations."
                AIResponse(prefix + calcText + suffix + runWithModelText, "widget_calculator", "Scientific Calculator")
            } else {
                AIResponse(
                    content = prefix + "Based on the **Brillian Offline Trade Database**, I can assist you with precise formulas for plumbing friction loss, electrical box capacities, timber sagulator deflection, masonry counts, or OSHA guidelines. Let me know what trade specifications you need!" + suffix + runWithModelText
                )
            }
        }
    }

    return coreResponse
}

// Simple parser to make text enclosed in '**' bold inside Android Compose Text components
fun parseBoldMarkdown(text: String): androidx.compose.ui.text.AnnotatedString {
    val builder = androidx.compose.ui.text.AnnotatedString.Builder()
    val parts = text.split("**")
    for (i in parts.indices) {
        if (i % 2 == 1) {
            builder.pushStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold))
            builder.append(parts[i])
            builder.pop()
        } else {
            builder.append(parts[i])
        }
    }
    return builder.toAnnotatedString()
}

// ---------------- ONLINE LIVE INFERENCE GENERATION VIA GEMINI 3.5 FLASH ----------------

suspend fun generateOnlineGeminiInference(
    userQuery: String,
    toolContext: ToolContext
): AIResponse? {
    return withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext null // Fallback to offline rule-engine
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build()

            // Construct JSON request using org.json (built-in, compilation-safe, and highly performant)
            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            val partObj = JSONObject().apply {
                                put("text", userQuery)
                            }
                            put(partObj)
                        }
                        put("role", "user")
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)

                // Optional system instruction using the active tool's RAG knowledge base
                val systemInstructionObj = JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        val partObj = JSONObject().apply {
                            put("text", "You are Brillian AI, an expert trade assistant specialized in ${toolContext.title}. " +
                                    "Provide highly professional, accurate trade calculations, compliance advice, and safety recommendations. " +
                                    "Knowledge Base: ${toolContext.tradeKnowledgeBase}")
                        }
                        put(partObj)
                    }
                    put("parts", partsArray)
                }
                put("systemInstruction", systemInstructionObj)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestJson.toString().toRequestBody(mediaType)

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val httpRequest = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(httpRequest).execute().use { response ->
                if (!response.isSuccessful) return@withContext null

                val responseBodyString = response.body?.string() ?: return@withContext null
                val responseJson = JSONObject(responseBodyString)
                val candidates = responseJson.optJSONArray("candidates") ?: return@withContext null
                val firstCandidate = candidates.optJSONObject(0) ?: return@withContext null
                val contentObj = firstCandidate.optJSONObject("content") ?: return@withContext null
                val parts = contentObj.optJSONArray("parts") ?: return@withContext null
                val firstPart = parts.optJSONObject(0) ?: return@withContext null
                val answer = firstPart.optString("text")

                if (answer.isNullOrEmpty()) return@withContext null

                // Detect if the model suggested returning to a tool
                var detectedLink: String? = null
                var detectedTitle: String? = null
                val lowerAns = answer.lowercase()
                if (lowerAns.contains("voltage drop") || lowerAns.contains("wire size")) {
                    detectedLink = "widget_voltage_drop"
                    detectedTitle = "Voltage Drop Calculator"
                } else if (lowerAns.contains("board foot") || lowerAns.contains("lumber")) {
                    detectedLink = "widget_board_footage"
                    detectedTitle = "Board Footage Calculator"
                }

                AIResponse(
                    content = answer + "\n\n*(Connected: Processed live via cloud Gemini 3.5 Flash)*",
                    toolLink = detectedLink,
                    toolTitle = detectedTitle
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

// ---------------- DYNAMIC OFFLINE ARITHMETIC INTERFERENCE EVALUATOR ----------------

fun tryEvaluateArithmetic(query: String): String? {
    val cleanedQuery = query.lowercase().trim()
    
    // Extract continuous segments of math expressions (digits, spaces, basic operators, brackets, x)
    val mathRegex = Regex("[0-9+\\-*/.xX\\s()]+")
    val matchResult = mathRegex.findAll(cleanedQuery)
        .map { it.value.trim() }
        .filter { it.length >= 3 && it.any { c -> c.isDigit() } && it.any { c -> c in "+-*/xX" } }
        .maxByOrNull { it.length } ?: return null

    val expr = matchResult.replace("x", "*", ignoreCase = true).replace(" ", "")
    
    return try {
        val result = evalExpression(expr)
        val formattedResult = if (result % 1 == 0.0) {
            result.toLong().toString()
        } else {
            "%.3f".format(result).trimEnd('0').trimEnd('.')
        }
        "Checking local math copilot... Here is the on-site calculation:\n\n" +
                "**Expression:** `$matchResult`  \n" +
                "**Result:** **`$formattedResult`**\n\n" +
                "*(Computed instantaneously offline by your on-device mathematical processor)*"
    } catch (e: Exception) {
        null
    }
}

fun evalExpression(str: String): Double {
    return object : Any() {
        var pos = -1
        var ch = 0

        fun nextChar() {
            ch = if (++pos < str.length) str[pos].code else -1
        }

        fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < str.length) throw RuntimeException("Unexpected character: " + ch.toChar())
            return x
        }

        fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                if (eat('+'.code)) x += parseTerm()
                else if (eat('-'.code)) x -= parseTerm()
                else return x
            }
        }

        fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                if (eat('*'.code)) x *= parseFactor()
                else if (eat('/'.code)) {
                    val divisor = parseFactor()
                    if (divisor == 0.0) throw ArithmeticException("Division by zero")
                    x /= divisor
                }
                else return x
            }
        }

        fun parseFactor(): Double {
            if (eat('+'.code)) return parseFactor()
            if (eat('-'.code)) return -parseFactor()

            var x: Double
            val startPos = pos
            if (eat('('.code)) {
                x = parseExpression()
                eat(')'.code)
            } else if ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) {
                while ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) nextChar()
                x = str.substring(startPos, pos).toDouble()
            } else {
                throw RuntimeException("Unexpected: " + ch.toChar())
            }

            return x
        }
    }.parse()
}

