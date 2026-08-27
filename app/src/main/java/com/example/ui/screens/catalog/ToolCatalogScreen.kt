package com.example.ui.screens.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.ToolDefinition
import com.example.ui.screens.customize.CustomizeDashboardViewModel
import com.example.ui.utils.ToolIconMapper

@Composable
fun ToolCatalogScreen(
    viewModel: CustomizeDashboardViewModel,
    onLaunchTool: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val allWidgets by viewModel.allWidgets.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }

    val categories = listOf("ALL", "Woodworking", "Civil Engineering", "Sensors", "Inventory", "Tasks", "Focus", "Utility")

    val filteredTools = ToolDefinition.ALL_TOOLS.filter { tool ->
        val matchesQuery = tool.title.contains(searchQuery, ignoreCase = true) ||
                tool.description.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == "ALL" || tool.category.equals(selectedCategory, ignoreCase = true)
        matchesQuery && matchesCategory
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Modular Tool Suite Catalog",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Tools") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("catalog_search_input")
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredTools, key = { it.id }) { tool ->
                    val widgetEntity = allWidgets.find { it.id == tool.id }
                    val isPinned = widgetEntity?.isPinned ?: true
                    val visuals = ToolIconMapper.getVisualsForTool(tool.id)

                    val customBgColorHex = widgetEntity?.backgroundColorHex ?: ""
                    val customStrokeColorHex = widgetEntity?.strokeColorHex ?: ""
                    val strokeWidthDp = widgetEntity?.strokeWidthDp ?: 1
                    val customIconName = widgetEntity?.iconName ?: ""
                    val thumbnailPattern = widgetEntity?.thumbnailPattern ?: "none"

                    val cardBgColor = remember(customBgColorHex) {
                        if (customBgColorHex.isNotBlank()) {
                            try { Color(android.graphics.Color.parseColor(customBgColorHex)) } catch (_: Exception) { null }
                        } else null
                    } ?: MaterialTheme.colorScheme.surfaceVariant

                    val strokeColor = remember(customStrokeColorHex) {
                        if (customStrokeColorHex.isNotBlank()) {
                            try { Color(android.graphics.Color.parseColor(customStrokeColorHex)) } catch (_: Exception) { null }
                        } else null
                    } ?: MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

                    val borderStroke = if (strokeWidthDp > 0) BorderStroke(strokeWidthDp.dp, strokeColor) else null
                    val displayIcon = if (customIconName.isNotBlank()) ToolIconMapper.getIconByName(customIconName) else visuals.icon

                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardBgColor),
                        shape = RoundedCornerShape(20.dp),
                        border = borderStroke,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLaunchTool(tool.id) }
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
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(visuals.containerColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = displayIcon,
                                        contentDescription = null,
                                        tint = visuals.contentColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = tool.title,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        if (isPinned) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.PushPin,
                                                contentDescription = "Pinned",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = tool.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                IconButton(
                                    onClick = { viewModel.togglePinById(tool.id, isPinned) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                                        contentDescription = if (isPinned) "Unpin Tool" else "Pin Tool",
                                        tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Button(
                                    onClick = { onLaunchTool(tool.id) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("launch_tool_${tool.id}")
                                ) {
                                    Text("Open")
                                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


