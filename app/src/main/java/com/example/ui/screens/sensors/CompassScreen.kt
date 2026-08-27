package com.example.ui.screens.sensors

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.utils.ToolIconMapper
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CompassScreen(
    viewModel: CompassViewModel,
    modifier: Modifier = Modifier
) {
    DisposableEffect(Unit) {
        viewModel.startSensors()
        onDispose { viewModel.stopSensors() }
    }

    val state by viewModel.compassState.collectAsState()
    val visuals = ToolIconMapper.getVisualsForTool("widget_compass")

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(visuals.containerColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = visuals.icon,
                            contentDescription = null,
                            tint = visuals.contentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "DIGITAL COMPASS & BEARING",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.1.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Azimuth, Cardinal & Sight Line Lock",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // Big Heading Readout
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${"%.0f".format(state.trueAzimuth)}° ${state.cardinalDirection}",
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF38BDF8)
                    )
                    Text(
                        text = "TRUE NORTH AZIMUTH",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                        color = Color(0xFF94A3B8)
                    )

                    state.lockedBearing?.let { lock ->
                        Spacer(modifier = Modifier.height(8.dp))
                        val off = state.bearingOffLock ?: 0f
                        Text(
                            text = "Locked Sight: ${"%.0f".format(lock)}° (Dev: ${if (off >= 0) "+${"%.0f".format(off)}" else "%.0f".format(off)}°)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (kotlin.math.abs(off) <= 3) Color(0xFF4ADE80) else Color(0xFFF87171)
                        )
                    }
                }
            }

            // Interactive Compass Dial Canvas
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(260.dp)) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.width / 2f - 10f

                    // Outer bezel ring
                    drawCircle(
                        color = Color(0xFF334155),
                        radius = radius,
                        center = center,
                        style = Stroke(width = 8f)
                    )

                    // Rotate compass dial according to heading
                    rotate(-state.trueAzimuth, pivot = center) {
                        // Tick marks
                        for (i in 0 until 360 step 15) {
                            val angleRad = (i - 90) * (PI / 180.0)
                            val isMajor = i % 90 == 0
                            val innerR = if (isMajor) radius - 24f else radius - 14f
                            val startX = center.x + innerR * cos(angleRad).toFloat()
                            val startY = center.y + innerR * sin(angleRad).toFloat()
                            val endX = center.x + radius * cos(angleRad).toFloat()
                            val endY = center.y + radius * sin(angleRad).toFloat()

                            drawLine(
                                color = if (isMajor) Color(0xFF38BDF8) else Color(0xFF64748B),
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = if (isMajor) 4f else 2f
                            )
                        }

                        // North Arrow Needle
                        val northPath = Path().apply {
                            moveTo(center.x, center.y - radius + 30f)
                            lineTo(center.x - 14f, center.y)
                            lineTo(center.x + 14f, center.y)
                            close()
                        }
                        drawPath(northPath, color = Color(0xFFEF4444))

                        // South Arrow Needle
                        val southPath = Path().apply {
                            moveTo(center.x, center.y + radius - 30f)
                            lineTo(center.x - 14f, center.y)
                            lineTo(center.x + 14f, center.y)
                            close()
                        }
                        drawPath(southPath, color = Color(0xFF94A3B8))
                    }

                    // Static top sight reticle
                    drawLine(
                        color = Color(0xFFFACC15),
                        start = Offset(center.x, center.y - radius - 5f),
                        end = Offset(center.x, center.y - radius + 25f),
                        strokeWidth = 6f
                    )

                    // Center Bullseye Level
                    drawCircle(
                        color = Color(0xFF0EA5E9),
                        radius = 12f,
                        center = center
                    )
                }
            }

            // Lock Sight Line Action Button
            Button(
                onClick = { viewModel.toggleLockBearing() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.lockedBearing != null) Color(0xFFDC2626) else MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("button_lock_bearing")
            ) {
                Icon(
                    imageVector = if (state.lockedBearing != null) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (state.lockedBearing != null) "Unlock Target Bearing" else "Lock Target Bearing Sight")
            }

            // Pitch & Roll Sensor Readout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Device Pitch", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${"%.1f".format(state.pitchDegrees)}°", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Device Roll", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${"%.1f".format(state.rollDegrees)}°", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }

            // Figure-8 Compass Calibration Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (state.isCalibrating) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Magnetometer Compass Calibration",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Text(
                        text = "To clear electromagnetic interference and fix direction drift, wave your device in a wide figure-8 motion (∞) through the air.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (state.isCalibrating) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Wave device in Figure-8 ∞ ...",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${"%.0f".format(state.calibrationProgress * 100)}%",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            androidx.compose.material3.LinearProgressIndicator(
                                progress = { state.calibrationProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Magnetic Flux: ${"%.1f".format(state.magneticStrengthuT)} μT",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Button(
                                onClick = { viewModel.startCalibration() },
                                modifier = Modifier.testTag("button_calibrate_compass")
                            ) {
                                Text("Start Figure-8 Calibration")
                            }
                        }
                    }
                }
            }

            // Manual Simulation Test Slider
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Navigation, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Manual Dial Heading Simulation", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    }
                    Slider(
                        value = state.azimuthDegrees,
                        onValueChange = { viewModel.setManualAzimuthSim(it) },
                        valueRange = 0f..359f,
                        modifier = Modifier.testTag("slider_manual_azimuth")
                    )
                }
            }
        }
    }
}
