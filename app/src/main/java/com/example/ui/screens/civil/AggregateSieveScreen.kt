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
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Warning
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
import kotlin.math.log10

@Composable
fun AggregateSieveScreen(
    viewModel: AggregateSieveViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

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
                                imageVector = Icons.Default.Grain,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Column {
                            Text(
                                text = "Aggregate Sieve Analysis",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Gradation Curves & Fineness Modulus (ASTM C136)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = {
                                viewModel.saveToLog()
                                Toast.makeText(context, "Sieve analysis saved to log", Toast.LENGTH_SHORT).show()
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
                                    appendLine("=== AGGREGATE SIEVE ANALYSIS REPORT ===")
                                    appendLine("Type: ${state.aggregateType.label}")
                                    appendLine("Total Sample Mass: ${String.format("%.1f", state.totalSampleMassGrams)} g")
                                    appendLine("Fineness Modulus (FM): ${String.format("%.2f", state.finenessModulus)} (${if (state.isFmWithinAstmC33) "PASS ASTM C33 (2.3-3.1)" else "OUT OF SPEC"})")
                                    appendLine("Classification: ${state.gradationClassification}")
                                    appendLine("Cu: ${String.format("%.2f", state.uniformityCoeffCu)} | Cc: ${String.format("%.2f", state.curvatureCoeffCc)}")
                                    appendLine("--------------------------------------")
                                    appendLine("Sieve | Mass (g) | % Retained | Cum % Ret | % Passing")
                                    state.sieves.forEach { s ->
                                        appendLine("${s.name.padEnd(14)} | ${String.format("%6.1f", s.massRetainedGrams)}g | ${String.format("%5.1f", s.percentRetained)}% | ${String.format("%5.1f", s.cumulativePercentRetained)}% | ${String.format("%5.1f", s.percentPassing)}%")
                                    }
                                }
                                clipboardManager.setText(AnnotatedString(text))
                                Toast.makeText(context, "Sieve analysis copied", Toast.LENGTH_SHORT).show()
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

            // Aggregate Preset Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AggregateType.values().forEach { t ->
                    FilterChip(
                        selected = state.aggregateType == t,
                        onClick = { viewModel.setAggregateType(t) },
                        label = { Text(t.label) }
                    )
                }
            }

            // Semi-Logarithmic Gradation Curve Canvas
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
                            text = "Semi-Log Gradation Curve (Particle Size Distribution)",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = if (state.isFmWithinAstmC33) Color(0xFF059669) else Color(0xFFD97706),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "FM: ${String.format("%.2f", state.finenessModulus)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Plots % Passing vs. Sieve Opening (mm) on logarithmic x-axis with ASTM envelope",
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
                        val padLeft = 40f
                        val padBottom = 30f
                        val padTop = 15f
                        val padRight = 15f

                        val plotWidth = width - padLeft - padRight
                        val plotHeight = height - padTop - padBottom

                        // Logarithmic scale: 0.05 mm to 50.0 mm (3 decades: 0.01 to 0.1, 0.1 to 1.0, 1.0 to 10.0, 10 to 100)
                        val minLog = log10(0.05)
                        val maxLog = log10(50.0)

                        fun getX(mm: Double): Float {
                            val clamped = mm.coerceIn(0.05, 50.0)
                            val frac = (log10(clamped) - minLog) / (maxLog - minLog)
                            return padLeft + frac.toFloat() * plotWidth
                        }

                        fun getY(pctPassing: Double): Float {
                            val clamped = pctPassing.coerceIn(0.0, 100.0)
                            val frac = (100.0 - clamped) / 100.0
                            return padTop + frac.toFloat() * plotHeight
                        }

                        // Grid lines for Y (0%, 25%, 50%, 75%, 100%)
                        for (pct in listOf(0.0, 25.0, 50.0, 75.0, 100.0)) {
                            val y = getY(pct)
                            drawLine(
                                color = Color(0xFF334155),
                                start = Offset(padLeft, y),
                                end = Offset(width - padRight, y),
                                strokeWidth = 1f
                            )
                        }

                        // Grid lines for Log X (0.1, 1.0, 10.0 mm)
                        for (xVal in listOf(0.075, 0.15, 0.3, 0.6, 1.18, 2.36, 4.75, 9.5, 19.0, 37.5)) {
                            val x = getX(xVal)
                            drawLine(
                                color = Color(0xFF334155).copy(alpha = 0.5f),
                                start = Offset(x, padTop),
                                end = Offset(x, height - padBottom),
                                strokeWidth = 1f
                            )
                        }

                        // Plot ASTM Upper / Lower Envelope bands if available
                        val upperPath = Path()
                        val lowerPath = Path()
                        var firstUpper = true
                        var firstLower = true

                        val validSpecSieves = state.sieves.filter { it.sizeMm > 0.05 && it.astmMinPassing != null && it.astmMaxPassing != null }
                        validSpecSieves.forEach { s ->
                            val x = getX(s.sizeMm)
                            val yMax = getY(s.astmMaxPassing!!)
                            val yMin = getY(s.astmMinPassing!!)
                            if (firstUpper) {
                                upperPath.moveTo(x, yMax)
                                lowerPath.moveTo(x, yMin)
                                firstUpper = false
                                firstLower = false
                            } else {
                                upperPath.lineTo(x, yMax)
                                lowerPath.lineTo(x, yMin)
                            }
                        }

                        if (!firstUpper) {
                            drawPath(
                                path = upperPath,
                                color = Color(0xFFF59E0B).copy(alpha = 0.6f),
                                style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f)))
                            )
                            drawPath(
                                path = lowerPath,
                                color = Color(0xFFF59E0B).copy(alpha = 0.6f),
                                style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f)))
                            )
                        }

                        // Plot Sample Gradation Curve (Cyan solid with points)
                        val samplePath = Path()
                        var firstSample = true
                        state.sieves.filter { it.sizeMm > 0.05 }.forEach { s ->
                            val x = getX(s.sizeMm)
                            val y = getY(s.percentPassing)
                            if (firstSample) {
                                samplePath.moveTo(x, y)
                                firstSample = false
                            } else {
                                samplePath.lineTo(x, y)
                            }
                            drawCircle(color = Color(0xFF38BDF8), radius = 4f, center = Offset(x, y))
                        }
                        drawPath(path = samplePath, color = Color(0xFF38BDF8), style = Stroke(width = 3.5f))
                    }
                }
            }

            // Results & Sizing Metrics
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
                        text = "Gradation Quality & Modulus Metrics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SieveStatBox(
                            title = "Fineness Modulus (FM)",
                            value = String.format("%.2f", state.finenessModulus),
                            subtitle = if (state.isFmWithinAstmC33) "ASTM C33 PASS (2.3 - 3.1)" else "FAIL (Adjust fine/coarse)",
                            modifier = Modifier.weight(1f)
                        )
                        SieveStatBox(
                            title = "Classification",
                            value = state.gradationClassification,
                            subtitle = "USCS / ASTM D2487",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SieveStatBox(
                            title = "Uniformity Coeff (Cu)",
                            value = String.format("%.2f", state.uniformityCoeffCu),
                            subtitle = "D60 / D10 (D60=${String.format("%.2f", state.d60Mm)}mm)",
                            modifier = Modifier.weight(1f)
                        )
                        SieveStatBox(
                            title = "Curvature Coeff (Cc)",
                            value = String.format("%.2f", state.curvatureCoeffCc),
                            subtitle = "(D30)² / (D10×D60) (D10=${String.format("%.2f", state.d10Mm)}mm)",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Interactive Sieve Table Input
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sieve Stack Data Entry",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Total Mass: ${String.format("%.1f", state.totalSampleMassGrams)} g",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Sieve Row Items
                    state.sieves.forEach { sieve ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1.3f)) {
                                Text(
                                    text = sieve.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (sieve.astmMinPassing != null && sieve.astmMaxPassing != null) {
                                    Text(
                                        text = "Spec: ${sieve.astmMinPassing.toInt()}-${sieve.astmMaxPassing.toInt()}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 10.sp,
                                        color = if (sieve.isWithinSpec) Color(0xFF059669) else Color(0xFFDC2626)
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = sieve.massRetainedGrams.toString(),
                                onValueChange = { v ->
                                    v.toDoubleOrNull()?.let { viewModel.updateMassRetained(sieve.id, it) }
                                },
                                label = { Text("Mass (g)") },
                                modifier = Modifier.weight(1.2f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )

                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "${String.format("%.1f", sieve.percentPassing)}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (sieve.isWithinSpec) MaterialTheme.colorScheme.onSurface else Color(0xFFDC2626)
                                )
                                Text(
                                    text = "Passing",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                    }
                }
            }
        }
    }
}

@Composable
private fun SieveStatBox(
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
