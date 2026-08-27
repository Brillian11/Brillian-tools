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
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocalShipping
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
fun ConcreteVolumeScreen(
    viewModel: ConcreteVolumeViewModel,
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
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Concrete Volume & Bag Mix Sizer",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Cubic yards/meters for slabs, footings, sonotubes & post holes with pre-mix dry bag counts",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Unit Selector & Shape Selector Card
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
                            text = "STRUCTURE TYPE & UNITS",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = !state.isMetric,
                                onClick = { viewModel.setUnitSystem(false) },
                                label = { Text("Imperial (yd³/ft/in)") }
                            )
                            FilterChip(
                                selected = state.isMetric,
                                onClick = { viewModel.setUnitSystem(true) },
                                label = { Text("Metric (m³/cm)") }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ConcreteShape.values().forEach { shape ->
                            FilterChip(
                                selected = state.shape == shape,
                                onClick = { viewModel.setShape(shape) },
                                label = { Text(shape.label) }
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
                        text = "DIMENSIONS & SPECIFICATIONS",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )

                    when (state.shape) {
                        ConcreteShape.SLAB, ConcreteShape.FOOTING -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = state.length.toString(),
                                    onValueChange = { viewModel.updateInputs(length = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text(if (state.isMetric) "Length (meters)" else "Length (feet)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = state.width.toString(),
                                    onValueChange = { viewModel.updateInputs(width = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text(if (state.isMetric) "Width (meters)" else "Width (feet)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = state.thicknessInchesOrCm.toString(),
                                    onValueChange = { viewModel.updateInputs(thickness = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text(if (state.isMetric) "Thickness / Depth (cm)" else "Thickness / Depth (in)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = state.quantity.toString(),
                                    onValueChange = { viewModel.updateInputs(quantity = it.toIntOrNull() ?: 1) },
                                    label = { Text("Quantity / Count") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        ConcreteShape.COLUMN -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = state.diameterInchesOrCm.toString(),
                                    onValueChange = { viewModel.updateInputs(diameter = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text(if (state.isMetric) "Column Diameter (cm)" else "Sonotube Diameter (in)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = state.heightFtOrM.toString(),
                                    onValueChange = { viewModel.updateInputs(height = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text(if (state.isMetric) "Height / Depth (m)" else "Height / Depth (ft)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            OutlinedTextField(
                                value = state.quantity.toString(),
                                onValueChange = { viewModel.updateInputs(quantity = it.toIntOrNull() ?: 1) },
                                label = { Text("Number of Columns / Piers") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        ConcreteShape.POST_HOLE -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = state.holeDiameterInOrCm.toString(),
                                    onValueChange = { viewModel.updateInputs(holeDiameter = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text(if (state.isMetric) "Hole Diameter (cm)" else "Hole Diameter (in)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = state.holeDepthInOrCm.toString(),
                                    onValueChange = { viewModel.updateInputs(holeDepth = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text(if (state.isMetric) "Hole Depth (cm)" else "Hole Depth (in)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = state.postSquareInOrCm.toString(),
                                    onValueChange = { viewModel.updateInputs(postSquare = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text(if (state.isMetric) "Post Width (cm)" else "Post Square (in, e.g. 4)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = state.postHoleCount.toString(),
                                    onValueChange = { viewModel.updateInputs(postHoles = it.toIntOrNull() ?: 1) },
                                    label = { Text("Number of Post Holes") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        ConcreteShape.STAIRS -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = state.stepCount.toString(),
                                    onValueChange = { viewModel.updateInputs(stepCount = it.toIntOrNull() ?: 1) },
                                    label = { Text("Number of Steps") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = state.stairWidthFtOrM.toString(),
                                    onValueChange = { viewModel.updateInputs(stairWidth = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text(if (state.isMetric) "Stair Width (m)" else "Stair Width (ft)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = state.stepRiseInOrCm.toString(),
                                    onValueChange = { viewModel.updateInputs(stepRise = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text(if (state.isMetric) "Step Rise (cm)" else "Step Rise (in)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = state.stepRunInOrCm.toString(),
                                    onValueChange = { viewModel.updateInputs(stepRun = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text(if (state.isMetric) "Step Run (cm)" else "Step Run (in)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        ConcreteShape.CURB -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = state.curbLengthFtOrM.toString(),
                                    onValueChange = { viewModel.updateInputs(curbLength = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text(if (state.isMetric) "Total Length (m)" else "Total Length (ft)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = state.curbHeightInOrCm.toString(),
                                    onValueChange = { viewModel.updateInputs(curbHeight = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text(if (state.isMetric) "Curb Height (cm)" else "Curb Height (in)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = state.curbWidthInOrCm.toString(),
                                    onValueChange = { viewModel.updateInputs(curbWidth = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text(if (state.isMetric) "Curb Width (cm)" else "Curb Width (in)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = state.gutterWidthInOrCm.toString(),
                                    onValueChange = { viewModel.updateInputs(gutterWidth = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text(if (state.isMetric) "Gutter Width (cm)" else "Gutter Width (in)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Waste / Spillage Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Waste / Over-Excavation Margin:", style = MaterialTheme.typography.labelSmall)
                            Text("+${state.wastePercent.toInt()}%", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                        Slider(
                            value = state.wastePercent.toFloat(),
                            onValueChange = { viewModel.updateInputs(waste = it.toDouble()) },
                            valueRange = 0f..25f,
                            steps = 4
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
                            text = "TOTAL CONCRETE VOLUME & SIZING",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        IconButton(onClick = {
                            val info = "Concrete Sizing (${state.shape.label}):\n" +
                                    "Total Volume (+${state.wastePercent.toInt()}% waste): ${String.format("%.2f", state.totalVolumeCuYardsWithWaste)} yd³ (${String.format("%.2f", state.totalVolumeCuMetersWithWaste)} m³)\n" +
                                    "Total Weight: ${String.format("%,.0f", state.totalWeightLbs)} lbs (${String.format("%.2f", state.totalWeightTonnes)} Tonnes)\n" +
                                    "Pre-Mix Bags Needed (${state.selectedBag.label}): ${state.bagsNeeded} bags\n" +
                                    "Ready-Mix Truck: ${String.format("%.2f", state.readyMixTruckLoads)} truckloads (@8 yd³)\n" +
                                    "Water Requirement: ~${String.format("%.1f", state.waterGallonsNeeded)} gal (${String.format("%.1f", state.waterLitersNeeded)} L)"
                            clipboardManager.setText(AnnotatedString(info))
                            viewModel.logCalculation()
                            Toast.makeText(context, "Copied Concrete Estimate!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Total Cubic Yards", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                text = "${String.format("%.2f", state.totalVolumeCuYardsWithWaste)} yd³",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Cubic Meters", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                text = "${String.format("%.2f", state.totalVolumeCuMetersWithWaste)} m³",
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
                            Text("Net Volume (Exact)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                text = "${String.format("%.2f", state.netVolumeCuYards)} yd³ / ${String.format("%.1f", state.netVolumeCuFt)} ft³",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Weight", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                text = "${String.format("%,.0f", state.totalWeightLbs)} lbs (${String.format("%.2f", state.totalWeightTonnes)} t)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            )
                        }
                    }

                    // Pre-mix Bag Mix Selection & Count
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "PRE-MIX DRY BAG YIELD",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                BagSize.values().forEach { bag ->
                                    FilterChip(
                                        selected = state.selectedBag == bag,
                                        onClick = { viewModel.setSelectedBag(bag) },
                                        label = { Text(bag.label) }
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${state.bagsNeeded} Bags",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.WaterDrop, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "~${String.format("%.1f", state.waterGallonsNeeded)} gal water",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }

                    // Ready-Mix Concrete Truck Option
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (state.readyMixTruckLoads >= 0.5) {
                                        "Ready-Mix Delivery: ${String.format("%.2f", state.readyMixTruckLoads)} Truckloads (@ 8 yd³/truck)"
                                    } else {
                                        "Ready-Mix Delivery: Short Load Fee likely applies (< 4 yd³)"
                                    },
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (state.totalVolumeCuYardsWithWaste >= 3.0) "Ordering ready-mix transit mixer truck is recommended for volumes > 2 yd³." else "Pre-mixed bagged concrete is cost-effective for small pours.",
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
