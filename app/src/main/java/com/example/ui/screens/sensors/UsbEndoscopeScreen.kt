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
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.ZoomIn
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.woodworking.ResultBadge

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UsbEndoscopeScreen(
    viewModel: UsbEndoscopeViewModel,
    modifier: Modifier = Modifier
) {
    val zoomLevel by viewModel.zoomLevel.collectAsState()
    val brightness by viewModel.brightness.collectAsState()
    val contrast by viewModel.contrast.collectAsState()
    val rotationDegrees by viewModel.rotationDegrees.collectAsState()
    val isMirrored by viewModel.isMirrored.collectAsState()
    val isGridVisible by viewModel.isGridVisible.collectAsState()
    val isTorchActive by viewModel.isTorchActive.collectAsState()
    val selectedDefect by viewModel.selectedDefect.collectAsState()
    val snapshotCount by viewModel.snapshotCount.collectAsState()
    val lastLogSaved by viewModel.lastLogSaved.collectAsState()

    var locationInput by remember { mutableStateOf("Wall Cavity Stud Bay 3") }
    var findingsNote by remember { mutableStateOf("Hairline crack near elbow fitting") }

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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
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
                            Icon(Icons.Default.Videocam, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "USB BORESCOPE & ENDOSCOPE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF16A34A).copy(alpha = 0.2f), CircleShape)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("PROBE READY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Live inspection feed for plumbing pipes, wall cavities, HVAC ductwork, and engine cylinders with defect tagging.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Viewport Screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .background(Color(0xFF0F172A), RoundedCornerShape(20.dp))
                    .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
            ) {
                // Viewfinder Canvas simulation / grid
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Simulated pipe / wall interior background
                    drawRect(Color(0xFF1E293B))
                    drawCircle(Color(0xFF0F172A), radius = w * 0.45f, center = Offset(w / 2f, h / 2f))

                    // Inspection Calibration Grid
                    if (isGridVisible) {
                        val gridCols = 8
                        val gridRows = 6
                        val colStep = w / gridCols
                        val rowStep = h / gridRows

                        for (i in 1 until gridCols) {
                            drawLine(
                                color = Color.Cyan.copy(alpha = 0.35f),
                                start = Offset(i * colStep, 0f),
                                end = Offset(i * colStep, h),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                        for (j in 1 until gridRows) {
                            drawLine(
                                color = Color.Cyan.copy(alpha = 0.35f),
                                start = Offset(0f, j * rowStep),
                                end = Offset(w, j * rowStep),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        // Center reticle
                        val cx = w / 2f
                        val cy = h / 2f
                        drawCircle(Color.Cyan.copy(alpha = 0.6f), radius = 20.dp.toPx(), center = Offset(cx, cy), style = Stroke(1.5.dp.toPx()))
                        drawLine(Color.Cyan.copy(alpha = 0.7f), Offset(cx - 30.dp.toPx(), cy), Offset(cx + 30.dp.toPx(), cy), strokeWidth = 1.5.dp.toPx())
                        drawLine(Color.Cyan.copy(alpha = 0.7f), Offset(cx, cy - 30.dp.toPx()), Offset(cx, cy + 30.dp.toPx()), strokeWidth = 1.5.dp.toPx())
                    }
                }

                // Top Viewport HUD
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
                            text = "ZOOM: ${String.format("%.1fx", zoomLevel)} | ${rotationDegrees}°",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "SNAPS: $snapshotCount",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Bottom Controls Bar over Viewport
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.rotate90() }) {
                        Icon(Icons.Default.RotateRight, contentDescription = "Rotate", tint = Color.White)
                    }
                    IconButton(onClick = { viewModel.toggleMirror() }) {
                        Icon(Icons.Default.Flip, contentDescription = "Mirror", tint = if (isMirrored) Color.Cyan else Color.White)
                    }
                    IconButton(onClick = { viewModel.toggleGrid() }) {
                        Icon(Icons.Default.GridOn, contentDescription = "Grid", tint = if (isGridVisible) Color.Cyan else Color.Gray)
                    }
                    IconButton(onClick = { viewModel.toggleTorch() }) {
                        Icon(Icons.Default.FlashOn, contentDescription = "LED Torch", tint = if (isTorchActive) Color(0xFFFDE047) else Color.Gray)
                    }
                }
            }

            // Quick Snapshot Action Button
            Button(
                onClick = { viewModel.takeSnapshot() },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("take_borescope_snapshot")
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("CAPTURE INSPECTION SNAPSHOT ($snapshotCount captured)", fontWeight = FontWeight.Bold)
            }

            // Zoom & Optical Adjustment Card
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ZoomIn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Digital Zoom: ${String.format("%.1fx", zoomLevel)}", fontWeight = FontWeight.Bold)
                        }
                    }

                    Slider(
                        value = zoomLevel,
                        onValueChange = { viewModel.setZoom(it) },
                        valueRange = 1.0f..5.0f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Brightness Exposure: ${String.format("%.1fx", brightness)}", fontSize = 12.sp)
                        Text("Contrast Boost: ${String.format("%.1fx", contrast)}", fontSize = 12.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Slider(
                            value = brightness,
                            onValueChange = { viewModel.setBrightness(it) },
                            valueRange = 0.5f..2.0f,
                            modifier = Modifier.weight(1f)
                        )
                        Slider(
                            value = contrast,
                            onValueChange = { viewModel.setContrast(it) },
                            valueRange = 0.5f..2.0f,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Defect Tagging & Classification
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Classify Observed Defect", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DefectCategory.values().forEach { defect ->
                            FilterChip(
                                selected = selectedDefect == defect,
                                onClick = { viewModel.setDefect(defect) },
                                label = { Text("${defect.label} [${defect.severity}]") }
                            )
                        }
                    }
                }
            }

            // Save Inspection Log Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Save Borescope Inspection Report", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    OutlinedTextField(
                        value = locationInput,
                        onValueChange = { locationInput = it },
                        label = { Text("Location / Cavity / Pipe ID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = findingsNote,
                        onValueChange = { findingsNote = it },
                        label = { Text("Findings & Defect Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { viewModel.saveInspectionLog(locationInput, findingsNote) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(if (lastLogSaved) Icons.Default.CheckCircle else Icons.Default.BookmarkBorder, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (lastLogSaved) "Inspection Report Saved" else "Save Report to Local Database")
                    }
                }
            }
        }
    }
}
