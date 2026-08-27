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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Foundation
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RetainingWallSizerScreen(
    viewModel: RetainingWallSizerViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val uDist = if (state.isMetric) "m" else "ft"
    val uThrust = if (state.isMetric) "kN/m" else "lbs/ft"

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
                                imageVector = Icons.Default.Foundation,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Column {
                            Text(
                                text = "Retaining Wall Sizer",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Lateral Earth Pressure & Geogrid Sizing",
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
                                    appendLine("=== RETAINING WALL GEOTECHNICAL REPORT ===")
                                    appendLine("Wall Type: ${state.wallType.label}")
                                    appendLine("Dimensions: Height ${state.wallHeight} $uDist, Length ${state.wallLength} $uDist, Embedment ${state.embedmentDepth} $uDist")
                                    appendLine("Soil: ${state.backfillType.label} (phi=${state.soilFrictionAngleDeg}°, gamma=${state.soilUnitWeight})")
                                    appendLine("Active Pressure Ka: ${String.format("%.4f", state.kaRankine)} | Passive Kp: ${String.format("%.3f", state.kpRankine)}")
                                    appendLine("Total Lateral Thrust: ${String.format("%.1f", state.totalActiveThrustPerFt)} $uThrust")
                                    appendLine("Overturning FS: ${String.format("%.2f", state.fsOverturning)} (${if (state.fsOverturning >= 1.5) "PASS" else "FAIL < 1.5"})")
                                    appendLine("Sliding FS: ${String.format("%.2f", state.fsSliding)} (${if (state.fsSliding >= 1.5) "PASS" else "FAIL < 1.5"})")
                                    appendLine("Geogrid Reinforcement: ${state.numGeogridTiers} tiers @ ${String.format("%.1f", state.geogridLengthPerTier)} $uDist length")
                                    appendLine("Total Geogrid Area: ${String.format("%.1f", state.totalGeogridAreaSqYd)} ${if (state.isMetric) "m²" else "sq yd"}")
                                    appendLine("Drainage Stone (#57): ${String.format("%.1f", state.drainageStoneVolumeCuYd)} cu yd (~${String.format("%.1f", state.drainageStoneTons)} tons)")
                                    appendLine("Estimated Blocks: ${state.totalBlockCount} units (${String.format("%.0f", state.wallFaceAreaSqFt)} sq ft face)")
                                }
                                clipboardManager.setText(AnnotatedString(text))
                                Toast.makeText(context, "Retaining wall report copied", Toast.LENGTH_SHORT).show()
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

            // Unit and Wall Type Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !state.isMetric,
                    onClick = { viewModel.setMetric(false) },
                    label = { Text("US Customary (ft/lbs)") }
                )
                FilterChip(
                    selected = state.isMetric,
                    onClick = { viewModel.setMetric(true) },
                    label = { Text("Metric (m/kN)") }
                )
            }

            // Wall Structure Types
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RetainingWallType.values().forEach { wt ->
                    FilterChip(
                        selected = state.wallType == wt,
                        onClick = { viewModel.setWallType(wt) },
                        label = { Text(wt.label) }
                    )
                }
            }

            // Interactive Cross-Section Visualization Canvas
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E293B)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Wall Cross-Section & Earth Thrust",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // FS Status Badge
                            val pass = state.fsOverturning >= 1.5 && state.fsSliding >= 1.5
                            Surface(
                                color = if (pass) Color(0xFF059669) else Color(0xFFDC2626),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (pass) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = if (pass) "STABLE (FS ≥ 1.5)" else "UNSTABLE (LOW FS)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = "Visualizes block tier courses, geogrid sheets, gravel drainage chimney, and active pressure thrust",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                    ) {
                        val width = size.width
                        val height = size.height

                        val wallLeftX = width * 0.25f
                        val wallThickness = width * 0.08f
                        val baseFootingY = height * 0.85f
                        val totalWallHeightPx = height * 0.65f
                        val wallTopY = baseFootingY - totalWallHeightPx

                        // 1. Backfill Soil Mass (Light Brownish Grey)
                        val backfillPath = Path().apply {
                            moveTo(wallLeftX + wallThickness, wallTopY)
                            lineTo(width, wallTopY - (width - wallLeftX - wallThickness) * 0.15f)
                            lineTo(width, baseFootingY)
                            lineTo(wallLeftX + wallThickness, baseFootingY)
                            close()
                        }
                        drawPath(path = backfillPath, color = Color(0xFF78716C).copy(alpha = 0.4f))

                        // 2. Clear Gravel Drainage Chimney (12" directly behind blocks)
                        val gravelWidth = width * 0.06f
                        drawRect(
                            color = Color(0xFF94A3B8).copy(alpha = 0.7f),
                            topLeft = Offset(wallLeftX + wallThickness, wallTopY + 10f),
                            size = Size(gravelWidth, totalWallHeightPx - 10f)
                        )

                        // 3. Perforated Drain Pipe at heel
                        drawCircle(
                            color = Color(0xFF0284C7),
                            radius = 9f,
                            center = Offset(wallLeftX + wallThickness + gravelWidth / 2f, baseFootingY - 14f)
                        )

                        // 4. Leveling Pad / Footing under Wall
                        drawRect(
                            color = Color(0xFFCBD5E1),
                            topLeft = Offset(wallLeftX - 10f, baseFootingY),
                            size = Size(wallThickness + 20f, 18f)
                        )

                        // 5. Modular Wall Blocks / Courses
                        val numCourses = 8
                        val courseHeight = totalWallHeightPx / numCourses
                        for (i in 0 until numCourses) {
                            val courseY = baseFootingY - (i + 1) * courseHeight
                            val setbackX = i * 2.5f // Batter setback
                            drawRect(
                                color = if (i % 2 == 0) Color(0xFFD97706) else Color(0xFFB45309),
                                topLeft = Offset(wallLeftX + setbackX, courseY),
                                size = Size(wallThickness, courseHeight - 1f)
                            )
                        }

                        // 6. Geogrid Reinforcement Layers (Green dashed lines extending back)
                        val numGrids = state.numGeogridTiers.coerceIn(1, 6)
                        val gridSpanPx = width * 0.45f
                        for (g in 1..numGrids) {
                            val gridY = baseFootingY - (g * (totalWallHeightPx / (numGrids + 1)))
                            drawLine(
                                color = Color(0xFF10B981),
                                start = Offset(wallLeftX + wallThickness, gridY),
                                end = Offset(wallLeftX + wallThickness + gridSpanPx, gridY),
                                strokeWidth = 4f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 4f))
                            )
                        }

                        // 7. Active Earth Pressure Triangle & Resultant Thrust Vector
                        val triangleStartX = wallLeftX + wallThickness + gravelWidth + 25f
                        val pressurePath = Path().apply {
                            moveTo(triangleStartX, wallTopY)
                            lineTo(triangleStartX + 65f, baseFootingY)
                            lineTo(triangleStartX, baseFootingY)
                            close()
                        }
                        drawPath(path = pressurePath, color = Color(0xFFEF4444).copy(alpha = 0.35f))
                        drawPath(path = pressurePath, color = Color(0xFFEF4444), style = Stroke(width = 2f))

                        // Thrust Arrow (Pa)
                        val thrustY = baseFootingY - (totalWallHeightPx * 0.33f)
                        drawLine(
                            color = Color(0xFFF43F5E),
                            start = Offset(triangleStartX + 70f, thrustY),
                            end = Offset(wallLeftX + wallThickness, thrustY),
                            strokeWidth = 4f
                        )
                        // Arrow head
                        drawCircle(color = Color(0xFFF43F5E), radius = 6f, center = Offset(wallLeftX + wallThickness, thrustY))
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
                        text = "Wall Geometry & Site Inputs",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.wallHeight.toString(),
                            onValueChange = { v -> v.toDoubleOrNull()?.let { viewModel.updateInputs(wallHeight = it) } },
                            label = { Text("Exposed Height ($uDist)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = state.wallLength.toString(),
                            onValueChange = { v -> v.toDoubleOrNull()?.let { viewModel.updateInputs(wallLength = it) } },
                            label = { Text("Wall Length ($uDist)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.embedmentDepth.toString(),
                            onValueChange = { v -> v.toDoubleOrNull()?.let { viewModel.updateInputs(embedmentDepth = it) } },
                            label = { Text("Embedment ($uDist)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = state.baseWidth.toString(),
                            onValueChange = { v -> v.toDoubleOrNull()?.let { viewModel.updateInputs(baseWidth = it) } },
                            label = { Text("Base Width ($uDist)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Soil & Surcharge Loading",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    // Soil Presets
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SoilBackfillType.values().forEach { sbt ->
                            FilterChip(
                                selected = state.backfillType == sbt,
                                onClick = { viewModel.setBackfillType(sbt) },
                                label = { Text(sbt.label) }
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.soilFrictionAngleDeg.toString(),
                            onValueChange = { v -> v.toDoubleOrNull()?.let { viewModel.updateInputs(soilFrictionAngleDeg = it) } },
                            label = { Text("Friction Angle φ (°)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = state.soilUnitWeight.toString(),
                            onValueChange = { v -> v.toDoubleOrNull()?.let { viewModel.updateInputs(soilUnitWeight = it) } },
                            label = { Text("Unit Wt γ (${if (state.isMetric) "kN/m³" else "pcf"})") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.backfillSlopeDeg.toString(),
                            onValueChange = { v -> v.toDoubleOrNull()?.let { viewModel.updateInputs(backfillSlopeDeg = it) } },
                            label = { Text("Backfill Slope β (°)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = state.surchargeLoad.toString(),
                            onValueChange = { v -> v.toDoubleOrNull()?.let { viewModel.updateInputs(surchargeLoad = it) } },
                            label = { Text("Surcharge q (${if (state.isMetric) "kPa" else "psf"})") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                }
            }

            // Results & Sizing Cards
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
                        text = "Geotechnical Sizing & Safety Factors",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        RetainingStatBox(
                            title = "Active Thrust (Pa)",
                            value = "${String.format("%.1f", state.totalActiveThrustPerFt)} $uThrust",
                            subtitle = "Ka = ${String.format("%.3f", state.kaRankine)}",
                            modifier = Modifier.weight(1f)
                        )
                        RetainingStatBox(
                            title = "Overturning FS",
                            value = String.format("%.2f", state.fsOverturning),
                            subtitle = if (state.fsOverturning >= 1.5) "PASS (≥1.5 required)" else "FAIL (Increase grid/base)",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        RetainingStatBox(
                            title = "Sliding FS",
                            value = String.format("%.2f", state.fsSliding),
                            subtitle = if (state.fsSliding >= 1.5) "PASS (≥1.5 required)" else "FAIL (Add key/grid)",
                            modifier = Modifier.weight(1f)
                        )
                        RetainingStatBox(
                            title = "Max Base Pressure",
                            value = "${String.format("%.0f", state.maxBearingPressure)} ${if (state.isMetric) "kPa" else "psf"}",
                            subtitle = "Eccentricity = ${String.format("%.2f", state.baseEccentricity)} $uDist",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Bill of Materials & Reinforcement Schedule",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        RetainingStatBox(
                            title = "Geogrid Layers",
                            value = "${state.numGeogridTiers} Tiers",
                            subtitle = "Length = ${String.format("%.1f", state.geogridLengthPerTier)} $uDist each",
                            modifier = Modifier.weight(1f)
                        )
                        RetainingStatBox(
                            title = "Total Geogrid Area",
                            value = "${String.format("%.1f", state.totalGeogridAreaSqYd)} ${if (state.isMetric) "m²" else "yd²"}",
                            subtitle = "${state.numGeogridTiers} rolls scheduled",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        RetainingStatBox(
                            title = "Drainage Stone (#57)",
                            value = "${String.format("%.1f", state.drainageStoneVolumeCuYd)} cu yd",
                            subtitle = "~${String.format("%.1f", state.drainageStoneTons)} tons (12\" chimney)",
                            modifier = Modifier.weight(1f)
                        )
                        RetainingStatBox(
                            title = "Total Wall Blocks",
                            value = "${state.totalBlockCount} units",
                            subtitle = "${String.format("%.0f", state.wallFaceAreaSqFt)} sq ft face (+10% waste)",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RetainingStatBox(
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
