package com.example.ui.screens.civil

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.math.BeamResult
import com.example.domain.math.LoadType
import com.example.domain.math.MaterialPreset
import com.example.ui.screens.woodworking.ResultBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeamDeflectionScreen(
    viewModel: BeamDeflectionViewModel,
    modifier: Modifier = Modifier
) {
    val spanLength by viewModel.spanLength.collectAsState()
    val loadType by viewModel.loadType.collectAsState()
    val uniformLoad by viewModel.uniformLoad.collectAsState()
    val pointLoad by viewModel.pointLoad.collectAsState()
    val beamWidth by viewModel.beamWidth.collectAsState()
    val beamHeight by viewModel.beamHeight.collectAsState()
    val selectedMaterial by viewModel.selectedMaterial.collectAsState()
    val result by viewModel.result.collectAsState()

    var materialDropdownExpanded by remember { mutableStateOf(false) }

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
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "BEAM DEFLECTION & MOMENT ANALYZER",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Asian & International Structural Engineering",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Calculates maximum shear force (Vmax), bending moment (Mmax), moment of inertia, and deflection ratio (L/360) for Asian timbers (Jati, Bangkirai, Kamper, Ulin, Sugi, Bamboo) and JIS steels.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                    )
                }
            }

            // Pass / Fail Safety Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (result.isDeflectionSafe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (result.isDeflectionSafe) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (result.isDeflectionSafe) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (result.isDeflectionSafe) "DEFLECTION WITHIN SAFE LIMIT (L/360)" else "EXCEEDS ALLOWABLE DEFLECTION LIMIT!",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (result.isDeflectionSafe) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "Actual Deflection: ${String.format("%.2f", result.maxDeflectionMm)} mm (Allowable: ${String.format("%.2f", result.allowableDeflectionMm)} mm)",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (result.isDeflectionSafe) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Asian & International Material Selection Dropdown
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
                        text = "Material Specification Dropdown",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    ExposedDropdownMenuBox(
                        expanded = materialDropdownExpanded,
                        onExpandedChange = { materialDropdownExpanded = !materialDropdownExpanded },
                        modifier = Modifier.fillMaxWidth().testTag("dropdown_material_select")
                    ) {
                        OutlinedTextField(
                            value = "${selectedMaterial.name} (E = ${selectedMaterial.elasticityModulusGpa} GPa)",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Selected Construction Material") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = materialDropdownExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = materialDropdownExpanded,
                            onDismissRequest = { materialDropdownExpanded = false }
                        ) {
                            MaterialPreset.PRESETS.forEach { preset ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(preset.name, fontWeight = FontWeight.Bold)
                                            Text(
                                                "Category: ${preset.category} | E = ${preset.elasticityModulusGpa} GPa | Density = ${preset.densityKgM3.toInt()} kg/m³",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.selectMaterial(preset)
                                        materialDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Inputs Card
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
                        text = "Span & Cross-Section",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    OutlinedTextField(
                        value = spanLength,
                        onValueChange = { viewModel.updateSpanLength(it) },
                        label = { Text("Span Length L (meters)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_beam_span")
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = beamWidth,
                            onValueChange = { viewModel.updateBeamWidth(it) },
                            label = { Text("Width b (mm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("input_beam_width")
                        )
                        OutlinedTextField(
                            value = beamHeight,
                            onValueChange = { viewModel.updateBeamHeight(it) },
                            label = { Text("Height h (mm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("input_beam_height")
                        )
                    }

                    // Load Type Chips
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = loadType == LoadType.UNIFORM_DISTRIBUTED,
                            onClick = { viewModel.updateLoadType(LoadType.UNIFORM_DISTRIBUTED) },
                            label = { Text("Uniform Load (UDL)") }
                        )
                        FilterChip(
                            selected = loadType == LoadType.POINT_LOAD_CENTER,
                            onClick = { viewModel.updateLoadType(LoadType.POINT_LOAD_CENTER) },
                            label = { Text("Center Point Load") }
                        )
                    }

                    if (loadType == LoadType.UNIFORM_DISTRIBUTED) {
                        OutlinedTextField(
                            value = uniformLoad,
                            onValueChange = { viewModel.updateUniformLoad(it) },
                            label = { Text("Uniform Load w (kN/m)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_beam_udl")
                        )
                    } else {
                        OutlinedTextField(
                            value = pointLoad,
                            onValueChange = { viewModel.updatePointLoad(it) },
                            label = { Text("Point Load P (kN)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_beam_point_load")
                        )
                    }
                }
            }

            // Badges Result Output
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ResultBadge(
                    title = "MAX BENDING MOMENT",
                    value = String.format("%.2f", result.maxBendingMomentKnm),
                    unit = "kN·m (Mmax)",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                ResultBadge(
                    title = "MAX SHEAR FORCE",
                    value = String.format("%.2f", result.maxShearForceKn),
                    unit = "kN (Vmax)",
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }

            // Shear Force & Bending Moment Diagrams
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Shear & Bending Moment Diagrams",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    BeamDiagramCanvas(
                        result = result,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BeamDiagramCanvas(
    result: BeamResult,
    modifier: Modifier = Modifier
) {
    val axisColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    val momentColor = MaterialTheme.colorScheme.primary
    val shearColor = MaterialTheme.colorScheme.tertiary

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val leftX = 40f
        val rightX = w - 40f
        val midY = h / 2f

        // Draw neutral axis baseline
        drawLine(
            color = axisColor,
            start = Offset(leftX, midY),
            end = Offset(rightX, midY),
            strokeWidth = 3f
        )

        // Supports
        drawLine(color = axisColor, start = Offset(leftX, midY - 12f), end = Offset(leftX, midY + 12f), strokeWidth = 6f)
        drawLine(color = axisColor, start = Offset(rightX, midY - 12f), end = Offset(rightX, midY + 12f), strokeWidth = 6f)

        // Elastic Deflection Curve
        val path = Path()
        path.moveTo(leftX, midY)
        val curvePeakY = midY + 30f
        path.quadraticTo((leftX + rightX) / 2f, curvePeakY, rightX, midY)

        drawPath(
            path = path,
            color = if (result.isDeflectionSafe) momentColor else Color(0xFFD32F2F),
            style = Stroke(width = 5f)
        )
    }
}
