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
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Roofing
import androidx.compose.material.icons.filled.SolarPower
import androidx.compose.material.icons.filled.WbSunny
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
fun SunPathTrackerScreen(
    viewModel: SunPathTrackerViewModel,
    modifier: Modifier = Modifier
) {
    val latitude by viewModel.latitude.collectAsState()
    val dayOfYear by viewModel.dayOfYear.collectAsState()
    val solarHour by viewModel.solarHour.collectAsState()
    val sunPosition by viewModel.sunPosition.collectAsState()
    val currentDayArc by viewModel.currentDayArc.collectAsState()
    val summerSolsticeArc by viewModel.summerSolsticeArc.collectAsState()
    val winterSolsticeArc by viewModel.winterSolsticeArc.collectAsState()
    val windowHeightM by viewModel.windowHeightM.collectAsState()
    val obstacleHeightM by viewModel.obstacleHeightM.collectAsState()
    val lastLogSaved by viewModel.lastLogSaved.collectAsState()

    var locationTag by remember { mutableStateOf("South Facade & Solar Siting") }

    val primarySunColor = Color(0xFFF59E0B)

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
                colors = CardDefaults.cardColors(containerColor = primarySunColor.copy(alpha = 0.12f)),
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
                            Icon(Icons.Default.WbSunny, contentDescription = null, tint = primarySunColor)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SUN PATH & SHADOW TRACKER",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp),
                                color = primarySunColor
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(primarySunColor.copy(alpha = 0.2f), CircleShape)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (sunPosition.isAboveHorizon) "DAYLIGHT" else "NIGHT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = primarySunColor
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Solar trajectory astronomy for passive solar window placement, roof eave shading, PV panel tilt, and shadow projection.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Sky Dome Trajectory Canvas Viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.6f)
                    .background(Color(0xFF0F172A), RoundedCornerShape(20.dp))
                    .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val horizonY = h * 0.85f

                    // Horizon line
                    drawLine(Color(0xFF334155), Offset(0f, horizonY), Offset(w, horizonY), strokeWidth = 1.5.dp.toPx())

                    // Helper to project (azimuth, elevation) onto 2D canvas
                    // Azimuth (60° to 300° mapped to 0..w), Elevation (0° to 90° mapped to horizonY..10)
                    fun projectPoint(az: Float, el: Float): Offset {
                        val normX = ((az - 60f) / 240f).coerceIn(0f, 1f)
                        val x = normX * w
                        val normY = (el / 90f).coerceIn(0f, 1f)
                        val y = horizonY - (normY * (horizonY - 20f))
                        return Offset(x, y)
                    }

                    // Draw Summer Solstice Arc (Orange)
                    if (summerSolsticeArc.size > 1) {
                        val pathSummer = Path()
                        summerSolsticeArc.forEachIndexed { i, pt ->
                            val off = projectPoint(pt.azimuthDeg, pt.elevationDeg)
                            if (i == 0) pathSummer.moveTo(off.x, off.y) else pathSummer.lineTo(off.x, off.y)
                        }
                        drawPath(pathSummer, Color(0xFFF97316).copy(alpha = 0.5f), style = Stroke(width = 1.5.dp.toPx()))
                    }

                    // Draw Winter Solstice Arc (Cyan)
                    if (winterSolsticeArc.size > 1) {
                        val pathWinter = Path()
                        winterSolsticeArc.forEachIndexed { i, pt ->
                            val off = projectPoint(pt.azimuthDeg, pt.elevationDeg)
                            if (i == 0) pathWinter.moveTo(off.x, off.y) else pathWinter.lineTo(off.x, off.y)
                        }
                        drawPath(pathWinter, Color(0xFF06B6D4).copy(alpha = 0.5f), style = Stroke(width = 1.5.dp.toPx()))
                    }

                    // Draw Current Day Arc (Gold Solid)
                    if (currentDayArc.size > 1) {
                        val pathCurrent = Path()
                        currentDayArc.forEachIndexed { i, pt ->
                            val off = projectPoint(pt.azimuthDeg, pt.elevationDeg)
                            if (i == 0) pathCurrent.moveTo(off.x, off.y) else pathCurrent.lineTo(off.x, off.y)
                        }
                        drawPath(pathCurrent, Color(0xFFFBBF24), style = Stroke(width = 3.dp.toPx()))
                    }

                    // Draw Current Sun Position Orb
                    val sunPos = projectPoint(sunPosition.azimuthDeg, sunPosition.elevationDeg)
                    drawCircle(Color(0xFFFEF08A), radius = 14.dp.toPx(), center = sunPos)
                    drawCircle(Color(0xFFF59E0B), radius = 8.dp.toPx(), center = sunPos)
                    drawCircle(Color.White, radius = 4.dp.toPx(), center = sunPos)
                }

                // HUD Overlays
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "TIME: %02d:%02d SOLAR".format(solarHour.toInt(), ((solarHour % 1) * 60).toInt()),
                            color = Color(0xFFFBBF24),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("• Summer Arc", color = Color(0xFFF97316), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("• Current", color = Color(0xFFFBBF24), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("• Winter Arc", color = Color(0xFF06B6D4), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Key Results Badges Triad
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                ResultBadge(
                    title = "SUN ELEVATION",
                    value = String.format("%.1f°", sunPosition.elevationDeg),
                    unit = "Altitude Angle",
                    color = primarySunColor,
                    modifier = Modifier.weight(1f)
                )
                ResultBadge(
                    title = "SUN AZIMUTH",
                    value = String.format("%.1f°", sunPosition.azimuthDeg),
                    unit = "Compass Bearing",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                ResultBadge(
                    title = "SOLAR NOON PEAK",
                    value = String.format("%.1f°", sunPosition.solarNoonElevationDeg),
                    unit = "Zenith Max",
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }

            // Time of Day & Day of Year Sliders Card
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
                        Text("Solar Hour: %02d:%02d".format(solarHour.toInt(), ((solarHour % 1) * 60).toInt()), fontWeight = FontWeight.Bold)
                        Text(if (solarHour in 11.5f..12.5f) "SOLAR NOON" else "", color = primarySunColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Slider(
                        value = solarHour,
                        onValueChange = { viewModel.setSolarHour(it) },
                        valueRange = 5.0f..19.0f,
                        modifier = Modifier.fillMaxWidth().testTag("solar_hour_slider")
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Day of Year: Day $dayOfYear", fontWeight = FontWeight.Bold)
                        Text(if (dayOfYear in 165..180) "Summer Solstice" else if (dayOfYear in 350..365) "Winter Solstice" else "Equinox Season", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = dayOfYear.toFloat(),
                        onValueChange = { viewModel.setDayOfYear(it.toInt()) },
                        valueRange = 1f..365f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Site Latitude: ${String.format("%.1f°", latitude)}", fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = latitude,
                        onValueChange = { viewModel.setLatitude(it) },
                        valueRange = -60f..70f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Passive Solar Shading & Overhang Sizer
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Roofing, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Window Overhang & Passive Solar Sizer", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }

                    Text(
                        text = "Window Height: ${String.format("%.2f m (%.1f ft)", windowHeightM, windowHeightM * 3.28f)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = windowHeightM,
                        onValueChange = { viewModel.setWindowHeight(it) },
                        valueRange = 0.8f..4.0f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    val overhangM = viewModel.getOptimalOverhangDepthM()
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("RECOMMENDED EAVE OVERHANG PROJECTION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "${String.format("%.2f m", overhangM)} (${String.format("%.1f in", overhangM * 39.37f)})",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "100% blocks high summer sun (cutting cooling load) while admitting low winter sun for heating.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Solar PV Tilt & Shadow Obstacle Sizer
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SolarPower, contentDescription = null, tint = primarySunColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Solar PV Tilt & Shadow Projection", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }

                    val pvTilt = viewModel.getOptimalPvTiltDeg()
                    val shadowL = viewModel.getObstacleShadowLengthM()

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        ResultBadge(
                            title = "PV FIXED TILT",
                            value = "${String.format("%.1f°", pvTilt)}",
                            unit = if (latitude >= 0) "True South (180°)" else "True North (0°)",
                            color = primarySunColor,
                            modifier = Modifier.weight(1f)
                        )
                        ResultBadge(
                            title = "SHADOW LENGTH",
                            value = "${String.format("%.1f m", shadowL)}",
                            unit = "For ${String.format("%.1fm", obstacleHeightM)} obstacle",
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text("Obstacle / Tree Height: ${String.format("%.1f m", obstacleHeightM)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = obstacleHeightM,
                        onValueChange = { viewModel.setObstacleHeight(it) },
                        valueRange = 1.0f..25.0f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Save Solar Report to Database
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Save Solar & Glazing Audit Log", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    OutlinedTextField(
                        value = locationTag,
                        onValueChange = { locationTag = it },
                        label = { Text("Site / Facade Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { viewModel.saveSolarAuditLog(locationTag) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(if (lastLogSaved) Icons.Default.CheckCircle else Icons.Default.BookmarkBorder, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (lastLogSaved) "Solar Audit Report Saved" else "Save Solar Trajectory to Log")
                    }
                }
            }
        }
    }
}
