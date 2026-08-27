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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrentLoopScalingScreen(
    viewModel: CurrentLoopScalingViewModel,
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
                            text = "4–20 mA Current Loop & Scaling",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Transmitter Calibration, NAMUR NE43 & PLC Counts",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        clipboardManager.setText(AnnotatedString(state.calculationSummary))
                        Toast.makeText(context, "Loop data copied to clipboard", Toast.LENGTH_SHORT).show()
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
            // 1. Hero Meter & Live Scaled Readings
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // NAMUR NE43 Status Chip
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(state.namurStatus.statusColorHex)
                        ) {
                            Text(
                                text = state.namurStatus.label,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Analog Arc Meter Canvas
                        AnalogMeterCanvas(
                            currentMa = state.currentMa,
                            percentage = state.percentage,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Live Readings Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            ReadingColumn(
                                label = "LOOP CURRENT",
                                value = "${String.format("%.3f", state.currentMa)} mA",
                                highlight = true
                            )
                            ReadingColumn(
                                label = "PROCESS VALUE",
                                value = "${String.format("%.2f", state.processVariable)} ${state.engineeringUnit}"
                            )
                            ReadingColumn(
                                label = "PERCENT SPAN",
                                value = "${String.format("%.1f", state.percentage)} %"
                            )
                            ReadingColumn(
                                label = "PLC COUNTS",
                                value = "${state.plcRawCounts}"
                            )
                        }
                    }
                }
            }

            // 2. Interactive Interactive Sliders & Quick Step Calibration
            item {
                Card(shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Interactive Signal Tuning & Calibration Steps",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick 5-Point Calibration Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                Pair(4.0, "0% (4mA)"),
                                Pair(8.0, "25% (8mA)"),
                                Pair(12.0, "50% (12mA)"),
                                Pair(16.0, "75% (16mA)"),
                                Pair(20.0, "100% (20mA)")
                            ).forEach { (ma, label) ->
                                OutlinedButton(
                                    onClick = { viewModel.updateCurrentMa(ma) },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp)
                                ) {
                                    Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Milliamp Slider (0 - 24 mA)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Current Loop (mA):", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            Text("${String.format("%.2f", state.currentMa)} mA", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Slider(
                            value = state.currentMa.toFloat(),
                            onValueChange = { viewModel.updateCurrentMa(it.toDouble()) },
                            valueRange = 0f..24f,
                            steps = 240
                        )

                        // Process Variable Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Process Variable (${state.engineeringUnit}):", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            Text("${String.format("%.2f", state.processVariable)} ${state.engineeringUnit}", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Slider(
                            value = state.processVariable.toFloat(),
                            onValueChange = { viewModel.updateProcessVariable(it.toDouble()) },
                            valueRange = state.pvMin.toFloat()..state.pvMax.toFloat()
                        )
                    }
                }
            }

            // 3. Process Span & Instrument Configuration
            item {
                Card(shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Transmitter Range & Transfer Function",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Transfer Function
                        Text("Sensor Response Curve:", style = MaterialTheme.typography.labelSmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ScalingTransferFunction.values().forEach { func ->
                                FilterChip(
                                    selected = state.transferFunction == func,
                                    onClick = { viewModel.setTransferFunction(func) },
                                    label = { Text(func.label, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick Unit Presets
                        Text("Engineering Units & Span Range:", style = MaterialTheme.typography.labelSmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                Triple(0.0, 100.0, "PSI"),
                                Triple(0.0, 10.0, "bar"),
                                Triple(0.0, 150.0, "°C"),
                                Triple(-40.0, 200.0, "°F"),
                                Triple(0.0, 500.0, "GPM")
                            ).forEach { (min, max, unit) ->
                                FilterChip(
                                    selected = state.engineeringUnit == unit && state.pvMin == min && state.pvMax == max,
                                    onClick = { viewModel.setProcessSpan(min, max, unit) },
                                    label = { Text("$min-$max $unit", fontSize = 10.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // PLC / DCS Profile
                        Text("PLC/DCS Analog Profile:", style = MaterialTheme.typography.labelSmall)
                        PlcDcsProfile.values().take(4).forEach { prof ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilterChip(
                                    selected = state.plcProfile == prof,
                                    onClick = { viewModel.setPlcProfile(prof) },
                                    label = { Text(prof.label, fontSize = 11.sp) }
                                )
                                Text(
                                    text = "${prof.minCount} to ${prof.maxCount}",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // 4. Loop Compliance & Burden Voltage Analysis
            item {
                Card(shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Loop Power & Burden Compliance",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (state.isLoopBurdenCompliant) Color(0xFF2E7D32) else Color(0xFFC62828)
                            ) {
                                Text(
                                    text = if (state.isLoopBurdenCompliant) "COMPLIANT" else "VOLTAGE STARVED",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LoopBurdenRow("Power Supply Voltage", "${String.format("%.1f", state.powerSupplyVdc)} VDC")
                        LoopBurdenRow("Transmitter Min Requirement", "${String.format("%.1f", state.txMinOperatingVdc)} VDC")
                        LoopBurdenRow("Sense Resistor (Shunt)", "${state.senseResistorOhms.toInt()} Ω (${String.format("%.2f", state.voltageDropSenseV)}V drop @ signal)")
                        LoopBurdenRow("Total Loop Burden Resistance", "${String.format("%.1f", state.totalLoopBurdenOhms)} Ω (Max Allowed: ${String.format("%.1f", state.maxAllowableBurdenOhms)} Ω)")
                        LoopBurdenRow("Voltage Available at Transmitter", "${String.format("%.2f", state.voltageAtTransmitterV)} VDC")
                        LoopBurdenRow("Compliance Safety Margin", "+${String.format("%.2f", state.loopVoltageMarginV)} VDC")
                    }
                }
            }
        }
    }
}

@Composable
private fun ReadingColumn(label: String, value: String, highlight: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = if (highlight) FontWeight.ExtraBold else FontWeight.Bold,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun LoopBurdenRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
}

@Composable
private fun AnalogMeterCanvas(
    currentMa: Double,
    percentage: Double,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .background(Color(0xFF191C20), RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h * 0.95f)
        val radius = h * 0.85f

        // Draw Arc Background (180 degrees from -180 to 0)
        drawArc(
            color = Color(0xFF37474F),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = 14f, cap = StrokeCap.Round)
        )

        // Draw Colored Valid Range (4mA to 20mA corresponds to 180 to 360 deg)
        val fillSweep = (percentage.coerceIn(0.0, 100.0) / 100.0 * 180.0).toFloat()
        drawArc(
            color = Color(0xFF00E676),
            startAngle = 180f,
            sweepAngle = fillSweep,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = 14f, cap = StrokeCap.Round)
        )

        // Draw Needle
        val angleRad = Math.toRadians(180.0 + fillSweep.toDouble())
        val needleEnd = Offset(
            x = (center.x + (radius - 16f) * cos(angleRad)).toFloat(),
            y = (center.y + (radius - 16f) * sin(angleRad)).toFloat()
        )

        drawLine(
            color = Color(0xFFFF5252),
            start = center,
            end = needleEnd,
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )

        // Center Pivot
        drawCircle(color = Color.White, radius = 7f, center = center)
        drawCircle(color = Color.Black, radius = 3f, center = center)
    }
}
