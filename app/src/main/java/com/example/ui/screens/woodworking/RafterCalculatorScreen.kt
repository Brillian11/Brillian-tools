package com.example.ui.screens.woodworking

import android.widget.Toast
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Roofing
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

@Composable
fun RafterCalculatorScreen(
    viewModel: RafterCalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var spanInput by remember { mutableStateOf(state.buildingSpanFeet.toString()) }
    var ridgeInput by remember { mutableStateOf(state.ridgeThicknessInches.toString()) }
    var overhangInput by remember { mutableStateOf(state.overhangInches.toString()) }

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
            // Header
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Roofing,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Rafter & Roof Pitch Calculator",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Common, hip, valley & jack rafters, pitch angles, birdsmouth seat cuts & HAP",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Pitch Selector & Slider
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ROOF PITCH (/12)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        )
                        Text(
                            text = "${state.pitchOver12.toInt()}/12 (${String.format("%.1f", state.pitchAngleDeg)}° slope)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Slider(
                        value = state.pitchOver12.toFloat(),
                        onValueChange = {
                            viewModel.updateInputs(
                                spanInput.toDoubleOrNull() ?: 24.0,
                                it.toDouble(),
                                ridgeInput.toDoubleOrNull() ?: 1.5,
                                overhangInput.toDoubleOrNull() ?: 16.0,
                                state.rafterLumberNominal,
                                state.birdsmouthSeatCutInches
                            )
                        },
                        valueRange = 2f..16f,
                        steps = 13,
                        modifier = Modifier.testTag("pitch_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(4.0, 6.0, 8.0, 10.0, 12.0).forEach { p ->
                            FilterChip(
                                selected = state.pitchOver12 == p,
                                onClick = {
                                    viewModel.updateInputs(
                                        spanInput.toDoubleOrNull() ?: 24.0,
                                        p,
                                        ridgeInput.toDoubleOrNull() ?: 1.5,
                                        overhangInput.toDoubleOrNull() ?: 16.0,
                                        state.rafterLumberNominal,
                                        state.birdsmouthSeatCutInches
                                    )
                                },
                                label = { Text("${p.toInt()}/12") }
                            )
                        }
                    }
                }
            }

            // Framing Dimensions Input
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
                        text = "BUILDING SPAN & RAFTER LUMBER",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = spanInput,
                            onValueChange = {
                                spanInput = it
                                it.toDoubleOrNull()?.let { sp ->
                                    viewModel.updateInputs(
                                        sp,
                                        state.pitchOver12,
                                        ridgeInput.toDoubleOrNull() ?: 1.5,
                                        overhangInput.toDoubleOrNull() ?: 16.0,
                                        state.rafterLumberNominal,
                                        state.birdsmouthSeatCutInches
                                    )
                                }
                            },
                            label = { Text("Building Span (ft)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = overhangInput,
                            onValueChange = {
                                overhangInput = it
                                it.toDoubleOrNull()?.let { ov ->
                                    viewModel.updateInputs(
                                        spanInput.toDoubleOrNull() ?: 24.0,
                                        state.pitchOver12,
                                        ridgeInput.toDoubleOrNull() ?: 1.5,
                                        ov,
                                        state.rafterLumberNominal,
                                        state.birdsmouthSeatCutInches
                                    )
                                }
                            },
                            label = { Text("Overhang (in)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = ridgeInput,
                            onValueChange = {
                                ridgeInput = it
                                it.toDoubleOrNull()?.let { rd ->
                                    viewModel.updateInputs(
                                        spanInput.toDoubleOrNull() ?: 24.0,
                                        state.pitchOver12,
                                        rd,
                                        overhangInput.toDoubleOrNull() ?: 16.0,
                                        state.rafterLumberNominal,
                                        state.birdsmouthSeatCutInches
                                    )
                                }
                            },
                            label = { Text("Ridge (in)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(0.8f)
                        )
                    }

                    // Lumber Size Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Rafter Lumber:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        listOf("2x6 (5.5\")", "2x8 (7.25\")", "2x10 (9.25\")").forEach { lumber ->
                            FilterChip(
                                selected = state.rafterLumberNominal == lumber,
                                onClick = {
                                    viewModel.updateInputs(
                                        spanInput.toDoubleOrNull() ?: 24.0,
                                        state.pitchOver12,
                                        ridgeInput.toDoubleOrNull() ?: 1.5,
                                        overhangInput.toDoubleOrNull() ?: 16.0,
                                        lumber,
                                        state.birdsmouthSeatCutInches
                                    )
                                },
                                label = { Text(lumber) }
                            )
                        }
                    }
                }
            }

            // Visual Roof Truss & Rafter Diagram
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "ROOF RAFTER CROSS-SECTION",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                    ) {
                        val primaryColor = MaterialTheme.colorScheme.primary
                        val secondaryColor = MaterialTheme.colorScheme.secondary

                        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            val w = size.width
                            val h = size.height
                            val midX = w / 2f
                            val topY = 20f
                            val wallLeftX = 40f
                            val wallRightX = w - 40f
                            val plateY = h - 30f

                            // Draw Tie Ceiling Joist / Plate baseline
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.5f),
                                start = Offset(wallLeftX, plateY),
                                end = Offset(wallRightX, plateY),
                                strokeWidth = 3f
                            )

                            // Draw Left and Right Rafters with Overhang tails
                            val leftTailX = 10f
                            val rightTailX = w - 10f
                            val tailY = plateY + 18f

                            val rafterPath = Path().apply {
                                moveTo(leftTailX, tailY)
                                lineTo(midX, topY)
                                lineTo(rightTailX, tailY)
                            }
                            drawPath(path = rafterPath, color = primaryColor, style = Stroke(width = 5f))

                            // Draw Center King Post / Rise Reference Line
                            drawLine(
                                color = secondaryColor,
                                start = Offset(midX, topY),
                                end = Offset(midX, plateY),
                                strokeWidth = 2f
                            )

                            // Ridge Beam at top
                            drawCircle(color = primaryColor, radius = 6f, center = Offset(midX, topY))
                        }
                    }
                }
            }

            // Results Card - Common Rafter
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
                            text = "COMMON RAFTER CUT SPECS",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        )
                        IconButton(onClick = {
                            val info = """
                                Common Rafter: ${state.commonRafterFtIn} (${String.format("%.1f", state.commonRafterLengthInches)}")
                                Total Length with Overhang Tail: ${state.totalCommonLengthWithTailFtIn} (${String.format("%.1f", state.totalCommonLengthWithTailInches)}")
                                Plumb Cut: ${String.format("%.1f", state.plumbCutAngleDeg)}° | Seat Cut: ${String.format("%.1f", state.seatCutAngleDeg)}°
                                Total Rise: ${state.totalRiseFtIn}
                                Hip/Valley Rafter: ${state.hipRafterFtIn} (Plumb Cut: ${String.format("%.1f", state.hipPlumbCutAngleDeg)}°)
                                Jack Rafter Spacing: 16" O.C. -> ${String.format("%.1f", state.jackStepDown16InOC)}" | 24" O.C. -> ${String.format("%.1f", state.jackStepDown24InOC)}"
                                Birdsmouth HAP: ${String.format("%.2f", state.heightAbovePlateInches)}"
                            """.trimIndent()
                            clipboardManager.setText(AnnotatedString(info))
                            viewModel.logRafterSpecs()
                            Toast.makeText(context, "Copied Rafter Cut Specs!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Common Length (to plate)", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = state.commonRafterFtIn,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${String.format("%.1f", state.commonRafterLengthInches)}\"",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Length (with tail)", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = state.totalCommonLengthWithTailFtIn,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "${String.format("%.1f", state.totalCommonLengthWithTailInches)}\"",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Plumb Cut Angle", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "${String.format("%.1f", state.plumbCutAngleDeg)}°",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            )
                        }
                        Column {
                            Text("Seat Cut Angle", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "${String.format("%.1f", state.seatCutAngleDeg)}°",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Birdsmouth HAP", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "${String.format("%.2f", state.heightAbovePlateInches)}\"",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }

            // Hip, Valley & Jack Rafters Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "HIP, VALLEY & JACK RAFTER SPECIFICATIONS",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Hip / Valley Length", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = state.hipRafterFtIn,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${String.format("%.1f", state.hipRafterLengthInches)}\"",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Hip Plumb Cut", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "${String.format("%.1f", state.hipPlumbCutAngleDeg)}°",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Jack Rafter Step-Down Differences:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("16\" O.C. Spacing: ${String.format("%.2f", state.jackStepDown16InOC)}\" step-down", style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace))
                        Text("24\" O.C. Spacing: ${String.format("%.2f", state.jackStepDown24InOC)}\" step-down", style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace))
                    }
                }
            }
        }
    }
}
