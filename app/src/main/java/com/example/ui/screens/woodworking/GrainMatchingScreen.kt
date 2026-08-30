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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Texture
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GrainMatchingScreen(
    viewModel: GrainMatchingViewModel,
    modifier: Modifier = Modifier
) {
    val boards by viewModel.boards.collectAsState()
    val matchMode by viewModel.matchMode.collectAsState()
    val selectedBoardId by viewModel.selectedBoardId.collectAsState()
    val lastLogSaved by viewModel.lastLogSaved.collectAsState()

    var projectTitle by remember { mutableStateOf("Walnut Dining Table Top") }
    val context = LocalContext.current

    val selectedBoard = boards.find { it.id == selectedBoardId } ?: boards.firstOrNull()
    val totalWidth = viewModel.getTotalPanelWidth()
    val panelLength = viewModel.getPanelLength()
    val totalAreaSqFt = viewModel.getPanelAreaSqFt()
    val boardFootage = viewModel.getBoardFootage(1.0)

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
            com.example.ui.components.ToolInfoBox(
                icon = Icons.Default.Texture,
                title = "Grain Matching & Board Layout",
                description = "Arrange and preview timber flitches side-by-side: Bookmatching, slip matching, flame symmetry, and alternating annual rings to prevent cupping."
            )

            // Panel Dimension Overview
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
                        text = "ASSEMBLED PANEL SPECIFICATIONS",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${String.format("%.1f\"", totalWidth)} × ${String.format("%.1f\"", panelLength)}",
                        color = Color(0xFFFBBF24),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Text(
                        text = "${boards.size} Planks Glued • ${String.format("%.2f sq ft", totalAreaSqFt)} • ${String.format("%.2f BF", boardFootage)} (4/4 Stock)",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ANNUAL RINGS", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            Text(boards.map { it.ringOrientation.symbol }.joinToString(" "), color = Color(0xFF38BDF8), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("GLUE JOINTS", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            Text("${boards.size - 1} seams", color = Color(0xFF4ADE80), fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("MATCH STYLE", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            Text(matchMode.name.take(10), color = Color(0xFFF472B6), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Real-time Grain Match Visual Canvas
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Tabletop Glue-Up Visualizer",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val totalPlankWidthInches = totalWidth
                            if (totalPlankWidthInches <= 0) return@Canvas

                            var currentX = 0f
                            val pxPerInch = w / totalPlankWidthInches.toFloat()

                            boards.forEachIndexed { i, board ->
                                val boardW = (board.widthInches * pxPerInch).toFloat()
                                val isSelected = board.id == selectedBoardId

                                // Board Background base tone
                                val baseColor = Color(board.woodToneHex)
                                drawRect(
                                    color = baseColor,
                                    topLeft = Offset(currentX, 0f),
                                    size = Size(boardW, h)
                                )

                                // Procedural Wood Grain Drawing
                                val grainColor = Color(0xFF451A03).copy(alpha = 0.45f)
                                val centerX = currentX + (boardW / 2f)
                                val flipMult = if (board.isFlippedHorizontal) -1f else 1f

                                when (board.grainPattern) {
                                    GrainPattern.CATHEDRAL_FLAME -> {
                                        // Draw multiple nested parabolic cathedral arches
                                        for (c in 1..4) {
                                            val archPath = Path()
                                            val archH = h * (0.3f + c * 0.16f)
                                            val archW = boardW * (0.25f + c * 0.2f)

                                            val apexY = if (board.isRotated180) h - (c * 25f) else (c * 25f)
                                            val baseY = if (board.isRotated180) 0f else h

                                            val leftX = (centerX - (archW / 2f) * flipMult).coerceIn(currentX, currentX + boardW)
                                            val rightX = (centerX + (archW / 2f) * flipMult).coerceIn(currentX, currentX + boardW)

                                            archPath.moveTo(leftX, baseY)
                                            archPath.quadraticBezierTo(centerX, apexY, rightX, baseY)
                                            drawPath(archPath, grainColor, style = Stroke(width = 2.dp.toPx()))
                                        }
                                    }
                                    GrainPattern.QUARTERSAWN_LINEAR -> {
                                        // Vertical straight lines
                                        val lineCount = 8
                                        val step = boardW / (lineCount + 1)
                                        for (l in 1..lineCount) {
                                            val lx = currentX + l * step
                                            drawLine(grainColor, Offset(lx, 0f), Offset(lx, h), strokeWidth = 1.2.dp.toPx())
                                        }
                                    }
                                    GrainPattern.CURLY_FIGURE -> {
                                        // Wavy horizontal ripples
                                        val waveCount = 12
                                        val stepY = h / waveCount
                                        for (wv in 0..waveCount) {
                                            val wy = wv * stepY
                                            val wavePath = Path()
                                            for (pt in 0..20) {
                                                val px = currentX + (pt / 20f) * boardW
                                                val py = wy + (sin(pt * 0.8f) * 6f)
                                                if (pt == 0) wavePath.moveTo(px, py) else wavePath.lineTo(px, py)
                                            }
                                            drawPath(wavePath, grainColor.copy(alpha = 0.35f), style = Stroke(width = 2.dp.toPx()))
                                        }
                                    }
                                    else -> {
                                        // Standard straight grain
                                        for (l in 1..5) {
                                            val lx = currentX + l * (boardW / 6f)
                                            drawLine(grainColor, Offset(lx, 0f), Offset(lx, h), strokeWidth = 1.5.dp.toPx())
                                        }
                                    }
                                }

                                // End-grain annual ring indicator at bottom
                                val ringSymbolY = h - 18f
                                val ringPath = Path()
                                val rw = minOf(boardW * 0.6f, 30f)
                                val rx = currentX + (boardW / 2f)
                                if (board.ringOrientation == RingOrientation.BARK_UP) {
                                    // ⌒ Crown up
                                    ringPath.moveTo(rx - rw / 2f, ringSymbolY)
                                    ringPath.quadraticBezierTo(rx, ringSymbolY - 12f, rx + rw / 2f, ringSymbolY)
                                } else {
                                    // ⌣ Crown down
                                    ringPath.moveTo(rx - rw / 2f, ringSymbolY - 12f)
                                    ringPath.quadraticBezierTo(rx, ringSymbolY, rx + rw / 2f, ringSymbolY - 12f)
                                }
                                drawPath(ringPath, Color(0xFFFBBF24), style = Stroke(width = 2.5.dp.toPx()))

                                // Selection border
                                if (isSelected) {
                                    drawRect(
                                        color = Color(0xFF38BDF8),
                                        topLeft = Offset(currentX, 0f),
                                        size = Size(boardW, h),
                                        style = Stroke(width = 3.dp.toPx())
                                    )
                                } else {
                                    // Glue joint seam line
                                    drawRect(
                                        color = Color(0xFF0F172A),
                                        topLeft = Offset(currentX, 0f),
                                        size = Size(boardW, h),
                                        style = Stroke(width = 1.dp.toPx())
                                    )
                                }

                                currentX += boardW
                            }
                        }
                    }
                }
            }

            // Matching Strategies Presets
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Apply Matching Presets", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MatchMode.values().forEach { mode ->
                            FilterChip(
                                selected = matchMode == mode,
                                onClick = { viewModel.applyPresetMatchMode(mode) },
                                label = { Text(mode.label) }
                            )
                        }
                    }
                }
            }

            // Selected Plank Controls
            if (selectedBoard != null) {
                val selectedIndex = boards.indexOfFirst { it.id == selectedBoard.id }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Selected: ${selectedBoard.label} (${String.format("%.1f\"", selectedBoard.widthInches)} W)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Row {
                                IconButton(
                                    onClick = { viewModel.moveBoardLeft(selectedIndex) },
                                    enabled = selectedIndex > 0
                                ) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Move Left")
                                }
                                IconButton(
                                    onClick = { viewModel.moveBoardRight(selectedIndex) },
                                    enabled = selectedIndex < boards.size - 1
                                ) {
                                    Icon(Icons.Default.ArrowForward, contentDescription = "Move Right")
                                }
                                IconButton(
                                    onClick = { viewModel.removePlank(selectedBoard.id) },
                                    enabled = boards.size > 1
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Plank", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        // Width Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Plank Width:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(String.format("%.2f\"", selectedBoard.widthInches), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = selectedBoard.widthInches.toFloat(),
                            onValueChange = { viewModel.updateSelectedBoard(it.toDouble(), selectedBoard.grainPattern) },
                            valueRange = 3f..14f,
                            steps = 22
                        )

                        // Action Buttons: Flip, Rotate, Annual Ring
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.flipHorizontal(selectedBoard.id) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Flip, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (selectedBoard.isFlippedHorizontal) "Mirrored" else "Mirror", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = { viewModel.rotate180(selectedBoard.id) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.RotateRight, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (selectedBoard.isRotated180) "180°" else "0°", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = { viewModel.toggleRingOrientation(selectedBoard.id) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Ring: ${selectedBoard.ringOrientation.symbol}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        // Grain Pattern selector
                        Text("Grain Pattern:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            GrainPattern.values().forEach { pat ->
                                FilterChip(
                                    selected = selectedBoard.grainPattern == pat,
                                    onClick = { viewModel.updateSelectedBoard(selectedBoard.widthInches, pat) },
                                    label = { Text(pat.label.take(18), fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            }

            // Add plank button
            OutlinedButton(
                onClick = { viewModel.addPlank() },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Another Timber Plank to Panel")
            }

            // Save to Log
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Save Grain Layout Plan", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    OutlinedTextField(
                        value = projectTitle,
                        onValueChange = { projectTitle = it },
                        label = { Text("Panel / Project Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            viewModel.saveGrainLayoutLog(projectTitle)
                            Toast.makeText(context, "Grain layout plan saved!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(if (lastLogSaved) Icons.Default.CheckCircle else Icons.Default.BookmarkBorder, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (lastLogSaved) "Grain Layout Saved to Database" else "Save Grain Layout to Database")
                    }
                }
            }
        }
    }
}
