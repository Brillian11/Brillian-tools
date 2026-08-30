package com.example.ui.screens.work

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.WorkProject
import com.example.data.repository.WorkSubtask
import com.example.data.repository.WorkTask

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WorkTrackingScreen(
    viewModel: WorkTrackingViewModel,
    modifier: Modifier = Modifier
) {
    val projects by viewModel.projects.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val subtasks by viewModel.subtasks.collectAsState()
    val timeLogs by viewModel.timeLogs.collectAsState()
    val userCurrency by viewModel.userCurrency.collectAsState()
    val isFocusActive by viewModel.isFocusModeActive.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsState()

    val activeTrackingProject by viewModel.activeTrackingProject.collectAsState()

    val appsToPause by viewModel.appsToPause.collectAsState()
    val isAppPauserEnabled by viewModel.isAppPauserEnabled.collectAsState()
    val pausedCount = remember(appsToPause) { appsToPause.count { it.isPaused } }
    var showAppPauserDialog by remember { mutableStateOf(false) }
    var customAppNameInput by remember { mutableStateOf("") }

    val context = LocalContext.current

    var selectedProjectId by remember { mutableStateOf<String?>(null) }
    var selectedProjectTab by remember { mutableStateOf(0) } // 0: Tasks, 1: Time Logs
    var showAddProjectDialog by remember { mutableStateOf(false) }
    var projectToEditState by remember { mutableStateOf<WorkProject?>(null) }
    var projectToDeleteState by remember { mutableStateOf<WorkProject?>(null) }

    var showAddManualLogDialog by remember { mutableStateOf(false) }
    var manualLogMinutes by remember { mutableStateOf("60") }
    var manualLogNote by remember { mutableStateOf("") }

    var projectFormName by remember { mutableStateOf("") }
    var projectFormClient by remember { mutableStateOf("") }
    var projectFormRate by remember { mutableStateOf("45.00") }

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var newTaskName by remember { mutableStateOf("") }

    // Pulse animation for Focus Mode Zen Loop
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Streamlined Combined Header Card (One Unified Control Bar)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Application List, Focus Mode & Target Object COMBINED IN ONE HORIZONTAL ROW
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Device App Pauser Chip
                        FilterChip(
                            selected = isAppPauserEnabled && pausedCount > 0,
                            onClick = { showAppPauserDialog = true },
                            label = { Text("⏸️ Apps ($pausedCount)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            modifier = Modifier.testTag("button_open_app_pauser")
                        )

                        // Focus Mode Chip
                        FilterChip(
                            selected = isFocusActive,
                            onClick = { viewModel.setFocusMode(!isFocusActive) },
                            label = { Text(if (isFocusActive) "✨ Focus ON" else "🎯 Focus Mode", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("button_focus_toggle")
                        )

                        // Active Target Object / Project Banner Pill
                        val currentSelectedProj = projects.find { it.id == selectedProjectId } ?: activeTrackingProject
                        val targetRateFormatted = if (currentSelectedProj != null) viewModel.formatCost(currentSelectedProj.hourlyRate) else ""
                        Surface(
                            color = if (currentSelectedProj != null) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (currentSelectedProj != null) Icons.Default.FolderSpecial else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (currentSelectedProj != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (currentSelectedProj != null)
                                        "Target: ${currentSelectedProj.name} • $targetRateFormatted/hr"
                                    else "Select Project",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (currentSelectedProj != null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        // App Pauser Shield Banner (if active)
                        if (isAppPauserEnabled && pausedCount > 0) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier.clickable { showAppPauserDialog = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.DoNotDisturbOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Shield Active: $pausedCount Paused",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

                    // Timer Display Widget
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = viewModel.formatDuration(elapsedSeconds),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = if (isTimerRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Accrued Labor Cost: ${viewModel.getAccruedCostFormatted()}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (isTimerRunning) {
                                OutlinedButton(
                                    onClick = { viewModel.pauseSession() },
                                    modifier = Modifier.testTag("button_timer_pause")
                                ) {
                                    Icon(Icons.Default.Pause, contentDescription = "Pause")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Pause")
                                }
                                Button(
                                    onClick = { viewModel.stopSession() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    modifier = Modifier.testTag("button_timer_stop")
                                ) {
                                    Icon(Icons.Default.Stop, contentDescription = "Stop")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Stop")
                                }
                            } else {
                                Button(
                                    onClick = {
                                        val projToTrack = projects.find { it.id == selectedProjectId } ?: projects.firstOrNull()
                                        if (projToTrack == null) {
                                            Toast.makeText(context, "Please create a project first!", Toast.LENGTH_SHORT).show()
                                            showAddProjectDialog = true
                                        } else {
                                            if (selectedProjectId == null) {
                                                selectedProjectId = projToTrack.id
                                            }
                                            viewModel.startSession(projToTrack)
                                        }
                                    },
                                    modifier = Modifier.testTag("button_timer_start")
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Start")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Start Track")
                                }
                            }
                        }
                    }
                }
            }

            // Project Selector Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Jobsite Projects", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                IconButton(
                    onClick = {
                        projectFormName = ""
                        projectFormClient = ""
                        projectFormRate = "45.00"
                        showAddProjectDialog = true
                    },
                    modifier = Modifier.testTag("button_add_project")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Project", tint = MaterialTheme.colorScheme.primary)
                }
            }

            // Customizable Horizontal Project Selection row
            if (projects.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "No projects active. Tap '+' to add your first customizable project!",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    projects.forEach { proj ->
                        val isSelected = selectedProjectId == proj.id
                        InputChip(
                            selected = isSelected,
                            onClick = { selectedProjectId = proj.id },
                            label = {
                                Text(
                                    text = "${proj.name}${if (proj.client.isNotBlank()) " • ${proj.client}" else ""}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                )
                            },
                            avatar = {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            projectToEditState = proj
                                            projectFormName = proj.name
                                            projectFormClient = proj.client
                                            projectFormRate = proj.hourlyRate.toString()
                                        },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Project",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(2.dp))
                                    IconButton(
                                        onClick = {
                                            projectToDeleteState = proj
                                        },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Delete Project",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.testTag("project_chip_${proj.name}")
                        )
                    }
                }
            }

            // Tasks & Time Logs Tab Section
            val currentProjId = selectedProjectId
            if (currentProjId == null) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Please select a project above to manage Tasks & Time Logs.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val projLogsCount = timeLogs.count { it.projectId == currentProjId }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedProjectTab == 0,
                        onClick = { selectedProjectTab = 0 },
                        label = { Text("📋 Tasks & Milestones", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tab_tasks")
                    )
                    FilterChip(
                        selected = selectedProjectTab == 1,
                        onClick = { selectedProjectTab = 1 },
                        label = { Text("⏱️ Time Sheet ($projLogsCount)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tab_time_logs")
                    )
                }

                if (selectedProjectTab == 0) {
                    // TAB 0: TASKS & MILESTONES
                    val projectTasks = tasks.filter { it.projectId == currentProjId }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tasks & Milestones", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        IconButton(
                            onClick = { showAddTaskDialog = true },
                            modifier = Modifier.testTag("button_add_task")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Task", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    if (projectTasks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No tasks found. Tap '+' above to add your first milestone!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(projectTasks) { task ->
                                var expandedReminderMenu by remember { mutableStateOf(false) }
                                var inlineSubtaskText by remember { mutableStateOf("") }
                                val nestedSubs = subtasks.filter { it.taskId == task.id }

                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = task.isCompleted,
                                                onCheckedChange = { viewModel.toggleTaskCompletion(task.id) }
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = task.name,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                                                ),
                                                modifier = Modifier.weight(1f)
                                            )

                                            // Reminder Schedule Bell
                                            Box {
                                                IconButton(onClick = { expandedReminderMenu = true }) {
                                                    Icon(
                                                        imageVector = if (task.hasReminder) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                                        contentDescription = "Set Reminder",
                                                        tint = if (task.hasReminder) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                                    )
                                                }
                                                DropdownMenu(
                                                    expanded = expandedReminderMenu,
                                                    onDismissRequest = { expandedReminderMenu = false }
                                                ) {
                                                    DropdownMenuItem(
                                                        text = { Text("10 Seconds (Test)") },
                                                        onClick = {
                                                            viewModel.setTaskReminder(task.id, 10)
                                                            expandedReminderMenu = false
                                                            Toast.makeText(context, "Reminder armed: 10s countdown!", Toast.LENGTH_SHORT).show()
                                                        }
                                                    )
                                                    DropdownMenuItem(
                                                        text = { Text("1 Minute") },
                                                        onClick = {
                                                            viewModel.setTaskReminder(task.id, 60)
                                                            expandedReminderMenu = false
                                                            Toast.makeText(context, "Reminder armed for 1m!", Toast.LENGTH_SHORT).show()
                                                        }
                                                    )
                                                    DropdownMenuItem(
                                                        text = { Text("5 Minutes") },
                                                        onClick = {
                                                            viewModel.setTaskReminder(task.id, 300)
                                                            expandedReminderMenu = false
                                                            Toast.makeText(context, "Reminder armed for 5m!", Toast.LENGTH_SHORT).show()
                                                        }
                                                    )
                                                    DropdownMenuItem(
                                                        text = { Text("15 Minutes") },
                                                        onClick = {
                                                            viewModel.setTaskReminder(task.id, 900)
                                                            expandedReminderMenu = false
                                                            Toast.makeText(context, "Reminder armed for 15m!", Toast.LENGTH_SHORT).show()
                                                        }
                                                    )
                                                }
                                            }

                                            IconButton(onClick = { viewModel.deleteTask(task.id) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete Task", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }

                                        // Subtask Section
                                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))

                                        if (nestedSubs.isNotEmpty()) {
                                            Column(
                                                modifier = Modifier.padding(start = 16.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                nestedSubs.forEach { sub ->
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Checkbox(
                                                            checked = sub.isCompleted,
                                                            onCheckedChange = { viewModel.toggleSubtaskCompletion(sub.id) },
                                                            modifier = Modifier.size(24.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = sub.name,
                                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                                textDecoration = if (sub.isCompleted) TextDecoration.LineThrough else null
                                                            ),
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        IconButton(
                                                            onClick = { viewModel.deleteSubtask(sub.id) },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outline)
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Add Subtask Row Inline
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 16.dp, top = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedTextField(
                                                value = inlineSubtaskText,
                                                onValueChange = { inlineSubtaskText = it },
                                                placeholder = { Text("Add nested subtask...") },
                                                singleLine = true,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(48.dp),
                                                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            IconButton(
                                                onClick = {
                                                    if (inlineSubtaskText.isNotBlank()) {
                                                        viewModel.addSubtask(task.id, inlineSubtaskText)
                                                        inlineSubtaskText = ""
                                                    }
                                                },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = "Add Subtask", tint = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // TAB 1: TIME LOGS & TIMESHEET FOR SELECTED PROJECT
                    val projectTimeLogs = timeLogs.filter { it.projectId == currentProjId }
                    val totalSecs = projectTimeLogs.sumOf { it.durationSeconds }
                    val totalCost = projectTimeLogs.sumOf { it.laborCost }

                    // Project Timesheet Overview Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(14.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Total Logged Labor",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = viewModel.formatDuration(totalSecs),
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Total Value: ${viewModel.formatCost(totalCost)}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Button(
                                onClick = {
                                    manualLogMinutes = "60"
                                    manualLogNote = ""
                                    showAddManualLogDialog = true
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("button_open_add_manual_log")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Log Time", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (projectTimeLogs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No time logs recorded for this project yet.\n\nUse the timer control above or tap '+ Log Time' to add manual work hours!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(projectTimeLogs) { log ->
                                val dateStr = java.text.SimpleDateFormat("MMM dd, yyyy • HH:mm", java.util.Locale.getDefault())
                                    .format(java.util.Date(log.timestamp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.Schedule,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = viewModel.formatDuration(log.durationSeconds),
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Surface(
                                                    color = MaterialTheme.colorScheme.primaryContainer,
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = viewModel.formatCost(log.laborCost),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "${log.note} • $dateStr",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        IconButton(onClick = { viewModel.deleteTimeLog(log.id) }) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete Time Log",
                                                tint = MaterialTheme.colorScheme.error,
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
        }

        // Delete Confirmation Dialog for Projects
        if (projectToDeleteState != null) {
            val projToDelete = projectToDeleteState!!
            AlertDialog(
                onDismissRequest = { projectToDeleteState = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Project?")
                    }
                },
                text = {
                    Text(
                        "Are you sure you want to delete '${projToDelete.name}'?\n\nThis will permanently remove all associated tasks, subtasks, and accrued labor history for this project.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteProject(projToDelete.id)
                            if (selectedProjectId == projToDelete.id) {
                                selectedProjectId = null
                            }
                            projectToDeleteState = null
                            Toast.makeText(context, "Project deleted!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete Project")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { projectToDeleteState = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Add / Edit Customizable Project Dialog
        if (showAddProjectDialog || projectToEditState != null) {
            val isEditing = projectToEditState != null
            AlertDialog(
                onDismissRequest = {
                    showAddProjectDialog = false
                    projectToEditState = null
                },
                title = { Text(if (isEditing) "Customize Project" else "Create New Project") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = projectFormName,
                            onValueChange = { projectFormName = it },
                            label = { Text("Project / Jobsite Name *") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_project_name"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = projectFormClient,
                            onValueChange = { projectFormClient = it },
                            label = { Text("Client / Category (e.g., Residential)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_project_client"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = projectFormRate,
                            onValueChange = { projectFormRate = it },
                            label = { Text("Hourly Labor Rate ($/hr)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_project_rate"),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (projectFormName.isNotBlank()) {
                                val rate = projectFormRate.toDoubleOrNull() ?: 45.0
                                if (isEditing) {
                                    val editProj = projectToEditState!!
                                    viewModel.updateProject(
                                        id = editProj.id,
                                        name = projectFormName.trim(),
                                        client = projectFormClient.trim(),
                                        hourlyRate = rate
                                    )
                                    Toast.makeText(context, "Project updated!", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.addProject(
                                        name = projectFormName.trim(),
                                        client = projectFormClient.trim(),
                                        hourlyRate = rate
                                    )
                                    selectedProjectId = projects.lastOrNull()?.id ?: selectedProjectId
                                    Toast.makeText(context, "Project created!", Toast.LENGTH_SHORT).show()
                                }
                                showAddProjectDialog = false
                                projectToEditState = null
                            }
                        }
                    ) {
                        Text(if (isEditing) "Save Changes" else "Create Project")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showAddProjectDialog = false
                            projectToEditState = null
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Add Task Dialog
        if (showAddTaskDialog) {
            AlertDialog(
                onDismissRequest = { showAddTaskDialog = false },
                title = { Text("Add Milestone Task") },
                text = {
                    OutlinedTextField(
                        value = newTaskName,
                        onValueChange = { newTaskName = it },
                        label = { Text("Task Description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_new_task"),
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val pId = selectedProjectId
                            if (newTaskName.isNotBlank() && pId != null) {
                                viewModel.addTask(pId, newTaskName)
                                newTaskName = ""
                                showAddTaskDialog = false
                                Toast.makeText(context, "Task Added!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddTaskDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Device App Pauser & Distraction Shield Dialog
        if (showAppPauserDialog) {
            AlertDialog(
                onDismissRequest = { showAppPauserDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AppBlocking, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pause Distracting Apps", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Checkmark installed device apps (${appsToPause.size}) to pause/disable notifications and access during focus work sessions.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TextButton(
                                    onClick = { viewModel.setAllAppsPaused(true) },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Check All", fontSize = 11.sp)
                                }
                                TextButton(
                                    onClick = { viewModel.setAllAppsPaused(false) },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Uncheck All", fontSize = 11.sp)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Shield", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(end = 4.dp))
                                Switch(
                                    checked = isAppPauserEnabled,
                                    onCheckedChange = { viewModel.toggleAppPauserEnabled(it) },
                                    modifier = Modifier.scale(0.85f)
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(appsToPause) { app ->
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (app.isPaused) 
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) 
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.toggleAppPauseCheckmark(app.id) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Checkbox(
                                                checked = app.isPaused,
                                                onCheckedChange = { viewModel.toggleAppPauseCheckmark(app.id) }
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text(
                                                    app.name,
                                                    fontWeight = FontWeight.SemiBold,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                Text(
                                                    app.category,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                        if (app.isPaused) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    "⏸️ Paused",
                                                    color = MaterialTheme.colorScheme.onPrimary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Add Custom App
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = customAppNameInput,
                                onValueChange = { customAppNameInput = it },
                                placeholder = { Text("Add custom app to pause...", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                            )
                            IconButton(
                                onClick = {
                                    if (customAppNameInput.isNotBlank()) {
                                        viewModel.addCustomAppToPause(customAppNameInput)
                                        customAppNameInput = ""
                                    }
                                }
                            ) {
                                Icon(Icons.Default.AddCircle, contentDescription = "Add App", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showAppPauserDialog = false }) {
                        Text("Save & Close")
                    }
                }
            )
        }

        // Add Manual Time Log Dialog
        if (showAddManualLogDialog) {
            AlertDialog(
                onDismissRequest = { showAddManualLogDialog = false },
                title = { Text("Log Work Hours Manually") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = manualLogMinutes,
                            onValueChange = { manualLogMinutes = it },
                            label = { Text("Duration (Minutes)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_manual_log_minutes"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = manualLogNote,
                            onValueChange = { manualLogNote = it },
                            label = { Text("Session Description / Note") },
                            placeholder = { Text("e.g. Foundation setup, design sprint") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_manual_log_note"),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val mins = manualLogMinutes.toLongOrNull() ?: 0L
                            val pId = selectedProjectId
                            if (mins > 0 && pId != null) {
                                viewModel.addManualTimeLog(pId, mins, manualLogNote)
                                showAddManualLogDialog = false
                                Toast.makeText(context, "Time Log Saved!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Please enter valid minutes!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("Save Time Log")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddManualLogDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Floating Active Timer Quick Control Bar Overlay Widget
        AnimatedVisibility(
            visible = (isTimerRunning || elapsedSeconds > 0) && !isFocusActive,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.inverseSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (isTimerRunning) Color.Green else Color.Yellow,
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = activeTrackingProject?.name ?: "Time Tracker",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.inverseOnSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = "${viewModel.formatDuration(elapsedSeconds)} • ${viewModel.getAccruedCostFormatted()}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primaryContainer
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                if (isTimerRunning) viewModel.pauseSession()
                                else {
                                    val proj = activeTrackingProject ?: projects.firstOrNull()
                                    if (proj != null) viewModel.startSession(proj)
                                }
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isTimerRunning) "Pause" else "Resume",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = { viewModel.stopSession() },
                            modifier = Modifier
                                .size(38.dp)
                                .background(MaterialTheme.colorScheme.error, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop & Save Log",
                                tint = MaterialTheme.colorScheme.onError,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // FOCUS MODE ZEN OVERLAY
        AnimatedVisibility(
            visible = isFocusActive,
            enter = fadeIn(animationSpec = tween(500)),
            exit = fadeOut(animationSpec = tween(500))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A)) // deep dark charcoal slate
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(28.dp)
                ) {
                    // Pulsing Lotus / Zen Centerpiece
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B).copy(alpha = 0.6f))
                            .scale(pulseScale),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = "Focusing",
                            tint = Color(0xFF10B981), // Emerald green
                            modifier = Modifier.size(80.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "FOCUS ZONE ACTIVE",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "External device alerts blocked natively.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    // Interactive timer view
                    Text(
                        text = viewModel.formatDuration(elapsedSeconds),
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF10B981)
                    )

                    Text(
                        text = "Cost Accrued: ${viewModel.getAccruedCostFormatted()}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.setFocusMode(false) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("button_exit_focus")
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Resume Communications", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}

// Simple extension helper for layout scale in overlay
fun Modifier.scale(scale: Float): Modifier = this.then(
    Modifier.graphicsLayer(
        scaleX = scale,
        scaleY = scale
    )
)
