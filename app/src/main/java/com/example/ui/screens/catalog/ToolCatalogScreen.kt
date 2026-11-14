package com.example.ui.screens.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SensorsOff
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.domain.model.ToolDefinition
import com.example.ui.screens.customize.CustomizeDashboardViewModel
import com.example.ui.utils.ToolIconMapper
import com.example.domain.sensor.DeviceSensorDetector
import com.example.domain.sensor.HardwareSensorSpec
import com.example.ui.screens.sensors.SensorUnavailableDialog
import kotlinx.coroutines.launch

@Composable
fun ToolCatalogScreen(
    viewModel: CustomizeDashboardViewModel,
    onLaunchTool: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val allWidgets by viewModel.allWidgets.collectAsState()

    val searchQuery by viewModel.catalogSearchQuery.collectAsState()
    val selectedCategory by viewModel.catalogSelectedCategory.collectAsState()

    val context = LocalContext.current
    val sensorDetector = remember { DeviceSensorDetector.getInstance(context) }
    val auditReport by sensorDetector.auditReport.collectAsState()
    var hardwareFilter by remember { mutableStateOf("ALL") } // "ALL", "AVAILABLE", "UNAVAILABLE"
    var unavailableToolTarget by remember { mutableStateOf<ToolDefinition?>(null) }

    val coroutineScope = rememberCoroutineScope()

    val categoryScrollState = rememberScrollState(initial = viewModel.catalogCategoryScrollOffset)
    LaunchedEffect(categoryScrollState.value) {
        viewModel.catalogCategoryScrollOffset = categoryScrollState.value
    }

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = viewModel.catalogScrollIndex,
        initialFirstVisibleItemScrollOffset = viewModel.catalogScrollOffset
    )

    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        viewModel.catalogScrollIndex = listState.firstVisibleItemIndex
        viewModel.catalogScrollOffset = listState.firstVisibleItemScrollOffset
    }

    val categories = listOf(
        "ALL",
        "Woodworking",
        "Electrical",
        "Civil Engineering",
        "Sensors",
        "Mechanical & HVAC",
        "Plumbing & Maintenance",
        "Safety & Compliance",
        "Painting & Coating",
        "Metalworks",
        "Outdoor Activities",
        "Site & Field",
        "Field Engineering",
        "Inventory",
        "Tasks",
        "Focus",
        "Utility"
    )

    val filteredTools = ToolDefinition.ALL_TOOLS.filter { tool ->
        val matchesQuery = tool.matchesSearch(searchQuery)
        val matchesCategory = selectedCategory == "ALL" || tool.category.equals(selectedCategory, ignoreCase = true)
        val isAvailable = sensorDetector.isToolAvailable(tool.id)
        val matchesHardware = when (hardwareFilter) {
            "AVAILABLE" -> isAvailable
            "UNAVAILABLE" -> !isAvailable
            else -> true
        }
        matchesQuery && matchesCategory && matchesHardware
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { query ->
                    viewModel.setCatalogSearchQuery(query)
                    viewModel.catalogScrollIndex = 0
                    viewModel.catalogScrollOffset = 0
                    coroutineScope.launch { listState.scrollToItem(0) }
                },
                label = { Text("Search ${ToolDefinition.ALL_TOOLS.size} Tools") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("catalog_search_input")
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(categoryScrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val count = if (cat == "ALL") ToolDefinition.ALL_TOOLS.size else ToolDefinition.ALL_TOOLS.count { it.category.equals(cat, ignoreCase = true) }
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = {
                            if (selectedCategory != cat) {
                                viewModel.setCatalogSelectedCategory(cat)
                                viewModel.catalogScrollIndex = 0
                                viewModel.catalogScrollOffset = 0
                                coroutineScope.launch { listState.scrollToItem(0) }
                            }
                        },
                        label = { Text("$cat ($count)") },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    )
                }
            }

            // Hardware Readiness Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = hardwareFilter == "ALL",
                    onClick = { hardwareFilter = "ALL" },
                    label = { Text("All Status") },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                )
                FilterChip(
                    selected = hardwareFilter == "AVAILABLE",
                    onClick = { hardwareFilter = "AVAILABLE" },
                    label = { Text("Hardware Ready (${auditReport.compatibleToolsCount})") },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                )
                val missingToolsCount = auditReport.totalToolsCount - auditReport.compatibleToolsCount
                if (missingToolsCount > 0) {
                    FilterChip(
                        selected = hardwareFilter == "UNAVAILABLE",
                        onClick = { hardwareFilter = "UNAVAILABLE" },
                        label = { Text("Requires Missing Sensor ($missingToolsCount)") },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    )
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(filteredTools, key = { _, tool -> tool.id }) { index, tool ->
                    val overallIndex = ToolDefinition.ALL_TOOLS.indexOfFirst { it.id == tool.id } + 1
                    val widgetEntity = allWidgets.find { it.id == tool.id }
                    val isPinned = widgetEntity?.isPinned ?: true
                    val visuals = ToolIconMapper.getVisualsForTool(tool.id)

                    val isSensorAvailable = remember(tool.id, auditReport) { sensorDetector.isToolAvailable(tool.id) }
                    val missingSensors = remember(tool.id, auditReport) { sensorDetector.getMissingSensorsForTool(tool.id) }

                    val customBgColorHex = widgetEntity?.backgroundColorHex ?: ""
                    val customStrokeColorHex = widgetEntity?.strokeColorHex ?: ""
                    val strokeWidthDp = widgetEntity?.strokeWidthDp ?: 1
                    val customIconName = widgetEntity?.iconName ?: ""
                    val thumbnailPattern = widgetEntity?.thumbnailPattern ?: "none"

                    val baseCardBgColor = remember(customBgColorHex) {
                        if (customBgColorHex.isNotBlank()) {
                            try { Color(android.graphics.Color.parseColor(customBgColorHex)) } catch (_: Exception) { null }
                        } else null
                    } ?: MaterialTheme.colorScheme.surfaceVariant

                    val cardBgColor = if (isSensorAvailable) baseCardBgColor else baseCardBgColor.copy(alpha = 0.7f)

                    val strokeColor = remember(customStrokeColorHex, isSensorAvailable) {
                        if (!isSensorAvailable) {
                            Color(0xFFFCA5A5)
                        } else if (customStrokeColorHex.isNotBlank()) {
                            try { Color(android.graphics.Color.parseColor(customStrokeColorHex)) } catch (_: Exception) { null }
                        } else null
                    } ?: MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

                    val borderStroke = if (!isSensorAvailable) {
                        BorderStroke(1.dp, Color(0xFFFCA5A5))
                    } else if (strokeWidthDp > 0) {
                        BorderStroke(strokeWidthDp.dp, strokeColor)
                    } else null

                    val displayIcon = if (customIconName.isNotBlank()) ToolIconMapper.getIconByName(customIconName) else visuals.icon

                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardBgColor),
                        shape = RoundedCornerShape(20.dp),
                        border = borderStroke,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isSensorAvailable) {
                                    onLaunchTool(tool.id)
                                } else {
                                    unavailableToolTarget = tool
                                }
                            }
                            .testTag("tool_catalog_card_${tool.id}")
                    ) {
                        Column {
                            // Thumbnail pattern header if configured
                            when (thumbnailPattern) {
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

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSensorAvailable) visuals.containerColor else Color(0xFFFEE2E2)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isSensorAvailable) displayIcon else Icons.Default.SensorsOff,
                                        contentDescription = null,
                                        tint = if (isSensorAvailable) visuals.contentColor else Color(0xFFDC2626),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = if (selectedCategory == "ALL" && searchQuery.isBlank()) {
                                                    "#${index + 1}"
                                                } else {
                                                    "#${index + 1} (${overallIndex})"
                                                },
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = tool.category,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        if (isPinned) {
                                            Icon(
                                                imageVector = Icons.Default.PushPin,
                                                contentDescription = "Pinned",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }

                                    if (!isSensorAvailable) {
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Surface(
                                            color = Color(0xFFFEE2E2),
                                            shape = RoundedCornerShape(4.dp),
                                            border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.SensorsOff,
                                                    contentDescription = null,
                                                    tint = Color(0xFFDC2626),
                                                    modifier = Modifier.size(11.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Sensor Missing: ${missingSensors.firstOrNull()?.name?.take(22) ?: "Hardware"}",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                                    color = Color(0xFF991B1B)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = tool.title,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSensorAvailable) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = tool.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = { viewModel.togglePinById(tool.id, isPinned) },
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .size(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                                            contentDescription = if (isPinned) "Unpin Tool" else "Pin Tool",
                                            tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    if (isSensorAvailable) {
                                        FilledTonalButton(
                                            onClick = { onLaunchTool(tool.id) },
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier
                                                .defaultMinSize(minWidth = 64.dp, minHeight = 48.dp)
                                                .heightIn(min = 48.dp)
                                                .testTag("launch_tool_${tool.id}")
                                        ) {
                                            Text(
                                                text = "Open",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Icon(
                                                imageVector = Icons.Default.ChevronRight,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    } else {
                                        Button(
                                            onClick = { unavailableToolTarget = tool },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFDC2626),
                                                contentColor = Color.White
                                            ),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            modifier = Modifier
                                                .defaultMinSize(minWidth = 64.dp, minHeight = 48.dp)
                                                .heightIn(min = 48.dp)
                                                .testTag("launch_tool_${tool.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.SensorsOff,
                                                contentDescription = null,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "Unavailable",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
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

        unavailableToolTarget?.let { tool ->
            val missing = sensorDetector.getMissingSensorsForTool(tool.id)
            SensorUnavailableDialog(
                toolTitle = tool.title,
                missingSensors = missing,
                onDismiss = { unavailableToolTarget = null },
                onOpenSensorsDetector = {
                    unavailableToolTarget = null
                    onLaunchTool("widget_sensor_detector")
                }
            )
        }
    }
}


