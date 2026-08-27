package com.example.ui.screens.sensors

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.*

@Composable
fun SlingAngleScreen(
    viewModel: SlingAngleViewModel,
    modifier: Modifier = Modifier
) {
    val loadWeight by viewModel.loadWeight.collectAsState()
    val numberOfLegs by viewModel.numberOfLegs.collectAsState()
    val slingAngle by viewModel.slingAngle.collectAsState()
    val isImperial by viewModel.isImperial.collectAsState()

    val tensionMultiplier by viewModel.tensionMultiplier.collectAsState()
    val tensionPerLeg by viewModel.tensionPerLeg.collectAsState()
    val safetyStatus by viewModel.safetyStatus.collectAsState()

    val angleVal = slingAngle.toDoubleOrNull() ?: 60.0

    var showHint by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Hint Box
        AnimatedVisibility(
            visible = showHint,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Rigging & Crane Sling Angle Sizer",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Calculates tension spikes on multi-leg lifting bridles at acute horizontal angles (30°, 45°, 60°). Never lift below 30° due to explosive stress!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    IconButton(
                        onClick = { showHint = false },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // Configuration Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rigging Load Parameters",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = { viewModel.toggleUnits() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.testTag("toggle_units_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Unit Switch",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isImperial) "lbs" else "kg",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                Divider()

                // Total Load Weight
                OutlinedTextField(
                    value = loadWeight,
                    onValueChange = { viewModel.setLoadWeight(it) },
                    label = { Text("Total Load Weight (${if (isImperial) "lbs" else "kg"})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("load_weight_input"),
                    singleLine = true
                )

                // Effective Sling Legs Selector
                Text(
                    text = "Effective Load-Bearing Legs (N)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val legsOptions = listOf(2, 3, 4)
                    legsOptions.forEach { legCount ->
                        val isSelected = numberOfLegs == legCount
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setNumberOfLegs(legCount) },
                            label = { Text("$legCount Legs") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chip_legs_$legCount")
                        )
                    }
                }

                // Horizontal Sling Angle
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Horizontal Sling Angle (θ)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${"%.1f".format(angleVal)}°",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = when (safetyStatus) {
                                "CRITICAL" -> Color(0xFFDC2626)
                                "WARNING" -> Color(0xFFD97706)
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                    }

                    Slider(
                        value = angleVal.toFloat(),
                        onValueChange = { viewModel.setSlingAngle("%.1f".format(it)) },
                        valueRange = 15f..90f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("angle_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("15° (UNSAFE)", style = MaterialTheme.typography.labelSmall, color = Color(0xFFDC2626))
                        Text("30°", style = MaterialTheme.typography.labelSmall)
                        Text("45°", style = MaterialTheme.typography.labelSmall)
                        Text("60°", style = MaterialTheme.typography.labelSmall)
                        Text("90° (Vertical)", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // Dynamic 2D Sling Bridle Canvas Visualizer
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Rigging Angle Visualizer",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                val primaryColor = MaterialTheme.colorScheme.primary
                val outlineColor = MaterialTheme.colorScheme.outline
                val currentAngle = angleVal

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height

                        // Center point at bottom represents the load center
                        val loadCenterX = width / 2.0f
                        val loadCenterY = height - 25.0f

                        // Draw load box
                        drawRect(
                            color = outlineColor,
                            topLeft = Offset(loadCenterX - 60.0f, loadCenterY - 10.0f),
                            size = androidx.compose.ui.geometry.Size(120.0f, 25.0f)
                        )

                        // Calculate hook position based on angle
                        // angle is horizontal, so:
                        // Tan(angle) = dy / dx
                        // dx = leg half width
                        val slingHalfSpan = 80.0f
                        val rad = Math.toRadians(currentAngle)
                        val dy = slingHalfSpan * tan(rad).toFloat()
                        val hookY = (loadCenterY - dy).coerceAtLeast(15.0f)

                        // Left connection point on load
                        val leftX = loadCenterX - slingHalfSpan
                        val rightX = loadCenterX + slingHalfSpan

                        // Draw horizontal baseline
                        drawLine(
                            color = outlineColor.copy(alpha = 0.3f),
                            start = Offset(leftX - 20.0f, loadCenterY),
                            end = Offset(rightX + 20.0f, loadCenterY),
                            strokeWidth = 2.0f
                        )

                        // Draw sling lines from left/right connection up to the hook
                        val lineAccentColor = when (safetyStatus) {
                            "CRITICAL" -> Color(0xFFDC2626)
                            "WARNING" -> Color(0xFFD97706)
                            else -> primaryColor
                        }

                        drawLine(
                            color = lineAccentColor,
                            start = Offset(leftX, loadCenterY),
                            end = Offset(loadCenterX, hookY),
                            strokeWidth = 6.0f,
                            cap = StrokeCap.Round
                        )

                        drawLine(
                            color = lineAccentColor,
                            start = Offset(rightX, loadCenterY),
                            end = Offset(loadCenterX, hookY),
                            strokeWidth = 6.0f,
                            cap = StrokeCap.Round
                        )

                        // Draw crane hook at top
                        drawCircle(
                            color = Color.DarkGray,
                            radius = 8.0f,
                            center = Offset(loadCenterX, hookY)
                        )

                        // Draw the horizontal angle indicator curve
                        // Arc representing horizontal angle θ
                        drawArc(
                            color = lineAccentColor,
                            startAngle = 180f,
                            sweepAngle = currentAngle.toFloat(),
                            useCenter = false,
                            topLeft = Offset(leftX - 15f, loadCenterY - 15f),
                            size = androidx.compose.ui.geometry.Size(30f, 30f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                        )
                    }
                }
            }
        }

        // Calculation Results Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Rigging Bridle Outputs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Tension Per Leg
                    Card(
                        modifier = Modifier.weight(1.2f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Tension Per Sling Leg", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("%.0f %s".format(tensionPerLeg, if (isImperial) "lbs" else "kg"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    // Tension Multiplier L/H
                    Card(
                        modifier = Modifier.weight(0.8f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Stress Multiplier", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("x %.3f".format(tensionMultiplier), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Safety Diagnostic Status Bar
                val (warnText, warnColor) = when (safetyStatus) {
                    "CRITICAL" -> "CRITICAL DANGER! Sling angle is below 30°. Sling tension is multiplied exponentially. High risk of catastrophic structural bridle failure!" to Color(0xFFDC2626)
                    "WARNING" -> "CAUTION! Angle is between 30° and 45°. Sling tension is amplified by 1.4x - 2.0x. Adjust rigging setup if possible." to Color(0xFFD97706)
                    else -> "SAFE LEVEL. Sling horizontal angle is above 45°. Dynamic lifting load is evenly distributed." to Color(0xFF16A34A)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(warnColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (safetyStatus == "SAFE") Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = warnColor
                        )
                        Text(
                            text = warnText,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = warnColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = { viewModel.logActivity() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("log_sling_angle_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Log",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Write to Field Notes Log")
                }
            }
        }
    }
}
