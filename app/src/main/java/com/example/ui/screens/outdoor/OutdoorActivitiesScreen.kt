package com.example.ui.screens.outdoor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutdoorActivitiesScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }

    val tabs = listOf(
        "Topo Map & Compass",
        "Sensors & Weather",
        "Hike & Hydration",
        "Survival & Bushcraft",
        "First Aid & SOS"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Outdoor Activities & Topo Suite",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Offline Topo, Dual-Dial Compass, Survival & Emergency",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("btn_outdoor_back")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Category Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 12.dp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (selectedTab) {
                    0 -> TopoMapAndCompassSection(context)
                    1 -> SensorsAndWeatherSection(context)
                    2 -> HikeAndHydrationSection()
                    3 -> SurvivalAndBushcraftSection()
                    4 -> FirstAidAndSosSection(context)
                }
            }
        }
    }
}

// ============================================================================
// TAB 0: TOPO MAP & COMPASS
// ============================================================================

@Composable
private fun TopoMapAndCompassSection(context: Context) {
    var contourInterval by remember { mutableStateOf(10) } // 10m or 50m
    var showRidgelines by remember { mutableStateOf(true) }
    var showWaterCatchment by remember { mutableStateOf(true) }
    var zoomScale by remember { mutableStateOf(1f) }

    var azimuthDegree by remember { mutableStateOf(342f) }
    var magneticDeclination by remember { mutableStateOf(4.2f) } // +4.2° E
    var lockAzimuth by remember { mutableStateOf(false) }

    // Real sensor listener for magnetic compass if device has sensor
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val compassSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ORIENTATION)
        
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null && !lockAzimuth) {
                    azimuthDegree = (event.values[0] + 360) % 360
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        compassSensor?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. Vector Topo Map Viewer Canvas
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Terrain, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Vector Topo & Contour Engine",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            "Offline MBTiles",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Controls row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = contourInterval == 10,
                            onClick = { contourInterval = 10 },
                            label = { Text("10m Contours", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = contourInterval == 50,
                            onClick = { contourInterval = 50 },
                            label = { Text("50m Contours", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = showRidgelines,
                            onClick = { showRidgelines = !showRidgelines },
                            label = { Text("Ridgelines", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = showWaterCatchment,
                            onClick = { showWaterCatchment = !showWaterCatchment },
                            label = { Text("Waterways", fontSize = 11.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Topo Map Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE8ECD7)) // Topo parchment background
                            .pointerInput(Unit) {
                                detectTransformGestures { _, _, zoom, _ ->
                                    zoomScale = (zoomScale * zoom).coerceIn(0.8f, 5.0f)
                                }
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            // Draw contour lines
                            val numLines = if (contourInterval == 10) 14 else 6
                            val contourColor = Color(0xFF8C6D46) // Classic topo brown

                            for (i in 1..numLines) {
                                val radius = (min(w, h) / 2.2f) * (i.toFloat() / numLines) * zoomScale
                                val path = Path().apply {
                                    val centerX = w / 2f
                                    val centerY = h / 2f
                                    for (angle in 0..360 step 10) {
                                        val rad = Math.toRadians(angle.toDouble())
                                        val noise = sin(rad * 4 + i) * 12 * zoomScale
                                        val x = centerX + (radius + noise).toFloat() * cos(rad).toFloat()
                                        val y = centerY + (radius + noise).toFloat() * sin(rad).toFloat()
                                        if (angle == 0) moveTo(x, y) else lineTo(x, y)
                                    }
                                    close()
                                }
                                drawPath(
                                    path = path,
                                    color = contourColor,
                                    style = Stroke(width = if (i % 5 == 0) 2.5f else 1f)
                                )
                            }

                            // Ridgelines overlay
                            if (showRidgelines) {
                                drawLine(
                                    color = Color(0xFFD32F2F),
                                    start = Offset(w * 0.15f, h * 0.2f),
                                    end = Offset(w * 0.85f, h * 0.75f),
                                    strokeWidth = 3f
                                )
                            }

                            // Water catchment waterways overlay
                            if (showWaterCatchment) {
                                val streamPath = Path().apply {
                                    moveTo(w * 0.8f, h * 0.1f)
                                    quadraticTo(w * 0.5f, h * 0.4f, w * 0.2f, h * 0.9f)
                                }
                                drawPath(streamPath, color = Color(0xFF1E88E5), style = Stroke(width = 3.5f))
                            }
                        }

                        // Map Legend HUD Overlay
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Text("Grid Ref: 48S KH 3482 9182", color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text("Elev: 1,842 m • Zoom: ${"%.1f".format(zoomScale)}x", color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // 2. Dual-Dial True North & Magnetic Declination Compass
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Explore, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Dual-Dial True North Compass",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(onClick = { lockAzimuth = !lockAzimuth }) {
                            Icon(
                                if (lockAzimuth) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = "Lock",
                                tint = if (lockAzimuth) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Compass Dial Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(220.dp)) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val radius = size.width / 2f - 10f

                            // Outer Dial Ring
                            drawCircle(color = Color(0xFF21252B), radius = radius, center = center)
                            drawCircle(color = Color(0xFF00E676), radius = radius, center = center, style = Stroke(width = 3f))

                            // Inner Declination Ring (True North Offset)
                            val trueNorthBearing = (azimuthDegree + magneticDeclination + 360) % 360

                            rotate(degrees = -azimuthDegree, pivot = center) {
                                // Draw Cardinal Points
                                val cardinalStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                drawCircle(color = Color.Red, radius = 6f, center = Offset(center.x, center.y - radius + 20f))
                            }

                            // Needle (Magnetic North = Red, True North = Cyan)
                            rotate(degrees = -azimuthDegree, pivot = center) {
                                val needlePath = Path().apply {
                                    moveTo(center.x, center.y - radius + 25f)
                                    lineTo(center.x - 12f, center.y)
                                    lineTo(center.x + 12f, center.y)
                                    close()
                                }
                                drawPath(needlePath, color = Color(0xFFFF5252))
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${azimuthDegree.toInt()}°",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val cardinal = when (azimuthDegree) {
                                in 337.5..360.0, in 0.0..22.5 -> "N"
                                in 22.5..67.5 -> "NE"
                                in 67.5..112.5 -> "E"
                                in 112.5..157.5 -> "SE"
                                in 157.5..202.5 -> "S"
                                in 202.5..247.5 -> "SW"
                                in 247.5..292.5 -> "W"
                                else -> "NW"
                            }
                            Text(cardinal, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Mag Declination: +${"%.1f".format(magneticDeclination)}° E", fontSize = 12.sp)
                        Text("MGRS: 48S KH 3482 9182", fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

// ============================================================================
// TAB 1: SENSORS & WEATHER
// ============================================================================

@Composable
private fun SensorsAndWeatherSection(context: Context) {
    var pressureHpa by remember { mutableStateOf(1013.25f) }
    var altitudeMeters by remember { mutableStateOf(450f) }
    var slopeAngleDeg by remember { mutableStateOf(34f) } // 30-45 avalanche risk

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. Barometric Altimeter & Pressure Trend Monitor
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Barometric Altimeter & Pressure Trend",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            "STABLE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E676),
                            modifier = Modifier
                                .background(Color(0xFF00E676).copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("BAROMETRIC PRESSURE", style = MaterialTheme.typography.labelSmall)
                            Text("${"%.1f".format(pressureHpa)} hPa", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("-0.4 hPa / hr", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Divider(modifier = Modifier.height(48.dp).width(1.dp))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ESTIMATED ALTITUDE", style = MaterialTheme.typography.labelSmall)
                            Text("${altitudeMeters.toInt()} m", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("1,476 ft ASL", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // 2. Inclinometer & Avalanche Danger Angle Gauge
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (slopeAngleDeg in 30f..45f) Color(0xFFFFF3E0) else MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.InvertColors, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Slope Pitch & Avalanche Inclinometer",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (slopeAngleDeg in 30f..45f) {
                            Text(
                                "AVALANCHE RISK (30°-45°)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFD32F2F),
                                modifier = Modifier
                                    .background(Color(0xFFD32F2F).copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Slider(
                        value = slopeAngleDeg,
                        onValueChange = { slopeAngleDeg = it },
                        valueRange = 0f..60f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Angle: ${slopeAngleDeg.toInt()}°", fontWeight = FontWeight.Bold)
                        Text(
                            text = when {
                                slopeAngleDeg < 25f -> "Low Incline (Safe)"
                                slopeAngleDeg in 25f..29f -> "Moderate Slope"
                                slopeAngleDeg in 30f..45f -> "CRITICAL AVALANCHE ZONE"
                                else -> "Extreme Cliff / Steep"
                            },
                            color = if (slopeAngleDeg in 30f..45f) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// TAB 2: HIKE & HYDRATION
// ============================================================================

@Composable
private fun HikeAndHydrationSection() {
    var distanceKm by remember { mutableStateOf(14.0f) }
    var elevationGainM by remember { mutableStateOf(850.0f) }
    var packWeightKg by remember { mutableStateOf(12.0f) }

    // Naismith's Rule: 1 hour per 5 km + 1 hour per 600m ascent
    val baseTimeHours = (distanceKm / 5.0f) + (elevationGainM / 600.0f)
    val packPenalty = (packWeightKg / 10.0f) * 0.2f
    val totalHikeTimeHours = baseTimeHours + packPenalty

    val fluidReqLiters = totalHikeTimeHours * 0.65f

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Naismith Hike Estimator
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Naismith's Hike Time & Effort Estimator",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Distance: ${"%.1f".format(distanceKm)} km", fontSize = 12.sp)
                    Slider(value = distanceKm, onValueChange = { distanceKm = it }, valueRange = 1f..40f)

                    Text("Elevation Ascent Gain: ${elevationGainM.toInt()} m", fontSize = 12.sp)
                    Slider(value = elevationGainM, onValueChange = { elevationGainM = it }, valueRange = 0f..2500f)

                    Text("Pack Weight: ${"%.1f".format(packWeightKg)} kg", fontSize = 12.sp)
                    Slider(value = packWeightKg, onValueChange = { packWeightKg = it }, valueRange = 0f..30f)

                    Divider(modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("ESTIMATED HIKE TIME", style = MaterialTheme.typography.labelSmall)
                            Text("${totalHikeTimeHours.toInt()}h ${((totalHikeTimeHours % 1) * 60).toInt()}m", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("FLUID REQUIREMENT", style = MaterialTheme.typography.labelSmall)
                            Text("${"%.1f".format(fluidReqLiters)} Liters", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// TAB 3: SURVIVAL & BUSHCRAFT
// ============================================================================

@Composable
private fun SurvivalAndBushcraftSection() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Firewood Energy & TPI Heat Matrix",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val woods = listOf(
                        Triple("Oak (White/Red)", "24.0 Million BTU/cord", "High Coals • Heavy Smoke: Low"),
                        Triple("Hickory", "28.5 Million BTU/cord", "Maximum Heat • High Ignition Temp"),
                        Triple("Pine (Yellow/White)", "15.0 Million BTU/cord", "Fast Kindling • High Spark Throw"),
                        Triple("Birch (Paper/Yellow)", "20.0 Million BTU/cord", "Easy Ignition • Excellent Bark Tinder")
                    )

                    woods.forEach { (name, btu, note) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(btu, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                Text(note, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// TAB 4: FIRST AID & SOS
// ============================================================================

@Composable
private fun FirstAidAndSosSection(context: Context) {
    var isSosActive by remember { mutableStateOf(false) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Emergency SOS Strobe & Whistle Beacon
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSosActive) Color(0xFFD32F2F) else MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = if (isSosActive) Color.White else Color(0xFFD32F2F)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "SOS Morse Code Strobe & Audio Whistle",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSosActive) Color.White else MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        "... --- ... (International Emergency Signal)",
                        fontSize = 12.sp,
                        color = if (isSosActive) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            isSosActive = !isSosActive
                            Toast.makeText(context, if (isSosActive) "SOS BEACON TRANSMITTING" else "SOS Stopped", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSosActive) Color.White else Color(0xFFD32F2F),
                            contentColor = if (isSosActive) Color(0xFFD32F2F) else Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isSosActive) "STOP EMERGENCY SOS BEACON" else "ACTIVATE EMERGENCY SOS BEACON", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Emergency Satellite / SMS Dispatch
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Satellite / SMS Compressed Emergency Dispatch",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val emergencyStr = "SOS! LAT: 44.9778, LON: -93.2650, ALT: 1842m, BAT: 84%, PARTY: 2. NEED WFA SPLINT ASSIST."

                    OutlinedTextField(
                        value = emergencyStr,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            Toast.makeText(context, "Copied Satellite Emergency String", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copy Satellite SOS Format String")
                    }
                }
            }
        }
    }
}
