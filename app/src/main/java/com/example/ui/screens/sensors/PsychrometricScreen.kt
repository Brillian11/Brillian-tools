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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PsychrometricScreen(
    viewModel: PsychrometricViewModel,
    modifier: Modifier = Modifier
) {
    val dryBulbTemp by viewModel.dryBulbTemp.collectAsState()
    val pressure by viewModel.pressure.collectAsState()
    val humidityInputType by viewModel.humidityInputType.collectAsState()
    val humidityValue by viewModel.humidityValue.collectAsState()

    val relativeHumidity by viewModel.relativeHumidity.collectAsState()
    val wetBulbTemp by viewModel.wetBulbTemp.collectAsState()
    val dewPointTemp by viewModel.dewPointTemp.collectAsState()
    val enthalpy by viewModel.enthalpy.collectAsState()
    val humidityRatio by viewModel.humidityRatio.collectAsState()
    val isImperial by viewModel.isImperial.collectAsState()

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
                            text = "Psychrometric Air State Calculator",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Computes wet bulb, dew point, relative humidity, and enthalpy. Select dry bulb and atmospheric pressure, then choose one humidity parameter to resolve the full state.",
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

        // Header Configuration Card
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
                        text = "Inputs & Parameters",
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
                            text = if (isImperial) "Imperial (°F, inHg)" else "Metric (°C, kPa)",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                Divider()

                // Dry Bulb Input
                OutlinedTextField(
                    value = dryBulbTemp,
                    onValueChange = { viewModel.setDryBulb(it) },
                    label = { Text("Dry Bulb Temperature (${if (isImperial) "°F" else "°C"})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dry_bulb_input"),
                    singleLine = true
                )

                // Atmospheric Pressure Input
                OutlinedTextField(
                    value = pressure,
                    onValueChange = { viewModel.setPressure(it) },
                    label = { Text("Barometric Pressure (${if (isImperial) "inHg" else "kPa"})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pressure_input"),
                    singleLine = true
                )

                // Humidity Input Type Selection
                Text(
                    text = "Select Humidity Input Parameter",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val types = listOf("RH" to "Rel. Humidity", "WB" to "Wet Bulb", "DP" to "Dew Point")
                    types.forEach { (typeId, label) ->
                        val isSelected = humidityInputType == typeId
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setHumidityInputType(typeId) },
                            label = { Text(label) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chip_$typeId")
                        )
                    }
                }

                // Humidity Input Value
                val unitLabel = when (humidityInputType) {
                    "RH" -> "%"
                    else -> if (isImperial) "°F" else "°C"
                }
                OutlinedTextField(
                    value = humidityValue,
                    onValueChange = { viewModel.setHumidityValue(it) },
                    label = { Text("Measured Value ($unitLabel)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("humidity_value_input"),
                    singleLine = true
                )
            }
        }

        // Thermodynamic Results Card
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
                    text = "Resolved Air State Outputs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))

                // Outputs Grid
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    maxItemsInEachRow = 2,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val itemWidthModifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()

                    // Relative Humidity Output (only relevant if input type is not RH)
                    val rhDisplay = "%.1f".format(relativeHumidity)
                    OutputItem(
                        title = "Relative Humidity",
                        value = "$rhDisplay %",
                        icon = Icons.Default.WaterDrop,
                        modifier = itemWidthModifier
                    )

                    // Dew Point Temp Output
                    val dpDisplayVal = if (isImperial) dewPointTemp * 1.8 + 32.0 else dewPointTemp
                    val dpDisplay = "%.1f".format(dpDisplayVal)
                    OutputItem(
                        title = "Dew Point",
                        value = "$dpDisplay ${if (isImperial) "°F" else "°C"}",
                        icon = Icons.Default.Grain,
                        modifier = itemWidthModifier
                    )

                    // Wet Bulb Temp Output
                    val wbDisplayVal = if (isImperial) wetBulbTemp * 1.8 + 32.0 else wetBulbTemp
                    val wbDisplay = "%.1f".format(wbDisplayVal)
                    OutputItem(
                        title = "Wet Bulb Temp",
                        value = "$wbDisplay ${if (isImperial) "°F" else "°C"}",
                        icon = Icons.Default.DeviceThermostat,
                        modifier = itemWidthModifier
                    )

                    // Enthalpy Output
                    val enthalpyDisplayVal = if (isImperial) enthalpy * 0.429922 else enthalpy // kJ/kg to BTU/lb
                    val enthalpyDisplay = "%.2f".format(enthalpyDisplayVal)
                    OutputItem(
                        title = "Enthalpy (h)",
                        value = "$enthalpyDisplay ${if (isImperial) "BTU/lb" else "kJ/kg"}",
                        icon = Icons.Default.Bolt,
                        modifier = itemWidthModifier
                    )

                    // Humidity Ratio Output
                    val hrDisplayVal = if (isImperial) humidityRatio * 7.0 else humidityRatio // g/kg to grains/lb
                    val hrDisplay = "%.1f".format(hrDisplayVal)
                    OutputItem(
                        title = "Humidity Ratio (W)",
                        value = "$hrDisplay ${if (isImperial) "gr/lb" else "g/kg"}",
                        icon = Icons.Default.Grain,
                        modifier = itemWidthModifier
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = { viewModel.logActivity() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("log_psychrometric_button"),
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

@Composable
fun OutputItem(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
