package com.example.ui.screens.sensors

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.woodworking.ResultBadge

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StrobeTachometerScreen(
    viewModel: StrobeTachometerViewModel,
    modifier: Modifier = Modifier
) {
    val mode by viewModel.mode.collectAsState()
    val rpm by viewModel.rpm.collectAsState()
    val bladeCount by viewModel.bladeCount.collectAsState()
    val dutyCyclePercent by viewModel.dutyCyclePercent.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val detectedRpm by viewModel.detectedRpm.collectAsState()
    val isOpticalAnalyzing by viewModel.isOpticalAnalyzing.collectAsState()
    val opticalConfidence by viewModel.opticalConfidence.collectAsState()
    val opticalWaveform by viewModel.opticalWaveform.collectAsState()
    val lastLogSaved by viewModel.lastLogSaved.collectAsState()

    var machineryNote by remember { mutableStateOf("Wood Lathe Spindle") }

    val activePrimaryColor = if (isRunning || isOpticalAnalyzing) Color(0xFFD97706) else MaterialTheme.colorScheme.primary

    // Screen pulse animation
    val transition = rememberInfiniteTransition(label = "strobe_pulse")
    val alphaAnim by transition.animateFloat(
        initialValue = 0.1f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (60.0 / rpm.coerceAtLeast(100) * 1000.0).toInt().coerceIn(20, 1000),
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "strobe_alpha"
    )

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
            // Mode Tab Selector
            TabRow(
                selectedTabIndex = if (mode == TachometerMode.STROBE_FLASH) 0 else 1,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Tab(
                    selected = mode == TachometerMode.STROBE_FLASH,
                    onClick = { viewModel.setMode(TachometerMode.STROBE_FLASH) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("LED Strobe Flash", fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = mode == TachometerMode.OPTICAL_CAMERA,
                    onClick = { viewModel.setMode(TachometerMode.OPTICAL_CAMERA) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Camera Optical", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // Hero Header Card
            Card(
                colors = CardDefaults.cardColors(containerColor = activePrimaryColor.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (mode == TachometerMode.STROBE_FLASH) "OPTICAL STROBE TACHOMETER" else "CAMERA OPTICAL TACHOMETER",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp),
                            color = activePrimaryColor
                        )
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isRunning || isOpticalAnalyzing) Color(0xFF16A34A).copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f),
                                    CircleShape
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isRunning || isOpticalAnalyzing) "LIVE ACTIVE" else "STANDBY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isRunning || isOpticalAnalyzing) Color(0xFF16A34A) else Color.Gray
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (mode == TachometerMode.STROBE_FLASH)
                            "Pulsing LED flash freezes rotating blades, chucks & pulleys to verify RPM without physical contact."
                        else
                            "Uses camera optical frame analysis to compute RPM by detecting reflective tape passes and blade frequency.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (mode == TachometerMode.STROBE_FLASH) {
                // Visual Strobe Flash Indicator Box
                if (isRunning) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .background(
                                color = Color.White.copy(alpha = alphaAnim),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⚡ STROBE LIGHT ACTIVE ($rpm RPM / ${String.format("%.1f", rpm / 60.0)} Hz) ⚡",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }

                // Strobe Result Badge
                ResultBadge(
                    title = "STROBE TARGET FREQUENCY",
                    value = "$rpm RPM",
                    unit = "${String.format("%.2f", rpm / 60.0)} Hz | ${rpm / bladeCount} RPM/blade ($bladeCount-blade)",
                    color = activePrimaryColor,
                    modifier = Modifier.fillMaxWidth()
                )

                // Start/Stop Strobe Button
                Button(
                    onClick = { viewModel.toggleStrobe() },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = activePrimaryColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("toggle_strobe_button")
                ) {
                    Icon(if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRunning) "STOP STROBE FLASH" else "START STROBE FLASH",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // Harmonic Multiplier Helper Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = activePrimaryColor, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Harmonic Multiplier Check", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }
                        Text(
                            text = "If rotating object appears stationary at 2x or 0.5x flash rate, test harmonic multipliers to find true fundamental RPM:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { viewModel.multiplyHarmonic(0.5f) },
                                modifier = Modifier.weight(1f)
                            ) { Text("0.5× (${rpm / 2})") }
                            OutlinedButton(
                                onClick = { viewModel.multiplyHarmonic(2.0f) },
                                modifier = Modifier.weight(1f)
                            ) { Text("2.0× (${rpm * 2})") }
                        }
                    }
                }

                // Speed Adjustment Controls
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Fine Speed Tuning",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Slider(
                            value = rpm.toFloat(),
                            onValueChange = { viewModel.setPresetRpm(it.toInt()) },
                            valueRange = 100f..15000f,
                            modifier = Modifier.fillMaxWidth().testTag("rpm_slider")
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(onClick = { viewModel.adjustRpm(-100) }, modifier = Modifier.weight(1f)) { Text("-100") }
                            OutlinedButton(onClick = { viewModel.adjustRpm(-10) }, modifier = Modifier.weight(1f)) { Text("-10") }
                            OutlinedButton(onClick = { viewModel.adjustRpm(-1) }, modifier = Modifier.weight(1f)) { Text("-1") }
                            OutlinedButton(onClick = { viewModel.adjustRpm(+1) }, modifier = Modifier.weight(1f)) { Text("+1") }
                            OutlinedButton(onClick = { viewModel.adjustRpm(+10) }, modifier = Modifier.weight(1f)) { Text("+10") }
                            OutlinedButton(onClick = { viewModel.adjustRpm(+100) }, modifier = Modifier.weight(1f)) { Text("+100") }
                        }

                        // Blade / Target Markers Count
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Blade / Mark Count: $bladeCount", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(1, 2, 3, 4, 6, 8).forEach { count ->
                                    FilterChip(
                                        selected = bladeCount == count,
                                        onClick = { viewModel.setBladeCount(count) },
                                        label = { Text("$count") }
                                    )
                                }
                            }
                        }
                    }
                }

            } else {
                // CAMERA OPTICAL MODE
                ResultBadge(
                    title = "DETECTED OPTICAL ROTATIONAL SPEED",
                    value = if (detectedRpm > 0) "$detectedRpm RPM" else "-- RPM",
                    unit = if (detectedRpm > 0) "${String.format("%.2f", detectedRpm / 60.0)} Hz | Signal Confidence: ${(opticalConfidence * 100).toInt()}%" else "Aim camera at reflective tape on shaft",
                    color = activePrimaryColor,
                    modifier = Modifier.fillMaxWidth()
                )

                // Start/Stop Optical Analysis
                Button(
                    onClick = { viewModel.toggleOpticalAnalysis() },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = activePrimaryColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("toggle_optical_button")
                ) {
                    Icon(if (isOpticalAnalyzing) Icons.Default.Stop else Icons.Default.Speed, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isOpticalAnalyzing) "STOP OPTICAL SAMPLING" else "START CAMERA OPTICAL SAMPLING",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // Optical Luminance Waveform Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Optical Luminance Modulation Waveform", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                                .padding(8.dp)
                        ) {
                            if (opticalWaveform.isNotEmpty()) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val w = size.width
                                    val h = size.height
                                    val midY = h / 2f
                                    val stepX = w / (opticalWaveform.size - 1).coerceAtLeast(1)

                                    // Center grid line
                                    drawLine(
                                        color = Color.DarkGray,
                                        start = Offset(0f, midY),
                                        end = Offset(w, midY),
                                        strokeWidth = 1.dp.toPx()
                                    )

                                    val path = Path()
                                    opticalWaveform.forEachIndexed { index, value ->
                                        val x = index * stepX
                                        val y = midY - (value * (h * 0.4f))
                                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                    }
                                    drawPath(path, Color(0xFF38BDF8), style = Stroke(width = 2.5.dp.toPx()))
                                }
                            } else {
                                Text(
                                    text = "Waveform waiting for optical stream...",
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Optical Peak Quality", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${(opticalConfidence * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = activePrimaryColor)
                        }
                        LinearProgressIndicator(
                            progress = { opticalConfidence },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = activePrimaryColor,
                            trackColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }

            // Machinery Speed Presets
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Standard Machinery Reference Speeds",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        viewModel.presets.forEach { preset ->
                            FilterChip(
                                selected = rpm == preset.rpm,
                                onClick = {
                                    viewModel.setPresetRpm(preset.rpm)
                                    machineryNote = preset.name
                                },
                                label = { Text("${preset.rpm} RPM: ${preset.name}") }
                            )
                        }
                    }
                }
            }

            // Save Measurement to Room Database
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Log Tachometer Verification", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    OutlinedTextField(
                        value = machineryNote,
                        onValueChange = { machineryNote = it },
                        label = { Text("Machinery / Motor Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { viewModel.saveTachometerLog(machineryNote) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(if (lastLogSaved) Icons.Default.CheckCircle else Icons.Default.BookmarkBorder, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (lastLogSaved) "RPM Log Saved to Database" else "Save RPM Measurement to Log")
                    }
                }
            }
        }
    }
}
