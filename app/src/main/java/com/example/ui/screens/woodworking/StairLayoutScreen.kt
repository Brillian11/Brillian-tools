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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Stairs
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.graphics.Path
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
fun StairLayoutScreen(
    viewModel: StairLayoutViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var totalRiseInput by remember { mutableStateOf(state.totalRiseInches.toString()) }
    var targetRiserInput by remember { mutableStateOf(state.targetRiserInches.toString()) }
    var treadRunInput by remember { mutableStateOf(state.treadRunInches.toString()) }
    var wellLengthInput by remember { mutableStateOf(state.stairOpeningWellLengthInches.toString()) }

    androidx.compose.runtime.LaunchedEffect(state.isMetric) {
        totalRiseInput = state.totalRiseInches.toString()
        targetRiserInput = state.targetRiserInches.toString()
        treadRunInput = state.treadRunInches.toString()
        wellLengthInput = state.stairOpeningWellLengthInches.toString()
    }

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
                            imageVector = Icons.Default.Stairs,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Stair Layout & Stringer Calculator",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Stringer cut geometry, step rise/run, throat depth & IRC code verification",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Unit System Selector (Imperial vs Metric)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = !state.isMetric,
                    onClick = { viewModel.setUnitSystem(false) },
                    label = { Text("Imperial (in / ft)") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = state.isMetric,
                    onClick = { viewModel.setUnitSystem(true) },
                    label = { Text("Metric (cm)") },
                    modifier = Modifier.weight(1f)
                )
            }

            // Input Parameters Card
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
                        text = "DIMENSION PARAMETERS",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = totalRiseInput,
                            onValueChange = {
                                totalRiseInput = it
                                it.toDoubleOrNull()?.let { r ->
                                    viewModel.updateInputs(
                                        r,
                                        targetRiserInput.toDoubleOrNull() ?: 7.5,
                                        treadRunInput.toDoubleOrNull() ?: 10.5,
                                        state.stringerLumberSize,
                                        wellLengthInput.toDoubleOrNull() ?: 120.0,
                                        state.upperFloorThicknessInches
                                    )
                                }
                            },
                            label = { Text(if (state.isMetric) "Total Rise (cm)" else "Total Rise (in)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = targetRiserInput,
                            onValueChange = {
                                targetRiserInput = it
                                it.toDoubleOrNull()?.let { tr ->
                                    viewModel.updateInputs(
                                        totalRiseInput.toDoubleOrNull() ?: 108.0,
                                        tr,
                                        treadRunInput.toDoubleOrNull() ?: 10.5,
                                        state.stringerLumberSize,
                                        wellLengthInput.toDoubleOrNull() ?: 120.0,
                                        state.upperFloorThicknessInches
                                    )
                                }
                            },
                            label = { Text(if (state.isMetric) "Target Riser (cm)" else "Target Riser (in)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = treadRunInput,
                            onValueChange = {
                                treadRunInput = it
                                it.toDoubleOrNull()?.let { trun ->
                                    viewModel.updateInputs(
                                        totalRiseInput.toDoubleOrNull() ?: 108.0,
                                        targetRiserInput.toDoubleOrNull() ?: 7.5,
                                        trun,
                                        state.stringerLumberSize,
                                        wellLengthInput.toDoubleOrNull() ?: 120.0,
                                        state.upperFloorThicknessInches
                                    )
                                }
                            },
                            label = { Text(if (state.isMetric) "Tread Run (cm)" else "Tread Run (in)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Stringer Lumber Stock Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Stringer Stock:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        listOf("2x12 (11.25\")", "2x10 (9.25\")").forEach { size ->
                            FilterChip(
                                selected = state.stringerLumberSize == size,
                                onClick = {
                                    viewModel.updateInputs(
                                        totalRiseInput.toDoubleOrNull() ?: 108.0,
                                        targetRiserInput.toDoubleOrNull() ?: 7.5,
                                        treadRunInput.toDoubleOrNull() ?: 10.5,
                                        size,
                                        wellLengthInput.toDoubleOrNull() ?: 120.0,
                                        state.upperFloorThicknessInches
                                    )
                                },
                                label = { Text(size) }
                            )
                        }
                    }
                }
            }

            // Visual Stringer Profile Diagram Canvas
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STRINGER ELEVATION & CUT GEOMETRY",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        )
                        Text(
                            text = "Pitch: ${String.format("%.1f", state.stairAngleDeg)}°",
                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                    ) {
                        val primaryColor = MaterialTheme.colorScheme.primary
                        val secondaryColor = MaterialTheme.colorScheme.secondary

                        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            val w = size.width
                            val h = size.height
                            val numSteps = state.stepCountRisers.coerceIn(3, 14)
                            val stepW = w / (numSteps + 1)
                            val stepH = h / (numSteps + 1)

                            // Draw Stair Steps Path
                            val stairPath = Path().apply {
                                moveTo(0f, h)
                                var curX = 0f
                                var curY = h
                                for (i in 0 until numSteps) {
                                    curY -= stepH
                                    lineTo(curX, curY) // Rise
                                    curX += stepW
                                    lineTo(curX, curY) // Tread Run
                                }
                                // Stringer bottom line
                                lineTo(curX, curY + stepH * 1.5f)
                                lineTo(stepW * 1.2f, h)
                                close()
                            }

                            drawPath(
                                path = stairPath,
                                color = primaryColor.copy(alpha = 0.25f)
                            )
                            drawPath(
                                path = stairPath,
                                color = primaryColor,
                                style = Stroke(width = 3f)
                            )

                            // Draw baseline ground & upper floor
                            drawLine(
                                color = Color.Gray,
                                start = Offset(0f, h),
                                end = Offset(w, h),
                                strokeWidth = 3f
                            )
                        }
                    }
                }
            }

            // Results Grid
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
                            text = "CALCULATED STRINGER SPECS",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        )
                        IconButton(onClick = {
                            val info = """
                                Stair Plan:
                                Risers: ${state.stepCountRisers} @ ${state.exactRiserDisplay}
                                Treads: ${state.treadCount} @ ${state.exactTreadRunDisplay}
                                Total Run: ${state.totalRunFeetInches} (${state.totalRunSubText})
                                Stringer Cut Length: ${state.stringerLengthDisplay}
                                Throat Thickness: ${state.stringerThroatDisplay}
                                Incline Angle: ${String.format("%.1f", state.stairAngleDeg)}°
                                Blondel Comfort Index: ${state.blondelDisplay}
                            """.trimIndent()
                            clipboardManager.setText(AnnotatedString(info))
                            viewModel.logStairPlan()
                            Toast.makeText(context, "Copied Stair Cut Specs!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Step Count (Risers)", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "${state.stepCountRisers} Risers",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = state.exactRiserDisplay,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                            )
                        }
                        Column {
                            Text("Tread Count", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "${state.treadCount} Treads",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            )
                            Text(
                                text = state.exactTreadRunDisplay,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Run", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = state.totalRunFeetInches,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = state.totalRunSubText,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Stringer Length", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = state.stringerLengthDisplay,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            )
                        }
                        Column {
                            Text("Throat Thickness", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = state.stringerThroatDisplay,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (state.isThroatCodeCompliant) Color(0xFF2E7D32) else Color(0xFFC62828)
                                )
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Blondel 2R+T Rule", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = state.blondelDisplay,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            )
                        }
                    }
                }
            }

            // Building Code Compliance Card (IRC)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "BUILDING CODE (IRC 2021) COMPLIANCE",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )

                    CodeCheckRow(
                        title = "Max Riser Height (${if (state.isMetric) "≤ 19.7 cm" else "≤ 7.75\""})",
                        value = state.exactRiserDisplay,
                        isPassed = state.isRiserCodeCompliant
                    )
                    CodeCheckRow(
                        title = "Min Tread Run (${if (state.isMetric) "≥ 25.4 cm" else "≥ 10.0\""})",
                        value = state.exactTreadRunDisplay,
                        isPassed = state.isTreadCodeCompliant
                    )
                    CodeCheckRow(
                        title = "Min Stringer Throat (${if (state.isMetric) "≥ 8.9 cm" else "≥ 3.5\""})",
                        value = state.stringerThroatDisplay,
                        isPassed = state.isThroatCodeCompliant
                    )
                }
            }
        }
    }
}

@Composable
fun CodeCheckRow(title: String, value: String, isPassed: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isPassed) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (isPassed) Color(0xFF2E7D32) else Color(0xFFC62828),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, style = MaterialTheme.typography.bodyMedium)
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = if (isPassed) Color(0xFF2E7D32) else Color(0xFFC62828)
            )
        )
    }
}
