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
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Straighten
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
fun RebarEstimatorScreen(
    viewModel: RebarEstimatorViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val rebarSizes = if (state.isMetric) RebarSize.METRIC_SIZES else RebarSize.US_SIZES

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
                            imageVector = Icons.Default.GridOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Rebar Spacing & Weight Estimator",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Total linear footage, weight (lbs/tonnes), lap splices, ties & grid spacing schedules",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Unit & Structure Type
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
                            text = "STRUCTURE & UNITS",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = !state.isMetric,
                                onClick = { viewModel.setUnitSystem(false) },
                                label = { Text("US Imperial (#4, ft)") }
                            )
                            FilterChip(
                                selected = state.isMetric,
                                onClick = { viewModel.setUnitSystem(true) },
                                label = { Text("Metric (12mm, m)") }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RebarStructure.values().forEach { struct ->
                            FilterChip(
                                selected = state.structure == struct,
                                onClick = { viewModel.setStructure(struct) },
                                label = { Text(struct.label) }
                            )
                        }
                    }

                    // Rebar Size Chips
                    Text("MAIN REBAR SIZE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rebarSizes.forEach { r ->
                            FilterChip(
                                selected = state.selectedRebar.name == r.name,
                                onClick = { viewModel.setSelectedRebar(r) },
                                label = { Text(r.name) }
                            )
                        }
                    }
                }
            }

            // Dimensions & Grid Configuration
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "DIMENSIONS & SPACING SPECIFICATIONS",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )

                    when (state.structure) {
                        RebarStructure.SLAB_GRID -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = state.length.toString(),
                                    onValueChange = { viewModel.updateInputs(length = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text(if (state.isMetric) "Length (m)" else "Length (ft)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = state.width.toString(),
                                    onValueChange = { viewModel.updateInputs(width = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text(if (state.isMetric) "Width (m)" else "Width (ft)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = state.gridSpacingInOrCm.toString(),
                                    onValueChange = { viewModel.updateInputs(spacing = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text(if (state.isMetric) "Grid Spacing (cm)" else "Grid Spacing (in, O.C.)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = state.edgeClearCoverInOrCm.toString(),
                                    onValueChange = { viewModel.updateInputs(edgeCover = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text(if (state.isMetric) "Clear Cover (cm)" else "Clear Cover (in)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Rebar Mats:", style = MaterialTheme.typography.bodyMedium)
                                FilterChip(
                                    selected = state.layersCount == 1,
                                    onClick = { viewModel.updateInputs(layers = 1) },
                                    label = { Text("Single Layer Mat") }
                                )
                                FilterChip(
                                    selected = state.layersCount == 2,
                                    onClick = { viewModel.updateInputs(layers = 2) },
                                    label = { Text("Double Mat (Top & Bottom)") }
                                )
                            }
                        }

                        RebarStructure.BEAM_FOOTING -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = state.beamLength.toString(),
                                    onValueChange = { viewModel.updateInputs(beamLength = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text(if (state.isMetric) "Beam Length (m)" else "Beam Length (ft)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = state.longitudinalBarCount.toString(),
                                    onValueChange = { viewModel.updateInputs(longBars = it.toIntOrNull() ?: 4) },
                                    label = { Text("Main Bar Count") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = state.beamWidthInOrCm.toString(),
                                    onValueChange = { viewModel.updateInputs(beamWidth = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text(if (state.isMetric) "Beam Width (cm)" else "Beam Width (in)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = state.beamDepthInOrCm.toString(),
                                    onValueChange = { viewModel.updateInputs(beamDepth = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text(if (state.isMetric) "Beam Depth (cm)" else "Beam Depth (in)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = state.stirrupSpacingInOrCm.toString(),
                                    onValueChange = { viewModel.updateInputs(stirrupSpacing = it.toDoubleOrNull() ?: 8.0) },
                                    label = { Text(if (state.isMetric) "Stirrup Spacing (cm)" else "Stirrup Spacing (in)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        RebarStructure.COLUMN -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = state.columnHeight.toString(),
                                    onValueChange = { viewModel.updateInputs(colHeight = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text(if (state.isMetric) "Height (m)" else "Height (ft)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = state.columnDiameterInOrCm.toString(),
                                    onValueChange = { viewModel.updateInputs(colDiameter = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text(if (state.isMetric) "Diameter (cm)" else "Diameter (in)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = state.mainVerticalBarsCount.toString(),
                                    onValueChange = { viewModel.updateInputs(colMainBars = it.toIntOrNull() ?: 6) },
                                    label = { Text("Vertical Main Bars") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = state.lateralTieSpacingInOrCm.toString(),
                                    onValueChange = { viewModel.updateInputs(colTieSpacing = it.toDoubleOrNull() ?: 8.0) },
                                    label = { Text(if (state.isMetric) "Spiral/Tie Spacing (cm)" else "Tie Spacing (in)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Waste Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Waste & Lap Splice Margin:", style = MaterialTheme.typography.labelSmall)
                            Text("+${state.wastePercent.toInt()}%", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                        Slider(
                            value = state.wastePercent.toFloat(),
                            onValueChange = { viewModel.updateInputs(waste = it.toDouble()) },
                            valueRange = 5f..25f,
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
                            text = "TOTAL REBAR SCHEDULE & WEIGHT",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        IconButton(onClick = {
                            val info = "Rebar Schedule (${state.structure.label}):\n" +
                                    "Selected Bar: ${state.selectedRebar.name}\n" +
                                    "Total Linear Length: ${String.format("%.1f", state.totalLinearFeet)} ft (${String.format("%.1f", state.totalLinearMeters)} m)\n" +
                                    "Total Steel Weight: ${String.format("%,.1f", state.totalWeightLbs)} lbs (${String.format("%.2f", state.totalWeightTonnes)} Tonnes / ${String.format("%,.1f", state.totalWeightKg)} kg)\n" +
                                    "Standard Stock Sticks (${state.stockStickLengthFtOrM.toInt()}${if (state.isMetric) "m" else "ft"}): ${state.stockSticksNeeded} sticks\n" +
                                    "Grid Intersections: ${state.gridIntersectionsCount}\n" +
                                    "Tie Wire: ~${String.format("%.1f", state.tieWireLbsNeeded)} lbs (16-gauge)\n" +
                                    "Rebar Support Chairs: ~${state.rebarChairsNeeded} pcs"
                            clipboardManager.setText(AnnotatedString(info))
                            viewModel.logCalculation()
                            Toast.makeText(context, "Copied Rebar Schedule!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Total Steel Weight", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                text = "${String.format("%,.0f", state.totalWeightLbs)} lbs",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Metric Tonnes / kg", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                text = "${String.format("%.2f", state.totalWeightTonnes)} t (${String.format("%,.0f", state.totalWeightKg)} kg)",
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
                            Text("Total Linear Run", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                text = "${String.format("%.1f", state.totalLinearFeet)} ft (${String.format("%.1f", state.totalLinearMeters)} m)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Stock Rebar Sticks", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                text = "${state.stockSticksNeeded} Sticks (@ ${state.stockStickLengthFtOrM.toInt()}${if (state.isMetric) "m" else "ft"})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            )
                        }
                    }

                    // Hardware & Accessories Breakdown
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Layers, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Chairs / Bolsters", style = MaterialTheme.typography.labelSmall)
                                Text("${state.rebarChairsNeeded} pcs", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Straighten, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Intersections", style = MaterialTheme.typography.labelSmall)
                                Text("${state.gridIntersectionsCount} ties", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.GridOn, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("16-Ga Tie Wire", style = MaterialTheme.typography.labelSmall)
                                Text("~${String.format("%.1f", state.tieWireLbsNeeded)} lbs", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }
        }
    }
}
