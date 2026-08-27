package com.example.ui.screens.electrical

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PowerFactorCorrectionScreen(
    viewModel: PowerFactorCorrectionViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Power Factor & Harmonics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Capacitor Bank Sizing (kVAR) & Detuned Reactors",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        clipboardManager.setText(AnnotatedString(state.calculationSummary))
                        Toast.makeText(context, "Power factor data copied", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Copy Summary")
                    }
                    IconButton(onClick = {
                        viewModel.saveToLogs()
                        Toast.makeText(context, "Saved to project log", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = "Save")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // 1. Hero Power Factor & kVAR Result Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "REQUIRED CAPACITOR BANK",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "${String.format("%.1f", state.requiredCapacitorKvar)} kVAR",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Standard Bank Size: ${state.recommendedStandardKvarBank} kVAR (${String.format("%.1f", state.deltaCapacitanceUfPerPhase)} µF/phase Δ)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("PF GAIN", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        "${String.format("%.2f", state.initialPowerFactor)} → ${String.format("%.2f", state.targetPowerFactor)}",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Vector Power Triangle Canvas
                        PowerTriangleCanvas(
                            state = state,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                        )
                    }
                }
            }

            // 2. Economic ROI & Energy Savings Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Economic ROI & Demand Charge Savings",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Icon(Icons.Default.TrendingDown, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            RoiMetricColumn("DEMAND REDUCTION", "-${String.format("%.1f", state.kvaReduction)} kVA")
                            RoiMetricColumn("ANNUAL SAVINGS", "$${String.format("%.0f", state.annualSavings)} / yr")
                            RoiMetricColumn("SIMPLE PAYBACK", "${String.format("%.1f", state.simplePaybackMonths)} Months")
                        }
                    }
                }
            }

            // 3. Electrical System Inputs
            item {
                Card(shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Electrical Load & Power Factor Targets",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Real Power (kW) Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Active Real Power (kW):", style = MaterialTheme.typography.bodyMedium)
                            Text("${state.realPowerKw.toInt()} kW", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Slider(
                            value = state.realPowerKw.toFloat(),
                            onValueChange = { viewModel.setRealPowerKw(it.toDouble()) },
                            valueRange = 10f..1000f,
                            steps = 99
                        )

                        // Initial Power Factor
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Existing Initial Power Factor:", style = MaterialTheme.typography.bodyMedium)
                            Text("${String.format("%.2f", state.initialPowerFactor)} (Lagging)", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Slider(
                            value = state.initialPowerFactor.toFloat(),
                            onValueChange = { viewModel.setInitialPf(it.toDouble()) },
                            valueRange = 0.50f..0.92f,
                            steps = 42
                        )

                        // Target Power Factor
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Target Corrected Power Factor:", style = MaterialTheme.typography.bodyMedium)
                            Text("${String.format("%.2f", state.targetPowerFactor)} (Lagging)", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Slider(
                            value = state.targetPowerFactor.toFloat(),
                            onValueChange = { viewModel.setTargetPf(it.toDouble()) },
                            valueRange = 0.88f..1.00f,
                            steps = 12
                        )

                        // Voltage Selection
                        Text("3-Phase System Line Voltage:", style = MaterialTheme.typography.labelSmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(208, 240, 480, 600, 4160).forEach { v ->
                                FilterChip(
                                    selected = state.systemVoltageV == v,
                                    onClick = { viewModel.setVoltage(v) },
                                    label = { Text("${v}V") }
                                )
                            }
                        }
                    }
                }
            }

            // 4. Harmonics & Detuned Reactors
            item {
                Card(shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Harmonic Detuning Reactor",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            if (state.isNearHarmonicHazard) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.error
                                ) {
                                    Text(
                                        "RESONANCE RISK!",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Detuned series iron-core reactors shift LC resonance below the 5th harmonic (250/300Hz), protecting capacitors from VFD distortion.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        HarmonicReactorTuning.values().forEach { reactor ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilterChip(
                                    selected = state.harmonicReactor == reactor,
                                    onClick = { viewModel.setHarmonicReactor(reactor) },
                                    label = { Text(reactor.label, fontSize = 11.sp) }
                                )
                                Text(
                                    text = if (reactor.tuningFreq60Hz > 0) "${reactor.tuningFreq60Hz.toInt()} Hz" else "None",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // 5. Engineering Metrics Breakdown Table
            item {
                Card(shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Technical Current & Power Schedule",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        PfDetailRow("Apparent Power (kVA)", "${String.format("%.1f", state.initialKva)} kVA → ${String.format("%.1f", state.correctedKva)} kVA (-${String.format("%.1f", state.kvaReduction)} kVA)")
                        PfDetailRow("Reactive Power (kVAR)", "${String.format("%.1f", state.initialKvar)} kVAR → ${String.format("%.1f", state.correctedKvar)} kVAR")
                        PfDetailRow("Feeder Current @ ${state.systemVoltageV}V", "${String.format("%.1f", state.initialCurrentAmps)} A → ${String.format("%.1f", state.correctedCurrentAmps)} A (-${String.format("%.1f", state.currentReductionAmps)} A)")
                        PfDetailRow("Cable I²R Heat Loss Reduction", "${String.format("%.1f", state.lineLossReductionPct)} % cooler feeders")
                        PfDetailRow("System Parallel Resonance", "${String.format("%.0f", state.parallelResonanceFrequencyHz)} Hz")
                        PfDetailRow("Delta Capacitance per Phase", "${String.format("%.1f", state.deltaCapacitanceUfPerPhase)} µF (Delta @ ${state.systemVoltageV}V)")
                    }
                }
            }
        }
    }
}

@Composable
private fun RoiMetricColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.secondary,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun PfDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
}

@Composable
private fun PowerTriangleCanvas(
    state: PowerFactorUiState,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .background(Color(0xFF16191E), RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        val w = size.width
        val h = size.height

        val origin = Offset(24f, h - 20f)
        val pLen = (w * 0.65f)

        // 1. Real Power P (Horizontal Axis)
        drawLine(
            color = Color(0xFF64B5F6),
            start = origin,
            end = Offset(origin.x + pLen, origin.y),
            strokeWidth = 3.5f,
            cap = StrokeCap.Round
        )

        // 2. Initial Q1 (Vertical Up from P tip)
        val q1Len = (h * 0.70f).toFloat()
        val q2Len = (q1Len * (state.correctedKvar / state.initialKvar.coerceAtLeast(1.0))).toFloat()

        val pTip = Offset(origin.x + pLen, origin.y)
        val q1Tip = Offset(pTip.x, pTip.y - q1Len)
        val q2Tip = Offset(pTip.x, pTip.y - q2Len)

        // Initial Q1 Line (Orange)
        drawLine(
            color = Color(0xFFFFB74D),
            start = pTip,
            end = q1Tip,
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )

        // Corrected Q2 Line (Green)
        drawLine(
            color = Color(0xFF81C784),
            start = pTip,
            end = q2Tip,
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )

        // Capacitor Qc deduction line (Red dashed from Q1 tip down to Q2 tip)
        drawLine(
            color = Color(0xFFFF5252),
            start = q1Tip,
            end = q2Tip,
            strokeWidth = 3.5f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
        )

        // Initial S1 Hypotenuse
        drawLine(
            color = Color(0xFFFFCC80),
            start = origin,
            end = q1Tip,
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
        )

        // Corrected S2 Hypotenuse (Bold Blue-Green)
        drawLine(
            color = Color(0xFF00E676),
            start = origin,
            end = q2Tip,
            strokeWidth = 3.5f
        )
    }
}
