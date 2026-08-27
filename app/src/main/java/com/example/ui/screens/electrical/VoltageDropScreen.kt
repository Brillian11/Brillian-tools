package com.example.ui.screens.electrical

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.Warning
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
fun VoltageDropScreen(
    viewModel: VoltageDropViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var vInput by remember { mutableStateOf(state.sourceVoltageV.toString()) }
    var iInput by remember { mutableStateOf(state.loadCurrentA.toString()) }
    var distInput by remember { mutableStateOf(state.oneWayDistanceFt.toString()) }

    fun syncInputs(v: Double, i: Double, d: Double) {
        viewModel.updateInputs(v, i, d)
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
                            imageVector = Icons.Default.ElectricMeter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Voltage Drop Calculator",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "NEC 210.19(A) 3% branch & 5% total line degradation analyzer",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Material & Phase Selectors
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "CONDUCTOR MATERIAL & PHASE SYSTEM",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.material == ConductorMaterial.COPPER,
                            onClick = { viewModel.setMaterial(ConductorMaterial.COPPER) },
                            label = { Text("Copper (Cu)") }
                        )
                        FilterChip(
                            selected = state.material == ConductorMaterial.ALUMINUM,
                            onClick = { viewModel.setMaterial(ConductorMaterial.ALUMINUM) },
                            label = { Text("Aluminum (Al)") }
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.phaseSystem == PhaseSystem.SINGLE_PHASE_2W,
                            onClick = { viewModel.setPhase(PhaseSystem.SINGLE_PHASE_2W) },
                            label = { Text("1-Phase 2-Wire") }
                        )
                        FilterChip(
                            selected = state.phaseSystem == PhaseSystem.THREE_PHASE_3W,
                            onClick = { viewModel.setPhase(PhaseSystem.THREE_PHASE_3W) },
                            label = { Text("3-Phase 3/4-Wire") }
                        )
                    }
                }
            }

            // Wire Size Picker (Horizontal Scroll)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SELECT WIRE GAUGE (AWG / KCMIL)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        )
                        val curWire = VoltageDropUiState.WIRE_SPECS[state.selectedWireIndex]
                        Text(
                            text = "${curWire.metricMm2} mm² (${curWire.ampacity75C}A @ 75°C)",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        )
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        itemsIndexed(VoltageDropUiState.WIRE_SPECS) { idx, wire ->
                            FilterChip(
                                selected = state.selectedWireIndex == idx,
                                onClick = { viewModel.setWireIndex(idx) },
                                label = { Text(wire.name) }
                            )
                        }
                    }
                }
            }

            // Circuit Inputs
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "CIRCUIT OPERATING CONDITIONS",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = vInput,
                            onValueChange = {
                                vInput = it
                                it.toDoubleOrNull()?.let { v ->
                                    syncInputs(v, iInput.toDoubleOrNull() ?: 15.0, distInput.toDoubleOrNull() ?: 100.0)
                                }
                            },
                            label = { Text("Source Voltage (V)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = iInput,
                            onValueChange = {
                                iInput = it
                                it.toDoubleOrNull()?.let { i ->
                                    syncInputs(vInput.toDoubleOrNull() ?: 120.0, i, distInput.toDoubleOrNull() ?: 100.0)
                                }
                            },
                            label = { Text("Load Current (A)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = distInput,
                        onValueChange = {
                            distInput = it
                            it.toDoubleOrNull()?.let { d ->
                                syncInputs(vInput.toDoubleOrNull() ?: 120.0, iInput.toDoubleOrNull() ?: 15.0, d)
                            }
                        },
                        label = { Text("One-Way Run Distance (Feet)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Standard voltage quick presets
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(12.0, 24.0, 120.0, 208.0, 240.0, 277.0, 480.0).forEach { presetV ->
                            FilterChip(
                                selected = state.sourceVoltageV == presetV,
                                onClick = {
                                    vInput = presetV.toInt().toString()
                                    syncInputs(presetV, iInput.toDoubleOrNull() ?: 15.0, distInput.toDoubleOrNull() ?: 100.0)
                                },
                                label = { Text("${presetV.toInt()}V") }
                            )
                        }
                    }
                }
            }

            // Results & NEC Status
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (state.isNecCompliant3Percent) MaterialTheme.colorScheme.surfaceVariant
                    else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                ),
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
                            text = "VOLTAGE DROP METRICS",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        )
                        IconButton(onClick = {
                            val wire = VoltageDropUiState.WIRE_SPECS[state.selectedWireIndex]
                            val info = "Voltage Drop Analysis (${wire.name} ${state.material.name}):\n" +
                                    "Distance: ${state.oneWayDistanceFt} ft\n" +
                                    "Source: ${state.sourceVoltageV} V @ ${state.loadCurrentA} A\n" +
                                    "Voltage Drop: ${String.format("%.2f", state.voltageDropV)} V (${String.format("%.2f", state.percentageDrop)}%)\n" +
                                    "Voltage at Load: ${String.format("%.2f", state.voltageAtLoadV)} V\n" +
                                    "Power Dissipation: ${String.format("%.1f", state.powerLossWatts)} W\n" +
                                    "NEC 3% Branch Compliant: ${if (state.isNecCompliant3Percent) "YES" else "NO"}\n" +
                                    "Recommended Size: ${state.recommendedWireName}"
                            clipboardManager.setText(AnnotatedString(info))
                            viewModel.logVoltageDrop()
                            Toast.makeText(context, "Copied Voltage Drop Report!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                    }

                    // Large Stat Highlight
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Voltage Drop", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "${String.format("%.2f", state.voltageDropV)} V",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (state.isNecCompliant3Percent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Percent Drop", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "${String.format("%.2f", state.percentageDrop)}%",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (state.isNecCompliant3Percent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Voltage at End of Line", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "${String.format("%.2f", state.voltageAtLoadV)} V",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Line Heat Loss", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "${String.format("%.1f", state.powerLossWatts)} W",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                    }

                    // NEC Compliance Badge
                    Surface(
                        color = if (state.isNecCompliant3Percent) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (state.isNecCompliant3Percent) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (state.isNecCompliant3Percent) Color(0xFF2E7D32) else Color(0xFFC62828),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (state.isNecCompliant3Percent) "NEC 210.19(A) Compliant (≤ 3% Drop)" else "Exceeds NEC 3% Branch Limit (> 3% Drop)",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (state.isNecCompliant3Percent) Color(0xFF2E7D32) else Color(0xFFC62828)
                                    )
                                )
                                if (!state.isNecCompliant3Percent) {
                                    Text(
                                        text = "Upsize wire to ${state.recommendedWireName} to reduce drop below 3.0%.",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFC62828))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
