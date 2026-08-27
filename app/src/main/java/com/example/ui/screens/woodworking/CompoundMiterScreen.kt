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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CropRotate
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Hexagon
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
fun CompoundMiterScreen(
    viewModel: CompoundMiterViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var cornerAngleInput by remember { mutableStateOf(state.wallCornerAngle.toString()) }
    var radiusInput by remember { mutableStateOf(state.outerRadiusInches.toString()) }

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
                            imageVector = Icons.Default.CropRotate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Compound Miter & Bevel Calculator",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Miter saw table & bevel tilt angles for crown molding & multi-sided frames",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Tab Selector
            TabRow(
                selectedTabIndex = state.mode,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = state.mode == 0,
                    onClick = { viewModel.setMode(0) },
                    text = { Text("Crown Molding (Flat on Saw)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Handyman, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_crown_molding")
                )
                Tab(
                    selected = state.mode == 1,
                    onClick = { viewModel.setMode(1) },
                    text = { Text("Polyhedral & Segmented", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Hexagon, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_polyhedral")
                )
            }

            if (state.mode == 0) {
                // Crown Molding Mode
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
                            text = "CROWN SPRING ANGLE (WALL TO CEILING)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(38.0, 45.0, 52.0).forEach { spr ->
                                val label = when (spr) {
                                    38.0 -> "38/52° (Standard)"
                                    45.0 -> "45/45° (Equal)"
                                    else -> "52/38° (Custom)"
                                }
                                FilterChip(
                                    selected = state.crownSpringAngle == spr,
                                    onClick = { viewModel.updateCrownInputs(spr, state.wallCornerAngle, state.isInsideCorner) },
                                    label = { Text(label) }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = cornerAngleInput,
                            onValueChange = {
                                cornerAngleInput = it
                                it.toDoubleOrNull()?.let { ang ->
                                    viewModel.updateCrownInputs(state.crownSpringAngle, ang, state.isInsideCorner)
                                }
                            },
                            label = { Text("Wall Corner Angle (degrees, 90° standard)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Crown Results
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
                                text = "EXACT SAW DIAL SETTINGS",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            )
                            IconButton(onClick = {
                                val info = "Crown Saw Settings (${state.crownSpringAngle}° Spring):\nMiter Saw Table Angle: ${String.format("%.2f", state.crownMiterAngle)}°\nBevel Saw Tilt Angle: ${String.format("%.2f", state.crownBevelAngle)}°"
                                clipboardManager.setText(AnnotatedString(info))
                                viewModel.logMiterPlan()
                                Toast.makeText(context, "Copied Crown Angles!", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Miter Saw Table Angle", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = "${String.format("%.2f", state.crownMiterAngle)}°",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Bevel Saw Tilt Angle", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = "${String.format("%.2f", state.crownBevelAngle)}°",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        // Cutting Positioning Guide
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Inside Corner Left Side: Miter Right ${String.format("%.1f", state.crownMiterAngle)}°, Bevel Left ${String.format("%.1f", state.crownBevelAngle)}°, Top against fence", style = MaterialTheme.typography.bodySmall)
                                Text("Inside Corner Right Side: Miter Left ${String.format("%.1f", state.crownMiterAngle)}°, Bevel Left ${String.format("%.1f", state.crownBevelAngle)}°, Bottom against fence", style = MaterialTheme.typography.bodySmall)
                                Text("Outside Corner Left Side: Miter Left ${String.format("%.1f", state.crownMiterAngle)}°, Bevel Left ${String.format("%.1f", state.crownBevelAngle)}°, Bottom against fence", style = MaterialTheme.typography.bodySmall)
                                Text("Outside Corner Right Side: Miter Right ${String.format("%.1f", state.crownMiterAngle)}°, Bevel Left ${String.format("%.1f", state.crownBevelAngle)}°, Top against fence", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            } else {
                // Polyhedral & Segmented Mode
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
                            text = "NUMBER OF SIDES (POLYGON)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(4, 6, 8, 12, 16).forEach { sides ->
                                val name = when (sides) {
                                    4 -> "4 Square"
                                    6 -> "6 Hex"
                                    8 -> "8 Octagon"
                                    12 -> "12 Dodecagon"
                                    else -> "16 Ring"
                                }
                                FilterChip(
                                    selected = state.numberOfSides == sides,
                                    onClick = { viewModel.updatePolyhedralInputs(sides, state.wallSlopeFlareAngle, state.outerRadiusInches) },
                                    label = { Text(name) }
                                )
                            }
                        }

                        // Flare Slope Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Wall Slope / Flare Angle", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text("${state.wallSlopeFlareAngle.toInt()}° from vertical", style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary))
                        }
                        Slider(
                            value = state.wallSlopeFlareAngle.toFloat(),
                            onValueChange = { viewModel.updatePolyhedralInputs(state.numberOfSides, it.toDouble(), state.outerRadiusInches) },
                            valueRange = 0f..45f,
                            steps = 9,
                            modifier = Modifier.testTag("slope_slider")
                        )

                        OutlinedTextField(
                            value = radiusInput,
                            onValueChange = {
                                radiusInput = it
                                it.toDoubleOrNull()?.let { r ->
                                    viewModel.updatePolyhedralInputs(state.numberOfSides, state.wallSlopeFlareAngle, r)
                                }
                            },
                            label = { Text("Outer Radius / Frame Radius (inches)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Polyhedral Results Card
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
                                text = "SEGMENT CUT ANGLES",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            )
                            IconButton(onClick = {
                                val info = "${state.numberOfSides}-Sided Polyhedral Plan:\nCompound Miter: ${String.format("%.2f", state.compoundMiterAngle)}°\nBevel Tilt: ${String.format("%.2f", state.compoundBevelAngle)}°\nSegment Outer Length: ${String.format("%.2f", state.segmentLengthInches)}\""
                                clipboardManager.setText(AnnotatedString(info))
                                viewModel.logMiterPlan()
                                Toast.makeText(context, "Copied Polyhedral Angles!", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Compound Miter", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = "${String.format("%.2f", state.compoundMiterAngle)}°",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column {
                                Text("Bevel Tilt Angle", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = "${String.format("%.2f", state.compoundBevelAngle)}°",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Segment Length", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = "${String.format("%.2f", state.segmentLengthInches)}\"",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
