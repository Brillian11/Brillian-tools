package com.example.ui.screens.electrical

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ConduitFillScreen(
    viewModel: ConduitFillViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var targetAmpsInput by remember { mutableStateOf(state.targetAmps.toString()) }

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
                            imageVector = Icons.Default.Cable,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Wire Gauge & Conduit Fill Sizing",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "NEC Chapter 9 Table 1 & 4 fill percentages and Table 310.16 wire ampacity",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Conduit Type & Trade Size
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "CONDUIT SPECIFICATION & TRADE SIZE",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ConduitType.values().forEach { type ->
                            FilterChip(
                                selected = state.conduitType == type,
                                onClick = { viewModel.setConduitType(type) },
                                label = { Text(type.name.replace('_', ' ')) }
                            )
                        }
                    }

                    Text(
                        text = "TRADE DIAMETER:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        itemsIndexed(ConduitFillUiState.TRADE_SIZES) { idx, sizeStr ->
                            FilterChip(
                                selected = state.selectedTradeSizeIndex == idx,
                                onClick = { viewModel.setTradeSizeIndex(idx) },
                                label = { Text(sizeStr) }
                            )
                        }
                    }
                }
            }

            // Conductors In Conduit (Dynamic List)
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
                            text = "CONDUCTORS IN RUN (${state.totalConductorsCount} Total)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        )
                    }

                    // Wire list items
                    state.wireList.forEach { wire ->
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(wire.wireType, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    Text(
                                        "${wire.count}x @ ${String.format("%.4f", wire.areaPerWireSqIn)} sq in = ${String.format("%.4f", wire.count * wire.areaPerWireSqIn)} sq in",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = { viewModel.updateWireCount(wire.id, wire.count - 1) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(18.dp))
                                    }
                                    Text(
                                        text = "${wire.count}",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                    IconButton(
                                        onClick = { viewModel.updateWireCount(wire.id, wire.count + 1) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = { viewModel.removeWire(wire.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Add standard wire preset buttons
                    Text("Add Common Wire Size:", style = MaterialTheme.typography.labelSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ConduitFillUiState.WIRE_LIBRARY.take(8).forEach { libWire ->
                            Button(
                                onClick = { viewModel.addWire(libWire.name, libWire.gauge, 1, libWire.areaSqIn) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                            ) {
                                Text("+ ${libWire.gauge}", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            // Visual Conduit Cross Section Canvas
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "CONDUIT FILL VISUALIZATION",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        val strokeCol = if (state.isConduitOverfilled) Color(0xFFC62828) else Color(0xFF2E7D32)
                        val primary = MaterialTheme.colorScheme.primary

                        Canvas(modifier = Modifier.size(140.dp)) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val pipeRadius = (size.width / 2f) - 10f

                            // Draw Conduit Outer Shell
                            drawCircle(
                                color = Color(0xFF9E9E9E),
                                radius = pipeRadius + 6f,
                                center = center,
                                style = Stroke(width = 6f)
                            )
                            // Draw Conduit Inner Area
                            drawCircle(
                                color = if (state.isConduitOverfilled) Color(0xFFFFEBEE) else Color(0xFFF1F8E9),
                                radius = pipeRadius,
                                center = center
                            )

                            // Render conductors inside
                            val wires = mutableListOf<Double>()
                            state.wireList.forEach { w ->
                                repeat(w.count) { wires.add(w.areaPerWireSqIn) }
                            }

                            if (wires.isNotEmpty()) {
                                val wireR = (pipeRadius / 4.5f).coerceIn(6f, 18f)
                                wires.forEachIndexed { i, _ ->
                                    val angle = (i * (360.0 / wires.size)).toDouble()
                                    val dist = if (wires.size == 1) 0.0 else (pipeRadius * 0.45)
                                    val rad = Math.toRadians(angle)
                                    val wx = center.x + (dist * cos(rad)).toFloat()
                                    val wy = center.y + (dist * sin(rad)).toFloat()

                                    val wireColor = when (i % 4) {
                                        0 -> Color(0xFF1E88E5) // Blue
                                        1 -> Color(0xFFD32F2F) // Red
                                        2 -> Color(0xFF212121) // Black
                                        else -> Color(0xFF43A047) // Green Ground
                                    }

                                    drawCircle(
                                        color = wireColor,
                                        radius = wireR,
                                        center = Offset(wx, wy)
                                    )
                                    drawCircle(
                                        color = Color.White,
                                        radius = wireR,
                                        center = Offset(wx, wy),
                                        style = Stroke(width = 1.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Results & NEC Fill Metrics
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (state.isConduitOverfilled) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
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
                            text = "NEC FILL PERCENTAGE METRICS",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        )
                        IconButton(onClick = {
                            val sizeName = ConduitFillUiState.TRADE_SIZES[state.selectedTradeSizeIndex]
                            val info = "Conduit Fill Sizing (${sizeName} ${state.conduitType.name}):\n" +
                                    "Total Conductors: ${state.totalConductorsCount}\n" +
                                    "Wire Area: ${String.format("%.4f", state.totalWiresAreaSqIn)} sq in\n" +
                                    "Conduit Total Area: ${String.format("%.4f", state.conduitTotalAreaSqIn)} sq in\n" +
                                    "Max Permissible Area (${state.maxAllowedFillPct}%): ${String.format("%.4f", state.maxAllowedAreaSqIn)} sq in\n" +
                                    "Actual Fill: ${String.format("%.1f", state.actualFillPct)}%\n" +
                                    "Status: ${if (state.isConduitOverfilled) "OVERFILLED" else "PASS"}\n" +
                                    "Recommended Minimum Size: ${state.recommendedTradeSize}"
                            clipboardManager.setText(AnnotatedString(info))
                            viewModel.logConduitFill()
                            Toast.makeText(context, "Copied Conduit Fill Summary!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                    }

                    // Progress Fill Bar
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                "Actual Fill: ${String.format("%.1f", state.actualFillPct)}%",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                "Max Code Limit: ${String.format("%.0f", state.maxAllowedFillPct)}%",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                        LinearProgressIndicator(
                            progress = { (state.actualFillPct / 100.0).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                            color = if (state.isConduitOverfilled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }

                    // Status Badge
                    Surface(
                        color = if (state.isConduitOverfilled) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (state.isConduitOverfilled) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (state.isConduitOverfilled) Color(0xFFC62828) else Color(0xFF2E7D32),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (state.isConduitOverfilled) "Conduit Overfilled (> ${String.format("%.0f", state.maxAllowedFillPct)}% NEC Limit)" else "Compliant with NEC Chapter 9",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (state.isConduitOverfilled) Color(0xFFC62828) else Color(0xFF2E7D32)
                                    )
                                )
                                Text(
                                    text = if (state.isConduitOverfilled) "Increase conduit to ${state.recommendedTradeSize} or larger." else "Trade size ${ConduitFillUiState.TRADE_SIZES[state.selectedTradeSizeIndex]} has ample capacity.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (state.isConduitOverfilled) Color(0xFFC62828) else Color(0xFF2E7D32)
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Minimum Wire Gauge Ampacity Sizer
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "CIRCUIT WIRE AMPACITY ESTIMATOR (NEC 310.16)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )

                    OutlinedTextField(
                        value = targetAmpsInput,
                        onValueChange = {
                            targetAmpsInput = it
                            it.toDoubleOrNull()?.let { a ->
                                viewModel.updateAmpacityInputs(a, state.ambientTempC, state.isContinuousLoad)
                            }
                        },
                        label = { Text("Circuit Load (Amps)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Continuous Load (+25% Headroom)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text("NEC requires 125% ampacity for >3 hr loads", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = state.isContinuousLoad,
                            onCheckedChange = { chk ->
                                viewModel.updateAmpacityInputs(targetAmpsInput.toDoubleOrNull() ?: 20.0, state.ambientTempC, chk)
                            }
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Required Rated Ampacity: ${String.format("%.1f", state.requiredAmpacity)} A", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = state.recommendedWireGauge,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
