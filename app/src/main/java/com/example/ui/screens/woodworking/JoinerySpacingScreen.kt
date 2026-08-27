package com.example.ui.screens.woodworking

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Splitscreen
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun JoinerySpacingScreen(
    viewModel: JoinerySpacingViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var widthInput by remember { mutableStateOf(state.workpieceWidthInches.toString()) }
    var countInput by remember { mutableStateOf(state.jointCount.toString()) }
    var elemSizeInput by remember { mutableStateOf(state.elementSizeInches.toString()) }
    var fixedEdgeInput by remember { mutableStateOf(state.fixedEdgeMarginInches.toString()) }

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
            // Header
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Splitscreen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Joinery & Tenon Spacing Calculator",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Equal spacing distributions for mortises, dominoes, dowels & pocket screws",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Joinery Type Selector
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "SELECT JOINERY FASTENER TYPE",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        JoineryType.values().forEach { type ->
                            val label = when (type) {
                                JoineryType.MORTISE_TENON -> "Mortise / Domino"
                                JoineryType.DOWELS -> "Dowel Pins"
                                JoineryType.POCKET_HOLES -> "Pocket Screws"
                                JoineryType.BOX_JOINTS -> "Finger Joints"
                            }
                            FilterChip(
                                selected = state.joineryType == type,
                                onClick = {
                                    viewModel.setJoineryType(type)
                                    elemSizeInput = state.elementSizeInches.toString()
                                },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }

            // Spacing Mode Selector
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "SPACING DISTRIBUTION PATTERN",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.spacingMode == SpacingDistributionMode.EQUAL_DIVISIONS,
                            onClick = { viewModel.setSpacingMode(SpacingDistributionMode.EQUAL_DIVISIONS) },
                            label = { Text("Equal Divisions (Edge = Pitch / 2)") }
                        )
                        FilterChip(
                            selected = state.spacingMode == SpacingDistributionMode.FIXED_EDGE_MARGIN,
                            onClick = { viewModel.setSpacingMode(SpacingDistributionMode.FIXED_EDGE_MARGIN) },
                            label = { Text("Fixed Edge Margins") }
                        )
                    }
                }
            }

            // Workpiece & Joint Parameters
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
                        text = "WORKPIECE & FASTENER DIMENSIONS",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = widthInput,
                            onValueChange = {
                                widthInput = it
                                it.toDoubleOrNull()?.let { w ->
                                    viewModel.updateInputs(
                                        w,
                                        state.workpieceThicknessInches,
                                        countInput.toIntOrNull() ?: 3,
                                        elemSizeInput.toDoubleOrNull() ?: 1.5,
                                        fixedEdgeInput.toDoubleOrNull() ?: 1.5
                                    )
                                }
                            },
                            label = { Text("Workpiece Width (in)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = countInput,
                            onValueChange = {
                                countInput = it
                                it.toIntOrNull()?.let { c ->
                                    viewModel.updateInputs(
                                        widthInput.toDoubleOrNull() ?: 18.0,
                                        state.workpieceThicknessInches,
                                        c,
                                        elemSizeInput.toDoubleOrNull() ?: 1.5,
                                        fixedEdgeInput.toDoubleOrNull() ?: 1.5
                                    )
                                }
                            },
                            label = { Text("Joint Count (Qty)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(0.8f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = elemSizeInput,
                            onValueChange = {
                                elemSizeInput = it
                                it.toDoubleOrNull()?.let { es ->
                                    viewModel.updateInputs(
                                        widthInput.toDoubleOrNull() ?: 18.0,
                                        state.workpieceThicknessInches,
                                        countInput.toIntOrNull() ?: 3,
                                        es,
                                        fixedEdgeInput.toDoubleOrNull() ?: 1.5
                                    )
                                }
                            },
                            label = { Text("Joint Width/Dia (in)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        if (state.spacingMode == SpacingDistributionMode.FIXED_EDGE_MARGIN) {
                            OutlinedTextField(
                                value = fixedEdgeInput,
                                onValueChange = {
                                    fixedEdgeInput = it
                                    it.toDoubleOrNull()?.let { fe ->
                                        viewModel.updateInputs(
                                            widthInput.toDoubleOrNull() ?: 18.0,
                                            state.workpieceThicknessInches,
                                            countInput.toIntOrNull() ?: 3,
                                            elemSizeInput.toDoubleOrNull() ?: 1.5,
                                            fe
                                        )
                                    }
                                },
                                label = { Text("Edge Margin (in)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Visual Joint Layout Diagram Canvas
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "WORKPIECE JOINERY LAYOUT",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                    ) {
                        val primaryColor = MaterialTheme.colorScheme.primary
                        val secondaryColor = MaterialTheme.colorScheme.secondary

                        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            val w = size.width
                            val h = size.height
                            val totalWidth = state.workpieceWidthInches

                            // Draw Workpiece Board
                            drawRoundRect(
                                color = Color(0xFFD7CCC8), // Light wood color
                                topLeft = Offset(0f, 20f),
                                size = Size(w, h - 40f),
                                cornerRadius = CornerRadius(8f, 8f)
                            )
                            drawRoundRect(
                                color = Color(0xFF8D6E63),
                                topLeft = Offset(0f, 20f),
                                size = Size(w, h - 40f),
                                cornerRadius = CornerRadius(8f, 8f),
                                style = Stroke(width = 2f)
                            )

                            // Draw Joinery Elements at exact normalized coordinates
                            state.centerlineCoordinatesInches.forEach { coordInches ->
                                val normalizedX = (coordInches / totalWidth).toFloat() * w
                                val elemW = ((state.elementSizeInches / totalWidth).toFloat() * w).coerceAtLeast(12f)
                                val elemH = h - 60f

                                // Draw Mortise / Dowel slot
                                drawRoundRect(
                                    color = primaryColor,
                                    topLeft = Offset(normalizedX - elemW / 2f, (h - elemH) / 2f),
                                    size = Size(elemW, elemH),
                                    cornerRadius = CornerRadius(6f, 6f)
                                )

                                // Draw Centerline tick mark
                                drawLine(
                                    color = Color.Red,
                                    start = Offset(normalizedX, 0f),
                                    end = Offset(normalizedX, h),
                                    strokeWidth = 2f
                                )
                            }
                        }
                    }
                }
            }

            // Results & Centerline Coordinates Grid
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
                            text = "CALCULATED SPACING & DRILL MARKS",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        )
                        IconButton(onClick = {
                            val marks = state.centerlineCoordinatesInches.mapIndexed { idx, coord ->
                                "Joint #${idx + 1}: ${String.format("%.3f", coord)}\" (${String.format("%.1f", state.centerlineCoordinatesMm[idx])} mm)"
                            }.joinToString("\n")
                            val info = "Joinery Spacing Plan (${state.joineryType.name}):\nPitch: ${String.format("%.3f", state.centerToCenterSpacingInches)}\"\nEdge Margin: ${String.format("%.3f", state.edgeMarginInches)}\"\n\nCenterline Marks from Reference Edge:\n$marks"
                            clipboardManager.setText(AnnotatedString(info))
                            viewModel.logJoineryPlan()
                            Toast.makeText(context, "Copied Drill Coordinates!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Center-to-Center (Pitch)", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "${String.format("%.3f", state.centerToCenterSpacingInches)}\"",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                        Column {
                            Text("Edge Margin", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "${String.format("%.3f", state.edgeMarginInches)}\"",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Gap Between", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "${String.format("%.3f", state.gapBetweenElementsInches)}\"",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Centerline Drill / Router Fence Marks from Left Edge:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))

                    state.centerlineCoordinatesInches.forEachIndexed { index, coord ->
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Joint #${index + 1} Centerline", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                Text(
                                    text = "${String.format("%.3f", coord)}\" (${String.format("%.1f", state.centerlineCoordinatesMm[index])} mm)",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
