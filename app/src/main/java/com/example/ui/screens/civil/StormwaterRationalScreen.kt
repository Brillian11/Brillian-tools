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
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import kotlin.math.max

@Composable
fun StormwaterRationalScreen(
    viewModel: StormwaterRationalViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val uArea = if (state.isMetric) "ha" else "acres"
    val uQ = if (state.isMetric) "m³/s" else "cfs"
    val uInt = if (state.isMetric) "mm/hr" else "in/hr"
    val uVel = if (state.isMetric) "m/s" else "fps"

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
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Column {
                            Text(
                                text = "Stormwater Runoff Sizer",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Rational Method (Q=CIA) & Culvert Sizing",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = {
                                viewModel.saveToLog()
                                Toast.makeText(context, "Hydrology calculation saved to log", Toast.LENGTH_SHORT).show()
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
                                    appendLine("=== STORMWATER RUNOFF & DRAINAGE REPORT ===")
                                    appendLine("Storm Recurrence: ${state.returnPeriod.label}")
                                    appendLine("Total Drainage Catchment: ${String.format("%.2f", state.totalDrainageArea)} $uArea")
                                    appendLine("Composite Runoff Coeff (C): ${String.format("%.3f", state.compositeRunoffCoeffC)}")
                                    appendLine("Rainfall Intensity (I): ${state.baseRainfallIntensityInHr} $uInt | Tc: ${state.timeOfConcentrationMin} min")
                                    appendLine("--------------------------------------")
                                    appendLine("Post-Development Peak Discharge (Q): ${String.format("%.2f", state.adjustedPeakDischargeQ)} $uQ")
                                    appendLine("Pre-Development Peak Discharge (Q): ${String.format("%.2f", state.preDevelopmentDischargeQ)} $uQ")
                                    appendLine("Recommended Culvert Diameter: ${state.recommendedStandardPipeSizeInches}\" ${state.pipeMaterial.label}")
                                    appendLine("Culvert Flow Velocity: ${String.format("%.1f", state.flowVelocityFps)} $uVel (Slope: ${state.culvertSlopePct}%)")
                                    appendLine("Required Detention Storage: ${String.format("%.0f", state.detentionStorageVolumeCuYd)} cu yd (~${String.format("%.2f", state.detentionStorageAcreFt)} acre-ft)")
                                }
                                clipboardManager.setText(AnnotatedString(text))
                                Toast.makeText(context, "Hydrology report copied to clipboard", Toast.LENGTH_SHORT).show()
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

            // Unit & Storm Recurrence Selection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !state.isMetric,
                    onClick = { viewModel.setMetric(false) },
                    label = { Text("US Units (cfs/acres)") }
                )
                FilterChip(
                    selected = state.isMetric,
                    onClick = { viewModel.setMetric(true) },
                    label = { Text("Metric (m³/s / ha)") }
                )
            }

            // Storm Return Period Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReturnPeriod.values().forEach { rp ->
                    FilterChip(
                        selected = state.returnPeriod == rp,
                        onClick = { viewModel.setReturnPeriod(rp) },
                        label = { Text("${rp.years}-Year Storm") }
                    )
                }
            }

            // Interactive Runoff Hydrograph & Culvert Canvas
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
                            text = "Hydrograph & Pipe Hydraulic Capacity",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = Color(0xFF0284C7),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Q = ${String.format("%.2f", state.adjustedPeakDischargeQ)} $uQ",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Modified Rational hydrograph showing peak attenuation & culvert full flow",
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

                        // Left: Rational Triangular Hydrograph (width * 0.55)
                        val hydroWidth = width * 0.52f
                        val groundY = height * 0.82f
                        val peakY = height * 0.20f

                        val tcX = hydroWidth * 0.40f
                        val endX = hydroWidth * 0.85f

                        // Grid lines
                        drawLine(color = Color(0xFF334155), start = Offset(20f, groundY), end = Offset(hydroWidth, groundY), strokeWidth = 1.5f)
                        drawLine(color = Color(0xFF334155), start = Offset(20f, 20f), end = Offset(20f, groundY), strokeWidth = 1.5f)

                        // Post-development Hydrograph (Cyan shaded triangle)
                        val postPath = Path().apply {
                            moveTo(20f, groundY)
                            lineTo(tcX, peakY)
                            lineTo(endX, groundY)
                            close()
                        }
                        drawPath(path = postPath, color = Color(0xFF0284C7).copy(alpha = 0.4f))
                        drawPath(path = postPath, color = Color(0xFF38BDF8), style = Stroke(width = 3f))

                        // Pre-development Allowable Release Line (Green dashed)
                        val prePeakY = groundY - (groundY - peakY) * 0.35f
                        val prePath = Path().apply {
                            moveTo(20f, groundY)
                            lineTo(tcX, prePeakY)
                            lineTo(endX, groundY)
                            close()
                        }
                        drawPath(path = prePath, color = Color(0xFF10B981), style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))))

                        // Peak marker
                        drawCircle(color = Color(0xFF38BDF8), radius = 5f, center = Offset(tcX, peakY))

                        // Right: Circular Culvert Cross-Section (width * 0.45)
                        val pipeCenterX = width * 0.78f
                        val pipeCenterY = height * 0.50f
                        val pipeRadius = height * 0.34f

                        // Outer Pipe Ring
                        drawCircle(
                            color = Color(0xFF64748B),
                            radius = pipeRadius,
                            center = Offset(pipeCenterX, pipeCenterY),
                            style = Stroke(width = 6f)
                        )

                        // Water Flow Depth inside pipe
                        val waterPath = Path().apply {
                            val waterY = pipeCenterY + pipeRadius * 0.2f
                            addArc(
                                oval = androidx.compose.ui.geometry.Rect(
                                    pipeCenterX - pipeRadius + 4f,
                                    pipeCenterY - pipeRadius + 4f,
                                    pipeCenterX + pipeRadius - 4f,
                                    pipeCenterY + pipeRadius - 4f
                                ),
                                startAngleDegrees = 20f,
                                sweepAngleDegrees = 140f
                            )
                        }
                        drawPath(path = waterPath, color = Color(0xFF38BDF8).copy(alpha = 0.5f))
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
                        text = "Rainfall Intensity & Hydraulic Parameters",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.baseRainfallIntensityInHr.toString(),
                            onValueChange = { v -> v.toDoubleOrNull()?.let { viewModel.updateInputs(baseRainfallIntensityInHr = it) } },
                            label = { Text("Rainfall Intensity I ($uInt)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = state.timeOfConcentrationMin.toString(),
                            onValueChange = { v -> v.toDoubleOrNull()?.let { viewModel.updateInputs(timeOfConcentrationMin = it) } },
                            label = { Text("Time of Concen. Tc (min)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.culvertSlopePct.toString(),
                            onValueChange = { v -> v.toDoubleOrNull()?.let { viewModel.updateInputs(culvertSlopePct = it) } },
                            label = { Text("Culvert Slope (%)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = state.preDevelopmentC.toString(),
                            onValueChange = { v -> v.toDoubleOrNull()?.let { viewModel.updateInputs(preDevelopmentC = it) } },
                            label = { Text("Pre-Dev Raw C (e.g. 0.20)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Culvert Conduit Material",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StormwaterPipeMaterial.values().forEach { pm ->
                            FilterChip(
                                selected = state.pipeMaterial == pm,
                                onClick = { viewModel.setPipeMaterial(pm) },
                                label = { Text(pm.label) }
                            )
                        }
                    }
                }
            }

            // Results & Sizing Outputs
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
                        text = "Hydraulic Sizing & Runoff Output",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StormwaterStatBox(
                            title = "Peak Discharge (Q_post)",
                            value = "${String.format("%.2f", state.adjustedPeakDischargeQ)} $uQ",
                            subtitle = "Composite C = ${String.format("%.2f", state.compositeRunoffCoeffC)}",
                            modifier = Modifier.weight(1f)
                        )
                        StormwaterStatBox(
                            title = "Pre-Dev Peak (Q_pre)",
                            value = "${String.format("%.2f", state.preDevelopmentDischargeQ)} $uQ",
                            subtitle = "Allowable Release Baseline",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StormwaterStatBox(
                            title = "Recommended Culvert",
                            value = "${state.recommendedStandardPipeSizeInches}\" Diameter",
                            subtitle = "Min req: ${String.format("%.1f", state.requiredPipeDiameterInches)}\"",
                            modifier = Modifier.weight(1f)
                        )
                        StormwaterStatBox(
                            title = "Flow Velocity",
                            value = "${String.format("%.1f", state.flowVelocityFps)} $uVel",
                            subtitle = "At ${state.culvertSlopePct}% grade",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StormwaterStatBox(
                            title = "Detention Storage Vol",
                            value = "${String.format("%.0f", state.detentionStorageVolumeCuYd)} cu yd",
                            subtitle = "~${String.format("%.2f", state.detentionStorageAcreFt)} acre-ft storage",
                            modifier = Modifier.weight(1f)
                        )
                        StormwaterStatBox(
                            title = "Total Catchment Area",
                            value = "${String.format("%.2f", state.totalDrainageArea)} $uArea",
                            subtitle = "Weighted Multi-Surface",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Sub-Catchment Breakdown Table
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Sub-Catchment Surface Areas (Composite C Builder)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    state.subAreas.forEach { sub ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = sub.name,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1.8f)
                            )
                            OutlinedTextField(
                                value = sub.areaAcres.toString(),
                                onValueChange = { v ->
                                    v.toDoubleOrNull()?.let { viewModel.updateSubArea(sub.id, it) }
                                },
                                label = { Text(uArea) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                        }
                        Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                    }
                }
            }
        }
    }
}

@Composable
private fun StormwaterStatBox(
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
