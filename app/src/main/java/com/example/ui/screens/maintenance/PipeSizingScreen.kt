package com.example.ui.screens.maintenance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PipeSizingScreen(
    viewModel: PipeSizingViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var showSavedMessage by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header & Unit Toggle
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE0F2FE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Water,
                                contentDescription = null,
                                tint = Color(0xFF0284C7)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Pipe Sizing & Friction Loss",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Hazen-Williams hydraulics & pressure drop",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (state.isMetric) "Metric (L/m/bar)" else "US (GPM/ft/psi)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = state.isMetric,
                            onCheckedChange = { viewModel.setUnitSystem(it) }
                        )
                    }
                }
            }
        }

        // Section 1: Pipe Material Selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "1. Select Pipe Material & Roughness",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PipeMaterial.values().forEach { mat ->
                        FilterChip(
                            selected = state.material == mat,
                            onClick = { viewModel.setMaterial(mat) },
                            label = { Text(mat.label, fontSize = 12.sp) },
                            leadingIcon = if (state.material == mat) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0284C7).copy(alpha = 0.2f),
                                selectedLabelColor = Color(0xFF0369A1)
                            )
                        )
                    }
                }
            }
        }

        // Section 2: Pipe Nominal Diameter
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "2. Nominal Pipe Size",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Actual ID: ${String.format("%.3f", state.innerDiameterInches)}\" (${String.format("%.1f", state.innerDiameterMm)} mm)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF0284C7),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NominalPipeSize.values().forEach { size ->
                        FilterChip(
                            selected = state.nominalSize == size,
                            onClick = { viewModel.setNominalSize(size) },
                            label = { Text(size.nominalStr, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        // Section 3: Flow Rate, Run Length & Supply Pressure
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "3. Flow Rate & Line Parameters",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Flow Rate
                val flowUnit = if (state.isMetric) "L/min" else "GPM"
                Text(
                    text = "Design Flow Rate: ${String.format("%.1f", state.flowRateGpmOrLpm)} $flowUnit",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
                Slider(
                    value = state.flowRateGpmOrLpm.toFloat(),
                    onValueChange = { viewModel.updateInputs(flowRate = it.toDouble()) },
                    valueRange = if (state.isMetric) 1f..350f else 0.5f..90f,
                    steps = 0
                )
                OutlinedTextField(
                    value = state.flowRateGpmOrLpm.toString(),
                    onValueChange = { it.toDoubleOrNull()?.let { v -> viewModel.updateInputs(flowRate = v) } },
                    label = { Text("Exact Flow Rate ($flowUnit)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Straight Pipe Length
                val lenUnit = if (state.isMetric) "Meters" else "Feet"
                Text(
                    text = "Straight Pipe Run: ${String.format("%.1f", state.pipeLengthFtOrM)} $lenUnit",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
                Slider(
                    value = state.pipeLengthFtOrM.toFloat(),
                    onValueChange = { viewModel.updateInputs(length = it.toDouble()) },
                    valueRange = if (state.isMetric) 1f..200f else 5f..500f,
                    steps = 0
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Static Supply Pressure
                val pressUnit = if (state.isMetric) "bar" else "psi"
                Text(
                    text = "Static Supply Pressure: ${String.format("%.1f", state.staticSupplyPressurePsiOrBar)} $pressUnit",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
                Slider(
                    value = state.staticSupplyPressurePsiOrBar.toFloat(),
                    onValueChange = { viewModel.updateInputs(supplyPressure = it.toDouble()) },
                    valueRange = if (state.isMetric) 1f..10f else 15f..120f,
                    steps = 0
                )
            }
        }

        // Section 4: Fittings Equivalent Length Counter
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "4. Valves & Fittings (Equivalent Length)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "+${String.format("%.1f", state.equivalentFittingsLengthFt)} ft eq",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                FittingCounterRow("90° Standard Elbows", state.elbow90Count) { viewModel.updateFittings(elbow90 = it) }
                FittingCounterRow("45° Elbows", state.elbow45Count) { viewModel.updateFittings(elbow45 = it) }
                FittingCounterRow("Tee (Flow-Through)", state.teeFlowThroughCount) { viewModel.updateFittings(teeFlow = it) }
                FittingCounterRow("Tee (Branch Run 90°)", state.teeBranchCount) { viewModel.updateFittings(teeBranch = it) }
                FittingCounterRow("Full-Port Ball Valves", state.ballValveCount) { viewModel.updateFittings(ballValve = it) }
                FittingCounterRow("Swing Check Valves", state.checkValveCount) { viewModel.updateFittings(checkValve = it) }
            }
        }

        // Section 5: Hydraulic Velocity & Diagnostics
        val velocityColor = when (state.velocityStatus) {
            VelocityStatus.OPTIMAL_DOMESTIC -> Color(0xFF16A34A)
            VelocityStatus.ACCEPTABLE_COMMERCIAL -> Color(0xFFCA8A04)
            VelocityStatus.SLUGGISH -> Color(0xFF0284C7)
            VelocityStatus.EXCESSIVE_EROSION -> Color(0xFFDC2626)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = velocityColor.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Flow Velocity Status",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = velocityColor)
                    )
                    Text(
                        text = "${String.format("%.2f", state.velocityFps)} ft/s (${String.format("%.2f", state.velocityMps)} m/s)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = velocityColor)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.velocityStatus.label,
                    style = MaterialTheme.typography.bodySmall.copy(color = velocityColor, fontWeight = FontWeight.SemiBold)
                )

                if (state.velocityStatus.isWarning) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Velocity exceeds 8.0 ft/s! Upsize pipe to ${state.recommendedNominalSize} to prevent copper pinhole erosion and hammering.",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF991B1B))
                            )
                        }
                    }
                }
            }
        }

        // Section 6: Primary Calculation Results
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Hydraulic Calculation Takeoff",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ResultBox(
                        title = "Pressure Drop (ΔP)",
                        value = if (state.isMetric) "${String.format("%.2f", state.pressureDropBar)} bar" else "${String.format("%.2f", state.pressureDropPsi)} psi",
                        subtitle = "${String.format("%.1f", state.pressureDropKpa)} kPa",
                        containerColor = Color(0xFFEFF6FF),
                        contentColor = Color(0xFF1D4ED8),
                        modifier = Modifier.weight(1f)
                    )

                    ResultBox(
                        title = "Residual Pressure",
                        value = if (state.isMetric) "${String.format("%.2f", state.residualPressureBar)} bar" else "${String.format("%.1f", state.residualPressurePsi)} psi",
                        subtitle = "At fixture outlet",
                        containerColor = Color(0xFFECFDF5),
                        contentColor = Color(0xFF047857),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ResultBox(
                        title = "Total Head Loss",
                        value = "${String.format("%.2f", state.totalHeadLossFt)} ft",
                        subtitle = "${String.format("%.2f", state.headLossPer100Ft)} ft / 100ft",
                        containerColor = Color(0xFFFEF3C7),
                        contentColor = Color(0xFFB45309),
                        modifier = Modifier.weight(1f)
                    )

                    ResultBox(
                        title = "Total Eq. Length",
                        value = if (state.isMetric) "${String.format("%.1f", state.totalEquivalentLengthM)} m" else "${String.format("%.1f", state.totalEquivalentLengthFt)} ft",
                        subtitle = "Pipe + ${state.elbow90Count + state.elbow45Count + state.teeFlowThroughCount + state.teeBranchCount + state.ballValveCount} fittings",
                        containerColor = Color(0xFFF3E8FF),
                        contentColor = Color(0xFF7E22CE),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Recommended Pipe Sizing",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "For flow of ${String.format("%.1f", if (state.isMetric) state.actualFlowLpm else state.actualFlowGpm)} ${if (state.isMetric) "L/min" else "GPM"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        color = Color(0xFF0284C7),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = state.recommendedNominalSize,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                        )
                    }
                }
            }
        }

        // Action Button: Save & Log Activity
        Button(
            onClick = {
                viewModel.logCalculation()
                showSavedMessage = true
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
        ) {
            Icon(Icons.Default.BookmarkBorder, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Pipe Takeoff to Job Log", fontWeight = FontWeight.Bold)
        }

        if (showSavedMessage) {
            Surface(
                color = Color(0xFFDCFCE7),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF16A34A))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hydraulic takeoff recorded to offline project log!",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF15803D), fontWeight = FontWeight.Medium)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun FittingCounterRow(
    label: String,
    count: Int,
    onCountChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { if (count > 0) onCountChange(count - 1) },
                modifier = Modifier.size(32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                }
            }
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            IconButton(
                onClick = { onCountChange(count + 1) },
                modifier = Modifier.size(32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ResultBox(
    title: String,
    value: String,
    subtitle: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = containerColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = contentColor)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = contentColor))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = contentColor.copy(alpha = 0.8f))
        }
    }
}
