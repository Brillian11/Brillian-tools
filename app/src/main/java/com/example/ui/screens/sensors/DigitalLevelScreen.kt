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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.sensor.InclinometerData
import com.example.ui.screens.woodworking.ResultBadge
import kotlin.math.abs

@Composable
fun DigitalLevelScreen(
    viewModel: DigitalLevelViewModel,
    modifier: Modifier = Modifier
) {
    val sensorData by viewModel.sensorData.collectAsState()
    val isHold by viewModel.isHold.collectAsState()

    val levelColor = if (sensorData.isLevel) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary

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
                colors = CardDefaults.cardColors(containerColor = levelColor.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DIGITAL INCLINOMETER & TUBE BUBBLE LEVEL",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp),
                        color = levelColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (sensorData.isLevel) "SURFACE IS PERFECTLY LEVEL (0.0°)" else "SURFACE INCLINED",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Real-time IMU incline sensor with central horizontal straight line level indicator and 2D bullseye gauge.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Readout Badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ResultBadge(
                    title = "PITCH ANGLE",
                    value = String.format("%+.1f°", sensorData.pitchDegrees),
                    unit = "vertical inclination",
                    color = levelColor,
                    modifier = Modifier.weight(1f)
                )
                ResultBadge(
                    title = "ROLL ANGLE",
                    value = String.format("%+.1f°", sensorData.rollDegrees),
                    unit = "horizontal tilt",
                    color = levelColor,
                    modifier = Modifier.weight(1f)
                )
            }

            // Straight Center Line Horizontal Level Gauge
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Horizontal Center Line Spirit Level",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    StraightLineLevelGauge(
                        sensorData = sensorData,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )
                }
            }

            // Circular Bullseye Level Canvas Gauge
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "2D Bullseye Crosshair Level Gauge",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    BullseyeLevelCanvas(
                        sensorData = sensorData,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }
            }

            // Controls (Tare & Hold Buttons)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { viewModel.setTare() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = levelColor),
                    modifier = Modifier.weight(1f).testTag("tare_zero_button")
                ) {
                    Text("Tare / Set Zero")
                }

                OutlinedButton(
                    onClick = { viewModel.resetTare() },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).testTag("reset_tare_button")
                ) {
                    Text("Reset Zero")
                }

                OutlinedButton(
                    onClick = { viewModel.toggleHold() },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).testTag("hold_readout_button")
                ) {
                    Text(if (isHold) "Resume" else "Hold")
                }
            }
        }
    }
}

@Composable
fun StraightLineLevelGauge(
    sensorData: InclinometerData,
    modifier: Modifier = Modifier
) {
    val isLevel = sensorData.isLevel
    val activeColor = if (isLevel) Color(0xFF2E7D32) else Color(0xFFD32F2F)
    val bgColor = Color(0xFF1E293B)
    val lineColor = if (isLevel) Color(0xFF4ADE80) else Color(0xFF94A3B8)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cy = h / 2f
        val tubeHeight = 36f
        val tubeTop = cy - (tubeHeight / 2f)

        // Draw outer spirit tube background
        drawRoundRect(
            color = bgColor,
            topLeft = Offset(10f, tubeTop),
            size = Size(w - 20f, tubeHeight),
            cornerRadius = CornerRadius(18f, 18f)
        )
        drawRoundRect(
            color = lineColor,
            topLeft = Offset(10f, tubeTop),
            size = Size(w - 20f, tubeHeight),
            cornerRadius = CornerRadius(18f, 18f),
            style = Stroke(width = 3f)
        )

        // Draw Center Zero Straight Line Axis across the whole middle
        drawLine(
            color = if (isLevel) Color(0xFF2E7D32) else Color(0xFF64748B),
            start = Offset(20f, cy),
            end = Offset(w - 20f, cy),
            strokeWidth = 4f
        )

        // Center Target Zone Vertical Tick Lines (0° tolerance boundaries)
        val cx = w / 2f
        val zoneWidth = 50f
        drawLine(
            color = activeColor,
            start = Offset(cx - zoneWidth / 2f, tubeTop - 6f),
            end = Offset(cx - zoneWidth / 2f, tubeTop + tubeHeight + 6f),
            strokeWidth = 3f
        )
        drawLine(
            color = activeColor,
            start = Offset(cx + zoneWidth / 2f, tubeTop - 6f),
            end = Offset(cx + zoneWidth / 2f, tubeTop + tubeHeight + 6f),
            strokeWidth = 3f
        )
        // Center Line Tick
        drawLine(
            color = activeColor,
            start = Offset(cx, tubeTop - 10f),
            end = Offset(cx, tubeTop + tubeHeight + 10f),
            strokeWidth = 4f
        )

        // Calculate horizontal bubble position based on roll angle
        val clampedRoll = sensorData.rollDegrees.coerceIn(-30f, 30f)
        val maxOffset = (w / 2f) - 40f
        val bubbleX = cx + (clampedRoll / 30f) * maxOffset

        // Draw level bubble along horizontal straight line
        drawCircle(
            color = activeColor,
            radius = 16f,
            center = Offset(bubbleX, cy)
        )
        drawCircle(
            color = Color.White,
            radius = 6f,
            center = Offset(bubbleX, cy)
        )
    }
}

@Composable
fun BullseyeLevelCanvas(
    sensorData: InclinometerData,
    modifier: Modifier = Modifier
) {
    val levelColor = if (sensorData.isLevel) Color(0xFF2E7D32) else Color(0xFFD32F2F)
    val circleOutlineColor = MaterialTheme.colorScheme.outline

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val maxRadius = minOf(cx, cy) * 0.85f

        // Draw Outer Target Circle
        drawCircle(
            color = circleOutlineColor,
            radius = maxRadius,
            center = Offset(cx, cy),
            style = Stroke(width = 4f)
        )

        // Draw Inner Center Target Zone (Level Threshold)
        drawCircle(
            color = levelColor.copy(alpha = 0.2f),
            radius = maxRadius * 0.2f,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = levelColor,
            radius = maxRadius * 0.2f,
            center = Offset(cx, cy),
            style = Stroke(width = 3f)
        )

        // Straight Axis Crosshairs
        drawLine(
            color = if (sensorData.isLevel) Color(0xFF2E7D32) else circleOutlineColor.copy(alpha = 0.6f),
            start = Offset(cx - maxRadius, cy),
            end = Offset(cx + maxRadius, cy),
            strokeWidth = if (sensorData.isLevel) 4f else 2f
        )
        drawLine(
            color = if (sensorData.isLevel) Color(0xFF2E7D32) else circleOutlineColor.copy(alpha = 0.6f),
            start = Offset(cx, cy - maxRadius),
            end = Offset(cx, cy + maxRadius),
            strokeWidth = if (sensorData.isLevel) 4f else 2f
        )

        // Calculate Bubble Displacement based on Roll (X) and Pitch (Y)
        val clampedRoll = sensorData.rollDegrees.coerceIn(-30f, 30f)
        val clampedPitch = sensorData.pitchDegrees.coerceIn(-30f, 30f)

        val bubbleX = cx + (clampedRoll / 30f) * (maxRadius * 0.75f)
        val bubbleY = cy - (clampedPitch / 30f) * (maxRadius * 0.75f)

        // Draw Moving Spirit Level Bubble
        drawCircle(
            color = levelColor,
            radius = 22f,
            center = Offset(bubbleX, bubbleY)
        )
    }
}
