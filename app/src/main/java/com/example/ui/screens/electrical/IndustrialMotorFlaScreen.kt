package com.example.ui.screens.electrical

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndustrialMotorFlaScreen(
    viewModel: IndustrialMotorFlaViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Industrial Motor FLA Sizer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "NEC Article 430 Branch Circuit & Protection",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        clipboardManager.setText(AnnotatedString(state.calculationSummary))
                        Toast.makeText(context, "Motor calculation copied", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Copy Summary")
                    }
                    IconButton(onClick = {
                        viewModel.saveToLogs()
                        Toast.makeText(context, "Saved to project log", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = "Save")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // 1. Hero Summary Card (FLA & Wire)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "FULL LOAD CURRENT (NEC FLA)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "${String.format("%.1f", state.tableFla)} Amps",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${state.horsepower} HP (${String.format("%.2f", state.nameplateKw)} kW) @ ${state.voltage}V ${state.phase.label}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "MIN WIRE",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${String.format("%.1f", state.minConductorAmpacity)} A",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Single-Line Motor Feed Diagram
                        MotorSingleLineDiagram(
                            state = state,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(115.dp)
                        )
                    }
                }
            }

            // 2. Motor Parameters Configuration
            item {
                Card(
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Motor Nameplate Specifications",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Phase Selector
                        Text("Phase & System:", style = MaterialTheme.typography.labelMedium)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MotorPhase.values().forEach { ph ->
                                FilterChip(
                                    selected = state.phase == ph,
                                    onClick = { viewModel.setPhase(ph) },
                                    label = { Text(ph.label) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Voltage Selector
                        Text("Operating Voltage (VAC):", style = MaterialTheme.typography.labelMedium)
                        val voltages = if (state.phase == MotorPhase.THREE_PHASE) {
                            listOf(200, 208, 230, 460, 575)
                        } else {
                            listOf(115, 200, 208, 230)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            voltages.forEach { v ->
                                FilterChip(
                                    selected = state.voltage == v,
                                    onClick = { viewModel.setVoltage(v) },
                                    label = { Text("${v}V") }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Horsepower Chips & Custom Stepper
                        Text("Motor Rating (Horsepower):", style = MaterialTheme.typography.labelMedium)
                        val commonHp = if (state.phase == MotorPhase.THREE_PHASE) {
                            listOf(1.0, 3.0, 5.0, 7.5, 10.0, 15.0, 20.0, 25.0, 30.0, 50.0, 75.0, 100.0)
                        } else {
                            listOf(0.25, 0.5, 0.75, 1.0, 1.5, 2.0, 3.0, 5.0)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            commonHp.take(4).forEach { hp ->
                                FilterChip(
                                    selected = state.horsepower == hp,
                                    onClick = { viewModel.setHorsepower(hp) },
                                    label = { Text("${if (hp % 1.0 == 0.0) hp.toInt() else hp} HP") }
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            commonHp.drop(4).take(4).forEach { hp ->
                                FilterChip(
                                    selected = state.horsepower == hp,
                                    onClick = { viewModel.setHorsepower(hp) },
                                    label = { Text("${if (hp % 1.0 == 0.0) hp.toInt() else hp} HP") }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Horsepower Increment Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Exact HP: ${state.horsepower} HP",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Row {
                                OutlinedButton(
                                    onClick = { viewModel.setHorsepower((state.horsepower - 5.0).coerceAtLeast(0.5)) },
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text("-5 HP")
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                OutlinedButton(
                                    onClick = { viewModel.setHorsepower(state.horsepower + 5.0) },
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text("+5 HP")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Service Factor & Conductor Material
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Service Factor:", style = MaterialTheme.typography.labelSmall)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf(1.0, 1.15, 1.25).forEach { sf ->
                                        FilterChip(
                                            selected = state.serviceFactor == sf,
                                            onClick = { viewModel.setServiceFactor(sf) },
                                            label = { Text("SF $sf") }
                                        )
                                    }
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text("Conductor Material:", style = MaterialTheme.typography.labelSmall)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf("Copper", "Aluminum").forEach { mat ->
                                        FilterChip(
                                            selected = state.conductorMaterial == mat,
                                            onClick = { viewModel.setConductorMaterial(mat) },
                                            label = { Text(mat) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Sizing & Protection Specifications Grid
            item {
                Card(
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "NEC 430 Protection & Conductor Schedule",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        MotorSpecRow(
                            label = "Branch Circuit Conductor (125% FLA)",
                            value = state.recommendedWireSize,
                            highlight = true
                        )
                        MotorSpecRow(
                            label = "Overload Heater / Relay (NEC 430.32)",
                            value = "${String.format("%.1f", state.overloadRatingAmps)} A (Max ${String.format("%.1f", state.maxOverloadTripAmps)} A)"
                        )
                        MotorSpecRow(
                            label = "Dual-Element Time Delay Fuse (175%)",
                            value = "${state.timeDelayFuseStandardAmps} A Class RK5/J"
                        )
                        MotorSpecRow(
                            label = "Inverse-Time Circuit Breaker (250%)",
                            value = "${state.inverseTimeBreakerAmps} A Standard Breaker"
                        )
                        MotorSpecRow(
                            label = "Non-Time Delay Fuse (300%)",
                            value = "${state.nonTimeDelayFuseAmps} A Class H/K"
                        )
                        MotorSpecRow(
                            label = "Instantaneous Trip MCP (800%)",
                            value = "${state.instantaneousMcpAmps} A Mag-Only"
                        )
                        MotorSpecRow(
                            label = "Starter Size Rating",
                            value = state.starterNemaSize,
                            highlight = true
                        )
                        MotorSpecRow(
                            label = "Locked Rotor Inrush Current (LRA)",
                            value = "≈ ${String.format("%.0f", state.lockedRotorAmpsEstimate)} A (NEMA Code ${state.codeLetter})"
                        )
                        MotorSpecRow(
                            label = "Min Disconnect Switch Size",
                            value = "${String.format("%.1f", state.disconnectMinAmps)} A (${state.disconnectMinHpRating.toInt()} HP rated)"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MotorSpecRow(
    label: String,
    value: String,
    highlight: Boolean = false
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
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1.3f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (highlight) FontWeight.ExtraBold else FontWeight.Bold,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
}

@Composable
private fun MotorSingleLineDiagram(
    state: IndustrialMotorUiState,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .background(Color(0xFF1B1D20), RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        val w = size.width
        val h = size.height

        val lineY = h * 0.45f
        val colorLine = Color(0xFF64B5F6)
        val colorMotor = Color(0xFF81C784)
        val colorBreaker = Color(0xFFFFB74D)

        // Main Feeder Line
        drawLine(
            color = colorLine,
            start = Offset(20f, lineY),
            end = Offset(w - 60f, lineY),
            strokeWidth = 3.5f
        )

        // 1. Breaker Symbol at 20%
        val cbX = w * 0.20f
        drawRect(
            color = colorBreaker,
            topLeft = Offset(cbX - 12f, lineY - 14f),
            size = Size(24f, 28f),
            style = Stroke(width = 2.5f)
        )

        // 2. Contactor Symbol at 45%
        val contX = w * 0.45f
        drawLine(color = Color.White, start = Offset(contX - 10f, lineY - 12f), end = Offset(contX + 10f, lineY - 12f), strokeWidth = 3f)
        drawLine(color = Color.White, start = Offset(contX - 10f, lineY + 12f), end = Offset(contX + 10f, lineY + 12f), strokeWidth = 3f)

        // 3. Overload Relay Symbol at 65%
        val olX = w * 0.65f
        drawCircle(color = Color(0xFFEF5350), radius = 10f, center = Offset(olX, lineY), style = Stroke(width = 2.5f))

        // 4. Motor Symbol at 85%
        val mX = w * 0.85f
        drawCircle(color = colorMotor, radius = 22f, center = Offset(mX, lineY))
        drawCircle(color = Color.White, radius = 22f, center = Offset(mX, lineY), style = Stroke(width = 2.5f))
    }
}
