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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.graphics.Brush
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
fun ThermalCameraScreen(
    viewModel: ThermalCameraViewModel,
    modifier: Modifier = Modifier
) {
    val palette by viewModel.palette.collectAsState()
    val isFahrenheit by viewModel.isFahrenheit.collectAsState()
    val emissivity by viewModel.emissivity.collectAsState()
    val centerTemp by viewModel.centerTemp.collectAsState()
    val maxTemp by viewModel.maxTemp.collectAsState()
    val minTemp by viewModel.minTemp.collectAsState()
    val hotSpot by viewModel.hotSpot.collectAsState()
    val coldSpot by viewModel.coldSpot.collectAsState()
    val isAlarmActive by viewModel.isAlarmActive.collectAsState()
    val alarmThresholdC by viewModel.alarmThresholdC.collectAsState()
    val selectedPreset by viewModel.selectedPreset.collectAsState()
    val lastLogSaved by viewModel.lastLogSaved.collectAsState()

    var auditNote by remember { mutableStateOf("Main Breaker Panel Lugs") }

    val gradientColors = when (palette) {
        ThermalPalette.IRONBOW -> listOf(Color(0xFF000033), Color(0xFF4B0082), Color(0xFF8B0000), Color(0xFFFF4500), Color(0xFFFFD700), Color(0xFFFFFFFF))
        ThermalPalette.RAINBOW -> listOf(Color(0xFF0000FF), Color(0xFF00FFFF), Color(0xFF00FF00), Color(0xFFFFFF00), Color(0xFFFF0000), Color(0xFFFFFFFF))
        ThermalPalette.WHITE_HOT -> listOf(Color(0xFF101010), Color(0xFF505050), Color(0xFF909090), Color(0xFFD0D0D0), Color(0xFFFFFFFF))
        ThermalPalette.BLACK_HOT -> listOf(Color(0xFFFFFFFF), Color(0xFFD0D0D0), Color(0xFF909090), Color(0xFF505050), Color(0xFF101010))
        ThermalPalette.LAVA -> listOf(Color(0xFF0A0A0A), Color(0xFF3B0000), Color(0xFF8B0000), Color(0xFFFF2200), Color(0xFFFF9900), Color(0xFFFFFF99))
        ThermalPalette.ARCTIC -> listOf(Color(0xFF001F3F), Color(0xFF0074D9), Color(0xFF7FDBFF), Color(0xFFFFFFFF), Color(0xFFFFDC00), Color(0xFFFF4136))
    }

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
            // Header Info Card
            Card(
                colors = CardDefaults.cardColors(containerColor = if (isAlarmActive) Color(0xFFDC2626).copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
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
                            Icon(Icons.Default.DeviceThermostat, contentDescription = null, tint = if (isAlarmActive) Color(0xFFDC2626) else MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "RADIOMETRIC THERMAL IMAGING",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp),
                                color = if (isAlarmActive) Color(0xFFDC2626) else MaterialTheme.colorScheme.primary
                            )
                        }
                        OutlinedButton(
                            onClick = { viewModel.toggleTemperatureUnit() },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (isFahrenheit) "Unit: °F" else "Unit: °C", fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Real-time radiometric false-color inspection for electrical hotspots, missing insulation, and plumbing leaks.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // High Temp Isotherm Warning Banner
            if (isAlarmActive) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("OVERHEAT ISOTHERM ALARM!", fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                            Text("Hot spot exceeds threshold (${viewModel.toDisplayTemp(alarmThresholdC)}). Inspect for high-resistance terminal failure.", fontSize = 12.sp, color = Color(0xFFB91C1C))
                        }
                    }
                }
            }

            // Radiometric False-Color Viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .background(Color.Black, RoundedCornerShape(20.dp))
                    .border(2.dp, if (isAlarmActive) Color(0xFFEF4444) else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
            ) {
                // Background Simulated Radiometric Thermal Field Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Radial hotspot brush
                    val hotCenter = Offset(hotSpot.x * w, hotSpot.y * h)
                    val coldCenter = Offset(coldSpot.x * w, coldSpot.y * h)

                    // Base gradient fill
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = gradientColors,
                            start = Offset(0f, 0f),
                            end = Offset(w, h)
                        )
                    )

                    // Draw center crosshair
                    val cx = w / 2f
                    val cy = h / 2f
                    val chSize = 24.dp.toPx()
                    drawLine(Color.White, Offset(cx - chSize, cy), Offset(cx + chSize, cy), strokeWidth = 2.dp.toPx())
                    drawLine(Color.White, Offset(cx, cy - chSize), Offset(cx, cy + chSize), strokeWidth = 2.dp.toPx())
                    drawCircle(Color.White, radius = 6.dp.toPx(), center = Offset(cx, cy), style = Stroke(1.5.dp.toPx()))

                    // Draw Hotspot marker
                    drawCircle(Color.Red, radius = 10.dp.toPx(), center = hotCenter, style = Stroke(2.5.dp.toPx()))
                    drawCircle(Color.Yellow, radius = 4.dp.toPx(), center = hotCenter)

                    // Draw Coldspot marker
                    drawCircle(Color.Cyan, radius = 10.dp.toPx(), center = coldCenter, style = Stroke(2.5.dp.toPx()))
                    drawCircle(Color.Blue, radius = 4.dp.toPx(), center = coldCenter)
                }

                // HUD Overlays
                // Top Left: Max & Min Spot Readouts
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(Color.Red, CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("MAX: ${viewModel.toDisplayTemp(maxTemp)}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(Color.Cyan, CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("MIN: ${viewModel.toDisplayTemp(minTemp)}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }

                // Top Right: Center Spot Readout
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text("CENTER: ${viewModel.toDisplayTemp(centerTemp)}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }

                // Bottom Center: Emissivity & Palette Badge
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ε = ${String.format("%.2f", emissivity)}", color = Color(0xFFFDE047), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("•", color = Color.Gray, fontSize = 11.sp)
                    Text(palette.label, color = Color.White, fontSize = 11.sp)
                    Text("•", color = Color.Gray, fontSize = 11.sp)
                    val deltaVal = maxTemp - minTemp
                    Text("ΔT: ${if (isFahrenheit) String.format("%.1f°F", deltaVal * 1.8f) else String.format("%.1f°C", deltaVal)}", color = Color(0xFF67E8F9), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Key Results Triad
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                ResultBadge(
                    title = "HOT SPOT (MAX)",
                    value = viewModel.toDisplayTemp(maxTemp),
                    unit = "Peak Load",
                    color = Color(0xFFEF4444),
                    modifier = Modifier.weight(1f)
                )
                ResultBadge(
                    title = "CENTER PROBE",
                    value = viewModel.toDisplayTemp(centerTemp),
                    unit = "Spot Meter",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                ResultBadge(
                    title = "COLD SPOT (MIN)",
                    value = viewModel.toDisplayTemp(minTemp),
                    unit = "Ambient / Leak",
                    color = Color(0xFF0284C7),
                    modifier = Modifier.weight(1f)
                )
            }

            // Palette Selector Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Thermal False-Color Palettes", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThermalPalette.values().forEach { p ->
                            FilterChip(
                                selected = palette == p,
                                onClick = { viewModel.setPalette(p) },
                                label = { Text(p.label) }
                            )
                        }
                    }
                }
            }

            // Emissivity (ε) Tuning Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Surface Emissivity (ε): ${String.format("%.2f", emissivity)}", fontWeight = FontWeight.Bold)
                        }
                        Text(selectedPreset, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }

                    Slider(
                        value = emissivity,
                        onValueChange = { viewModel.setEmissivity(it) },
                        valueRange = 0.10f..1.00f,
                        modifier = Modifier.fillMaxWidth().testTag("emissivity_slider")
                    )

                    Text("Trade Material Presets:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        viewModel.emissivityPresets.forEach { preset ->
                            FilterChip(
                                selected = emissivity == preset.value,
                                onClick = { viewModel.applyPreset(preset) },
                                label = { Text("${preset.name} (${preset.value})") }
                            )
                        }
                    }
                }
            }

            // Isotherm Threshold Slider
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Overheat Alarm Threshold: ${viewModel.toDisplayTemp(alarmThresholdC)}", fontWeight = FontWeight.Bold)
                    Slider(
                        value = alarmThresholdC,
                        onValueChange = { viewModel.setAlarmThreshold(it) },
                        valueRange = 30f..120f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "NEC 110.14(C) terminal rating standard: 60°C or 75°C max operating temperature for copper conductors.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Save Inspection Log
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Save Thermal Audit Report", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    OutlinedTextField(
                        value = auditNote,
                        onValueChange = { auditNote = it },
                        label = { Text("Equipment / Inspection Target") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { viewModel.saveThermalAuditLog(auditNote) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(if (lastLogSaved) Icons.Default.CheckCircle else Icons.Default.BookmarkBorder, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (lastLogSaved) "Thermal Report Saved to Database" else "Save Radiometric Report to Log")
                    }
                }
            }
        }
    }
}
