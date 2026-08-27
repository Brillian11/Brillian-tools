package com.example.ui.screens.customize

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.entity.DashboardWidgetEntity
import com.example.ui.utils.ToolIconMapper

@Composable
fun CustomizeDashboardScreen(
    viewModel: CustomizeDashboardViewModel,
    modifier: Modifier = Modifier
) {
    val widgets by viewModel.allWidgets.collectAsState()
    var expandedWidgetId by remember { mutableStateOf<String?>(null) }

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
                text = "Customize Dashboard & Tool Cards",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Customize individual card icons, background colors, stroke widths, stroke colors, and thumbnail patterns.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(widgets, key = { _, item -> item.id }) { index, widget ->
                    val visuals = ToolIconMapper.getVisualsForTool(widget.id)
                    val isExpanded = expandedWidgetId == widget.id

                    val displayIcon = if (widget.iconName.isNotBlank()) ToolIconMapper.getIconByName(widget.iconName) else visuals.icon

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (widget.isPinned)
                                MaterialTheme.colorScheme.surfaceVariant
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customize_widget_card_${widget.id}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
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
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Pinned", style = MaterialTheme.typography.labelSmall)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Switch(
                                        checked = widget.isPinned,
                                        onCheckedChange = { viewModel.togglePin(widget) },
                                        modifier = Modifier.testTag("pin_switch_${widget.id}")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = widget.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(
                                        selected = widget.spanSize == 1,
                                        onClick = { viewModel.setSpanSize(widget, 1) },
                                        label = { Text("Half") }
                                    )
                                    FilterChip(
                                        selected = widget.spanSize == 2,
                                        onClick = { viewModel.setSpanSize(widget, 2) },
                                        label = { Text("Full") }
                                    )
                                    OutlinedButton(
                                        onClick = { expandedWidgetId = if (isExpanded) null else widget.id }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ColorLens,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Card Style", style = MaterialTheme.typography.labelSmall)
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Row {
                                    IconButton(
                                        onClick = { viewModel.moveUp(index) },
                                        enabled = index > 0
                                    ) {
                                        Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up")
                                    }
                                    IconButton(
                                        onClick = { viewModel.moveDown(index) },
                                        enabled = index < widgets.size - 1
                                    ) {
                                        Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down")
                                    }
                                }
                            }

                            // Expandable Customizer Section
                            AnimatedVisibility(visible = isExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp)
                                        .background(
                                            MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "CUSTOM CARD APPEARANCE",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        ),
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    // 1. Background Colors
                                    Text("Background Color:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        ToolIconMapper.PRESET_BACKGROUND_COLORS.forEach { (hex, name) ->
                                            val isSel = widget.backgroundColorHex == hex
                                            FilterChip(
                                                selected = isSel,
                                                onClick = {
                                                    viewModel.updateWidgetStyle(
                                                        widget.id, hex, widget.strokeColorHex,
                                                        widget.strokeWidthDp, widget.iconName, widget.thumbnailPattern
                                                    )
                                                },
                                                label = { Text(name, fontSize = 11.sp) }
                                            )
                                        }
                                    }

                                    // 2. Stroke Width
                                    Text("Border Stroke Width:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf(0 to "None (0dp)", 1 to "Thin (1dp)", 2 to "Medium (2dp)", 3 to "Thick (3dp)", 4 to "Extra Thick (4dp)").forEach { (w, name) ->
                                            FilterChip(
                                                selected = widget.strokeWidthDp == w,
                                                onClick = {
                                                    viewModel.updateWidgetStyle(
                                                        widget.id, widget.backgroundColorHex, widget.strokeColorHex,
                                                        w, widget.iconName, widget.thumbnailPattern
                                                    )
                                                },
                                                label = { Text(name, fontSize = 11.sp) }
                                            )
                                        }
                                    }

                                    // 3. Stroke Color
                                    Text("Border Stroke Color:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        ToolIconMapper.PRESET_STROKE_COLORS.forEach { (hex, name) ->
                                            val isSel = widget.strokeColorHex == hex
                                            FilterChip(
                                                selected = isSel,
                                                onClick = {
                                                    viewModel.updateWidgetStyle(
                                                        widget.id, widget.backgroundColorHex, hex,
                                                        widget.strokeWidthDp, widget.iconName, widget.thumbnailPattern
                                                    )
                                                },
                                                label = { Text(name, fontSize = 11.sp) }
                                            )
                                        }
                                    }

                                    // 4. Custom Icon Selection
                                    Text("Card Icon:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        ToolIconMapper.AVAILABLE_ICONS.forEach { iconName ->
                                            val isSel = widget.iconName == iconName
                                            val icVector = ToolIconMapper.getIconByName(iconName)
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                                    .border(
                                                        width = if (isSel) 2.dp else 0.dp,
                                                        color = if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                    .clickable {
                                                        viewModel.updateWidgetStyle(
                                                            widget.id, widget.backgroundColorHex, widget.strokeColorHex,
                                                            widget.strokeWidthDp, iconName, widget.thumbnailPattern
                                                        )
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = icVector,
                                                    contentDescription = iconName,
                                                    tint = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }

                                    // 5. Thumbnail Accent Pattern
                                    Text("Card Thumbnail Decoration:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        ToolIconMapper.THUMBNAIL_PATTERNS.forEach { (pattern, name) ->
                                            val isSel = widget.thumbnailPattern == pattern
                                            FilterChip(
                                                selected = isSel,
                                                onClick = {
                                                    viewModel.updateWidgetStyle(
                                                        widget.id, widget.backgroundColorHex, widget.strokeColorHex,
                                                        widget.strokeWidthDp, widget.iconName, pattern
                                                    )
                                                },
                                                label = { Text(name, fontSize = 11.sp) }
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
}

