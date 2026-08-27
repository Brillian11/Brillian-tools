package com.example.ui.screens.sensors

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.woodworking.ResultBadge

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BleMultimeterScreen(
    viewModel: BleMultimeterViewModel,
    modifier: Modifier = Modifier
) {
    val isConnected by viewModel.isConnected.collectAsState()
    val deviceName by viewModel.deviceName.collectAsState()
    val function by viewModel.function.collectAsState()
    val isHold by viewModel.isHold.collectAsState()
    val reading by viewModel.reading.collectAsState()
    val history by viewModel.history.collectAsState()
    val alarmThreshold by viewModel.alarmThreshold.collectAsState()
    val isAlarmTriggered by viewModel.isAlarmTriggered.collectAsState()
    val lastLogSaved by viewModel.lastLogSaved.collectAsState()

    var circuitNote by remember { mutableStateOf("Main 200A Service Feeder L1-N") }

    val activeColor = if (isAlarmTriggered) Color(0xFFDC2626) else Color(0xFFD97706)

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Card(
                colors = CardDefaults.cardColors(containerColor = activeColor.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ElectricMeter, contentDescription = null, tint = activeColor)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "BLE SMART MULTIMETER & CLAMP",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp),
                                color = activeColor
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isConnected) Color(0xFF16A34A).copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f),
                                    CircleShape
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (isConnected) Icons.Default.BluetoothConnected else Icons.Default.BluetoothDisabled,
                                    contentDescription = null,
                                    tint = if (isConnected) Color(0xFF16A34A) else Color.Gray,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isConnected) "CONNECTED" else "OFFLINE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isConnected) Color(0xFF16A34A) else Color.Gray
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$deviceName (30+ ft Wireless Range)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Wireless telemetry stream for measuring live switchgear and breaker panels safely outside arc-flash boundaries.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Arc Flash Safety Advisory
            if (function.isArcFlashHazard) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("NFPA 70E Arc-Flash Remote Safety", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF78350F))
                            Text("Maintain safe boundary outside flash perimeter while energizing high-voltage panels.", fontSize = 11.sp, color = Color(0xFF92400E))
                        }
                    }
                }
            }

            // Digital LCD Screen Display
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, if (isAlarmTriggered) Color(0xFFDC2626) else Color(0xFF334155), RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top LCD Indicators
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("AUTO RANGE", color = Color(0xFF94A3B8), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        if (isHold) {
                            Text("HOLD", color = Color(0xFFFBBF24), fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                        Text(function.label.uppercase(), color = Color(0xFF38BDF8), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Giant Digital LCD Readout
                    Text(
                        text = if (isConnected) String.format("%.2f", reading.value) else "----",
                        color = if (isAlarmTriggered) Color(0xFFEF4444) else Color(0xFF34D399),
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )

                    Text(
                        text = reading.unit,
                        color = Color(0xFFE2E8F0),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Min / Max / Avg Row
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("MIN", color = Color(0xFF94A3B8), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text(String.format("%.2f", reading.minVal), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("AVG", color = Color(0xFF94A3B8), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text(String.format("%.2f", reading.avgVal), color = Color(0xFF67E8F9), fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("MAX", color = Color(0xFF94A3B8), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text(String.format("%.2f", reading.maxVal), color = Color(0xFFF87171), fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            // Rolling Live Chart Waveform
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Live Trend Waveform", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Text("${reading.unit} vs Time", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            // Draw baseline grid lines
                            drawLine(Color.DarkGray, Offset(0f, h * 0.25f), Offset(w, h * 0.25f), strokeWidth = 0.5.dp.toPx())
                            drawLine(Color.DarkGray, Offset(0f, h * 0.50f), Offset(w, h * 0.50f), strokeWidth = 0.5.dp.toPx())
                            drawLine(Color.DarkGray, Offset(0f, h * 0.75f), Offset(w, h * 0.75f), strokeWidth = 0.5.dp.toPx())

                            if (history.size > 1) {
                                val minH = (history.minOrNull() ?: 0f) * 0.95f
                                val maxH = (history.maxOrNull() ?: 100f) * 1.05f
                                val range = (maxH - minH).coerceAtLeast(1f)

                                val stepX = w / (history.size - 1)
                                val path = Path()

                                history.forEachIndexed { i, v ->
                                    val x = i * stepX
                                    val norm = (v - minH) / range
                                    val y = h - (norm * h)
                                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                }

                                drawPath(path, Color(0xFF34D399), style = Stroke(width = 2.5.dp.toPx()))
                            }
                        }
                    }
                }
            }

            // Quick Actions (Hold & Connect)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { viewModel.toggleHold() },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isHold) Color(0xFFD97706) else MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1f).height(50.dp).testTag("toggle_hold_button")
                ) {
                    Icon(if (isHold) Icons.Default.PlayArrow else Icons.Default.Pause, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isHold) "RESUME" else "DATA HOLD", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { viewModel.toggleConnection() },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Icon(if (isConnected) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isConnected) "DISCONNECT" else "CONNECT", fontWeight = FontWeight.Bold)
                }
            }

            // Multimeter Function Mode Chips
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select Multimeter Function", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MeterFunction.values().forEach { f ->
                            FilterChip(
                                selected = function == f,
                                onClick = { viewModel.setFunction(f) },
                                label = { Text("${f.label} (${f.unit})") }
                            )
                        }
                    }
                }
            }

            // Alarm Threshold Setting
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("High Threshold Warning: ${alarmThreshold.toInt()} ${function.unit}", fontWeight = FontWeight.Bold)
                        if (isAlarmTriggered) {
                            Text("ALERT TRIGGERED!", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    Slider(
                        value = alarmThreshold,
                        onValueChange = { viewModel.setAlarmThreshold(it) },
                        valueRange = (alarmThreshold * 0.2f)..(alarmThreshold * 3.0f).coerceAtLeast(10f),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Save Multimeter Log
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Save Telemetry Data Log", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    OutlinedTextField(
                        value = circuitNote,
                        onValueChange = { circuitNote = it },
                        label = { Text("Circuit / Equipment Tested") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { viewModel.saveMultimeterLog(circuitNote) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(if (lastLogSaved) Icons.Default.CheckCircle else Icons.Default.BookmarkBorder, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (lastLogSaved) "Multimeter Log Saved" else "Save Telemetry to Local Database")
                    }
                }
            }
        }
    }
}
