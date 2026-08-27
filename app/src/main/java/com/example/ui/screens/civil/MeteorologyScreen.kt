package com.example.ui.screens.civil

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.utils.ToolIconMapper

@Composable
fun MeteorologyScreen(
    viewModel: MeteorologyViewModel,
    modifier: Modifier = Modifier
) {
    val weather by viewModel.weatherState.collectAsState()
    val selectedPresetIndex by viewModel.selectedPresetIndex.collectAsState()
    val visuals = ToolIconMapper.getVisualsForTool("widget_meteorology")

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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(visuals.containerColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = visuals.icon,
                            contentDescription = null,
                            tint = visuals.contentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "METEOROLOGY & SITE WEATHER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.1.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = weather.locationName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // Station Presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val presetLabels = listOf("Yard", "Coastal", "Quarry", "Highland")
                presetLabels.forEachIndexed { idx, label ->
                    FilterChip(
                        selected = selectedPresetIndex == idx,
                        onClick = { viewModel.selectPreset(idx) },
                        label = { Text(label) },
                        modifier = Modifier.testTag("preset_chip_$idx")
                    )
                }
            }

            // Primary Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Thermostat, contentDescription = null, tint = Color(0xFF1D4ED8))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Temperature", style = MaterialTheme.typography.labelMedium, color = Color(0xFF1D4ED8))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${"%.1f".format(weather.tempC)}°C",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF1E3A8A)
                        )
                        Text(
                            text = "${"%.1f".format(weather.tempF)}°F",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF3B82F6)
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color(0xFF047857))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Humidity", style = MaterialTheme.typography.labelMedium, color = Color(0xFF047857))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${weather.humidityPercent}%",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF064E3B)
                        )
                        Text(
                            text = "Dew: ${"%.1f".format(weather.dewPointC)}°C",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF059669)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Air, contentDescription = null, tint = Color(0xFFB45309))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Wind Speed", style = MaterialTheme.typography.labelMedium, color = Color(0xFFB45309))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${"%.0f".format(weather.windKmH)} km/h",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF78350F)
                        )
                        Text(
                            text = "${weather.windDirection} (${"%.1f".format(weather.windMph)} mph)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFD97706)
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E8FF)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Compress, contentDescription = null, tint = Color(0xFF7E22CE))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Barometer", style = MaterialTheme.typography.labelMedium, color = Color(0xFF7E22CE))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${"%.1f".format(weather.pressureHpa)} hPa",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF581C87)
                        )
                        Text(
                            text = "${"%.2f".format(weather.pressureInHg)} inHg",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9333EA)
                        )
                    }
                }
            }

            // Air Quality Index Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Air Quality Index (AQI)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text(
                            text = "${weather.aqiIndex} - ${weather.aqiStatus}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (weather.aqiIndex <= 50) Color(0xFF15803D) else Color(0xFFB45309)
                        )
                    }
                }
            }

            // Jobsite Construction Field Advisories
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Build, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Jobsite Operation Advisories", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }

                    // Concrete Curing Advisory
                    val evapRate = weather.concreteEvaporationRate
                    val isConcreteOk = evapRate < 0.1
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isConcreteOk) Color(0xFFDCFCE7) else Color(0xFFFEE2E2))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Concrete Curing (ACI 305): ${if (isConcreteOk) "IDEAL" else "HIGH EVAPORATION RISK"}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isConcreteOk) Color(0xFF15803D) else Color(0xFFB91C1C)
                        )
                        Text(
                            text = "Est. surface evaporation rate: ${"%.3f".format(evapRate)} lb/ft²/hr (Threshold = 0.10). ${if (!isConcreteOk) "Apply fog sprays or curing compound to prevent plastic shrinkage cracking!" else "Safe for flatwork placement."}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Paint & Coating Advisory
                    val dewMargin = weather.tempC - weather.dewPointC
                    val isPaintOk = dewMargin >= 3.0
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isPaintOk) Color(0xFFE0F2FE) else Color(0xFFFEF3C7))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Steel Paint/Coating: ${if (isPaintOk) "SAFE TO APPLY" else "CONDENSATION RISK"}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isPaintOk) Color(0xFF0369A1) else Color(0xFFB45309)
                        )
                        Text(
                            text = "Temperature margin above dew point: ${"%.1f".format(dewMargin)}°C (Min required = 3.0°C).",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Crane & Scaffolding Wind Advisory
                    val isWindOk = weather.windKmH <= 32.0
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isWindOk) Color(0xFFF1F5F9) else Color(0xFFFEE2E2))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Crane & Scaffold Wind Limit: ${if (isWindOk) "SAFE OPERATIONS" else "WIND WARNING (>20 MPH)"}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isWindOk) Color(0xFF334155) else Color(0xFFB91C1C)
                        )
                        Text(
                            text = "Current gust speed: ${"%.0f".format(weather.windKmH)} km/h (${"%.1f".format(weather.windMph)} mph).",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Interactive Environment Field Test Sliders
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Field Condition Test Simulation", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

                    Text("Temperature: ${"%.1f".format(weather.tempC)}°C", style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = weather.tempC.toFloat(),
                        onValueChange = { viewModel.updateTemperature(it.toDouble()) },
                        valueRange = 0f..45f,
                        modifier = Modifier.testTag("slider_weather_temp")
                    )

                    Text("Humidity: ${weather.humidityPercent}%", style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = weather.humidityPercent.toFloat(),
                        onValueChange = { viewModel.updateHumidity(it.toInt()) },
                        valueRange = 10f..100f,
                        modifier = Modifier.testTag("slider_weather_humidity")
                    )

                    Text("Wind Speed: ${"%.0f".format(weather.windKmH)} km/h", style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = weather.windKmH.toFloat(),
                        onValueChange = { viewModel.updateWindSpeed(it.toDouble()) },
                        valueRange = 0f..60f,
                        modifier = Modifier.testTag("slider_weather_wind")
                    )
                }
            }
        }
    }
}
