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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SegmentedTurningScreen(
    viewModel: SegmentedTurningViewModel,
    modifier: Modifier = Modifier
) {
    val segmentCount by viewModel.segmentCount.collectAsState()
    val outerDiameter by viewModel.outerDiameter.collectAsState()
    val wallThickness by viewModel.wallThickness.collectAsState()
    val ringThickness by viewModel.ringThickness.collectAsState()
    val sawKerf by viewModel.sawKerf.collectAsState()
    val calculations by viewModel.calculations.collectAsState()
    val ringStack by viewModel.ringStack.collectAsState()
    val lastLogSaved by viewModel.lastLogSaved.collectAsState()

    var projectName by remember { mutableStateOf("Segmented Walnut & Maple Bowl") }
    var activeTab by remember { mutableStateOf(0) } // 0: Single Ring, 1: Vessel Stack Profile

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val presetSegments = listOf(6, 8, 10, 12, 16, 18, 20, 24, 32, 36, 48)

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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
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
                            Icon(Icons.Default.DonutLarge, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SEGMENTED TURNING CALCULATOR",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Precise miter angles, trapezoid edge dimensions, stave strip width, and board stock length for multi-sided wooden rings & turned vessels.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Primary Key Metric Hero Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Color(0xFFF59E0B), RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "MITER SAW BLADE ANGLE",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = String.format("%.2f°", calculations.miterAngleDeg),
                        color = Color(0xFFFBBF24),
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Text(
                        text = "Cut 2 ends per segment • Total ${segmentCount * 2} miter cuts for full 360° ring",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("OUTER EDGE (SEL)", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            Text(String.format("%.3f\"", calculations.segmentOuterEdgeInches), color = Color(0xFF38BDF8), fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("INNER EDGE", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            Text(String.format("%.3f\"", calculations.segmentInnerEdgeInches), color = Color(0xFF4ADE80), fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("STRIP WIDTH", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            Text(String.format("%.3f\"", calculations.boardStripWidthInches), color = Color(0xFFF472B6), fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            // Tab View for Visual Render (Single Ring vs Vessel Stack)
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Segment Ring Geometry", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Vessel Stack Profile (${ringStack.size} Rings)", fontWeight = FontWeight.Bold) }
                )
            }

            // Visual Canvas Display
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (activeTab == 0) {
                        Text(
                            text = "$segmentCount-Sided Segment Ring (${String.format("%.1f\"", outerDiameter)} OD)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val cX = size.width / 2f
                                val cY = size.height / 2f
                                val maxR = minOf(size.width, size.height) * 0.42f

                                val rOut = maxR
                                val id = (outerDiameter - (wallThickness * 2.0)).coerceAtLeast(0.1)
                                val rIn = (rOut * (id / outerDiameter)).toFloat().coerceAtLeast(10f)

                                val n = segmentCount
                                val stepAngle = (2.0 * PI) / n

                                val woodColors = listOf(
                                    Color(0xFFD97706), Color(0xFFB45309), Color(0xFF92400E), Color(0xFF78350F)
                                )

                                for (i in 0 until n) {
                                    val a1 = i * stepAngle - PI / 2
                                    val a2 = (i + 1) * stepAngle - PI / 2

                                    val x1Out = cX + (rOut * cos(a1)).toFloat()
                                    val y1Out = cY + (rOut * sin(a1)).toFloat()
                                    val x2Out = cX + (rOut * cos(a2)).toFloat()
                                    val y2Out = cY + (rOut * sin(a2)).toFloat()

                                    val x2In = cX + (rIn * cos(a2)).toFloat()
                                    val y2In = cY + (rIn * sin(a2)).toFloat()
                                    val x1In = cX + (rIn * cos(a1)).toFloat()
                                    val y1In = cY + (rIn * sin(a1)).toFloat()

                                    val segPath = Path().apply {
                                        moveTo(x1Out, y1Out)
                                        lineTo(x2Out, y2Out)
                                        lineTo(x2In, y2In)
                                        lineTo(x1In, y1In)
                                        close()
                                    }

                                    val color = woodColors[i % woodColors.size]
                                    drawPath(segPath, color.copy(alpha = 0.85f))
                                    drawPath(segPath, Color.White, style = Stroke(width = 1.5.dp.toPx()))
                                }

                                // Center axis mark
                                drawLine(Color.Gray.copy(alpha = 0.5f), Offset(cX - 15f, cY), Offset(cX + 15f, cY), strokeWidth = 1.dp.toPx())
                                drawLine(Color.Gray.copy(alpha = 0.5f), Offset(cX, cY - 15f), Offset(cX, cY + 15f), strokeWidth = 1.dp.toPx())
                            }
                        }
                    } else {
                        Text(
                            text = "Turned Vessel Cross-Section Stack",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val cX = size.width / 2f
                                val totalRings = ringStack.size
                                if (totalRings == 0) return@Canvas

                                val maxOd = ringStack.maxOf { it.outerDiameterInches }
                                val maxW = size.width * 0.75f
                                val scaleX = (maxW / maxOd).toFloat()

                                val ringH = 220.dp.toPx() / (totalRings + 1)

                                ringStack.forEachIndexed { i, ring ->
                                    val y = (totalRings - 1 - i) * ringH + ringH * 0.5f
                                    val wOut = (ring.outerDiameterInches * scaleX).toFloat()
                                    val wIn = (ring.innerDiameterInches * scaleX).toFloat()

                                    // Left wall segment
                                    val leftWallLeft = cX - wOut / 2f
                                    val leftWallW = (wOut - wIn) / 2f
                                    drawRect(
                                        color = if (i % 2 == 0) Color(0xFFD97706) else Color(0xFFB45309),
                                        topLeft = Offset(leftWallLeft, y),
                                        size = Size(leftWallW, ringH * 0.9f)
                                    )
                                    drawRect(
                                        color = Color.White.copy(alpha = 0.7f),
                                        topLeft = Offset(leftWallLeft, y),
                                        size = Size(leftWallW, ringH * 0.9f),
                                        style = Stroke(width = 1.dp.toPx())
                                    )

                                    // Right wall segment
                                    val rightWallLeft = cX + wIn / 2f
                                    val rightWallW = (wOut - wIn) / 2f
                                    drawRect(
                                        color = if (i % 2 == 0) Color(0xFFD97706) else Color(0xFFB45309),
                                        topLeft = Offset(rightWallLeft, y),
                                        size = Size(rightWallW, ringH * 0.9f)
                                    )
                                    drawRect(
                                        color = Color.White.copy(alpha = 0.7f),
                                        topLeft = Offset(rightWallLeft, y),
                                        size = Size(rightWallW, ringH * 0.9f),
                                        style = Stroke(width = 1.dp.toPx())
                                    )
                                }

                                // Center Lathe turning axis
                                drawLine(
                                    color = Color(0xFF38BDF8),
                                    start = Offset(cX, 10f),
                                    end = Offset(cX, size.height - 10f),
                                    strokeWidth = 1.5.dp.toPx()
                                )
                            }
                        }
                    }
                }
            }

            // Input Parameters Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Ring Parameters", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                    // Preset Segment Chips
                    Text("Segments per Ring (N): $segmentCount", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetSegments.forEach { seg ->
                            FilterChip(
                                selected = segmentCount == seg,
                                onClick = { viewModel.updateInputs(segments = seg) },
                                label = { Text("$seg seg") }
                            )
                        }
                    }

                    // Outer Diameter Slider & Field
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Outer Diameter (OD):", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(String.format("%.2f\"", outerDiameter), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = outerDiameter.toFloat(),
                        onValueChange = { viewModel.updateInputs(od = it.toDouble()) },
                        valueRange = 2f..24f,
                        steps = 87
                    )

                    // Wall Thickness Slider & Field
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Wall Thickness (Radial Depth):", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(String.format("%.2f\"", wallThickness), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = wallThickness.toFloat(),
                        onValueChange = { viewModel.updateInputs(wall = it.toDouble()) },
                        valueRange = 0.25f..3.0f,
                        steps = 21
                    )

                    // Ring Thickness & Saw Kerf
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = ringThickness.toString(),
                            onValueChange = { it.toDoubleOrNull()?.let { v -> viewModel.updateInputs(thickness = v) } },
                            label = { Text("Ring Height (in)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = sawKerf.toString(),
                            onValueChange = { it.toDoubleOrNull()?.let { v -> viewModel.updateInputs(kerf = v) } },
                            label = { Text("Saw Kerf (in)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Material Stock Bill of Materials Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Stock Material & Cut List", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Required Board Width:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(String.format("%.3f\" (min)", calculations.boardStripWidthInches), fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Required Board Length:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(String.format("%.1f\" (~%.2f ft)", calculations.totalBoardLengthInches, calculations.totalBoardLengthInches / 12.0), fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Finished Ring Area:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(String.format("%.2f sq in", calculations.totalRingAreaSqIn), fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Miter Cut Facets:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${segmentCount * 2} facets", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = { viewModel.addRingToStack() },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add This Ring to Vessel Stack")
                    }
                }
            }

            // Log & Save Project
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Save Turning Calculation", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    OutlinedTextField(
                        value = projectName,
                        onValueChange = { projectName = it },
                        label = { Text("Project / Bowl Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            viewModel.saveCalculationLog(projectName)
                            Toast.makeText(context, "Segmented turning specs logged!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(if (lastLogSaved) Icons.Default.CheckCircle else Icons.Default.BookmarkBorder, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (lastLogSaved) "Specification Logged to Database" else "Save Specification to Database")
                    }
                }
            }
        }
    }
}
