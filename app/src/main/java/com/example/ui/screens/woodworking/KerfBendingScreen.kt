package com.example.ui.screens.woodworking

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.math.KerfBendingResult

import androidx.compose.ui.draw.clip
import com.example.ui.utils.ToolIconMapper

@Composable
fun KerfBendingScreen(
    viewModel: KerfBendingViewModel,
    modifier: Modifier = Modifier
) {
    val isMultiSection by viewModel.isMultiSectionMode.collectAsState()
    val boardLength by viewModel.boardLength.collectAsState()
    val sectionsList by viewModel.sectionsList.collectAsState()
    val thickness by viewModel.boardThickness.collectAsState()
    val kerf by viewModel.bladeKerf.collectAsState()
    val radius by viewModel.targetRadius.collectAsState()
    val angle by viewModel.bendAngle.collectAsState()
    val veneer by viewModel.veneerAllowance.collectAsState()
    val result by viewModel.result.collectAsState()
    val multiResult by viewModel.multiResult.collectAsState()
    val visuals = ToolIconMapper.getVisualsForTool("widget_kerf_bending")

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title & Description
            com.example.ui.components.ToolInfoBox(
                icon = visuals.icon,
                title = "Kerf Bending & Radial Cuts",
                description = "Calculates exact cut spacing, depth, and pass count to bend solid timber or plywood across single or multiple kerfed bend zones on 1 board."
            )

            // Mode Selector Switch (Single Bend vs Multi-Section Bend)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                androidx.compose.material3.FilterChip(
                    selected = !isMultiSection,
                    onClick = { viewModel.toggleMultiSectionMode(false) },
                    label = { Text("Single Curve Bend") },
                    modifier = Modifier.weight(1f).testTag("chip_single_kerf")
                )
                androidx.compose.material3.FilterChip(
                    selected = isMultiSection,
                    onClick = { viewModel.toggleMultiSectionMode(true) },
                    label = { Text("Multi-Section Kerf (e.g. 1m Board)") },
                    modifier = Modifier.weight(1f).testTag("chip_multi_kerf")
                )
            }

            // Warning Banner if any
            val activeWarning = if (isMultiSection) multiResult.warningMessage else result.warningMessage
            activeWarning?.let { warning ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = warning,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Global Blade & Board Inputs
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Board & Saw Blade Properties",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    if (isMultiSection) {
                        OutlinedTextField(
                            value = boardLength,
                            onValueChange = { viewModel.updateBoardLength(it) },
                            label = { Text("Total Board Length (e.g. 1000 mm = 1 meter)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_board_length")
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = thickness,
                            onValueChange = { viewModel.updateBoardThickness(it) },
                            label = { Text("Board Thickness (mm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("input_board_thickness")
                        )
                        OutlinedTextField(
                            value = kerf,
                            onValueChange = { viewModel.updateBladeKerf(it) },
                            label = { Text("Blade Kerf (mm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("input_blade_kerf")
                        )
                    }

                    OutlinedTextField(
                        value = veneer,
                        onValueChange = { viewModel.updateVeneerAllowance(it) },
                        label = { Text("Uncut Face Veneer Remaining (mm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_veneer_allowance")
                    )
                }
            }

            if (isMultiSection) {
                // Multi-Section Bend Configuration Cards
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Kerf Bend Sections (${sectionsList.size})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            androidx.compose.material3.Button(
                                onClick = { viewModel.addKerfSection() },
                                modifier = Modifier.testTag("button_add_section")
                            ) {
                                Text("+ Add Bend Section")
                            }
                        }

                        sectionsList.forEachIndexed { idx, sec ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Kerf Section ${sec.sectionIndex}",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        if (sectionsList.size > 1) {
                                            androidx.compose.material3.TextButton(
                                                onClick = { viewModel.removeKerfSection(idx) },
                                                modifier = Modifier.testTag("button_remove_section_$idx")
                                            ) {
                                                Text("Remove", color = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedTextField(
                                            value = sec.targetRadiusMm.toString(),
                                            onValueChange = { val rVal = it.toDoubleOrNull() ?: sec.targetRadiusMm; viewModel.updateSectionParams(idx, rVal, sec.bendAngleDegrees) },
                                            label = { Text("Radius (mm)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                            value = sec.bendAngleDegrees.toString(),
                                            onValueChange = { val aVal = it.toDoubleOrNull() ?: sec.bendAngleDegrees; viewModel.updateSectionParams(idx, sec.targetRadiusMm, aVal) },
                                            label = { Text("Bend Angle (°)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Multi-Section Results Breakdown Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Multi-Section Board Summary",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ResultBadge(
                                title = "TOTAL BOARD CUTS",
                                value = "${multiResult.totalCutsAcrossBoard}",
                                unit = "cuts across board",
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            ResultBadge(
                                title = "CUT DEPTH",
                                value = String.format("%.2f", multiResult.bladeCutDepthMm),
                                unit = "mm depth",
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        multiResult.sectionResults.forEach { secRes ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "Section ${secRes.sectionIndex}: ${secRes.totalKerfCuts} cuts @ ${String.format("%.1f", secRes.spacingMm)}mm C-to-C",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Position: ${String.format("%.0f", secRes.startPositionMm)}mm to ${String.format("%.0f", secRes.endPositionMm)}mm along 1m board",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Visual Multi-Section Board Diagram
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Multi-Section Kerf Board Layout (1m Board Diagram)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        MultiSectionKerfCanvas(
                            multiResult = multiResult,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                        )
                    }
                }
            } else {
                // Single Bend Input Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Single Bend Geometry",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = radius,
                                onValueChange = { viewModel.updateTargetRadius(it) },
                                label = { Text("Inside Radius (mm)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).testTag("input_inside_radius")
                            )
                            OutlinedTextField(
                                value = angle,
                                onValueChange = { viewModel.updateBendAngle(it) },
                                label = { Text("Bend Angle (°)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).testTag("input_bend_angle")
                            )
                        }
                    }
                }

                // Single Bend Results Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Cut Specifications",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ResultBadge(
                                title = "TOTAL KERF CUTS",
                                value = "${result.totalKerfCuts}",
                                unit = "passes",
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            ResultBadge(
                                title = "CUT PITCH SPACING",
                                value = String.format("%.2f", result.centerToCenterSpacingMm),
                                unit = "mm C-to-C",
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ResultBadge(
                                title = "BLADE CUT DEPTH",
                                value = String.format("%.2f", result.bladeCutDepthMm),
                                unit = "mm depth",
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.weight(1f)
                            )
                            ResultBadge(
                                title = "MATERIAL REMOVED",
                                value = String.format("%.1f", result.totalMaterialToRemoveMm),
                                unit = "mm delta",
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Visual Cross Section Diagram
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Kerf Cut Cross-Section Diagram",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        KerfDiagramCanvas(
                            result = result,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MultiSectionKerfCanvas(
    multiResult: com.example.domain.math.MultiSectionKerfResult,
    modifier: Modifier = Modifier
) {
    val woodColor = Color(0xFFD7CCC8)
    val cutColor = Color(0xFF3E2723)
    val primaryColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val boardH = h * 0.45f
        val startY = (h - boardH) / 2f

        // Draw Full Board
        drawRect(
            color = woodColor,
            topLeft = Offset(10f, startY),
            size = Size(w - 20f, boardH)
        )

        val totalBoardLen = multiResult.totalBoardLengthMm.coerceAtLeast(100.0)
        val scale = (w - 20f) / totalBoardLen

        // Render each section kerf cuts
        multiResult.sectionResults.forEach { sec ->
            val startX = 10f + (sec.startPositionMm * scale).toFloat()
            val endX = 10f + (sec.endPositionMm * scale).toFloat()
            val secW = (endX - startX).coerceAtLeast(4f)

            // Draw Section Highlight
            drawRect(
                color = primaryColor.copy(alpha = 0.15f),
                topLeft = Offset(startX, startY),
                size = Size(secW, boardH)
            )

            // Draw Slots
            val cuts = sec.totalKerfCuts.coerceIn(1, 25)
            val slotSpacing = secW / (cuts + 1)
            val cutH = boardH * 0.85f

            for (i in 1..cuts) {
                val cx = startX + (i * slotSpacing)
                drawRect(
                    color = cutColor,
                    topLeft = Offset(cx - 1.5f, startY),
                    size = Size(3f, cutH)
                )
            }
        }
    }
}

@Composable
fun ResultBadge(
    title: String,
    value: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.12f), shape = RoundedCornerShape(12.dp))
            .border(1.dp, color.copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun KerfDiagramCanvas(
    result: KerfBendingResult,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val woodColor = Color(0xFFD7CCC8)
    val cutColor = Color(0xFF3E2723)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val boardH = h * 0.5f
        val startY = (h - boardH) / 2f

        // Draw Wood Board
        drawRect(
            color = woodColor,
            topLeft = Offset(10f, startY),
            size = Size(w - 20f, boardH)
        )

        // Draw Kerf Slots
        val cuts = result.totalKerfCuts.coerceIn(1, 40)
        val availableW = w - 40f
        val slotSpacing = availableW / (cuts + 1)
        val cutDepthRatio = (result.bladeCutDepthMm / 19.0).coerceIn(0.2, 0.95).toFloat()
        val cutH = boardH * cutDepthRatio

        for (i in 1..cuts) {
            val cx = 20f + (i * slotSpacing)
            drawRect(
                color = cutColor,
                topLeft = Offset(cx - 3f, startY),
                size = Size(6f, cutH)
            )
        }

        // Uncut Face Veneer Line
        drawLine(
            color = primaryColor,
            start = Offset(10f, startY + cutH),
            end = Offset(w - 10f, startY + cutH),
            strokeWidth = 3f
        )
    }
}
