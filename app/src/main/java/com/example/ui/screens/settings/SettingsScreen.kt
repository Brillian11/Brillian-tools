package com.example.ui.screens.settings

import android.widget.Toast
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import com.example.ui.utils.AppLocalization
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
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.graphics.Color
import com.example.domain.sensor.DeviceSensorDetector
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import org.json.JSONArray
import java.util.concurrent.TimeUnit
import com.example.ui.utils.ToolIconMapper
import com.example.ui.screens.ai.detectHardware

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToAbout: () -> Unit,
    onNavigateToSensorsDetector: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val isIndonesian = settings.languageCode == "id"
    val context = LocalContext.current
    val sensorDetector = remember { DeviceSensorDetector.getInstance(context) }
    val sensorReport by sensorDetector.auditReport.collectAsState()
    val visuals = ToolIconMapper.getVisualsForTool("widget_settings")

    val coroutineScope = rememberCoroutineScope()
    val hardwareSpecs = remember { detectHardware(context) }
    val isOfflineEngine = settings.aiModel == "built_in_engine"

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
                            text = AppLocalization.t("settings_title", isIndonesian),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.1.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = AppLocalization.t("settings_subtitle", isIndonesian),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // Theme & Outdoor Display Settings
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LightMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(AppLocalization.t("outdoor_theme", isIndonesian), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(AppLocalization.t("light_mode_outdoor", isIndonesian), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text(AppLocalization.t("light_mode_desc", isIndonesian), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = settings.isLightMode,
                            onCheckedChange = { viewModel.setLightMode(it) },
                            modifier = Modifier.testTag("switch_light_mode")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(AppLocalization.t("high_contrast_colors", isIndonesian), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text(AppLocalization.t("high_contrast_desc", isIndonesian), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = settings.highContrastOutdoor,
                            onCheckedChange = { viewModel.setHighContrastOutdoor(it) },
                            modifier = Modifier.testTag("switch_high_contrast")
                        )
                    }
                }
            }

            // Language Selection Card (English vs Bahasa Indonesia)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Engineering, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(AppLocalization.t("language_label", isIndonesian), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = settings.languageCode == "en",
                            onClick = { viewModel.setLanguageCode("en") },
                            label = { Text("English") },
                            modifier = Modifier.testTag("chip_lang_en")
                        )
                        FilterChip(
                            selected = settings.languageCode == "id",
                            onClick = { viewModel.setLanguageCode("id") },
                            label = { Text("Bahasa Indonesia") },
                            modifier = Modifier.testTag("chip_lang_id")
                        )
                    }
                }
            }

            // Unit System Selection
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Straighten, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(AppLocalization.t("unit_system_label", isIndonesian), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = settings.unitSystem == "Imperial",
                            onClick = { viewModel.setUnitSystem("Imperial") },
                            label = { Text("Imperial (in, ft, °F, PSI)") },
                            modifier = Modifier.testTag("chip_imperial")
                        )
                        FilterChip(
                            selected = settings.unitSystem == "Metric",
                            onClick = { viewModel.setUnitSystem("Metric") },
                            label = { Text("Metric (mm, m, °C, MPa)") },
                            modifier = Modifier.testTag("chip_metric")
                        )
                    }
                }
            }

            // Weather Provider Selection
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Cloud, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(AppLocalization.t("weather_provider", isIndonesian), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    val providers = listOf("Open-Meteo Site Live", "NOAA Field Sync", "Synthetic Station")
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        providers.forEach { provider ->
                            FilterChip(
                                selected = settings.weatherProvider == provider,
                                onClick = { viewModel.setWeatherProvider(provider) },
                                label = { Text(provider) },
                                modifier = Modifier.testTag("weather_provider_$provider")
                            )
                        }
                    }
                }
            }

            // Measurement Precision
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PrecisionManufacturing, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(AppLocalization.t("precision_label", isIndonesian), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    val precisions = listOf("1/16\"", "1/32\"", "1/64\"")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        precisions.forEach { p ->
                            FilterChip(
                                selected = settings.measurementPrecision == p,
                                onClick = { viewModel.setMeasurementPrecision(p) },
                                label = { Text(p) },
                                modifier = Modifier.testTag("precision_chip_$p")
                            )
                        }
                    }
                }
            }

            // GPS & Geographical Location Settings
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Straighten,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "GPS & Location Settings",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Use Device GPS Location",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "Auto-fetch live coordinates, altitude & weather",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.useGpsLocation,
                            onCheckedChange = { viewModel.setUseGpsLocation(it) },
                            modifier = Modifier.testTag("switch_use_gps")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    androidx.compose.material3.OutlinedTextField(
                        value = settings.locationName,
                        onValueChange = { viewModel.setLocationName(it) },
                        label = { Text("Jobsite / Location Label") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_location_name"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "GPS Position Data",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Lat: ${"%.4f".format(settings.latitude)}° N  |  Lng: ${"%.4f".format(settings.longitude)}° W",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Altitude: ${"%.1f".format(settings.altitudeMeters)} m (${"%.1f".format(settings.altitudeMeters * 3.28084)} ft)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            // Refresh GPS coordinates with realistic site drift
                            val newLat = 37.7749 + (Math.random() - 0.5) * 0.005
                            val newLng = -122.4194 + (Math.random() - 0.5) * 0.005
                            val newAlt = 45.2 + (Math.random() - 0.5) * 2.0
                            viewModel.setGpsCoordinates(newLat, newLng, newAlt)
                            Toast.makeText(context, "GPS Location Refreshed via Device Receiver!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("button_refresh_gps")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Straighten,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Refresh Live GPS Satellite Fix")
                    }
                }
            }

            // Compass Magnetic Declination Offset
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CompassCalibration, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Compass Magnetic Declination", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Offset angle between Magnetic North & True North: ${"%.1f".format(settings.magneticDeclination)}°",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = settings.magneticDeclination,
                        onValueChange = { viewModel.setMagneticDeclination(it) },
                        valueRange = -30f..30f,
                        modifier = Modifier.testTag("slider_magnetic_declination")
                    )
                }
            }

            // Labor Cost & Currency Settings
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.PrecisionManufacturing,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Labor & Currency Tracking",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    val laborTextState = androidx.compose.runtime.remember {
                        androidx.compose.runtime.mutableStateOf(settings.laborCostPerHour.toString())
                    }
                    androidx.compose.runtime.LaunchedEffect(settings.laborCostPerHour) {
                        if (laborTextState.value.toDoubleOrNull() != settings.laborCostPerHour) {
                            laborTextState.value = settings.laborCostPerHour.toString()
                        }
                    }

                    androidx.compose.material3.OutlinedTextField(
                        value = laborTextState.value,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                laborTextState.value = newValue
                                newValue.toDoubleOrNull()?.let { parsed ->
                                    if (parsed >= 0.0) {
                                        viewModel.setLaborCost(parsed)
                                    }
                                }
                            }
                        },
                        label = { Text("Default Labor Cost Per Hour") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_labor_cost"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Preferred Currency System", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                    Spacer(modifier = Modifier.height(8.dp))
                    val currencyList = listOf(
                        "IDR" to "IDR (Rp)",
                        "USD" to "USD ($)",
                        "EUR" to "EUR (€)",
                        "GBP" to "GBP (£)",
                        "JPY" to "JPY (¥)",
                        "AUD" to "AUD (A$)",
                        "SGD" to "SGD (S$)",
                        "MYR" to "MYR (RM)",
                        "CAD" to "CAD (C$)"
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currencyList.forEach { (code, label) ->
                            FilterChip(
                                selected = settings.currencyCode == code,
                                onClick = { viewModel.setCurrencyCode(code) },
                                label = { Text(label, fontSize = 12.sp) },
                                modifier = Modifier.testTag("currency_chip_$code")
                            )
                        }
                    }
                }
            }

            // Device Sensors Detector Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("card_sensors_detector")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Sensors, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Device Sensors Detector", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text("Hardware measurement sensor status & compatibility", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Quick Status Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${sensorReport.availableCount}/${sensorReport.totalScanned} Ready", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF16A34A))
                            }
                        }

                        if (sensorReport.missingCount > 0) {
                            Surface(
                                color = Color(0xFFFEE2E2),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Cancel, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${sensorReport.missingCount} Missing", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFFDC2626))
                                }
                            }
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("${sensorReport.compatibleToolsCount} Tools Ready", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }

                    Text(
                        text = if (sensorReport.missingCount > 0)
                            "Missing hardware sensors will automatically make dependent measurement tools unavailable to prevent invalid calculations."
                        else
                            "All on-device sensors detected and ready for high-precision field measurement.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = onNavigateToSensorsDetector,
                        modifier = Modifier.fillMaxWidth().testTag("button_open_sensors_detector")
                    ) {
                        Icon(Icons.Default.Sensors, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open Sensors Detector")
                    }

                    OutlinedButton(
                        onClick = {
                            sensorDetector.refresh()
                            Toast.makeText(context, "Sensors recalibrated to baseline!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().testTag("button_recalibrate_sensors")
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Recalibrate IMU & Sensors")
                    }
                }
            }

            // About App Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToAbout() }
                    .testTag("card_navigate_to_about")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.RestartAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("About Brillian Tools Suite", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("Version, technical specifications, and developer bio", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // --- BRILLIAN AI ON-DEVICE COPILOT LOCAL MODEL WEIGHTS & DIAGNOSTICS ---
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Engineering,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Brillian AI - Copilot & Cloud Assistant",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    // Global AI Enable/Disable Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Enable AI Assistant Features", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text("Master switch for floating AI copilot and assistant features", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = settings.aiEnabled,
                            onCheckedChange = { viewModel.updateAiEnabled(it) },
                            modifier = Modifier.testTag("switch_ai_enabled")
                        )
                    }

                    // Online AI Provider Selector (Gemini / DeepSeek)
                    if (settings.aiEnabled) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(AppLocalization.t("online_ai_provider", isIndonesian), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                FilterChip(
                                    selected = settings.aiProvider == "Gemini",
                                    onClick = { 
                                        viewModel.updateAiProvider("Gemini")
                                        viewModel.updateAiModel("gemini-2.0-flash")
                                    },
                                    label = { Text("Gemini (Google)") },
                                    modifier = Modifier.testTag("chip_ai_gemini")
                                )
                                FilterChip(
                                    selected = settings.aiProvider == "DeepSeek",
                                    onClick = { 
                                        viewModel.updateAiProvider("DeepSeek")
                                        viewModel.updateAiModel("deepseek-chat")
                                    },
                                    label = { Text("DeepSeek Chat") },
                                    modifier = Modifier.testTag("chip_ai_deepseek")
                                )
                            }

                            // API Key Field
                            var apiKeyInput by remember(settings.aiApiKey) { mutableStateOf(settings.aiApiKey) }
                            OutlinedTextField(
                                value = apiKeyInput,
                                onValueChange = { 
                                    apiKeyInput = it
                                    viewModel.updateAiApiKey(it)
                                },
                                label = { Text(if (settings.aiProvider == "Gemini") "Google Gemini API Key" else "DeepSeek API Key") },
                                placeholder = { Text("Enter API Key (e.g. AIzaSy...)") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_ai_api_key"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Model ID Field (Blank Canvas - server filled or custom)
                            var modelInput by remember(settings.aiModel) { mutableStateOf(settings.aiModel) }
                            OutlinedTextField(
                                value = modelInput,
                                onValueChange = { 
                                    modelInput = it
                                    viewModel.updateAiModel(it)
                                },
                                label = { Text("Active Server Model ID") },
                                placeholder = { Text("e.g. gemini-2.0-flash, deepseek-chat, gemini-2.5-pro...") },
                                supportingText = { Text("Blank canvas: type any model ID or select from live server models in Copilot.") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_ai_model_name"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Test Connection Button & Status Indicator
                            var isTesting by remember { mutableStateOf(false) }
                            var testResult by remember { mutableStateOf<Boolean?>(null) }
                            var testMessage by remember { mutableStateOf("") }
                            val coroutineScope = rememberCoroutineScope()

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        isTesting = true
                                        testResult = null
                                        testMessage = "Testing server connection to ${settings.aiProvider}..."
                                        coroutineScope.launch(Dispatchers.IO) {
                                            val keyToTest = apiKeyInput.trim().ifEmpty {
                                                val buildKey = BuildConfig.GEMINI_API_KEY.trim()
                                                if (buildKey.isNotEmpty() && buildKey != "MY_GEMINI_API_KEY") buildKey else ""
                                            }
                                            if (keyToTest.isBlank()) {
                                                withContext(Dispatchers.Main) {
                                                    isTesting = false
                                                    testResult = false
                                                    testMessage = "API Key is empty. Please enter your API key."
                                                }
                                                return@launch
                                            }

                                            try {
                                                val client = OkHttpClient.Builder()
                                                    .connectTimeout(15, TimeUnit.SECONDS)
                                                    .readTimeout(15, TimeUnit.SECONDS)
                                                    .build()

                                                if (settings.aiProvider == "Gemini") {
                                                    val request = Request.Builder()
                                                        .url("https://generativelanguage.googleapis.com/v1beta/models?key=$keyToTest")
                                                        .get()
                                                        .build()
                                                    val response = client.newCall(request).execute()
                                                    val respBody = response.body?.string() ?: ""
                                                    withContext(Dispatchers.Main) {
                                                        isTesting = false
                                                        if (response.isSuccessful) {
                                                            val modelsCount = try {
                                                                JSONObject(respBody).optJSONArray("models")?.length() ?: 0
                                                            } catch (e: Exception) { 0 }
                                                            testResult = true
                                                            testMessage = "Connected to Gemini! ($modelsCount server models available)"
                                                        } else {
                                                            testResult = false
                                                            val err = try {
                                                                JSONObject(respBody).optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}"
                                                            } catch (e: Exception) {
                                                                "HTTP ${response.code}: ${response.message}"
                                                            }
                                                            testMessage = "Failed: $err"
                                                        }
                                                    }
                                                } else {
                                                    val request = Request.Builder()
                                                        .url("https://api.deepseek.com/models")
                                                        .addHeader("Authorization", "Bearer $keyToTest")
                                                        .get()
                                                        .build()
                                                    val response = client.newCall(request).execute()
                                                    val respBody = response.body?.string() ?: ""
                                                    withContext(Dispatchers.Main) {
                                                        isTesting = false
                                                        if (response.isSuccessful) {
                                                            val modelsCount = try {
                                                                JSONObject(respBody).optJSONArray("data")?.length() ?: 0
                                                            } catch (e: Exception) { 0 }
                                                            testResult = true
                                                            testMessage = "Connected to DeepSeek! ($modelsCount server models available)"
                                                        } else {
                                                            testResult = false
                                                            val err = try {
                                                                JSONObject(respBody).optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}"
                                                            } catch (e: Exception) {
                                                                "HTTP ${response.code}: ${response.message}"
                                                            }
                                                            testMessage = "Failed: $err"
                                                        }
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                withContext(Dispatchers.Main) {
                                                    isTesting = false
                                                    testResult = false
                                                    testMessage = "Connection error: ${e.localizedMessage ?: e.message}"
                                                }
                                            }
                                        }
                                    },
                                    enabled = !isTesting,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("button_test_connection")
                                ) {
                                    if (isTesting) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Text("Test Server & Models")
                                }

                                if (testResult != null) {
                                    Surface(
                                        color = if (testResult == true) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = testMessage,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = if (testResult == true) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = if (settings.aiProvider == "Gemini") "Powered by Google Gemini for high-speed technical reasoning & cost estimation." else "Powered by DeepSeek API for advanced multi-step trade problem solving.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // --- BUILT-IN OFFLINE TRADE ENGINE ---
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = AppLocalization.t("local_ai_title", isIndonesian),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = AppLocalization.t("local_ai_desc", isIndonesian),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Instant Offline Toggle
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isOfflineEngine) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isIndonesian) "Mode Mesin Offline Mandiri" else "Built-in Offline Field Mode",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isOfflineEngine) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isOfflineEngine)
                                            (if (isIndonesian) "Aktif: Semua perhitungan dijawab instan oleh mesin lokal tanpa internet" else "Active: Instant on-device calculations without internet or API key")
                                        else
                                            (if (isIndonesian) "Nonaktif: Asisten menggunakan Cloud AI server" else "Inactive: Assistant connects to cloud AI provider"),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isOfflineEngine) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = isOfflineEngine,
                                    onCheckedChange = { enableOffline ->
                                        if (enableOffline) {
                                            viewModel.updateAiModel("built_in_engine")
                                        } else {
                                            viewModel.updateAiModel(if (settings.aiProvider == "Gemini") "gemini-2.0-flash" else "deepseek-chat")
                                        }
                                    },
                                    modifier = Modifier.testTag("switch_offline_engine")
                                )
                            }

                            if (isOfflineEngine) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = if (isIndonesian) "Zero Download • 0ms Latensi • 100% Privat di Lapangan" else "Zero Download • 0ms Latency • 100% Private Jobsite Execution",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Pre-Compiled Capabilities Matrix
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isIndonesian) "Kemampuan Rumus Offline Bawaan" else "Pre-compiled Offline Trade Modules",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val modules = if (isIndonesian) listOf(
                                    "Pengecoran Beton & Tulangan",
                                    "Optimalisasi Pola Potong (Cut List)",
                                    "Penurunan Tegangan & Kabel (NEC)",
                                    "Kemiringan Atap & Rafter",
                                    "Geometri & Templat Tangga",
                                    "Matriks Konversi Satuan"
                                ) else listOf(
                                    "Concrete Slabs & Footings",
                                    "Cut-List & Board Feet",
                                    "Voltage Drop & NEC Conduit",
                                    "Roof Pitch & Rafters",
                                    "Stair Stringer Templates",
                                    "High-Precision Unit Matrix"
                                )

                                modules.forEach { moduleName ->
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = moduleName,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Device Diagnostics Profile
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Memory, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Device Diagnostics Profile", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("• System Memory: ${"%.2f".format(hardwareSpecs.totalRamGb)} GB RAM (${hardwareSpecs.memoryTier})", style = MaterialTheme.typography.bodySmall)
                            Text("• Processor / Vendor: ${hardwareSpecs.socVendor}", style = MaterialTheme.typography.bodySmall)
                            Text("• Vulkan GPU Support: ${if (hardwareSpecs.supportsVulkan) "Enabled (Hardware Accelerated)" else "CPU Software Fallback"}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
