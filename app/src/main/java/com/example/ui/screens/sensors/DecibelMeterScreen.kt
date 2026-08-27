package com.example.ui.screens.sensors

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.screens.woodworking.ResultBadge
import com.example.ui.utils.ToolIconMapper

@Composable
fun DecibelMeterScreen(
    viewModel: DecibelMeterViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dbData by viewModel.dbData.collectAsState()

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasMicPermission = isGranted }
    )

    LaunchedEffect(Unit) {
        if (!hasMicPermission) {
            launcher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val warningColor = if (dbData.isOshaWarning) Color(0xFFDC2626) else MaterialTheme.colorScheme.primary
    val visuals = ToolIconMapper.getVisualsForTool("widget_decibel_meter")

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
                colors = CardDefaults.cardColors(containerColor = warningColor.copy(alpha = 0.12f)),
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
                            imageVector = if (hasMicPermission) Icons.Default.Mic else Icons.Default.MicOff,
                            contentDescription = null,
                            tint = visuals.contentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "DECIBEL SOUND METER",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp),
                            color = warningColor
                        )
                        Text(
                            text = "Acoustic Decibel & Safety Monitor",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // OSHA Danger Card
            if (dbData.isOshaWarning) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFDC2626)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "OSHA WARNING: Noise level exceeds 85 dB(A)! Approved hearing protection required.",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF991B1B)
                        )
                    }
                }
            }

            // Real-time Sound Gauge Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "${"%.1f".format(dbData.currentDb)} dB",
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                        color = warningColor
                    )
                    Text(
                        text = when {
                            dbData.currentDb < 45f -> "Quiet / Ambient Level"
                            dbData.currentDb < 70f -> "Normal Conversation Level"
                            dbData.currentDb < 85f -> "Loud Environment / Machinery"
                            else -> "DANGER: High Noise Hazard"
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Real-Time Sound Frequency Spectrum Bars
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF0F172A))
                            .padding(8.dp)
                    ) {
                        val history = dbData.waveform.ifEmpty { List(20) { 40f } }
                        val barCount = history.size
                        val barWidth = (size.width - (barCount - 1) * 6f) / barCount

                        history.forEachIndexed { i, dbVal ->
                            val heightRatio = ((dbVal - 20f) / 100f).coerceIn(0.1f, 1f)
                            val barHeight = size.height * heightRatio
                            val x = i * (barWidth + 6f)
                            val y = size.height - barHeight

                            val barColor = when {
                                dbVal >= 85f -> Color(0xFFEF4444)
                                dbVal >= 70f -> Color(0xFFF59E0B)
                                else -> Color(0xFF10B981)
                            }

                            drawRoundRect(
                                color = barColor,
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(4f, 4f)
                            )
                        }
                    }

                    // Linear meter
                    val progress = ((dbData.currentDb - 20f) / 100f).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .clip(CircleShape),
                        color = warningColor,
                        trackColor = MaterialTheme.colorScheme.surface
                    )
                }
            }

            // Stat Readouts (Min, Avg, Peak)
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ResultBadge(
                    title = "MIN NOISE",
                    value = "${"%.1f".format(dbData.minDb)} dB",
                    unit = "lowest",
                    color = Color(0xFF059669),
                    modifier = Modifier.weight(1f)
                )
                ResultBadge(
                    title = "AVG NOISE",
                    value = "${"%.1f".format(dbData.avgDb)} dB",
                    unit = "average",
                    color = Color(0xFF2563EB),
                    modifier = Modifier.weight(1f)
                )
                ResultBadge(
                    title = "PEAK HOLD",
                    value = "${"%.1f".format(dbData.peakDb)} dB",
                    unit = "maximum",
                    color = warningColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

