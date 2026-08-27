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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
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
fun WoodMoistureScreen(
    viewModel: WoodMoistureViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var widthInput by remember { mutableStateOf(state.initialWidthInches.toString()) }
    var thicknessInput by remember { mutableStateOf(state.initialThicknessInches.toString()) }

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
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Wood Moisture & Shrinkage Estimator",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Tangential & radial dimensional movement based on target EMC & wood grain",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Species Selector Chips
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "SELECT WOOD SPECIES",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.speciesList) { species ->
                            FilterChip(
                                selected = state.selectedSpecies.name == species.name,
                                onClick = { viewModel.selectSpecies(species) },
                                label = { Text(species.name) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Tangential: ${state.selectedSpecies.totalTangentialPct}% | Radial: ${state.selectedSpecies.totalRadialPct}% | T/R Ratio: ${state.selectedSpecies.trRatio}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Grain Orientation Selector
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "GRAIN ORIENTATION (END-GRAIN PROFILE)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GrainOrientation.values().forEach { grain ->
                            FilterChip(
                                selected = state.grainOrientation == grain,
                                onClick = { viewModel.setGrainOrientation(grain) },
                                label = {
                                    Text(
                                        when (grain) {
                                            GrainOrientation.FLATSAWN -> "Flatsawn (Tangential)"
                                            GrainOrientation.QUARTERSAWN -> "Quartersawn (Radial)"
                                            GrainOrientation.RIFTSAWN -> "Riftsawn (45° Rings)"
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Moisture Range Sliders
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Initial Moisture Content (MC)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        Text(
                            text = "${state.initialMoisturePct.toInt()}% MC",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary)
                        )
                    }
                    Slider(
                        value = state.initialMoisturePct.toFloat(),
                        onValueChange = {
                            viewModel.updateInputs(
                                it.toDouble(),
                                state.targetMoisturePct,
                                widthInput.toDoubleOrNull() ?: 8.0,
                                thicknessInput.toDoubleOrNull() ?: 1.0
                            )
                        },
                        valueRange = 4f..35f,
                        steps = 30,
                        modifier = Modifier.testTag("initial_mc_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Target Interior Equilibrium (EMC)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        Text(
                            text = "${state.targetMoisturePct.toInt()}% MC",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.secondary)
                        )
                    }
                    Slider(
                        value = state.targetMoisturePct.toFloat(),
                        onValueChange = {
                            viewModel.updateInputs(
                                state.initialMoisturePct,
                                it.toDouble(),
                                widthInput.toDoubleOrNull() ?: 8.0,
                                thicknessInput.toDoubleOrNull() ?: 1.0
                            )
                        },
                        valueRange = 4f..20f,
                        steps = 15,
                        modifier = Modifier.testTag("target_mc_slider")
                    )
                }
            }

            // Board Dimensions Card
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
                        text = "INITIAL BOARD DIMENSIONS",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = widthInput,
                            onValueChange = {
                                widthInput = it
                                it.toDoubleOrNull()?.let { w ->
                                    viewModel.updateInputs(state.initialMoisturePct, state.targetMoisturePct, w, thicknessInput.toDoubleOrNull() ?: 1.0)
                                }
                            },
                            label = { Text("Initial Width (in)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = thicknessInput,
                            onValueChange = {
                                thicknessInput = it
                                it.toDoubleOrNull()?.let { t ->
                                    viewModel.updateInputs(state.initialMoisturePct, state.targetMoisturePct, widthInput.toDoubleOrNull() ?: 8.0, t)
                                }
                            },
                            label = { Text("Thickness (in)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Results & Shrinkage Summary
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
                            text = "SHRINKAGE & MOVEMENT FORECAST",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        )
                        IconButton(onClick = {
                            val info = """
                                Wood Moisture Forecast:
                                Species: ${state.selectedSpecies.name} (${state.grainOrientation.name})
                                Moisture Drop: ${state.initialMoisturePct}% -> ${state.targetMoisturePct}% MC (Δ${state.moistureDeltaPct}%)
                                Width Change: ${String.format("%.3f", state.widthChangeInches)}" (${String.format("%.2f", state.effectiveShrinkagePct)}%)
                                Final Width: ${String.format("%.3f", state.finalWidthInches)}" (${String.format("%.1f", state.finalWidthMm)} mm)
                                Thickness Change: ${String.format("%.3f", state.thicknessChangeInches)}"
                            """.trimIndent()
                            clipboardManager.setText(AnnotatedString(info))
                            viewModel.logMoisturePlan()
                            Toast.makeText(context, "Copied Moisture Forecast!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Width Shrinkage", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "${String.format("%.3f", state.widthChangeInches)}\"",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFFC62828)
                                )
                            )
                            Text(
                                text = "${String.format("%.2f", state.effectiveShrinkagePct)}% reduction",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Final Dry Width", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "${String.format("%.3f", state.finalWidthInches)}\"",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = "${String.format("%.1f", state.finalWidthMm)} mm",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                            )
                        }
                    }

                    // Warping / Cupping Risk Banner
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(state.warpingRiskAssessment, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
