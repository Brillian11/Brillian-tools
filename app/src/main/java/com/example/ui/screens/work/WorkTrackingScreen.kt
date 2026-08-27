package com.example.ui.screens.work

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
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
    val isFocusActive by viewModel.isFocusModeActive.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsState()

    val context = LocalContext.current

    var selectedProjectId by remember { mutableStateOf<String?>(null) }
    var showAddProjectDialog by remember { mutableStateOf(false) }
    var newProjectName by remember { mutableStateOf("") }

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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Focus Mode & Timer Controller Row
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Active Session & Focus", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }

                        // Focus Mode Button
                        Button(
                            onClick = { viewModel.setFocusMode(!isFocusActive) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFocusActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("button_focus_toggle")
                        ) {
                            Icon(
                                imageVector = if (isFocusActive) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isFocusActive) "Deactivate Focus" else "Enter Focus Mode")
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

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
                                    onClick = { viewModel.startSession() },
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
                    onClick = { showAddProjectDialog = true },
                    modifier = Modifier.testTag("button_add_project")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Project", tint = MaterialTheme.colorScheme.primary)
                }
            }

            // Horizontal Project Selection row
            if (projects.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "No projects active. Create a project to start tracking detailed tasks!",
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
                        .height(48.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    projects.forEach { proj ->
                        val isSelected = selectedProjectId == proj.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedProjectId = proj.id },
                            label = { Text(proj.name) },
                            modifier = Modifier.testTag("project_chip_${proj.name}"),
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        viewModel.deleteProject(proj.id)
                                        if (selectedProjectId == proj.id) {
                                            selectedProjectId = null
                                        }
                                        Toast.makeText(context, "Project deleted!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Delete Project", modifier = Modifier.size(12.dp))
                                }
                            }
                        )
                    }
                }
            }

            // Tasks List
            val currentProjId = selectedProjectId
            if (currentProjId == null) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Please select a project above to manage nested Tasks & Subtasks.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
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
                                    Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))

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
            }
        }

        // Add Project Dialog
        if (showAddProjectDialog) {
            AlertDialog(
                onDismissRequest = { showAddProjectDialog = false },
                title = { Text("Create New Project") },
                text = {
                    OutlinedTextField(
                        value = newProjectName,
                        onValueChange = { newProjectName = it },
                        label = { Text("Project / Jobsite Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_new_project"),
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newProjectName.isNotBlank()) {
                                viewModel.addProject(newProjectName)
                                selectedProjectId = projects.lastOrNull()?.id ?: selectedProjectId
                                newProjectName = ""
                                showAddProjectDialog = false
                                Toast.makeText(context, "Project Created!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddProjectDialog = false }) {
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
