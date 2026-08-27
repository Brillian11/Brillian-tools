package com.example.ui.screens.sensors

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun DuctSizerScreen(
    viewModel: DuctSizerViewModel,
    modifier: Modifier = Modifier
) {
    val cfm by viewModel.cfm.collectAsState()
    val frictionRateInput by viewModel.frictionRateInput.collectAsState()
    val rectangularWidth by viewModel.rectangularWidth.collectAsState()

    val calculatedDiameter by viewModel.calculatedDiameter.collectAsState()
    val calculatedVelocity by viewModel.calculatedVelocity.collectAsState()
    val calculatedRectHeight by viewModel.calculatedRectHeight.collectAsState()
    val velocityPressure by viewModel.velocityPressure.collectAsState()

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
                            text = "Equal Friction Duct Sizer",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Determines duct dimensions and airflow velocity. Enter airflow rate and friction. Standard SMACNA design recommends 0.08 to 0.12 in. wg / 100 ft.",
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

        // Calculation Inputs Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Airflow & Friction Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Divider()

                // Airflow Volume (CFM) Input
                OutlinedTextField(
                    value = cfm,
                    onValueChange = { viewModel.setCfm(it) },
                    label = { Text("Airflow Volume (CFM)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cfm_input"),
                    singleLine = true
                )

                // Friction rate input
                OutlinedTextField(
                    value = frictionRateInput,
                    onValueChange = { viewModel.setFrictionRate(it) },
                    label = { Text("Friction Loss Rate (in. wg per 100 ft)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("friction_rate_input"),
                    singleLine = true
                )

                Divider()

                // Rectangular Width
                Text(
                    text = "Rectangular Sizing Helper",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Specify desired rectangular duct width to solve for equivalent height (based on Huebscher's formula).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = rectangularWidth,
                    onValueChange = { viewModel.setRectWidth(it) },
                    label = { Text("Duct Width (inches)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rect_width_input"),
                    singleLine = true
                )
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
                    text = "Duct Dimension Solutions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))

                // Standard outputs layout
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Round Duct Diameter
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Round Diameter", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("%.1f\"".format(calculatedDiameter), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Air Velocity
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Air Velocity", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("%.0f FPM".format(calculatedVelocity), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Equivalent Rectangular Duct
                        Card(
                            modifier = Modifier.weight(1.2f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Equivalent Rectangular Size", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                val wVal = rectangularWidth.toDoubleOrNull() ?: 16.0
                                Text("%.0f\" × %.1f\"".format(wVal, calculatedRectHeight), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        // Velocity Pressure
                        Card(
                            modifier = Modifier.weight(0.8f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Velocity Press.", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("%.4f\" wg".format(velocityPressure), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Airflow Velocity Diagnostic Bar
                val (velWarning, velColor) = when {
                    calculatedVelocity < 400.0 -> "Velocity too low. Risk of air stagnation or dust settling." to Color(0xFFD97706)
                    calculatedVelocity in 400.0..1000.0 -> "Residential Low-Velocity - Optimal & quiet range." to Color(0xFF16A34A)
                    calculatedVelocity in 1000.0..1500.0 -> "Commercial Medium-Velocity - Safe, typical office range." to Color(0xFF16A34A)
                    calculatedVelocity in 1500.0..2200.0 -> "Industrial High-Velocity - Suitable for mains and risers." to Color(0xFF16A34A)
                    else -> "Velocity extremely high! Risk of substantial duct noise and vibration." to Color(0xFFDC2626)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(velColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (velColor == Color(0xFF16A34A)) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = velColor
                        )
                        Text(
                            text = velWarning,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = velColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = { viewModel.logActivity() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("log_duct_sizer_button"),
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
