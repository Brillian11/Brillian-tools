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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Terrain
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CutFillEarthworkScreen(
    viewModel: CutFillEarthworkViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

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
                            imageVector = Icons.Default.Terrain,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cut & Fill Earthwork Volume",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Excavation trenches, grading cut/fill, pit basement volumes, swell/shrinkage & hauling trucks",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Mode & Units
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
                            text = "EARTHWORK METHOD",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = !state.isMetric,
                                onClick = { viewModel.setUnitSystem(false) },
                                label = { Text("US (yd³/ft)") }
                            )
                            FilterChip(
                                selected = state.isMetric,
                                onClick = { viewModel.setUnitSystem(true) },
                                label = { Text("Metric (m³/m)") }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EarthworkMode.values().forEach { mode ->
                            FilterChip(
                                selected = state.mode == mode,
                                onClick = { viewModel.setMode(mode) },
                                label = { Text(mode.label) }
                            )
                        }
                    }

                    // Soil Type
                    Text("SOIL / MATERIAL TYPE (SWELL & SHRINKAGE)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SoilType.values().forEach { soil ->
                            FilterChip(
                                selected = state.soilType == soil,
                                onClick = { viewModel.setSoilType(soil) },
                                label = { Text("${soil.label} (+${soil.swellPct.toInt()}% / -${soil.shrinkPct.toInt()}%)") }
                            )
                        }
                    }
                }
            }

            // Input Parameters
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "DIMENSIONS & PROFILE SPECIFICATIONS",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )

                    when (state.mode) {
                        EarthworkMode.TRENCH -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = state.trenchLength.toString(),
                                    onValueChange = { viewModel.updateInputs(tLength = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text(if (state.isMetric) "Length (m)" else "Length (ft)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = state.trenchDepth.toString(),
                                    onValueChange = { viewModel.updateInputs(tDepth = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text(if (state.isMetric) "Depth (m)" else "Depth (ft)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = state.trenchTopWidth.toString(),
                                    onValueChange = { viewModel.updateInputs(tTopW = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text(if (state.isMetric) "Top Width (m)" else "Top Width (ft)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = state.trenchBottomWidth.toString(),
                                    onValueChange = { viewModel.updateInputs(tBotW = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text(if (state.isMetric) "Bottom Width (m)" else "Bottom Width (ft)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = state.gravelBeddingDepthInOrCm.toString(),
                                    onValueChange = { viewModel.updateInputs(gravelBed = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text(if (state.isMetric) "Gravel Bedding (cm)" else "Gravel Bedding (in)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = state.dumpTruckCapacityCuYdOrM3.toString(),
                                    onValueChange = { viewModel.updateInputs(truckCap = it.toDoubleOrNull() ?: 12.0) },
                                    label = { Text(if (state.isMetric) "Truck Capacity (m³)" else "Truck Capacity (yd³)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        EarthworkMode.AVERAGE_END_AREA -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = state.station1CutArea.toString(),
                                    onValueChange = { viewModel.updateInputs(s1Cut = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text(if (state.isMetric) "Station 1 Area (m²)" else "Station 1 Area (sq.ft)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = state.station2CutArea.toString(),
                                    onValueChange = { viewModel.updateInputs(s2Cut = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text(if (state.isMetric) "Station 2 Area (m²)" else "Station 2 Area (sq.ft)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = state.stationDistance.toString(),
                                    onValueChange = { viewModel.updateInputs(sDist = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text(if (state.isMetric) "Distance (m)" else "Distance (ft)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = state.dumpTruckCapacityCuYdOrM3.toString(),
                                    onValueChange = { viewModel.updateInputs(truckCap = it.toDoubleOrNull() ?: 12.0) },
                                    label = { Text(if (state.isMetric) "Truck Capacity (m³)" else "Truck Capacity (yd³)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        EarthworkMode.PIT_BASEMENT -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = state.pitLength.toString(),
                                    onValueChange = { viewModel.updateInputs(pLen = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text(if (state.isMetric) "Length (m)" else "Length (ft)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = state.pitWidth.toString(),
                                    onValueChange = { viewModel.updateInputs(pWid = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text(if (state.isMetric) "Width (m)" else "Width (ft)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = state.pitDepth.toString(),
                                    onValueChange = { viewModel.updateInputs(pDep = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text(if (state.isMetric) "Depth (m)" else "Depth (ft)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = state.pitClearanceMargin.toString(),
                                    onValueChange = { viewModel.updateInputs(pClear = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text(if (state.isMetric) "Over-dig Margin (m)" else "Over-dig Margin (ft)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        EarthworkMode.EMBANKMENT_BERM -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = state.bermLength.toString(),
                                    onValueChange = { viewModel.updateInputs(bLen = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text(if (state.isMetric) "Length (m)" else "Length (ft)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = state.bermHeight.toString(),
                                    onValueChange = { viewModel.updateInputs(bHeight = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text(if (state.isMetric) "Height (m)" else "Height (ft)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = state.bermCrestWidth.toString(),
                                    onValueChange = { viewModel.updateInputs(bCrest = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text(if (state.isMetric) "Crest Top Width (m)" else "Crest Top Width (ft)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }
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
                            text = "EARTHWORK VOLUMES & HAUL ESTIMATE",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        IconButton(onClick = {
                            val info = "Earthwork (${state.mode.label}):\n" +
                                    "Bank Volume (In-Situ): ${String.format("%.2f", state.bankVolumeCuYards)} yd³ (${String.format("%.2f", state.bankVolumeCuMeters)} m³)\n" +
                                    "Loose Volume (+${state.soilType.swellPct.toInt()}% swell): ${String.format("%.2f", state.looseVolumeCuYards)} yd³ (${String.format("%.2f", state.looseVolumeCuMeters)} m³)\n" +
                                    "Compacted Volume (-${state.soilType.shrinkPct.toInt()}% shrink): ${String.format("%.2f", state.compactedVolumeCuYards)} yd³\n" +
                                    "Total Hauling Truckloads (@ ${state.dumpTruckCapacityCuYdOrM3.toInt()}${if (state.isMetric) "m³" else "yd³"}): ${state.dumpTruckLoads} trucks\n" +
                                    "Soil Weight: ~${String.format("%.1f", state.soilWeightTons)} Tons\n" +
                                    "Gravel Bedding: ~${String.format("%.1f", state.gravelBackfillTons)} Tons"
                            clipboardManager.setText(AnnotatedString(info))
                            viewModel.logCalculation()
                            Toast.makeText(context, "Copied Earthwork Volume!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Loose Volume (Haul Away)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                text = "${String.format("%.1f", state.looseVolumeCuYards)} yd³",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Loose Metric Volume", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                text = "${String.format("%.1f", state.looseVolumeCuMeters)} m³",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Bank In-Situ Volume", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                text = "${String.format("%.2f", state.bankVolumeCuYards)} yd³ (${String.format("%.2f", state.bankVolumeCuMeters)} m³)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Compacted Fill Volume", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                text = "${String.format("%.2f", state.compactedVolumeCuYards)} yd³ (${String.format("%.2f", state.compactedVolumeCuMeters)} m³)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            )
                        }
                    }

                    // Truckloads & Bedding
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "${state.dumpTruckLoads} Dump Truckloads Required",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Based on ${state.dumpTruckCapacityCuYdOrM3.toInt()}${if (state.isMetric) "m³" else "yd³"} trucks with ${state.soilType.label} (+${state.soilType.swellPct.toInt()}% swell).",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
