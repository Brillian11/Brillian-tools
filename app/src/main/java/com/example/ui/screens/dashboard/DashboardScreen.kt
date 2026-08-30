package com.example.ui.screens.dashboard

import com.example.data.database.entity.QuickNoteEntity
import com.example.ui.utils.ToolIconMapper

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Foundation
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.ui.text.style.TextOverflow
import com.example.domain.model.ToolDefinition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.entity.DashboardWidgetEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToTool: (String) -> Unit,
    onNavigateToCustomize: () -> Unit,
    modifier: Modifier = Modifier
) {
    val widgets by viewModel.pinnedWidgets.collectAsState()
    val tasks by viewModel.quickTasks.collectAsState()
    val notes by viewModel.quickNotes.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val searchQuery by viewModel.searchQuery.collectAsState()

            // Simplified One-Row Quick Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { 
                    Text(
                        "Search 109 tools & calculators...",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    ) 
                },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_quick_search"),
                shape = RoundedCornerShape(16.dp)
            )

            if (searchQuery.isEmpty()) {
                // Minimized Material You Weather, Location & Geographical Meters Widget
                MaterialYouWeatherWidget(
                    userSettings = userSettings
                )
            }

            if (searchQuery.isNotEmpty()) {
                val searchResults = ToolDefinition.ALL_TOOLS.filter { tool ->
                    tool.matchesSearch(searchQuery)
                }

                Text(
                    text = "Catalog Search Results (${searchResults.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (searchResults.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No tools found matching \"$searchQuery\"", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                } else {
                    searchResults.forEach { tool ->
                        val visuals = ToolIconMapper.getVisualsForTool(tool.id)
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToTool(tool.id) }
                                .testTag("search_result_item_${tool.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(visuals.containerColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = visuals.icon,
                                        contentDescription = null,
                                        tint = visuals.contentColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tool.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = tool.category,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = tool.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2
                                    )
                                }
                                Button(
                                    onClick = { onNavigateToTool(tool.id) },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Open")
                                }
                            }
                        }
                    }
                }
            }

            // Render Dynamic Widget Grid
            if (widgets.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No pinned widgets on your dashboard.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = onNavigateToCustomize) {
                                Text("Add Frequent Tools")
                            }
                        }
                    }
                }
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    maxItemsInEachRow = 2
                ) {
                    widgets.forEach { widget ->
                        val isFullWidth = widget.spanSize == 2
                        val cardModifier = if (isFullWidth) {
                            Modifier.fillMaxWidth()
                        } else {
                            Modifier.fillMaxWidth(0.48f)
                        }

                        DashboardWidgetCard(
                            widget = widget,
                            onOpenTool = { onNavigateToTool(widget.id) },
                            modifier = cardModifier
                        ) {
                            when (widget.id) {
                                "widget_tasks" -> QuickTaskDashboardWidget(
                                    tasks = tasks,
                                    onAddTask = { viewModel.addTaskQuick(it) },
                                    onToggleTask = { viewModel.toggleTask(it) }
                                )
                                "widget_timer" -> MiniFocusTimerWidget(
                                    onOpenTimer = { onNavigateToTool("widget_timer") }
                                )
                                "widget_unit_converter" -> MiniConverterWidget(
                                    onOpenConverter = { onNavigateToTool("widget_unit_converter") }
                                )
                                "widget_color_palette", "widget_color_tools" -> MiniColorWidget(
                                    onOpenDevTools = { onNavigateToTool("widget_color_tools") }
                                )
                                "widget_notes" -> MiniNotesWidget(
                                    notes = notes,
                                    onOpenNotes = { onNavigateToTool("widget_notes") }
                                )
                                "widget_expense" -> MiniExpenseWidget(
                                    onQuickLog = { amt, cat -> viewModel.quickLogExpense(amt, cat) },
                                    onOpenExpense = { onNavigateToTool("widget_expense") }
                                )
                                else -> DefaultToolMiniWidget(
                                    subtitle = widget.subtitle,
                                    onOpenTool = { onNavigateToTool(widget.id) }
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
fun DashboardWidgetCard(
    widget: DashboardWidgetEntity,
    onOpenTool: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val visuals = ToolIconMapper.getVisualsForTool(widget.id)

    val cardBgColor = remember(widget.backgroundColorHex) {
        if (widget.backgroundColorHex.isNotBlank()) {
            try { Color(android.graphics.Color.parseColor(widget.backgroundColorHex)) } catch (_: Exception) { null }
        } else null
    } ?: MaterialTheme.colorScheme.surfaceVariant

    val strokeColor = remember(widget.strokeColorHex) {
        if (widget.strokeColorHex.isNotBlank()) {
            try { Color(android.graphics.Color.parseColor(widget.strokeColorHex)) } catch (_: Exception) { null }
        } else null
    } ?: MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    val borderStroke = if (widget.strokeWidthDp > 0) BorderStroke(widget.strokeWidthDp.dp, strokeColor) else null
    val displayIcon = if (widget.iconName.isNotBlank()) ToolIconMapper.getIconByName(widget.iconName) else visuals.icon

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        shape = RoundedCornerShape(24.dp),
        border = borderStroke,
        modifier = modifier
            .clickable(onClick = onOpenTool)
            .testTag("dashboard_card_${widget.id}")
    ) {
        Column {
            // Thumbnail Pattern Header Decoration if enabled
            when (widget.thumbnailPattern) {
                "gradient" -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(
                                androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    listOf(visuals.contentColor, visuals.containerColor, Color(0xFF6366F1))
                                )
                            )
                    )
                }
                "accent_banner" -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(visuals.contentColor)
                    )
                }
                "dots" -> {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .background(visuals.containerColor.copy(alpha = 0.4f))
                    ) {
                        val dotRadius = 2f
                        val spacing = 16f
                        var x = spacing / 2
                        while (x < size.width) {
                            drawCircle(visuals.contentColor.copy(alpha = 0.6f), radius = dotRadius, center = Offset(x, size.height / 2))
                            x += spacing
                        }
                    }
                }
                "glow" -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(visuals.containerColor)
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(visuals.containerColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = displayIcon,
                                contentDescription = null,
                                tint = visuals.contentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = widget.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1
                        )
                    }

                    IconButton(
                        onClick = onOpenTool,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Open ${widget.title}"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                content()
            }
        }
    }
}


@Composable
fun DefaultToolMiniWidget(
    subtitle: String,
    onOpenTool: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenTool)
    ) {
        if (subtitle.isNotBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(6.dp))
        }
        Text(
            text = "Open Tool →",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun QuickTaskDashboardWidget(
    tasks: List<com.example.data.database.entity.QuickTaskEntity>,
    onAddTask: (String) -> Unit,
    onToggleTask: (com.example.data.database.entity.QuickTaskEntity) -> Unit
) {
    var newTaskText by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newTaskText,
                onValueChange = { newTaskText = it },
                placeholder = { Text("Quick add task...") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("dashboard_quick_task_input")
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    onAddTask(newTaskText)
                    newTaskText = ""
                },
                modifier = Modifier.testTag("dashboard_add_task_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }

        val displayTasks = tasks.take(4)
        if (displayTasks.isEmpty()) {
            Text(
                text = "No active tasks. Add one above!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            displayTasks.forEach { task ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = task.isCompleted,
                        onCheckedChange = { onToggleTask(task) },
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun MiniFocusTimerWidget(onOpenTimer: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenTimer)
    ) {
        Text(
            text = "25:00",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Pomodoro Focus Work",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MiniConverterWidget(onOpenConverter: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenConverter)
    ) {
        Text("100 m = 328.08 ft", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
        Text("1 kg = 2.20 lbs", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Tap to convert any unit ->", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun MiniColorWidget(onOpenDevTools: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenDevTools)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf("#6750A4", "#7D5260", "#625B71", "#006A6A").forEach { hex ->
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(android.graphics.Color.parseColor(hex)))
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text("Hex Picker & Key Gen", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun MiniNotesWidget(
    notes: List<QuickNoteEntity>,
    onOpenNotes: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenNotes)
    ) {
        if (notes.isEmpty()) {
            Text("No notes yet. Tap to write ->", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            notes.take(2).forEach { note ->
                Text("• ${note.title}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), maxLines = 1)
            }
        }
    }
}

@Composable
fun MiniExpenseWidget(
    onQuickLog: (Double, String) -> Unit,
    onOpenExpense: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Quick Log:", style = MaterialTheme.typography.labelSmall)
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedButton(
                onClick = { onQuickLog(5.0, "Coffee/Snack") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("+$5")
            }
            OutlinedButton(
                onClick = { onQuickLog(15.0, "Lunch/Food") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("+$15")
            }
        }
    }
}

data class DayForecast(
    val dayName: String,
    val dateStr: String,
    val condition: String,
    val highTempC: Int,
    val lowTempC: Int,
    val precipPercent: Int,
    val windKmH: Int,
    val windDir: String,
    val icon: ImageVector
)

@Composable
fun MaterialYouWeatherWidget(
    userSettings: com.example.data.repository.UserSettings,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var livePressureHpa by remember { mutableStateOf(1013.2) }
    var isExpanded by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val pressureSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PRESSURE)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.values?.firstOrNull()?.let { pressure ->
                    if (pressure > 0) {
                        livePressureHpa = pressure.toDouble()
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        pressureSensor?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    val tempC = 24.5
    val tempF = tempC * 1.8 + 32.0 // 76.1
    val humidity = 62
    val dewPointC = 16.8
    val dewPointF = dewPointC * 1.8 + 32.0 // 62.2

    val isImperial = userSettings.unitSystem.equals("Imperial", ignoreCase = true)

    val primaryTempText = if (isImperial) "%.1f°F".format(tempF) else "%.1f°C".format(tempC)
    val secondaryTempText = if (isImperial) "%.1f°C".format(tempC) else "%.1f°F".format(tempF)
    val dewPointText = if (isImperial) "Dew Pt %.1f°F".format(dewPointF) else "Dew Pt %.1f°C".format(dewPointC)
    val windSpeedText = if (isImperial) "7.5 mph" else "12 km/h"
    val pressureText = if (isImperial) "%.2f inHg".format(livePressureHpa * 0.02953) else "%.1f hPa".format(livePressureHpa)

    val altitudeText = if (isImperial) {
        "%.1f ft".format(userSettings.altitudeMeters * 3.28084)
    } else {
        "%.1f m".format(userSettings.altitudeMeters)
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("material_you_weather_widget")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Location Header replacing placeholder
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = userSettings.locationName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "GPS: ${"%.4f".format(userSettings.latitude)}°, ${"%.4f".format(userSettings.longitude)}°",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = primaryTempText,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 32.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = secondaryTempText,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                        )
                        Text(
                            text = "Partly Cloudy",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.WbCloudy,
                    contentDescription = "Partly Cloudy Weather",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Geographical Meters Strip (Altitude, Pressure, AQI, Humidity, Wind)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Air,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Alt $altitudeText",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Decl ${"%.1f".format(userSettings.magneticDeclination)}°",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Field 48.2 μT",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // AQI
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AQI 42",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Good (Clean)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                // Humidity
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$humidity% Humid",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = dewPointText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                // Wind
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = null,
                            tint = Color(0xFF8B5CF6),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = windSpeedText,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Dir ENE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                // Precip & Pressure
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Grain,
                            contentDescription = null,
                            tint = Color(0xFF06B6D4),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "12% Precip",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = pressureText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Expand / Collapse Toggle Bar
            Surface(
                onClick = { isExpanded = !isExpanded },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("button_expand_weather")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isExpanded) "Hide Detailed Forecast & Sun Arc" else "Expand Forecast, Rain & Sun Arc Data",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle Forecast Details",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // EXPANDABLE WEATHER DETAILS SECTION
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. SUN MOVEMENT & SOLAR TRAJECTORY ARC
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.WbSunny,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Sun Movement & Solar Trajectory",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                Text(
                                    text = "Daylight 13h 34m",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Sun Arc Canvas Visualizer
                            SolarArcCanvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Sunrise / Sunset & Solar Data Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text(text = "Sunrise", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = "06:14 AM", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    Text(text = "Azimuth: 68° ENE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "Solar Noon", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = "13:01 PM", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    Text(text = "Elev: +72.4° (Apex)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "Sunset", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = "19:48 PM", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    Text(text = "Azimuth: 292° WNW", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                                }
                            }
                        }
                    }

                    // 2. HOURLY PRECIPITATION HOUR RADAR & PROBABILITY
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.WaterDrop,
                                        contentDescription = null,
                                        tint = Color(0xFF0284C7),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Precipitation Hour Breakdown",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                Text(
                                    text = "Peak: 2.8 mm @ 10:00",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF0284C7)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Hourly precipitation bar chart items
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val precipHours = listOf(
                                    Triple("06:00", "5%", "0.0 mm"),
                                    Triple("07:00", "10%", "0.0 mm"),
                                    Triple("08:00", "35%", "0.2 mm"),
                                    Triple("09:00", "60%", "1.5 mm"),
                                    Triple("10:00", "75%", "2.8 mm"),
                                    Triple("11:00", "40%", "0.8 mm"),
                                    Triple("12:00", "15%", "0.1 mm"),
                                    Triple("13:00", "5%", "0.0 mm"),
                                    Triple("14:00", "0%", "0.0 mm"),
                                    Triple("15:00", "0%", "0.0 mm"),
                                    Triple("16:00", "5%", "0.0 mm"),
                                    Triple("17:00", "10%", "0.0 mm"),
                                    Triple("18:00", "0%", "0.0 mm")
                                )

                                precipHours.forEach { (time, pct, vol) ->
                                    val probVal = pct.replace("%", "").toIntOrNull() ?: 0
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.width(44.dp)
                                    ) {
                                        Text(text = pct, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF0284C7))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .width(14.dp)
                                                .height(50.dp)
                                                .background(Color(0xFFE0F2FE), RoundedCornerShape(7.dp)),
                                            contentAlignment = Alignment.BottomCenter
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height((50 * (probVal / 100f)).dp.coerceAtLeast(4.dp))
                                                    .background(Color(0xFF0284C7), RoundedCornerShape(7.dp))
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = vol, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                                    }
                                }
                            }
                        }
                    }

                    // 3. HOURLY & 5-DAY WEATHER FORECAST
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "5-Day Weather Forecast Outlook",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            val dailyForecasts = listOf(
                                Tuple5("Today", "24° / 29°C", "75° / 84°F", "Partly Cloudy", "20% Precip"),
                                Tuple5("Fri", "22° / 27°C", "71° / 80°F", "Scattered Showers", "65% Precip"),
                                Tuple5("Sat", "23° / 28°C", "73° / 82°F", "Mostly Sunny", "10% Precip"),
                                Tuple5("Sun", "25° / 30°C", "77° / 86°F", "Clear Sky", "0% Precip"),
                                Tuple5("Mon", "24° / 29°C", "75° / 84°F", "Thunderstorms", "80% Precip")
                            )

                            dailyForecasts.forEach { (day, tempMetric, tempImp, cond, precip) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = day,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.width(60.dp)
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = if (cond.contains("Rain") || cond.contains("Showers") || cond.contains("Thunder")) Icons.Default.Thunderstorm else if (cond.contains("Cloudy")) Icons.Default.WbCloudy else Icons.Default.WbSunny,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = cond,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = if (isImperial) tempImp else tempMetric,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = precip,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF0284C7)
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
}

// Data helper data class for 5-day forecast layout
private data class Tuple5<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)

@Composable
fun SolarArcCanvas(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val horizonY = h * 0.75f

        // Draw Horizon Line
        drawLine(
            color = Color(0xFF94A3B8),
            start = Offset(0f, horizonY),
            end = Offset(w, horizonY),
            strokeWidth = 2f
        )

        // Draw Parabolic Sun Trajectory Arc
        val path = Path().apply {
            moveTo(w * 0.08f, horizonY)
            quadraticTo(
                w * 0.50f, -h * 0.15f, // Zenith Control Point
                w * 0.92f, horizonY
            )
        }

        drawPath(
            path = path,
            color = Color(0xFFF59E0B),
            style = Stroke(width = 4f)
        )

        // Draw Current Sun Position along Arc (approx 35% progression in morning)
        val sunX = w * 0.35f
        val sunY = horizonY - (h * 0.55f)

        // Sun Glow
        drawCircle(
            color = Color(0xFFFDE047).copy(alpha = 0.35f),
            radius = 18f,
            center = Offset(sunX, sunY)
        )

        // Sun Core
        drawCircle(
            color = Color(0xFFF59E0B),
            radius = 10f,
            center = Offset(sunX, sunY)
        )

        // Sunrise & Sunset Base Dots
        drawCircle(color = Color(0xFFEA580C), radius = 6f, center = Offset(w * 0.08f, horizonY))
        drawCircle(color = Color(0xFFC026D3), radius = 6f, center = Offset(w * 0.92f, horizonY))
    }
}



