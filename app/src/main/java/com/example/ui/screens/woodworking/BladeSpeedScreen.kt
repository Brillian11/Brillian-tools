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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BladeSpeedScreen(
    viewModel: BladeSpeedViewModel,
    modifier: Modifier = Modifier
) {
    val machineType by viewModel.machineType.collectAsState()
    val targetMaterial by viewModel.targetMaterial.collectAsState()
    val motorRpm by viewModel.motorRpm.collectAsState()
    val motorPulley by viewModel.motorPulleyDiameter.collectAsState()
    val arborPulley by viewModel.arborPulleyDiameter.collectAsState()
    val bladeDiameter by viewModel.bladeWheelDiameter.collectAsState()
    val toothCount by viewModel.bladeToothCount.collectAsState()
    val calculation by viewModel.calculation.collectAsState()
    val lastLogSaved by viewModel.lastLogSaved.collectAsState()

    var machineName by remember { mutableStateOf("14\" Workshop Band Saw") }
    val context = LocalContext.current
    val statusColor = Color(calculation.safetyStatus.colorHex)

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
                            Icon(Icons.Default.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "BLADE SURFACE SPEED (SFPM) CALCULATOR",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Computes blade surface feet per minute (SFPM), arbor RPM, and pulley drive reduction ratios with material speed safety recommendations.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Hero Metric Digital Readout
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
                        text = "BLADE SURFACE SPEED (SFPM)",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = String.format("%,.0f", calculation.surfaceFeetPerMinute),
                        color = statusColor,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Text(
                        text = "FT / MIN (${String.format("%.1f m/s", calculation.metersPerSecond)} • ${String.format("%.0f m/min", calculation.metersPerMinute)})",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ARBOR SPEED", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            Text(String.format("%.0f RPM", calculation.arborRpm), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("PULLEY RATIO", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            Text(String.format("%.2f : 1", calculation.pulleyRatio), color = Color(0xFF38BDF8), fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("TOOTH IMPACT", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            Text(String.format("%,.0f Hz", calculation.toothImpactRateHz), color = Color(0xFF4ADE80), fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            // Material Safety & Advisory Status
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
                        if (calculation.safetyStatus == SpeedSafetyStatus.OPTIMAL) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = calculation.safetyStatus.label,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = calculation.safetyStatus.advisory,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Target for ${targetMaterial.label}: ${String.format("%,.0f - %,.0f SFPM", targetMaterial.minSfpm, targetMaterial.maxSfpm)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Interactive Pulley & Blade Drive Canvas
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
                        text = "V-Belt Pulley & Saw Arbor Schematic",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val cY = h / 2f

                            val motorX = w * 0.22f
                            val arborX = w * 0.52f
                            val bladeX = w * 0.82f

                            val rMotor = (motorPulley * 7.0).toFloat().coerceIn(16f, 40f)
                            val rArbor = (arborPulley * 7.0).toFloat().coerceIn(16f, 50f)
                            val rBlade = (bladeDiameter * 3.5).toFloat().coerceIn(24f, 65f)

                            // Belt between motor pulley and arbor pulley
                            val beltPath = Path().apply {
                                moveTo(motorX, cY - rMotor)
                                lineTo(arborX, cY - rArbor)
                                lineTo(arborX, cY + rArbor)
                                lineTo(motorX, cY + rMotor)
                                close()
                            }
                            drawPath(beltPath, Color(0xFF475569), style = Stroke(width = 3.dp.toPx()))

                            // Motor Pulley
                            drawCircle(Color(0xFF334155), rMotor, Offset(motorX, cY))
                            drawCircle(Color(0xFF38BDF8), rMotor, Offset(motorX, cY), style = Stroke(width = 2.dp.toPx()))
                            drawCircle(Color(0xFF0F172A), 6f, Offset(motorX, cY))

                            // Arbor Pulley
                            drawCircle(Color(0xFF334155), rArbor, Offset(arborX, cY))
                            drawCircle(Color(0xFF38BDF8), rArbor, Offset(arborX, cY), style = Stroke(width = 2.dp.toPx()))
                            drawCircle(Color(0xFF0F172A), 6f, Offset(arborX, cY))

                            // Connecting Arbor Shaft
                            drawLine(Color(0xFF94A3B8), Offset(arborX, cY), Offset(bladeX, cY), strokeWidth = 4.dp.toPx())

                            // Blade / Band Saw Wheel
                            drawCircle(Color(0xFF1E293B), rBlade, Offset(bladeX, cY))
                            drawCircle(statusColor, rBlade, Offset(bladeX, cY), style = Stroke(width = 2.5.dp.toPx()))

                            // Blade teeth indications
                            val teethNum = 12
                            for (t in 0 until teethNum) {
                                val ang = (t * (2.0 * Math.PI) / teethNum).toFloat()
                                val tx1 = bladeX + (rBlade * cos(ang))
                                val ty1 = cY + (rBlade * sin(ang))
                                val tx2 = bladeX + ((rBlade + 6f) * cos(ang + 0.1f))
                                val ty2 = cY + ((rBlade + 6f) * sin(ang + 0.1f))
                                drawLine(statusColor, Offset(tx1, ty1), Offset(tx2, ty2), strokeWidth = 2.dp.toPx())
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Text("Motor: ${String.format("%.1f\"", motorPulley)} (${String.format("%.0f", motorRpm)} RPM)", color = Color(0xFF94A3B8), fontSize = 10.sp)
                        Text("Arbor: ${String.format("%.1f\"", arborPulley)} (${String.format("%.0f", calculation.arborRpm)} RPM)", color = Color(0xFF94A3B8), fontSize = 10.sp)
                        Text("Blade: ${String.format("%.1f\"", bladeDiameter)}", color = Color(0xFF94A3B8), fontSize = 10.sp)
                    }
                }
            }

            // Saw Machine Presets
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Machine Type Presets", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SawMachineType.values().forEach { mach ->
                            FilterChip(
                                selected = machineType == mach,
                                onClick = { viewModel.applyMachineType(mach) },
                                label = { Text(mach.label) }
                            )
                        }
                    }
                }
            }

            // Target Material Selector
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Target Material Being Cut", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TargetMaterial.values().forEach { mat ->
                            FilterChip(
                                selected = targetMaterial == mat,
                                onClick = { viewModel.setTargetMaterial(mat) },
                                label = { Text(mat.label) }
                            )
                        }
                    }
                }
            }

            // Pulley & Speed Inputs
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Motor & Pulley Dimensions", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                    // Motor RPM
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Motor RPM:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(String.format("%.0f RPM", motorRpm), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = motorRpm.toFloat(),
                        onValueChange = { viewModel.updateInputs(motorRpm = it.toDouble()) },
                        valueRange = 800f..4000f,
                        steps = 31
                    )

                    // Motor Pulley
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Drive Pulley (Motor):", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(String.format("%.2f\"", motorPulley), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = motorPulley.toFloat(),
                        onValueChange = { viewModel.updateInputs(motorPulley = it.toDouble()) },
                        valueRange = 1f..12f,
                        steps = 22
                    )

                    // Arbor Pulley
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Driven Pulley (Arbor / Wheel):", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(String.format("%.2f\"", arborPulley), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = arborPulley.toFloat(),
                        onValueChange = { viewModel.updateInputs(arborPulley = it.toDouble()) },
                        valueRange = 1f..16f,
                        steps = 30
                    )

                    // Blade / Wheel Diameter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Blade / Band Wheel Diameter:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(String.format("%.1f\"", bladeDiameter), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = bladeDiameter.toFloat(),
                        onValueChange = { viewModel.updateInputs(bladeDiameter = it.toDouble()) },
                        valueRange = 4f..24f,
                        steps = 20
                    )
                }
            }

            // Save to Log
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Save Blade Speed Calculation", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    OutlinedTextField(
                        value = machineName,
                        onValueChange = { machineName = it },
                        label = { Text("Saw Name / Equipment") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            viewModel.saveSpeedLog(machineName)
                            Toast.makeText(context, "Blade speed calculation saved!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(if (lastLogSaved) Icons.Default.CheckCircle else Icons.Default.BookmarkBorder, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (lastLogSaved) "Calculation Saved to Database" else "Save Speed to Database")
                    }
                }
            }
        }
    }
}
