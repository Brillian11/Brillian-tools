package com.example.ui.screens.electrical

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoxFillCapacityScreen(
    viewModel: BoxFillCapacityViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var selectedTab by remember { mutableStateOf(0) } // 0: Calculator & Box Selection, 1: NEC 314.16 Reference Table

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Junction Box Fill Calculator",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "NEC 314.16 Conductor & Device Volume Sizing",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        clipboardManager.setText(AnnotatedString(state.calculationSummary))
                        Toast.makeText(context, "Calculations copied to clipboard", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Copy Summary")
                    }
                    IconButton(onClick = {
                        viewModel.saveToLogs()
                        Toast.makeText(context, "Saved to project calculations log", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = "Save to Log")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Box Sizer & Layout") },
                    icon = { Icon(Icons.Default.SquareFoot, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Standard Box Library") },
                    icon = { Icon(Icons.Default.TableChart, contentDescription = null) }
                )
            }

            if (selectedTab == 0) {
                BoxFillMainCalculatorContent(
                    state = state,
                    viewModel = viewModel
                )
            } else {
                StandardBoxLibraryTab(
                    standardBoxes = viewModel.standardBoxes,
                    selectedBox = state.selectedStandardBox,
                    onSelectBox = {
                        viewModel.selectStandardBox(it)
                        selectedTab = 0
                    }
                )
            }
        }
    }
}

@Composable
private fun BoxFillMainCalculatorContent(
    state: BoxFillUiState,
    viewModel: BoxFillCapacityViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // 1. Live Result Card & Capacity Visualizer
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (state.isOverfilled) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    }
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "REQUIRED BOX VOLUME",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (state.isOverfilled) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${String.format("%.2f", state.totalRequiredVolumeCuIn)} cu in",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (state.isOverfilled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "(${String.format("%.1f", state.totalRequiredVolumeCm3)} cm³ / mL)",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (state.isOverfilled) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        // Status Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (state.isOverfilled) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (state.isOverfilled) Icons.Default.Warning else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (state.isOverfilled) "OVERFILLED" else "COMPLIANT",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Capacity fill progress bar
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Selected Box: ${state.selectedStandardBox?.name ?: "Custom Box"}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${String.format("%.1f", state.effectiveAvailableVolumeCuIn)} cu in (${String.format("%.1f", state.fillPercentage)}%)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        LinearProgressIndicator(
                            progress = { (state.fillPercentage / 100.0).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color = if (state.isOverfilled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2D Interactive Junction Box Visual
                    JunctionBoxVisualCanvas(
                        state = state,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    )
                }
            }
        }

        // 2. Volume Breakdown Details
        item {
            OutlinedCard(
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "NEC 314.16 Deduction Breakdown",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    BreakdownRow(
                        title = "Conductors (${state.conductors.sumOf { it.count }} wires)",
                        value = "${String.format("%.2f", state.totalConductorVolumeCuIn)} cu in"
                    )
                    BreakdownRow(
                        title = "Internal Clamps (${if (state.internalClampCount > 0) "1 allowance @ largest wire" else "None"})",
                        value = "${String.format("%.2f", state.clampAllowanceVolumeCuIn)} cu in"
                    )
                    BreakdownRow(
                        title = "Support Fittings (${state.supportFittingCount} studs)",
                        value = "${String.format("%.2f", state.supportFittingVolumeCuIn)} cu in"
                    )
                    BreakdownRow(
                        title = "Device Yokes (${state.deviceYokeCountSingle} 1-gang + ${state.deviceYokeCountDouble} 2-gang)",
                        value = "${String.format("%.2f", state.deviceAllowanceVolumeCuIn)} cu in"
                    )
                    BreakdownRow(
                        title = "Ground Conductors (${state.groundWireCount} grounds + ${state.isolatedGroundCount} isolated)",
                        value = "${String.format("%.2f", state.groundAllowanceVolumeCuIn)} cu in"
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    BreakdownRow(
                        title = "Total Required Minimum Volume",
                        value = "${String.format("%.2f", state.totalRequiredVolumeCuIn)} cu in",
                        isBold = true
                    )
                }
            }
        }

        // 3. Conductors Section (Add/Edit)
        item {
            Card(
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Conductors in Box",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            FilledTonalButton(
                                onClick = { expanded = true },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Wire", fontSize = 13.sp)
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                WireGauge.values().forEach { gauge ->
                                    DropdownMenuItem(
                                        text = { Text("${gauge.awg} (${gauge.volumeCuIn} cu in)") },
                                        onClick = {
                                            viewModel.addConductor(gauge)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (state.conductors.isEmpty()) {
                        Text(
                            text = "No conductors added. Tap '+ Add Wire' above.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        state.conductors.forEach { entry ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = entry.gauge.awg,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "${entry.gauge.volumeCuIn} cu in per wire = ${String.format("%.2f", entry.count * entry.gauge.volumeCuIn)} cu in",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { viewModel.updateConductorCount(entry.id, entry.count - 1) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease")
                                    }
                                    Text(
                                        text = "${entry.count}",
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                    IconButton(
                                        onClick = { viewModel.updateConductorCount(entry.id, entry.count + 1) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase")
                                    }
                                    IconButton(
                                        onClick = { viewModel.removeConductor(entry.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }

        // 4. Hardware, Devices & Grounds Configuration
        item {
            Card(
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Hardware, Devices & Grounds",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Internal Cable Clamps
                    CounterRow(
                        title = "Internal Cable Clamps",
                        subtitle = "1 allowance of largest conductor if present",
                        count = state.internalClampCount,
                        onCountChange = { viewModel.setInternalClamps(it) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                    // Single-Gang Device Yokes (Switches, Receptacles, GFCIs)
                    CounterRow(
                        title = "Single-Gang Device Yokes",
                        subtitle = "2 allowances each (Switches, Duplex Receptacles, GFCIs)",
                        count = state.deviceYokeCountSingle,
                        onCountChange = { viewModel.setDeviceYokes(it, state.deviceYokeCountDouble) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                    // Double-Gang Devices
                    CounterRow(
                        title = "Double-Gang Device Yokes",
                        subtitle = "4 allowances each (Ranges, Cooktop receptacles)",
                        count = state.deviceYokeCountDouble,
                        onCountChange = { viewModel.setDeviceYokes(state.deviceYokeCountSingle, it) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                    // Ground Conductors
                    CounterRow(
                        title = "Equipment Grounding Wires (EGC)",
                        subtitle = "1 allowance up to 4 grounds (+0.25 each >4)",
                        count = state.groundWireCount,
                        onCountChange = { viewModel.setGroundWires(it, state.isolatedGroundCount) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                    // Support Studs / Luminaire Hickeys
                    CounterRow(
                        title = "Luminaire Studs / Hickeys",
                        subtitle = "1 allowance each",
                        count = state.supportFittingCount,
                        onCountChange = { viewModel.setSupportFittings(it) }
                    )
                }
            }
        }

        // 5. Plaster Ring / Extension Volume
        item {
            Card(
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Mud Ring / Plaster Ring Extension",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Add volume for raised plaster rings or box extension rings (cu in)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(0.0, 3.5, 5.0, 7.3, 9.0).forEach { vol ->
                            FilterChip(
                                selected = state.plasterRingExtensionVolume == vol,
                                onClick = { viewModel.setPlasterRingExtension(vol) },
                                label = { Text(if (vol == 0.0) "None" else "+${vol}\"³") }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CounterRow(
    title: String,
    subtitle: String,
    count: Int,
    onCountChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { onCountChange(count - 1) },
                enabled = count > 0,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease")
            }
            Text(
                text = "$count",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            IconButton(
                onClick = { onCountChange(count + 1) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase")
            }
        }
    }
}

@Composable
private fun BreakdownRow(
    title: String,
    value: String,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun JunctionBoxVisualCanvas(
    state: BoxFillUiState,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .background(Color(0xFF1E1E24), RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        val w = size.width
        val h = size.height

        // Outer Metallic Box Frame
        drawRoundRect(
            color = Color(0xFF616161),
            topLeft = Offset(12f, 10f),
            size = Size(w - 24f, h - 20f),
            cornerRadius = CornerRadius(8f, 8f),
            style = Stroke(width = 3f)
        )

        // Screw mounting ears
        drawCircle(color = Color(0xFFAAAAAA), radius = 4f, center = Offset(24f, 18f))
        drawCircle(color = Color(0xFFAAAAAA), radius = 4f, center = Offset(w - 24f, 18f))
        drawCircle(color = Color(0xFFAAAAAA), radius = 4f, center = Offset(24f, h - 18f))
        drawCircle(color = Color(0xFFAAAAAA), radius = 4f, center = Offset(w - 24f, h - 18f))

        // Knockouts in background
        val koRadius = 14f
        drawCircle(
            color = Color(0xFF33333D),
            radius = koRadius,
            center = Offset(w * 0.25f, h * 0.5f),
            style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f))
        )
        drawCircle(
            color = Color(0xFF33333D),
            radius = koRadius,
            center = Offset(w * 0.75f, h * 0.5f),
            style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f))
        )

        // Draw Conductor bundles
        var startX = 35f
        val wireY = h * 0.5f
        val wireColors = listOf(Color(0xFF1E88E5), Color(0xFFE53935), Color(0xFF43A047), Color(0xFFFDD835), Color(0xFFFB8C00))

        var colorIdx = 0
        state.conductors.forEach { c ->
            val wireColor = wireColors[colorIdx % wireColors.size]
            colorIdx++

            for (i in 0 until c.count.coerceAtMost(10)) {
                drawCircle(
                    color = wireColor,
                    radius = (c.gauge.volumeCuIn * 2.5f).toFloat().coerceIn(4f, 9f),
                    center = Offset(startX, wireY)
                )
                startX += 16f
                if (startX > w - 40f) break
            }
        }

        // Draw Ground wires in Green
        for (g in 0 until state.groundWireCount.coerceAtMost(6)) {
            drawCircle(
                color = Color(0xFF00E676),
                radius = 4.5f,
                center = Offset(startX, wireY)
            )
            startX += 12f
            if (startX > w - 30f) break
        }

        // Draw Device Yoke in middle
        if (state.deviceYokeCountSingle > 0) {
            drawRoundRect(
                color = Color(0xFF78909C),
                topLeft = Offset(w * 0.45f, 16f),
                size = Size(w * 0.1f, h - 32f),
                cornerRadius = CornerRadius(3f, 3f),
                style = Stroke(width = 2f)
            )
        }
    }
}

@Composable
private fun StandardBoxLibraryTab(
    standardBoxes: List<StandardBoxSpec>,
    selectedBox: StandardBoxSpec?,
    onSelectBox: (StandardBoxSpec) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Standard Metal & Plastic Box Sizes (NEC Table 314.16(A))",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        items(standardBoxes) { box ->
            val isSelected = box.name == selectedBox?.name
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectBox(box) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                ),
                border = if (isSelected) Stroke(2f).let { null } else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = box.name,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${box.boxType} • ${box.dimensions}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${box.volumeCuIn} cu in",
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${String.format("%.0f", box.volumeCuIn * 16.387)} cm³",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
