package com.example.ui.screens.civil

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

@Composable
fun StationingCogoScreen(
    viewModel: StationingCogoViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val uDist = if (state.isMetric) "m" else "ft"

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
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Column {
                            Text(
                                text = "Stationing & Offset COGO",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Coordinate Geometry & Alignment Solver",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = {
                                viewModel.saveToLog()
                                Toast.makeText(context, "Calculations saved to log", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.BookmarkBorder,
                                contentDescription = "Save to Log",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        IconButton(
                            onClick = {
                                val text = buildString {
                                    appendLine("=== COGO ALIGNMENT REPORT ===")
                                    appendLine("Mode: ${state.mode.label}")
                                    appendLine("Start Station: ${viewModel.formatStation(state.startStation, state.isMetric)}")
                                    appendLine("Start (N, E, Z): ${state.startNorthing}, ${state.startEasting}, ${state.startElevation}")
                                    appendLine("Azimuth: ${state.azimuthDegrees}° | Grade: ${state.longitudinalGradePct}%")
                                    if (state.mode == CogoCalcMode.STATION_OFFSET_TO_COORD) {
                                        appendLine("Target Station: ${viewModel.formatStation(state.targetStation, state.isMetric)} | Offset: ${state.targetOffset} $uDist")
                                        appendLine("Computed Northing: ${String.format("%.4f", state.calculatedNorthing)}")
                                        appendLine("Computed Easting: ${String.format("%.4f", state.calculatedEasting)}")
                                        appendLine("Centerline Elev (Z): ${String.format("%.3f", state.calculatedCenterlineZ)} $uDist")
                                        appendLine("Edge / Offset Elev: ${String.format("%.3f", state.calculatedElevation)} $uDist")
                                    } else if (state.mode == CogoCalcMode.COORD_TO_STATION_OFFSET) {
                                        appendLine("Shot Point (N, E): ${state.pointNorthing}, ${state.pointEasting}")
                                        appendLine("Inverse Station: ${viewModel.formatStation(state.inverseStation, state.isMetric)}")
                                        appendLine("Inverse Offset: ${String.format("%.3f", state.inverseOffset)} $uDist (${if (state.inverseOffset >= 0) "RIGHT" else "LEFT"})")
                                        appendLine("Elev Diff to CL: ${String.format("%.3f", state.inverseElevationDiff)} $uDist")
                                    } else if (state.mode == CogoCalcMode.CURVE_SOLVER) {
                                        appendLine("Curve Radius (R): ${state.curveRadius} $uDist | Delta: ${state.deltaAngleDeg}°")
                                        appendLine("Tangent (T): ${String.format("%.3f", state.curveTangentLength)} $uDist")
                                        appendLine("Length (L): ${String.format("%.3f", state.curveArcLength)} $uDist")
                                        appendLine("Chord (C): ${String.format("%.3f", state.curveChordLength)} $uDist")
                                        appendLine("PC Sta: ${viewModel.formatStation(state.pcStation, state.isMetric)} | PT Sta: ${viewModel.formatStation(state.ptStation, state.isMetric)}")
                                    }
                                }
                                clipboardManager.setText(AnnotatedString(text))
                                Toast.makeText(context, "COGO report copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Report",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Unit and Mode Toggle Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !state.isMetric,
                    onClick = { viewModel.setMetric(false) },
                    label = { Text("US Survey (ft)") }
                )
                FilterChip(
                    selected = state.isMetric,
                    onClick = { viewModel.setMetric(true) },
                    label = { Text("Metric (m)") }
                )
            }

            // Calculation Mode Selection Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CogoCalcMode.values().forEach { m ->
                    FilterChip(
                        selected = state.mode == m,
                        onClick = { viewModel.setMode(m) },
                        label = { Text(m.label, maxLines = 1) },
                        leadingIcon = {
                            Icon(
                                imageVector = when (m) {
                                    CogoCalcMode.STATION_OFFSET_TO_COORD -> Icons.Default.Navigation
                                    CogoCalcMode.COORD_TO_STATION_OFFSET -> Icons.Default.Map
                                    CogoCalcMode.CURVE_SOLVER -> Icons.Default.LinearScale
                                    CogoCalcMode.SUPERELEVATION_GRADE -> Icons.Default.Straighten
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            // Interactive 2D Coordinate Geometry Graphic Canvas
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E293B)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Alignment & Offset Visualizer",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Interactive centerline vector, curve tangent, and perpendicular offset projection",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                    ) {
                        val width = size.width
                        val height = size.height
                        val center = Offset(width * 0.35f, height * 0.70f)

                        // Grid lines
                        val gridSpacing = 30f
                        for (x in 0..(width / gridSpacing).toInt()) {
                            drawLine(
                                color = Color(0xFF334155).copy(alpha = 0.4f),
                                start = Offset(x * gridSpacing, 0f),
                                end = Offset(x * gridSpacing, height),
                                strokeWidth = 1f
                            )
                        }
                        for (y in 0..(height / gridSpacing).toInt()) {
                            drawLine(
                                color = Color(0xFF334155).copy(alpha = 0.4f),
                                start = Offset(0f, y * gridSpacing),
                                end = Offset(width, y * gridSpacing),
                                strokeWidth = 1f
                            )
                        }

                        // Compass North Arrow
                        val compassOrigin = Offset(width - 35f, 35f)
                        drawLine(
                            color = Color(0xFFEF4444),
                            start = compassOrigin,
                            end = Offset(compassOrigin.x, compassOrigin.y - 24f),
                            strokeWidth = 3f
                        )
                        drawCircle(
                            color = Color(0xFFEF4444),
                            radius = 3f,
                            center = Offset(compassOrigin.x, compassOrigin.y - 24f)
                        )

                        // Baseline Centerline Vector
                        val azRad = Math.toRadians(state.azimuthDegrees)
                        val dx = sin(azRad).toFloat()
                        val dy = -cos(azRad).toFloat() // Screen Y is down, North is up

                        val lineLength = min(width, height) * 0.75f
                        val endTangent = Offset(center.x + dx * lineLength, center.y + dy * lineLength)

                        // Baseline line (Gold / Amber)
                        drawLine(
                            color = Color(0xFFFBBF24),
                            start = center,
                            end = endTangent,
                            strokeWidth = 4f
                        )

                        // Station tick marks along baseline
                        val numTicks = 5
                        for (i in 0..numTicks) {
                            val t = i / numTicks.toFloat()
                            val tickPos = Offset(center.x + dx * lineLength * t, center.y + dy * lineLength * t)
                            val perpDx = cos(azRad).toFloat()
                            val perpDy = sin(azRad).toFloat()

                            drawLine(
                                color = Color(0xFFFDE68A),
                                start = Offset(tickPos.x - perpDx * 6f, tickPos.y - perpDy * 6f),
                                end = Offset(tickPos.x + perpDx * 6f, tickPos.y + perpDy * 6f),
                                strokeWidth = 2f
                            )
                        }

                        // POB / Start Station point
                        drawCircle(color = Color(0xFF38BDF8), radius = 6f, center = center)

                        if (state.mode == CogoCalcMode.CURVE_SOLVER) {
                            // Draw horizontal curve arc
                            val curveStart = Offset(center.x + dx * (lineLength * 0.3f), center.y + dy * (lineLength * 0.3f))
                            val curvePi = Offset(center.x + dx * (lineLength * 0.65f), center.y + dy * (lineLength * 0.65f))
                            
                            val curvePath = Path().apply {
                                moveTo(curveStart.x, curveStart.y)
                                quadraticBezierTo(curvePi.x, curvePi.y, endTangent.x, endTangent.y)
                            }
                            drawPath(
                                path = curvePath,
                                color = Color(0xFF34D399),
                                style = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 6f)))
                            )
                            drawCircle(color = Color(0xFFEC4899), radius = 5f, center = curvePi)
                            drawCircle(color = Color(0xFF34D399), radius = 5f, center = curveStart)
                        } else {
                            // Target Station & Offset Point
                            val frac = 0.55f
                            val targetClPos = Offset(center.x + dx * lineLength * frac, center.y + dy * lineLength * frac)

                            // Perpendicular offset vector (Right is +90 deg)
                            val offsetPerpDx = cos(azRad).toFloat()
                            val offsetPerpDy = sin(azRad).toFloat()
                            val offsetScale = (state.targetOffset / 30.0).toFloat().coerceIn(-40f, 40f)

                            val targetOffsetPoint = Offset(
                                targetClPos.x + offsetPerpDx * (offsetScale * 20f),
                                targetClPos.y + offsetPerpDy * (offsetScale * 20f)
                            )

                            // Offset projection line (Cyan dashed)
                            drawLine(
                                color = Color(0xFF38BDF8),
                                start = targetClPos,
                                end = targetOffsetPoint,
                                strokeWidth = 3f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
                            )
                            drawCircle(color = Color(0xFFF59E0B), radius = 5f, center = targetClPos)
                            drawCircle(color = Color(0xFFEC4899), radius = 7f, center = targetOffsetPoint)
                        }
                    }
                }
            }

            // Input Fields Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Baseline Alignment Parameters",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.startStation.toString(),
                            onValueChange = { v -> v.toDoubleOrNull()?.let { viewModel.updateInputs(startStation = it) } },
                            label = { Text("Start Station (${if (state.isMetric) "1+000" else "10+00"})") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = state.azimuthDegrees.toString(),
                            onValueChange = { v -> v.toDoubleOrNull()?.let { viewModel.updateInputs(azimuthDegrees = it) } },
                            label = { Text("Azimuth (deg)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.startNorthing.toString(),
                            onValueChange = { v -> v.toDoubleOrNull()?.let { viewModel.updateInputs(startNorthing = it) } },
                            label = { Text("Start Northing (N)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = state.startEasting.toString(),
                            onValueChange = { v -> v.toDoubleOrNull()?.let { viewModel.updateInputs(startEasting = it) } },
                            label = { Text("Start Easting (E)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.startElevation.toString(),
                            onValueChange = { v -> v.toDoubleOrNull()?.let { viewModel.updateInputs(startElevation = it) } },
                            label = { Text("Start Elev Z ($uDist)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = state.longitudinalGradePct.toString(),
                            onValueChange = { v -> v.toDoubleOrNull()?.let { viewModel.updateInputs(longitudinalGradePct = it) } },
                            label = { Text("Long. Grade (%)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when (state.mode) {
                            CogoCalcMode.STATION_OFFSET_TO_COORD -> "Target Station & Offset"
                            CogoCalcMode.COORD_TO_STATION_OFFSET -> "Field Survey Point Coordinates"
                            CogoCalcMode.CURVE_SOLVER -> "Horizontal Circular Curve Setup"
                            CogoCalcMode.SUPERELEVATION_GRADE -> "Cross Section & Superelevation"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    if (state.mode == CogoCalcMode.STATION_OFFSET_TO_COORD) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = state.targetStation.toString(),
                                onValueChange = { v -> v.toDoubleOrNull()?.let { viewModel.updateInputs(targetStation = it) } },
                                label = { Text("Target Station") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                            OutlinedTextField(
                                value = state.targetOffset.toString(),
                                onValueChange = { v -> v.toDoubleOrNull()?.let { viewModel.updateInputs(targetOffset = it) } },
                                label = { Text("Offset $uDist (+R / -L)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                        }
                    } else if (state.mode == CogoCalcMode.COORD_TO_STATION_OFFSET) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = state.pointNorthing.toString(),
                                onValueChange = { v -> v.toDoubleOrNull()?.let { viewModel.updateInputs(pointNorthing = it) } },
                                label = { Text("Shot Northing (N)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                            OutlinedTextField(
                                value = state.pointEasting.toString(),
                                onValueChange = { v -> v.toDoubleOrNull()?.let { viewModel.updateInputs(pointEasting = it) } },
                                label = { Text("Shot Easting (E)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                        }
                        OutlinedTextField(
                            value = state.pointElevation.toString(),
                            onValueChange = { v -> v.toDoubleOrNull()?.let { viewModel.updateInputs(pointElevation = it) } },
                            label = { Text("Shot Elevation Z ($uDist)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    } else if (state.mode == CogoCalcMode.CURVE_SOLVER) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = state.curveRadius.toString(),
                                onValueChange = { v -> v.toDoubleOrNull()?.let { viewModel.updateInputs(curveRadius = it) } },
                                label = { Text("Radius R ($uDist)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                            OutlinedTextField(
                                value = state.deltaAngleDeg.toString(),
                                onValueChange = { v -> v.toDoubleOrNull()?.let { viewModel.updateInputs(deltaAngleDeg = it) } },
                                label = { Text("Delta Angle Δ (deg)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                        }
                        OutlinedTextField(
                            value = state.piStation.toString(),
                            onValueChange = { v -> v.toDoubleOrNull()?.let { viewModel.updateInputs(piStation = it) } },
                            label = { Text("P.I. Station") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    } else if (state.mode == CogoCalcMode.SUPERELEVATION_GRADE) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = state.laneWidth.toString(),
                                onValueChange = { v -> v.toDoubleOrNull()?.let { viewModel.updateInputs(laneWidth = it) } },
                                label = { Text("Lane Width ($uDist)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                            OutlinedTextField(
                                value = state.normalCrownPct.toString(),
                                onValueChange = { v -> v.toDoubleOrNull()?.let { viewModel.updateInputs(normalCrownPct = it) } },
                                label = { Text("Normal Crown (%)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                        }
                    }
                }
            }

            // Results Display Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Computed Coordinate Geometry Output",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (state.mode == CogoCalcMode.STATION_OFFSET_TO_COORD) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CogoStatBox(
                                title = "Computed Northing (N)",
                                value = String.format("%.4f", state.calculatedNorthing),
                                subtitle = "Target Offset Point",
                                modifier = Modifier.weight(1f)
                            )
                            CogoStatBox(
                                title = "Computed Easting (E)",
                                value = String.format("%.4f", state.calculatedEasting),
                                subtitle = "Target Offset Point",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CogoStatBox(
                                title = "Centerline Elev (Z)",
                                value = "${String.format("%.3f", state.calculatedCenterlineZ)} $uDist",
                                subtitle = "At Station ${viewModel.formatStation(state.targetStation, state.isMetric)}",
                                modifier = Modifier.weight(1f)
                            )
                            CogoStatBox(
                                title = "Offset Point Elev (Z)",
                                value = "${String.format("%.3f", state.calculatedElevation)} $uDist",
                                subtitle = "Cross-fall ${state.normalCrownPct}%",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else if (state.mode == CogoCalcMode.COORD_TO_STATION_OFFSET) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CogoStatBox(
                                title = "Projected Station",
                                value = viewModel.formatStation(state.inverseStation, state.isMetric),
                                subtitle = "Along Baseline",
                                modifier = Modifier.weight(1f)
                            )
                            CogoStatBox(
                                title = "Perpendicular Offset",
                                value = "${String.format("%.3f", abs(state.inverseOffset))} $uDist",
                                subtitle = if (state.inverseOffset >= 0) "RIGHT OF CL" else "LEFT OF CL",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        CogoStatBox(
                            title = "Elevation Difference to CL",
                            value = "${String.format("%+.3f", state.inverseElevationDiff)} $uDist",
                            subtitle = if (state.inverseElevationDiff >= 0) "ABOVE Baseline Profile" else "BELOW Baseline Profile",
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else if (state.mode == CogoCalcMode.CURVE_SOLVER) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CogoStatBox(
                                title = "Tangent Length (T)",
                                value = "${String.format("%.3f", state.curveTangentLength)} $uDist",
                                subtitle = "PI to PC / PT",
                                modifier = Modifier.weight(1f)
                            )
                            CogoStatBox(
                                title = "Curve Length (L)",
                                value = "${String.format("%.3f", state.curveArcLength)} $uDist",
                                subtitle = "Arc Along Baseline",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CogoStatBox(
                                title = "Long Chord (C)",
                                value = "${String.format("%.3f", state.curveChordLength)} $uDist",
                                subtitle = "Straight PC to PT",
                                modifier = Modifier.weight(1f)
                            )
                            CogoStatBox(
                                title = "External Dist (E)",
                                value = "${String.format("%.3f", state.curveExternalDist)} $uDist",
                                subtitle = "PI to Mid Arc",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CogoStatBox(
                                title = "PC Station",
                                value = viewModel.formatStation(state.pcStation, state.isMetric),
                                subtitle = "Point of Curvature",
                                modifier = Modifier.weight(1f)
                            )
                            CogoStatBox(
                                title = "PT Station",
                                value = viewModel.formatStation(state.ptStation, state.isMetric),
                                subtitle = "Point of Tangency",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else if (state.mode == CogoCalcMode.SUPERELEVATION_GRADE) {
                        val outerEdgeDrop = state.laneWidth * (state.superElevationRatePct / 100.0)
                        val normalCrownDrop = state.laneWidth * (abs(state.normalCrownPct) / 100.0)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CogoStatBox(
                                title = "Banked Edge Rise",
                                value = "${String.format("%.3f", outerEdgeDrop)} $uDist",
                                subtitle = "At ${state.superElevationRatePct}% e_max",
                                modifier = Modifier.weight(1f)
                            )
                            CogoStatBox(
                                title = "Normal Crown Drop",
                                value = "${String.format("%.3f", normalCrownDrop)} $uDist",
                                subtitle = "At ${state.normalCrownPct}% Crown",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CogoStatBox(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
