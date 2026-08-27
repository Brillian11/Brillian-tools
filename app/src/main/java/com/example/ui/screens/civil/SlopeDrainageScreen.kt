package com.example.ui.screens.civil

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Water
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SlopeDrainageScreen(
    viewModel: SlopeDrainageViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val standardPipesUS = listOf(4.0, 6.0, 8.0, 10.0, 12.0, 15.0, 18.0, 24.0, 30.0, 36.0)
    val standardPipesMetric = listOf(100.0, 150.0, 200.0, 250.0, 300.0, 400.0, 500.0, 600.0, 800.0, 1000.0)
    val pipeOptions = if (state.isMetric) standardPipesMetric else standardPipesUS

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
            // Header Card
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
                            imageVector = Icons.Default.Water,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Slope, Drainage & Culvert Gradient Sizer",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Manning's pipe flow capacity, Rational runoff method (Q=CIA), trench drop & slope grade deltas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Unit Selector & Surface Catchment
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SURFACE RUNOFF COEFFICIENT",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = !state.isMetric,
                                onClick = { viewModel.setUnitSystem(false) },
                                label = { Text("US (CFS/ft)") }
                            )
                            FilterChip(
                                selected = state.isMetric,
                                onClick = { viewModel.setUnitSystem(true) },
                                label = { Text("Metric (L/s, m)") }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SurfaceType.values().forEach { surf ->
                            FilterChip(
                                selected = state.selectedSurface == surf,
                                onClick = { viewModel.setSurface(surf) },
                                label = { Text(surf.label) }
                            )
                        }
                    }

                    // Pipe Material
                    Text("CULVERT PIPE MATERIAL", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PipeMaterial.values().forEach { mat ->
                            FilterChip(
                                selected = state.selectedMaterial == mat,
                                onClick = { viewModel.setMaterial(mat) },
                                label = { Text(mat.label) }
                            )
                        }
                    }
                }
            }

            // Slope & Elevation Inputs
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "ELEVATION, RUN & DIAMETER",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = state.upstreamElevation.toString(),
                            onValueChange = { viewModel.updateInputs(upElev = it.toDoubleOrNull() ?: 0.0) },
                            label = { Text(if (state.isMetric) "Upstream Invert (m)" else "Upstream Invert (ft)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = state.downstreamElevation.toString(),
                            onValueChange = { viewModel.updateInputs(downElev = it.toDoubleOrNull() ?: 0.0) },
                            label = { Text(if (state.isMetric) "Downstream Invert (m)" else "Downstream Invert (ft)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = state.horizontalRunLength.toString(),
                            onValueChange = { viewModel.updateInputs(runLen = it.toDoubleOrNull() ?: 0.0) },
                            label = { Text(if (state.isMetric) "Horizontal Run (m)" else "Horizontal Run (ft)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = state.catchmentAreaAcresOrHa.toString(),
                            onValueChange = { viewModel.updateInputs(area = it.toDoubleOrNull() ?: 0.0) },
                            label = { Text(if (state.isMetric) "Catchment Area (ha)" else "Catchment Area (acres)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = state.rainfallIntensityInOrMmHr.toString(),
                            onValueChange = { viewModel.updateInputs(intensity = it.toDoubleOrNull() ?: 0.0) },
                            label = { Text(if (state.isMetric) "Rain Intensity (mm/hr)" else "Rain Intensity (in/hr)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Pipe Diameter Selector
                    Text("PIPE DIAMETER", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        pipeOptions.forEach { dia ->
                            FilterChip(
                                selected = state.pipeDiameterInchesOrMm == dia,
                                onClick = { viewModel.updateInputs(pipeDia = dia) },
                                label = { Text("${dia.toInt()}${if (state.isMetric) "mm" else "\""}") }
                            )
                        }
                    }
                }
            }

            // Results Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SLOPE GRADIENT & HYDRAULIC SIZING",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        IconButton(onClick = {
                            val info = "Slope & Drainage Sizing:\n" +
                                    "Elevation Delta (ΔH): ${String.format("%.2f", state.elevationDelta)} ${if (state.isMetric) "m" else "ft"}\n" +
                                    "Slope Gradient: ${String.format("%.2f", state.percentGrade)}% (Ratio ${state.slopeRatio}, ${String.format("%.2f", state.angleDegrees)}°)\n" +
                                    "Drop Rate: ${String.format("%.2f", state.dropPerFootInches)} in/ft (${String.format("%.1f", state.dropPerMeterMm)} mm/m)\n" +
                                    "Design Storm Peak Runoff (Q=CIA): ${String.format("%.2f", state.peakRunoffCfs)} CFS (${String.format("%.1f", state.peakRunoffGpm)} GPM / ${String.format("%.1f", state.peakRunoffLps)} L/s)\n" +
                                    "Pipe Flow Capacity (Manning's): ${String.format("%.2f", state.pipeCapacityCfs)} CFS (${String.format("%.1f", state.pipeCapacityGpm)} GPM / ${String.format("%.1f", state.pipeCapacityLps)} L/s)\n" +
                                    "Flow Velocity: ${String.format("%.2f", state.flowVelocityFtS)} ft/s (${String.format("%.2f", state.flowVelocityMS)} m/s)\n" +
                                    "Capacity Adequacy: ${if (state.isAdequateForRunoff) "PASS (Adequate)" else "FAIL (Undersized Pipe)"}"
                            clipboardManager.setText(AnnotatedString(info))
                            viewModel.logCalculation()
                            Toast.makeText(context, "Copied Drainage Specs!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Slope Gradient", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                text = "${String.format("%.2f", state.percentGrade)}%",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Trench Drop Rate", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                text = if (state.isMetric) "${String.format("%.1f", state.dropPerMeterMm)} mm/m" else "${String.format("%.3f", state.dropPerFootInches)}\"/ft",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Pipe Flow Capacity", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                text = "${String.format("%.2f", state.pipeCapacityCfs)} CFS (${String.format("%.0f", state.pipeCapacityGpm)} GPM)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Peak Storm Runoff", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                text = "${String.format("%.2f", state.peakRunoffCfs)} CFS (${String.format("%.0f", state.peakRunoffGpm)} GPM)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            )
                        }
                    }

                    // Adequacy Badge
                    val pipeUnit = if (state.isMetric) "mm" else "in"
                    val gradeText = String.format("%.2f", state.percentGrade)
                    Surface(
                        color = if (state.isAdequateForRunoff) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (state.isAdequateForRunoff) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (state.isAdequateForRunoff) Color(0xFF2E7D32) else Color(0xFFC62828),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (state.isAdequateForRunoff) "Pipe Sizing Adequate (${state.pipeDiameterInchesOrMm.toInt()} $pipeUnit @ $gradeText%)" else "Undersized Pipe (Exceeds Peak Runoff)",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (state.isAdequateForRunoff) Color(0xFF2E7D32) else Color(0xFFC62828)
                                    )
                                )
                                Text(
                                    text = "Flow velocity is ${String.format("%.1f", state.flowVelocityFtS)} ft/s (${String.format("%.2f", state.flowVelocityMS)} m/s). ${if (state.velocitySelfCleaningOk) "Meets self-cleaning scour velocity (≥2 ft/s)." else "Warning: Sub-2 ft/s may cause sediment buildup."}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (state.isAdequateForRunoff) Color(0xFF2E7D32) else Color(0xFFC62828)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
