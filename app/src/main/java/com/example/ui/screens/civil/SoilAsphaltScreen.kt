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
import androidx.compose.material.icons.filled.PrecisionManufacturing
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
fun SoilAsphaltScreen(
    viewModel: SoilAsphaltViewModel,
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
                            imageVector = Icons.Default.PrecisionManufacturing,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Soil Compaction & Asphalt Tonnage Estimator",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Converts paving area, lift depth & compacted density to US short tons, metric tonnes & dump truckloads",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Material & Units
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
                            text = "MATERIAL TYPE & DENSITY",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = !state.isMetric,
                                onClick = { viewModel.setUnitSystem(false) },
                                label = { Text("US (Tons/ft/in)") }
                            )
                            FilterChip(
                                selected = state.isMetric,
                                onClick = { viewModel.setUnitSystem(true) },
                                label = { Text("Metric (Tonnes/m/cm)") }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PavingMaterial.values().forEach { mat ->
                            FilterChip(
                                selected = state.selectedMaterial == mat,
                                onClick = { viewModel.setMaterial(mat) },
                                label = { Text(mat.label) }
                            )
                        }
                    }
                }
            }

            // Dimension Inputs
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "PAVING AREA & LIFT THICKNESS",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = state.lengthFtOrM.toString(),
                            onValueChange = { viewModel.updateInputs(length = it.toDoubleOrNull() ?: 0.0) },
                            label = { Text(if (state.isMetric) "Length (m)" else "Length (ft)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = state.widthFtOrM.toString(),
                            onValueChange = { viewModel.updateInputs(width = it.toDoubleOrNull() ?: 0.0) },
                            label = { Text(if (state.isMetric) "Width (m)" else "Width (ft)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = state.thicknessInOrCm.toString(),
                            onValueChange = { viewModel.updateInputs(thickness = it.toDoubleOrNull() ?: 0.0) },
                            label = { Text(if (state.isMetric) "Compacted Depth (cm)" else "Compacted Depth (in)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = state.truckCapacityTons.toString(),
                            onValueChange = { viewModel.updateInputs(truckCap = it.toDoubleOrNull() ?: 15.0) },
                            label = { Text("Dump Truck Size (Tons)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Compaction Roll-Down Factor
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Compaction Roll-Down Factor:", style = MaterialTheme.typography.labelSmall)
                            Text("+${state.rollDownFactorPct.toInt()}% Loose Height", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                        Slider(
                            value = state.rollDownFactorPct.toFloat(),
                            onValueChange = { viewModel.updateInputs(rollDown = it.toDouble()) },
                            valueRange = 10f..35f,
                            steps = 4
                        )
                    }

                    // Waste Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Waste & Edge Compaction Margin:", style = MaterialTheme.typography.labelSmall)
                            Text("+${state.wastePercent.toInt()}%", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                        Slider(
                            value = state.wastePercent.toFloat(),
                            onValueChange = { viewModel.updateInputs(waste = it.toDouble()) },
                            valueRange = 0f..20f,
                            steps = 3
                        )
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
                            text = "TOTAL MATERIAL TONNAGE & TRUCKLOADS",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        IconButton(onClick = {
                            val info = "Paving & Compaction (${state.selectedMaterial.label}):\n" +
                                    "Total Area: ${String.format("%.0f", state.totalAreaSqFt)} sq.ft (${String.format("%.1f", state.totalAreaSqYds)} sq.yd / ${String.format("%.1f", state.totalAreaSqM)} m²)\n" +
                                    "Total Material Weight: ${String.format("%.2f", state.totalWeightTons)} US Short Tons (${String.format("%.2f", state.totalWeightTonnes)} Metric Tonnes)\n" +
                                    "Compacted Volume: ${String.format("%.2f", state.compactedVolumeCuYds)} yd³ (${String.format("%.2f", state.compactedVolumeCuM)} m³)\n" +
                                    "Loose Spreading Volume (+${state.rollDownFactorPct.toInt()}%): ${String.format("%.2f", state.looseVolumeCuYds)} yd³\n" +
                                    "Dump Truckloads (@ ${state.truckCapacityTons.toInt()} Tons): ${state.dumpTruckLoads} trucks\n" +
                                    "Application Rate: ~${String.format("%.1f", state.applicationRateLbsSqYd)} lbs/sq.yd\n" +
                                    (if (state.selectedMaterial.isAsphalt) "Tack Coat Emulsion: ~${String.format("%.1f", state.tackCoatGallons)} gal (${String.format("%.1f", state.tackCoatLiters)} L)\n" else "")
                            clipboardManager.setText(AnnotatedString(info))
                            viewModel.logCalculation()
                            Toast.makeText(context, "Copied Tonnage Estimate!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("US Short Tons (2,000 lbs)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                text = "${String.format("%.2f", state.totalWeightTons)} Tons",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Metric Tonnes (1,000 kg)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                text = "${String.format("%.2f", state.totalWeightTonnes)} Tonnes",
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
                            Text("Total Surface Area", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                text = "${String.format("%.0f", state.totalAreaSqFt)} ft² (${String.format("%.1f", state.totalAreaSqYds)} yd²)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Compacted Volume", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                text = "${String.format("%.2f", state.compactedVolumeCuYds)} yd³ (${String.format("%.2f", state.compactedVolumeCuM)} m³)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            )
                        }
                    }

                    // Truckload info & Tack Coat
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
                                    text = if (state.selectedMaterial.isAsphalt) {
                                        "Tack coat emulsion: ~${String.format("%.1f", state.tackCoatGallons)} gal (${String.format("%.1f", state.tackCoatLiters)} L). Application rate: ~${String.format("%.0f", state.applicationRateLbsSqYd)} lbs/yd²."
                                    } else {
                                        "Spreading volume: ${String.format("%.1f", state.looseVolumeCuYds)} yd³ loose (+${state.rollDownFactorPct.toInt()}% pre-compaction)."
                                    },
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
