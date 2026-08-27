package com.example.ui.screens.woodworking

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FormatAlignJustify
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SagulatorScreen(
    viewModel: SagulatorViewModel,
    modifier: Modifier = Modifier
) {
    val selectedSpecies by viewModel.selectedSpecies.collectAsState()
    val spanInches by viewModel.shelfSpanInches.collectAsState()
    val depthInches by viewModel.shelfDepthInches.collectAsState()
    val thicknessInches by viewModel.shelfThicknessInches.collectAsState()
    val loadLbs by viewModel.appliedLoadLbs.collectAsState()
    val loadDistribution by viewModel.loadDistribution.collectAsState()
    val supportType by viewModel.supportType.collectAsState()
    val hasEdging by viewModel.hasHardwoodEdging.collectAsState()
    val edgingHeight by viewModel.edgingHeightInches.collectAsState()
    val edgingThickness by viewModel.edgingThicknessInches.collectAsState()
    val sagResult by viewModel.sagResult.collectAsState()
    val lastLogSaved by viewModel.lastLogSaved.collectAsState()

    var bookcaseNote by remember { mutableStateOf("Main Library Living Room Bookcase") }
    var speciesMenuExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val statusColor = Color(sagResult.rating.colorHex)

    val thicknessPresets = listOf(
        0.5 to "1/2\"",
        0.75 to "3/4\"",
        1.0 to "1.0\" (4/4)",
        1.25 to "1-1/4\" (5/4)",
        1.5 to "1-1/2\" (2x)"
    )

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
                colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.12f)),
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
                            Icon(Icons.Default.FormatAlignJustify, contentDescription = null, tint = statusColor)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "LUMBER SAGULATOR (SHELF DEFLECTION)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp),
                                color = statusColor
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Calculates structural beam deflection, MOE bending resistance, and maximum span limits for solid hardwoods, softwoods, and sheet goods.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Deflection Assessment Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (sagResult.rating == SagRating.IMPERCEPTIBLE) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = sagResult.rating.label,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = sagResult.rating.advisory,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Hero Deflection Digital Metric Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, statusColor, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TOTAL CENTER DEFLECTION",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = String.format("%.3f\"", sagResult.totalDeflectionInches),
                        color = statusColor,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Text(
                        text = "${String.format("%.3f\"", sagResult.deflectionPerFootInches)} per foot of span (Limit: 0.033\"/ft)",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("SHELF WEIGHT", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            Text(String.format("%.1f lbs", sagResult.shelfWeightLbs), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("TOTAL LOAD", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            Text(String.format("%.1f lbs", sagResult.totalLoadWithShelfLbs), color = Color(0xFF38BDF8), fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("INERTIA (I)", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            Text(String.format("%.3f in⁴", sagResult.momentOfInertiaIn4), color = Color(0xFF4ADE80), fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            // Interactive Deflection Beam Diagram Canvas
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Structural Beam Deflection Curve",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val padX = 40f
                            val beamY = h * 0.35f
                            val beamLen = w - (padX * 2)

                            // Supports on ends
                            val leftSupport = Offset(padX, beamY + 12f)
                            val rightSupport = Offset(w - padX, beamY + 12f)

                            // Draw support triangles
                            drawPath(
                                path = Path().apply {
                                    moveTo(leftSupport.x, beamY)
                                    lineTo(leftSupport.x - 14f, beamY + 28f)
                                    lineTo(leftSupport.x + 14f, beamY + 28f)
                                    close()
                                },
                                color = Color(0xFF64748B)
                            )

                            drawPath(
                                path = Path().apply {
                                    moveTo(rightSupport.x, beamY)
                                    lineTo(rightSupport.x - 14f, beamY + 28f)
                                    lineTo(rightSupport.x + 14f, beamY + 28f)
                                    close()
                                },
                                color = Color(0xFF64748B)
                            )

                            // Exaggerated visual sag curve for visual clarity
                            val maxVisualSagPx = (sagResult.totalDeflectionInches * 400.0).toFloat().coerceIn(2f, 60f)

                            val beamPath = Path()
                            val steps = 30
                            for (i in 0..steps) {
                                val t = i.toFloat() / steps
                                val x = padX + (t * beamLen)
                                // Parabolic deflection curve: y = 4 * sag * t * (1-t)
                                val sagAtPoint = 4f * maxVisualSagPx * t * (1f - t)
                                val y = beamY + sagAtPoint
                                if (i == 0) beamPath.moveTo(x, y) else beamPath.lineTo(x, y)
                            }

                            // Original straight baseline (dashed)
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.4f),
                                start = Offset(padX, beamY),
                                end = Offset(w - padX, beamY),
                                strokeWidth = 1.dp.toPx()
                            )

                            // Deflected shelf line
                            drawPath(beamPath, statusColor, style = Stroke(width = 4.dp.toPx()))

                            // Load arrow indicator
                            val arrowX = w / 2f
                            val arrowBottomY = beamY + maxVisualSagPx - 4f
                            drawLine(
                                color = Color(0xFF38BDF8),
                                start = Offset(arrowX, arrowBottomY - 30f),
                                end = Offset(arrowX, arrowBottomY),
                                strokeWidth = 2.5.dp.toPx()
                            )
                        }
                    }

                    Text(
                        text = "Span: ${String.format("%.0f\"", spanInches)} • Load: ${String.format("%.0f lbs", loadLbs)} • ${selectedSpecies.name}",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }

            // Material Selection
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Wood Species & Material", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                    Box {
                        OutlinedButton(
                            onClick = { speciesMenuExpanded = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${selectedSpecies.name} (${selectedSpecies.category})", fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.ExpandMore, contentDescription = null)
                            }
                        }

                        DropdownMenu(
                            expanded = speciesMenuExpanded,
                            onDismissRequest = { speciesMenuExpanded = false }
                        ) {
                            viewModel.speciesList.forEach { sp ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(sp.name, fontWeight = FontWeight.Bold)
                                            Text("MOE: ${String.format("%,.0f", sp.moePsi)} psi • ${sp.densityLbsCuFt} lbs/ft³", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    },
                                    onClick = {
                                        viewModel.setSpecies(sp)
                                        speciesMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Text(
                        text = "Stiffness (MOE): ${String.format("%,.0f", selectedSpecies.moePsi)} PSI • Density: ${selectedSpecies.densityLbsCuFt} lbs/cu ft",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Shelf Dimensions
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Dimensions & Thickness", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                    // Thickness chips
                    Text("Shelf Thickness: ${String.format("%.3f\"", thicknessInches)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        thicknessPresets.forEach { (t, label) ->
                            FilterChip(
                                selected = thicknessInches == t,
                                onClick = { viewModel.updateDimensions(thickness = t) },
                                label = { Text(label) }
                            )
                        }
                    }

                    // Span Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Shelf Span (Length):", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(String.format("%.1f\" (%d ft)", spanInches, (spanInches / 12).toInt()), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = spanInches.toFloat(),
                        onValueChange = { viewModel.updateDimensions(span = it.toDouble()) },
                        valueRange = 12f..72f,
                        steps = 59
                    )

                    // Depth Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Shelf Depth (Front-to-Back):", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(String.format("%.2f\"", depthInches), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = depthInches.toFloat(),
                        onValueChange = { viewModel.updateDimensions(depth = it.toDouble()) },
                        valueRange = 6f..30f,
                        steps = 47
                    )

                    // Applied Load Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Applied Load (Total):", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(String.format("%.0f lbs", loadLbs), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = loadLbs.toFloat(),
                        onValueChange = { viewModel.updateDimensions(load = it.toDouble()) },
                        valueRange = 0f..250f,
                        steps = 50
                    )
                }
            }

            // Joinery & Stiffener Options
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Joinery & Stiffener Reinforcement", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                    // Support End Type TabRow
                    TabRow(
                        selectedTabIndex = if (supportType == ShelfSupportType.SIMPLY_SUPPORTED) 0 else 1,
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = supportType == ShelfSupportType.SIMPLY_SUPPORTED,
                            onClick = { viewModel.updateDimensions(support = ShelfSupportType.SIMPLY_SUPPORTED) },
                            text = { Text("Shelf Pins (Floating)", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = supportType == ShelfSupportType.FIXED_DADO,
                            onClick = { viewModel.updateDimensions(support = ShelfSupportType.FIXED_DADO) },
                            text = { Text("Glued Dadoes (Fixed)", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                    }

                    // Load distribution
                    TabRow(
                        selectedTabIndex = if (loadDistribution == LoadDistribution.UNIFORMLY_DISTRIBUTED) 0 else 1,
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = loadDistribution == LoadDistribution.UNIFORMLY_DISTRIBUTED,
                            onClick = { viewModel.updateDimensions(dist = LoadDistribution.UNIFORMLY_DISTRIBUTED) },
                            text = { Text("Uniform (Books)", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = loadDistribution == LoadDistribution.CENTER_POINT,
                            onClick = { viewModel.updateDimensions(dist = LoadDistribution.CENTER_POINT) },
                            text = { Text("Center Point Load", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                    }

                    // Front hardwood apron/edging toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Front Hardwood Apron / Stiffener Lip", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Adds vertical edge face to drastically reduce sag", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = hasEdging,
                            onCheckedChange = { viewModel.updateDimensions(edging = it) }
                        )
                    }

                    if (hasEdging) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = edgingHeight.toString(),
                                onValueChange = { it.toDoubleOrNull()?.let { v -> viewModel.updateDimensions(edgeH = v) } },
                                label = { Text("Apron Height (in)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = edgingThickness.toString(),
                                onValueChange = { it.toDoubleOrNull()?.let { v -> viewModel.updateDimensions(edgeT = v) } },
                                label = { Text("Thickness (in)") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Save to Log
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Save Shelf Calculation", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    OutlinedTextField(
                        value = bookcaseNote,
                        onValueChange = { bookcaseNote = it },
                        label = { Text("Location / Cabinet Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            viewModel.saveSagLog(bookcaseNote)
                            Toast.makeText(context, "Shelf sag calculation saved!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(if (lastLogSaved) Icons.Default.CheckCircle else Icons.Default.BookmarkBorder, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (lastLogSaved) "Calculation Saved to Database" else "Save Sag Calculation to Database")
                    }
                }
            }
        }
    }
}
