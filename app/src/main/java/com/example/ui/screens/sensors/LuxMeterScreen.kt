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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
fun LuxMeterScreen(
    viewModel: LuxMeterViewModel,
    modifier: Modifier = Modifier
) {
    val currentLux by viewModel.currentLux.collectAsState()
    val isFootCandles by viewModel.isFootCandles.collectAsState()
    val minLux by viewModel.minLux.collectAsState()
    val maxLux by viewModel.maxLux.collectAsState()
    val avgLux by viewModel.avgLux.collectAsState()
    val history by viewModel.history.collectAsState()
    val selectedStandard by viewModel.selectedStandard.collectAsState()
    val compliance by viewModel.compliance.collectAsState()
    val lastLogSaved by viewModel.lastLogSaved.collectAsState()

    var workspaceNote by remember { mutableStateOf("Main Assembly Workbench #2") }

    val statusColor = Color(compliance.colorHex)

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
                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = statusColor)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SURFACE LUX & FOOT-CANDLE METER",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp),
                                color = statusColor
                            )
                        }
                        OutlinedButton(
                            onClick = { viewModel.toggleUnit() },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (isFootCandles) "Unit: Foot-Candles" else "Unit: Lux (lx)", fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Hardware ambient light sensor photometry & IESNA/OSHA workplace lighting code compliance auditor.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Compliance Status Banner
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
                        if (compliance == ComplianceResult.COMPLIANT) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = compliance.status,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = compliance.advisory,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Big Digital Photometer Display
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
                        text = "MEASURED TASK ILLUMINATION",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = viewModel.formatLux(currentLux),
                        color = if (compliance == ComplianceResult.COMPLIANT) Color(0xFF34D399) else Color(0xFFFBBF24),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Text(
                        text = "Standard Target: ${viewModel.formatLux(selectedStandard.targetLux)} (Range: ${viewModel.formatLux(selectedStandard.minLux)} - ${viewModel.formatLux(selectedStandard.maxLux)})",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("MIN", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            Text(viewModel.formatLux(minLux), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("AVG", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            Text(viewModel.formatLux(avgLux), color = Color(0xFF67E8F9), fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("MAX", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            Text(viewModel.formatLux(maxLux), color = Color(0xFFF87171), fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            // Live Photometry Trend Canvas
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Illumination Stability Trend", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            drawLine(Color.DarkGray, Offset(0f, h * 0.5f), Offset(w, h * 0.5f), strokeWidth = 0.5.dp.toPx())

                            if (history.size > 1) {
                                val minH = (history.minOrNull() ?: 0f) * 0.9f
                                val maxH = (history.maxOrNull() ?: 1000f) * 1.1f
                                val range = (maxH - minH).coerceAtLeast(10f)

                                val stepX = w / (history.size - 1)
                                val path = Path()

                                history.forEachIndexed { i, v ->
                                    val x = i * stepX
                                    val norm = (v - minH) / range
                                    val y = h - (norm * h)
                                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                }

                                drawPath(path, Color(0xFFFBBF24), style = Stroke(width = 2.5.dp.toPx()))
                            }
                        }
                    }
                }
            }

            // IESNA / OSHA Task Standard Presets
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select Task Lighting Code Standard", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        viewModel.standardsList.forEach { std ->
                            FilterChip(
                                selected = selectedStandard.id == std.id,
                                onClick = { viewModel.setStandard(std) },
                                label = { Text("${std.taskName} (${viewModel.formatLux(std.targetLux)})") }
                            )
                        }
                    }
                    Text(
                        text = "Standard: ${selectedStandard.standardRef} • Minimum required: ${viewModel.formatLux(selectedStandard.minLux)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Save Lighting Audit Log
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Save Photometry Audit Report", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    OutlinedTextField(
                        value = workspaceNote,
                        onValueChange = { workspaceNote = it },
                        label = { Text("Bench / Station / Room Location") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { viewModel.saveLuxLog(workspaceNote) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(if (lastLogSaved) Icons.Default.CheckCircle else Icons.Default.BookmarkBorder, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (lastLogSaved) "Photometry Report Saved" else "Save Lighting Audit to Database")
                    }
                }
            }
        }
    }
}
