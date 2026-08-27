package com.example.ui.screens.civil

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.domain.math.FrequencyBand
import com.example.domain.math.SatellitePointerResult
import com.example.domain.math.SatellitePreset
import com.example.ui.screens.woodworking.ResultBadge
import com.example.ui.utils.ToolIconMapper
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParabolicFocusScreen(
    viewModel: ParabolicFocusViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(Unit) {
        viewModel.startSensors(context)
        onDispose { viewModel.stopSensors() }
    }

    val res by viewModel.result.collectAsState()
    val orientation by viewModel.deviceOrientation.collectAsState()
    val isGpsActive by viewModel.isGpsActive.collectAsState()

    var satDropdownExpanded by remember { mutableStateOf(false) }
    var isArCameraActive by remember { mutableStateOf(false) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasCameraPermission = isGranted }
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (isArCameraActive) {
            // FULL SCREEN AR SATELLITE POINTER CAMERA VIEW
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                if (hasCameraPermission) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }
                                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                                } catch (_: Exception) {}
                            }, ContextCompat.getMainExecutor(ctx))
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // AR Satellite Crosshair Overlay Canvas with Sky Arc Projection
                ArSatellitePointerCanvas(
                    targetAzimuthDeg = res.azimuthTrueDeg,
                    targetElevationDeg = res.elevationDeg,
                    currentAzimuthDeg = orientation.azimuthDeg.toDouble(),
                    currentElevationDeg = orientation.pitchDeg.toDouble(),
                    rotationMatrix = orientation.rotationMatrix,
                    userLat = res.userLatitude,
                    userLon = res.userLongitude,
                    selectedSat = res.selectedSat,
                    allSatellites = SatellitePreset.POPULAR_SATELLITES,
                    modifier = Modifier.fillMaxSize()
                )

                // Top Translucent Header
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 24.dp, start = 16.dp, end = 16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xEE0F172A))
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SATELLITE POINTER: ${res.selectedSat.name}",
                                color = Color(0xFF60A5FA),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Target Azimuth: %.1f° | Elev: %.1f° | LNB Skew: %.1f°".format(
                                    res.azimuthTrueDeg, res.elevationDeg, res.lnbSkewDeg
                                ),
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        IconButton(onClick = { isArCameraActive = false }) {
                            Icon(Icons.Default.FullscreenExit, contentDescription = "Close Camera", tint = Color.White)
                        }
                    }
                }

                // Bottom Realtime Guidance Bar
                val azDiff = (res.azimuthTrueDeg - orientation.azimuthDeg + 360) % 360
                val normAzDiff = if (azDiff > 180) azDiff - 360 else azDiff
                val elDiff = res.elevationDeg - orientation.pitchDeg

                val isLocked = abs(normAzDiff) < 3.0 && abs(elDiff) < 3.0

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (isLocked) Color(0xEE059669) else Color(0xEE0F172A))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (isLocked) "SATELLITE SIGNAL MATCHED!" else "ALIGNMENT GUIDANCE",
                                color = if (isLocked) Color.White else Color(0xFFFACC15),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = when {
                                    isLocked -> "Phone aligned with ${res.selectedSat.name}. Lock dish elevation to %.1f°".format(res.elevationDeg)
                                    normAzDiff > 3 -> "Rotate phone RIGHT by %.1f°".format(normAzDiff)
                                    normAzDiff < -3 -> "Rotate phone LEFT by %.1f°".format(-normAzDiff)
                                    elDiff > 3 -> "Tilt phone UP by %.1f°".format(elDiff)
                                    else -> "Tilt phone DOWN by %.1f°".format(-elDiff)
                                },
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Icon(
                            imageVector = if (isLocked) Icons.Default.GpsFixed else Icons.Default.Explore,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        } else {
            // MAIN SCROLLABLE DASHBOARD VIEW
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
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
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SatelliteAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "PARABOLIC DISH & SATELLITE POINTER",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Dish Focus, Multi-LNB & Satellite Alignment",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                // AR Camera Satellite Pointer Trigger Button
                Button(
                    onClick = {
                        if (!hasCameraPermission) {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        } else {
                            isArCameraActive = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_open_ar_satellite_pointer")
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "OPEN AR CAMERA SATELLITE POINTER",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // Satellite & RF Band Selection Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = if (res.selectedSat.band.isCellular) "Cellular & Tower Frequency Band" else "Target Satellite & Broadcast Band",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Text(
                            text = "TV Satellite Broadcast Bands:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FrequencyBand.entries.filter { !it.isCellular && it != FrequencyBand.CUSTOM }.forEach { band ->
                                FilterChip(
                                    selected = res.selectedSat.band == band,
                                    onClick = { viewModel.updateBand(band) },
                                    label = { Text(band.title) }
                                )
                            }
                        }

                        Text(
                            text = "Cellular & Directional Tower Links (900 / 1800 / 2100 / 5G):",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FrequencyBand.entries.filter { it.isCellular }.take(4).forEach { band ->
                                    FilterChip(
                                        selected = res.selectedSat.band == band,
                                        onClick = { viewModel.updateBand(band) },
                                        label = { Text(band.title) }
                                    )
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FrequencyBand.entries.filter { it.isCellular }.drop(4).forEach { band ->
                                    FilterChip(
                                        selected = res.selectedSat.band == band,
                                        onClick = { viewModel.updateBand(band) },
                                        label = { Text(band.title) }
                                    )
                                }
                            }
                        }

                        // Preset Satellite Dropdown (Only relevant for TV Satellite dish reception)
                        if (!res.selectedSat.band.isCellular) {
                            ExposedDropdownMenuBox(
                                expanded = satDropdownExpanded,
                                onExpandedChange = { satDropdownExpanded = !satDropdownExpanded },
                                modifier = Modifier.fillMaxWidth().testTag("dropdown_satellite_select")
                            ) {
                                OutlinedTextField(
                                    value = "${res.selectedSat.name} (${res.selectedSat.longitudeFormatted})",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Primary Satellite Target (LNB #1)") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = satDropdownExpanded) },
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )

                                ExposedDropdownMenu(
                                    expanded = satDropdownExpanded,
                                    onDismissRequest = { satDropdownExpanded = false }
                                ) {
                                    SatellitePreset.POPULAR_SATELLITES.filter { !it.band.isCellular }.forEach { preset ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(preset.name, fontWeight = FontWeight.Bold)
                                                    Text(
                                                        "Orbital Lon: ${preset.longitudeFormatted} | Coverage: ${preset.coverageRegion}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            },
                                            onClick = {
                                                viewModel.selectSatellite(preset)
                                                satDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            if (res.selectedSat.name == "Custom Orbital Slot") {
                                OutlinedTextField(
                                    value = res.customSatLongitude.toString(),
                                    onValueChange = { viewModel.updateCustomSatLongitude(it.toDoubleOrNull() ?: 108.0) },
                                    label = { Text("Custom Satellite Longitude (°E positive, °W negative)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        } else {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "CELLULAR DIRECTIONAL TOWER LINK MODE",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF1D4ED8)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Optimized for 4G LTE / 5G NR parabolic grid & dish antennas pointing directly towards regional cellular BTS towers.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF1E3A8A)
                                    )
                                }
                            }
                        }
                    }
                }

                // GPS Location Calibration Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "GPS Dish Alignment Calibration",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            if (isGpsActive) {
                                Text("GPS Active", style = MaterialTheme.typography.labelSmall, color = Color(0xFF059669))
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = String.format("%.4f", res.userLatitude),
                                onValueChange = {
                                    val lat = it.toDoubleOrNull()
                                    if (lat != null) viewModel.updateLocation(lat, res.userLongitude)
                                },
                                label = { Text("User Latitude (°)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = String.format("%.4f", res.userLongitude),
                                onValueChange = {
                                    val lon = it.toDoubleOrNull()
                                    if (lon != null) viewModel.updateLocation(res.userLatitude, lon)
                                },
                                label = { Text("User Longitude (°)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Primary Alignment Result Badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ResultBadge(
                        title = "AZIMUTH BEARING",
                        value = "%.1f°".format(res.azimuthTrueDeg),
                        unit = "True North Compass",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    ResultBadge(
                        title = "ELEVATION ANGLE",
                        value = "%.1f°".format(res.elevationDeg),
                        unit = if (res.isVisibleAboveHorizon) "Above Horizon" else "Below Horizon",
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                    if (!res.selectedSat.band.isCellular) {
                        ResultBadge(
                            title = "LNB SKEW TILT",
                            value = "%.1f°".format(res.lnbSkewDeg),
                            unit = "Polarization Rotation",
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        val fsplDb = 20 * kotlin.math.log10(1000.0) + 20 * kotlin.math.log10(res.frequencyGhz * 1e9) - 147.55
                        ResultBadge(
                            title = "PATH LOSS (1km)",
                            value = "%.1f dB".format(fsplDb),
                            unit = "Free Space FSPL",
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Parabolic Geometry & Focal Distance Card
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(if (res.selectedSat.band.isCellular) "Dipole Focal Spot (f)" else "LNB Focal Length (f)", style = MaterialTheme.typography.labelMedium, color = Color(0xFF1D4ED8))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "%.1f cm".format(res.focalLengthCm),
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF1E3A8A)
                            )
                            Text(
                                text = "%.2f\" inches".format(res.focalLengthCm / 2.54),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF3B82F6)
                            )
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Focal Ratio (f/d)", style = MaterialTheme.typography.labelMedium, color = Color(0xFF047857))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "f/d = %.3f".format(res.focalRatio),
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF064E3B)
                            )
                            Text(
                                text = res.dishTypeCategory,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF059669)
                            )
                        }
                    }
                }

                // Multi-LNB Position Correction Section (ONLY visible for TV Satellite dishes)
                if (!res.selectedSat.band.isCellular) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Multi-LNB Bracket Rail & Target Selection",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Configure up to 4 LNB feeds on a single dish rail. Each LNB can be explicitly picked according to your preferences.",
                                style = MaterialTheme.typography.bodySmall
                            )

                            // LNB Count Selector Chips
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                (1..4).forEach { count ->
                                    FilterChip(
                                        selected = res.lnbCount == count,
                                        onClick = { viewModel.updateLnbCount(count) },
                                        label = { Text("$count LNB${if (count > 1) "s" else ""}") }
                                    )
                                }
                            }

                            // Individual Satellite Pickers for LNB 1, LNB 2, LNB 3, LNB 4
                            (0 until res.lnbCount).forEach { lnbIdx ->
                                val lnbNum = lnbIdx + 1
                                val isPrimary = (lnbIdx == 0)
                                val currentLnbSat = if (isPrimary) res.selectedSat else res.secondarySatellites.getOrElse(lnbIdx - 1) { SatellitePreset.POPULAR_SATELLITES[0] }

                                var lnbDropdownExpanded by remember { mutableStateOf(false) }

                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isPrimary) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "LNB #$lnbNum Feed Target:",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleSmall
                                            )
                                            if (isPrimary) {
                                                Text("Center Prime Focus (0.0 cm)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                            } else {
                                                val lnbOffsetInfo = res.multiLnbOffsets.find { it.lnbIndex == lnbNum }
                                                val cm = lnbOffsetInfo?.offsetDistanceCm ?: 0.0
                                                Text(
                                                    text = "Rail Offset: %.1f cm (%.2f\")".format(abs(cm), abs(cm / 2.54)),
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF2563EB),
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        }

                                        if (isPrimary) {
                                            Text(
                                                text = "${currentLnbSat.name} (${currentLnbSat.longitudeFormatted})",
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        } else {
                                            ExposedDropdownMenuBox(
                                                expanded = lnbDropdownExpanded,
                                                onExpandedChange = { lnbDropdownExpanded = !lnbDropdownExpanded },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                OutlinedTextField(
                                                    value = "${currentLnbSat.name} (${currentLnbSat.longitudeFormatted})",
                                                    onValueChange = {},
                                                    readOnly = true,
                                                    label = { Text("Select Satellite for LNB #$lnbNum") },
                                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = lnbDropdownExpanded) },
                                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                                )

                                                ExposedDropdownMenu(
                                                    expanded = lnbDropdownExpanded,
                                                    onDismissRequest = { lnbDropdownExpanded = false }
                                                ) {
                                                    SatellitePreset.POPULAR_SATELLITES.filter { !it.band.isCellular }.forEach { satPreset ->
                                                        DropdownMenuItem(
                                                            text = {
                                                                Text("${satPreset.name} (${satPreset.longitudeFormatted})", fontWeight = FontWeight.Bold)
                                                            },
                                                            onClick = {
                                                                viewModel.updateSecondarySatellite(lnbIdx - 1, satPreset)
                                                                lnbDropdownExpanded = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Interactive Parabolic Ray Diagram
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Interactive Parabolic Ray Diagram & Multi-LNB Focus", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFFF8FAFC))
                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E293B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                val w = size.width
                                val h = size.height
                                val apexX = w / 2f
                                val apexY = h - 30f

                                val scaleX = (w * 0.8f) / res.dishDiameterCm.toFloat()
                                val maxH = (res.focalLengthCm.coerceAtLeast(res.dishDepthCm) * 1.3).toFloat()
                                val scaleY = (h - 60f) / maxH

                                // Draw Parabola dish curve y = x^2 / (4f)
                                val parabolaPath = Path()
                                val halfD = (res.dishDiameterCm / 2.0).toFloat()

                                var first = true
                                for (xVal in -halfD.toInt()..halfD.toInt()) {
                                    val x = xVal.toFloat()
                                    val yCm = (x * x) / (4f * res.focalLengthCm.toFloat())
                                    val px = apexX + x * scaleX
                                    val py = apexY - yCm * scaleY

                                    if (first) {
                                        parabolaPath.moveTo(px, py)
                                        first = false
                                    } else {
                                        parabolaPath.lineTo(px, py)
                                    }
                                }

                                drawPath(parabolaPath, color = Color(0xFF38BDF8), style = Stroke(width = 6f))

                                // Focal Point & Multi LNB Bracket Rail
                                val focalY = apexY - res.focalLengthCm.toFloat() * scaleY
                                drawLine(color = Color.White.copy(alpha = 0.6f), start = Offset(apexX - 60f, focalY), end = Offset(apexX + 60f, focalY), strokeWidth = 3f)

                                // Draw LNB dots
                                res.multiLnbOffsets.forEach { lnb ->
                                    val lnbPx = apexX + (lnb.offsetDistanceCm.toFloat() * scaleX * 0.5f)
                                    drawCircle(
                                        color = if (lnb.isPrimary) Color(0xFFEF4444) else Color(0xFFFACC15),
                                        radius = if (lnb.isPrimary) 10f else 8f,
                                        center = Offset(lnbPx, focalY)
                                    )
                                }
                            }
                        }
                    }
                }

                // Dish Dimension Sliders
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Dish Geometry Dimensions", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

                        Text("Dish Diameter (d): %.0f cm (%.1f ft / %.0f\")".format(res.dishDiameterCm, res.dishDiameterCm / 30.48, res.dishDiameterCm / 2.54), style = MaterialTheme.typography.bodySmall)
                        Slider(
                            value = res.dishDiameterCm.toFloat(),
                            onValueChange = { viewModel.updateDishDiameter(it.toDouble()) },
                            valueRange = 40f..400f,
                            modifier = Modifier.testTag("slider_dish_diameter")
                        )

                        Text("Center Depth (c): %.1f cm (%.2f\")".format(res.dishDepthCm, res.dishDepthCm / 2.54), style = MaterialTheme.typography.bodySmall)
                        Slider(
                            value = res.dishDepthCm.toFloat(),
                            onValueChange = { viewModel.updateDishDepth(it.toDouble()) },
                            valueRange = 2f..80f,
                            modifier = Modifier.testTag("slider_dish_depth")
                        )

                        Text("Efficiency: %.0f%% | RF Gain @ %.1f GHz: %.1f dBi".format(res.efficiencyPercent, res.frequencyGhz, res.gainDbi), style = MaterialTheme.typography.bodySmall)
                        Slider(
                            value = res.efficiencyPercent.toFloat(),
                            onValueChange = { viewModel.updateEfficiency(it.toDouble()) },
                            valueRange = 30f..90f,
                            modifier = Modifier.testTag("slider_dish_efficiency")
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ArSatellitePointerCanvas(
    targetAzimuthDeg: Double,
    targetElevationDeg: Double,
    currentAzimuthDeg: Double,
    currentElevationDeg: Double,
    rotationMatrix: FloatArray,
    userLat: Double,
    userLon: Double,
    selectedSat: SatellitePreset,
    allSatellites: List<SatellitePreset>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f

        // Center reticle
        drawCircle(color = Color(0x66000000), radius = 32.dp.toPx(), center = Offset(centerX, centerY))
        drawCircle(color = Color(0xFF3B82F6), radius = 24.dp.toPx(), center = Offset(centerX, centerY), style = Stroke(width = 3.dp.toPx()))
        drawCircle(color = Color.White, radius = 4.dp.toPx(), center = Offset(centerX, centerY))

        // Compass crosshairs
        drawLine(color = Color(0xAA3B82F6), start = Offset(centerX - 40.dp.toPx(), centerY), end = Offset(centerX + 40.dp.toPx(), centerY), strokeWidth = 2f)
        drawLine(color = Color(0xAA3B82F6), start = Offset(centerX, centerY - 40.dp.toPx()), end = Offset(centerX, centerY + 40.dp.toPx()), strokeWidth = 2f)

        val fScale = size.width * 1.2f // Focal scale for 60 deg camera FOV

        // Helper lambda to project (Az, El) in degrees into screen (X, Y)
        val projectToScreen: (Double, Double) -> Offset? = { az, el ->
            val azRad = Math.toRadians(az)
            val elRad = Math.toRadians(el)

            // World unit direction vector (X=East, Y=North, Z=Up)
            val Sx = kotlin.math.cos(elRad) * kotlin.math.sin(azRad)
            val Sy = kotlin.math.cos(elRad) * kotlin.math.cos(azRad)
            val Sz = kotlin.math.sin(elRad)

            // Device frame coordinates: S_device = R^T * S_world
            val R = rotationMatrix
            val SdevX = R[0] * Sx + R[3] * Sy + R[6] * Sz
            val SdevY = R[1] * Sx + R[4] * Sy + R[7] * Sz
            val SdevZ = R[2] * Sx + R[5] * Sy + R[8] * Sz

            val Zforward = -SdevZ
            if (Zforward > 0.05) {
                val px = centerX + (fScale * (SdevX / Zforward)).toFloat()
                val py = centerY - (fScale * (SdevY / Zforward)).toFloat()
                Offset(px, py)
            } else {
                null
            }
        }

        // 1. Draw Geostationary Sky Arc (Clarke Belt) across orbital longitudes
        val arcPoints = mutableListOf<Offset>()
        for (lon in -180..180 step 2) {
            val (az, el) = SatellitePreset.calculateLookAngles(userLat, userLon, lon.toDouble())
            if (el > 0) {
                projectToScreen(az, el)?.let { arcPoints.add(it) }
            }
        }

        if (arcPoints.size >= 2) {
            val path = Path().apply {
                moveTo(arcPoints[0].x, arcPoints[0].y)
                for (i in 1 until arcPoints.size) {
                    lineTo(arcPoints[i].x, arcPoints[i].y)
                }
            }
            drawPath(path = path, color = Color(0xFFFFD700), style = Stroke(width = 4.dp.toPx()))
        }

        // 2. Draw Satellites along the Geostationary Sky Arc
        allSatellites.forEach { sat ->
            val satLon = if (sat.name == "Custom Orbital Slot") selectedSat.orbitalLongitudeDeg else sat.orbitalLongitudeDeg
            val (satAz, satEl) = SatellitePreset.calculateLookAngles(userLat, userLon, satLon)

            if (satEl > 0) {
                val pt = projectToScreen(satAz, satEl)
                if (pt != null) {
                    val isSelected = (sat.name == selectedSat.name)

                    if (isSelected) {
                        // Highlighted Target Satellite
                        drawCircle(color = Color(0xFFFF3B30), radius = 18.dp.toPx(), center = pt)
                        drawCircle(color = Color(0xFFFFD700), radius = 26.dp.toPx(), center = pt, style = Stroke(width = 4.dp.toPx()))
                        drawCircle(color = Color.White, radius = 6.dp.toPx(), center = pt)

                        // Connecting guide line from camera center to selected sat
                        drawLine(
                            color = Color(0xFFFFD700),
                            start = Offset(centerX, centerY),
                            end = pt,
                            strokeWidth = 3f
                        )
                    } else {
                        // Secondary Satellite along Arc
                        drawCircle(color = Color(0xFF38BDF8), radius = 10.dp.toPx(), center = pt)
                        drawCircle(color = Color.White, radius = 8.dp.toPx(), center = pt, style = Stroke(width = 2.dp.toPx()))
                    }
                }
            }
        }
    }
}
