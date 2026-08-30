package com.example.ui.screens.sensors

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Size
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
fun VibrationAnalyzerScreen(
    viewModel: VibrationAnalyzerViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        throw RuntimeException("Crash triggered by back button on Vibration Analyzer")
    }
    val isSampling by viewModel.isSampling.collectAsState()
    val machineClass by viewModel.machineClass.collectAsState()
    val operatingRpm by viewModel.operatingRpm.collectAsState()
    val metrics by viewModel.metrics.collectAsState()
    val timeWaveform by viewModel.timeWaveform.collectAsState()
    val frequencySpectrum by viewModel.frequencySpectrum.collectAsState()
    val lastLogSaved by viewModel.lastLogSaved.collectAsState()

    var machineryName by remember { mutableStateOf("10\" Table Saw Arbor Motor") }

    val statusColor = Color(metrics.severityZone.colorHex)

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
                colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.12f)),
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
                            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = statusColor)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "VIBRATION SPECTRUM ANALYZER",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp),
                                color = statusColor
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(statusColor.copy(alpha = 0.2f), CircleShape)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = metrics.severityZone.zone,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Hardware accelerometer FFT spectral analysis & ISO 10816-3 machinery vibration severity evaluation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ISO 10816-3 Severity Status Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (metrics.severityZone == IsoSeverityZone.ZONE_D || metrics.severityZone == IsoSeverityZone.ZONE_C) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "ISO 10816-3: ${metrics.severityZone.status}",
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Diagnostic: ${metrics.dominantFault}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Key Results Badges Triad
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                ResultBadge(
                    title = "VELOCITY RMS",
                    value = String.format("%.2f", metrics.rmsVelocityMmS),
                    unit = "mm/s RMS",
                    color = statusColor,
                    modifier = Modifier.weight(1f)
                )
                ResultBadge(
                    title = "PEAK ACCEL",
                    value = String.format("%.2f", metrics.peakAccelG),
                    unit = "g Peak",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                ResultBadge(
                    title = "PEAK FREQ",
                    value = String.format("%.1f", metrics.peakFreqHz),
                    unit = "Hz (${metrics.peakRpm} RPM)",
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }

            // FFT Frequency Spectrum Canvas Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("FFT Frequency Spectrum (0 - 120 Hz)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Text("1X: ${operatingRpm / 60}Hz | 2X: ${(operatingRpm / 60) * 2}Hz", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
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

                            if (frequencySpectrum.isNotEmpty()) {
                                val barWidth = (w / frequencySpectrum.size) * 0.75f
                                frequencySpectrum.forEachIndexed { i, (hz, amp) ->
                                    val x = (w / frequencySpectrum.size) * i
                                    val barH = (amp / 1.0f).coerceIn(0.05f, 1.0f) * (h * 0.9f)
                                    val y = h - barH

                                    val barColor = when {
                                        amp > 0.5f -> Color(0xFFEF4444)
                                        amp > 0.2f -> Color(0xFFF59E0B)
                                        else -> Color(0xFF38BDF8)
                                    }

                                    drawRect(
                                        color = barColor,
                                        topLeft = Offset(x, y),
                                        size = Size(barWidth, barH)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Real-Time Acceleration Time Waveform Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Time-Domain Acceleration Waveform (g)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val midY = h / 2f

                            drawLine(Color.DarkGray, Offset(0f, midY), Offset(w, midY), strokeWidth = 1.dp.toPx())

                            if (timeWaveform.isNotEmpty()) {
                                val stepX = w / (timeWaveform.size - 1).coerceAtLeast(1)
                                val path = Path()
                                timeWaveform.forEachIndexed { idx, v ->
                                    val x = idx * stepX
                                    val y = midY - (v * (h * 0.4f))
                                    if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                }
                                drawPath(path, Color(0xFF10B981), style = Stroke(width = 2.dp.toPx()))
                            }
                        }
                    }
                }
            }

            // Start/Stop Toggle Button
            Button(
                onClick = {
                    if (isSampling) viewModel.stopAnalyzer() else viewModel.startAnalyzer()
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isSampling) Color(0xFFDC2626) else MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("toggle_vibration_sampling")
            ) {
                Icon(if (isSampling) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isSampling) "STOP VIBRATION SAMPLING" else "RESUME LIVE SAMPLING", fontWeight = FontWeight.Bold)
            }

            // Machine Class Selector (ISO 10816-3)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Machine Classification (ISO 10816-3)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MachineClass.values().forEach { mClass ->
                            FilterChip(
                                selected = machineClass == mClass,
                                onClick = { viewModel.setMachineClass(mClass) },
                                label = { Text(mClass.label) }
                            )
                        }
                    }
                    Text(
                        text = machineClass.powerRange,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Operating RPM Setting
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Operating Machine Speed: $operatingRpm RPM", fontWeight = FontWeight.Bold)
                        Text("${operatingRpm / 60} Hz", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = operatingRpm.toFloat(),
                        onValueChange = { viewModel.setOperatingRpm(it.toInt()) },
                        valueRange = 300f..10000f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Save Vibration Log
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Save Vibration Audit Report", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    OutlinedTextField(
                        value = machineryName,
                        onValueChange = { machineryName = it },
                        label = { Text("Machine Asset ID / Tag") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { viewModel.saveVibrationLog(machineryName) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(if (lastLogSaved) Icons.Default.CheckCircle else Icons.Default.BookmarkBorder, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (lastLogSaved) "Vibration Report Saved" else "Save Spectrum Audit to Database")
                    }
                }
            }
        }
    }
}
