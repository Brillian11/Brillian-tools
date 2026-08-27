package com.example.ui.screens.electrical

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
fun BreakerPanelScreen(
    viewModel: BreakerPanelViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }

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
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Breaker Panel Load Calculator",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Single/Three-Phase load balancing, neutral unbalance & busbar limits",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Panel Configuration Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "PANEL VOLTAGE & BUSBAR RATING",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PanelSystemType.values().forEach { type ->
                            FilterChip(
                                selected = state.panelType == type,
                                onClick = { viewModel.setPanelType(type) },
                                label = { Text(if (type == PanelSystemType.SPLIT_PHASE_120_240) "120/240V 1-Phase" else type.name.replace('_', ' ')) }
                            )
                        }
                    }

                    Text(
                        text = "MAIN SERVICE BREAKER RATING:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(100, 150, 200, 400).forEach { rating ->
                            FilterChip(
                                selected = state.mainBreakerAmps == rating,
                                onClick = { viewModel.setMainBreaker(rating) },
                                label = { Text("${rating}A Main") }
                            )
                        }
                    }
                }
            }

            // Phase Balance Dashboard Meters
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (state.isOverloaded) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.surfaceVariant
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
                            text = "PHASE CURRENT DISTRIBUTION",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        )
                        IconButton(onClick = {
                            val info = "Breaker Panel Schedule (${state.panelType.voltageLabel}):\n" +
                                    "Total Connected: ${String.format("%.1f", state.totalConnectedVa)} VA\n" +
                                    "Phase A: ${String.format("%.1f", state.phaseACurrentAmps)} A (${String.format("%.0f", state.phaseALoadVa)} VA)\n" +
                                    "Phase B: ${String.format("%.1f", state.phaseBCurrentAmps)} A (${String.format("%.0f", state.phaseBLoadVa)} VA)\n" +
                                    (if (state.panelType.isThreePhase) "Phase C: ${String.format("%.1f", state.phaseCCurrentAmps)} A (${String.format("%.0f", state.phaseCLoadVa)} VA)\n" else "") +
                                    "Neutral Unbalance: ${String.format("%.1f", state.neutralUnbalanceAmps)} A\n" +
                                    "Phase Imbalance: ${String.format("%.1f", state.phaseImbalancePct)}%\n" +
                                    "Busbar Utilization: ${String.format("%.1f", state.busbarUtilizationPct)}% of ${state.busbarRatingAmps}A"
                            clipboardManager.setText(AnnotatedString(info))
                            viewModel.logPanelLoad()
                            Toast.makeText(context, "Copied Panel Metrics!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                    }

                    // Phase A Bar
                    PhaseMeterBar(
                        phaseName = "Phase A (Line 1)",
                        currentA = state.phaseACurrentAmps,
                        va = state.phaseALoadVa,
                        maxCapacityA = state.mainBreakerAmps.toDouble(),
                        barColor = Color(0xFF1E88E5)
                    )

                    // Phase B Bar
                    PhaseMeterBar(
                        phaseName = "Phase B (Line 2)",
                        currentA = state.phaseBCurrentAmps,
                        va = state.phaseBLoadVa,
                        maxCapacityA = state.mainBreakerAmps.toDouble(),
                        barColor = Color(0xFFE53935)
                    )

                    // Phase C Bar (If 3-phase)
                    if (state.panelType.isThreePhase) {
                        PhaseMeterBar(
                            phaseName = "Phase C (Line 3)",
                            currentA = state.phaseCCurrentAmps,
                            va = state.phaseCLoadVa,
                            maxCapacityA = state.mainBreakerAmps.toDouble(),
                            barColor = Color(0xFF43A047)
                        )
                    }

                    // Neutral & Imbalance Metrics
                    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Neutral Unbalance", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    "${String.format("%.1f", state.neutralUnbalanceAmps)} A",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                )
                            }
                            Column {
                                Text("Phase Imbalance", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    "${String.format("%.1f", state.phaseImbalancePct)}%",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (state.phaseImbalancePct < 15.0) Color(0xFF2E7D32) else Color(0xFFFB8C00)
                                    )
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Busbar Capacity", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    "${String.format("%.1f", state.busbarUtilizationPct)}%",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (state.isOverloaded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Circuit Schedule Management
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
                            text = "CIRCUIT DIRECTORY (${state.circuits.size} Circuits)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        )
                        Button(
                            onClick = { showAddDialog = true },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Circuit")
                        }
                    }

                    Text("Tip: Tap phase chip (e.g. 'Phase A') to rebalance single-pole loads between legs.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    // Circuit items
                    state.circuits.forEach { circuit ->
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(circuit.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    Text(
                                        "${circuit.breakerRatingAmps}A ${circuit.poleType.name.replace('_', ' ')} • ${String.format("%.0f", circuit.loadVa)} VA",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    FilterChip(
                                        selected = true,
                                        onClick = { viewModel.toggleCircuitPhase(circuit.id) },
                                        label = { Text("Phase ${circuit.assignedPhase}") }
                                    )

                                    IconButton(
                                        onClick = { viewModel.removeCircuit(circuit.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var cName by remember { mutableStateOf("") }
        var cAmps by remember { mutableStateOf("20") }
        var cVa by remember { mutableStateOf("1500") }
        var cPole by remember { mutableStateOf(CircuitPoleType.SINGLE_POLE) }
        var cPhase by remember { mutableStateOf("A") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New Circuit") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = cName,
                        onValueChange = { cName = it },
                        label = { Text("Circuit Name / Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = cAmps,
                        onValueChange = { cAmps = it },
                        label = { Text("Breaker Rating (Amps)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = cVa,
                        onValueChange = { cVa = it },
                        label = { Text("Connected Load (VA / Watts)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = cPole == CircuitPoleType.SINGLE_POLE,
                            onClick = { cPole = CircuitPoleType.SINGLE_POLE; cPhase = "A" },
                            label = { Text("1-Pole") }
                        )
                        FilterChip(
                            selected = cPole == CircuitPoleType.DOUBLE_POLE,
                            onClick = { cPole = CircuitPoleType.DOUBLE_POLE; cPhase = "AB" },
                            label = { Text("2-Pole") }
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (cName.isNotBlank()) {
                        viewModel.addCircuit(
                            name = cName,
                            breakerAmps = cAmps.toIntOrNull() ?: 20,
                            pole = cPole,
                            phase = cPhase,
                            loadVa = cVa.toDoubleOrNull() ?: 1500.0
                        )
                        showAddDialog = false
                    }
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PhaseMeterBar(
    phaseName: String,
    currentA: Double,
    va: Double,
    maxCapacityA: Double,
    barColor: Color
) {
    val pct = (currentA / maxCapacityA).toFloat().coerceIn(0f, 1f)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(phaseName, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
            Text(
                "${String.format("%.1f", currentA)} A (${String.format("%.0f", va)} VA)",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            )
        }
        LinearProgressIndicator(
            progress = { pct },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = if (pct > 0.8f) Color(0xFFC62828) else barColor,
            trackColor = MaterialTheme.colorScheme.surface
        )
    }
}
