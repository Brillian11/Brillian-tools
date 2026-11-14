package com.example.ui.screens.sensors

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.sensor.DeviceSensorDetector
import com.example.domain.sensor.HardwareSensorSpec
import com.example.domain.sensor.SensorAuditReport
import com.example.domain.sensor.SensorCategory

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SensorDetectorScreen(
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val detector = remember { DeviceSensorDetector.getInstance(context) }
    val report by detector.auditReport.collectAsState()

    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "AVAILABLE", "MISSING"

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
            // Header Bar
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
                            imageVector = Icons.Default.Sensors,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Device Sensors Detector",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Comprehensive diagnostic scan of on-device measurement hardware & tool compatibility",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            // Overview Summary Metrics
            SensorAuditSummaryCard(
                report = report,
                onRescan = { detector.refresh() }
            )

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == "ALL",
                    onClick = { selectedFilter = "ALL" },
                    label = { Text("All Sensors (${report.totalScanned})") },
                    leadingIcon = if (selectedFilter == "ALL") {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )

                FilterChip(
                    selected = selectedFilter == "AVAILABLE",
                    onClick = { selectedFilter = "AVAILABLE" },
                    label = { Text("Available (${report.availableCount})") },
                    leadingIcon = {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                    }
                )

                FilterChip(
                    selected = selectedFilter == "MISSING",
                    onClick = { selectedFilter = "MISSING" },
                    label = { Text("Missing (${report.missingCount})") },
                    leadingIcon = {
                        Icon(Icons.Default.Cancel, contentDescription = null, tint = if (report.missingCount > 0) Color(0xFFDC2626) else Color.Gray, modifier = Modifier.size(16.dp))
                    }
                )
            }

            // Categorized Sensors List
            val filteredSensors = remember(report, selectedFilter) {
                when (selectedFilter) {
                    "AVAILABLE" -> report.sensors.filter { it.isAvailable }
                    "MISSING" -> report.sensors.filter { !it.isAvailable }
                    else -> report.sensors
                }
            }

            if (filteredSensors.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (selectedFilter == "MISSING") "Great news! No missing sensors found on this device." else "No sensors found for selected filter.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                val grouped = filteredSensors.groupBy { it.category }
                grouped.forEach { (category, sensorsInCategory) ->
                    Text(
                        text = category.displayName.uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.1.sp
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                    )

                    sensorsInCategory.forEach { spec ->
                        SensorDetailCard(spec = spec)
                    }
                }
            }
        }
    }
}

@Composable
fun SensorAuditSummaryCard(
    report: SensorAuditReport,
    onRescan: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HARDWARE READINESS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FilledTonalButton(
                    onClick = onRescan,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp).testTag("button_rescan_sensors")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Re-Scan", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Available Metric
                MetricBadgeBox(
                    title = "Available",
                    count = "${report.availableCount}/${report.totalScanned}",
                    subtext = "Sensors Active",
                    color = Color(0xFF16A34A),
                    modifier = Modifier.weight(1f)
                )

                // Missing Metric
                MetricBadgeBox(
                    title = "Missing",
                    count = "${report.missingCount}",
                    subtext = "Not Installed",
                    color = if (report.missingCount > 0) Color(0xFFDC2626) else Color(0xFF16A34A),
                    modifier = Modifier.weight(1f)
                )

                // Tools Compatibility Metric
                MetricBadgeBox(
                    title = "Tools Ready",
                    count = "${report.compatibleToolsCount}/${report.totalToolsCount}",
                    subtext = "${((report.compatibleToolsCount.toFloat() / report.totalToolsCount.toFloat()) * 100).toInt()}% Suite",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1.2f)
                )
            }

            if (report.missingCount > 0) {
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "${report.missingCount} hardware sensor(s) missing on this phone. Dependent tools will be marked as unavailable to avoid inaccurate calculations.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF78350F), fontSize = 11.sp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricBadgeBox(
    title: String,
    count: String,
    subtext: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = color, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = count,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                ),
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtext, style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp), color = color.copy(alpha = 0.85f))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SensorDetailCard(spec: HardwareSensorSpec) {
    val statusColor = if (spec.isAvailable) Color(0xFF16A34A) else Color(0xFFDC2626)
    val statusBg = if (spec.isAvailable) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)

    val icon: ImageVector = when (spec.id) {
        "magnetometer" -> Icons.Default.Sensors
        "accelerometer" -> Icons.Default.ScreenRotation
        "gyroscope" -> Icons.Default.Explore
        "barometer" -> Icons.Default.Landscape
        "light" -> Icons.Default.Lightbulb
        "microphone" -> Icons.Default.GraphicEq
        "camera" -> Icons.Default.Camera
        "camera_flash" -> Icons.Default.FlashOn
        "bluetooth_le" -> Icons.Default.Bluetooth
        "consumer_ir" -> Icons.Default.SettingsRemote
        "usb_host" -> Icons.Default.Usb
        else -> Icons.Default.PermDeviceInformation
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (spec.isAvailable) MaterialTheme.colorScheme.outlineVariant else Color(0xFFFCA5A5)),
        modifier = Modifier.fillMaxWidth().testTag("sensor_card_${spec.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(statusBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = spec.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = spec.technicalName,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Available vs Missing Status Chip
                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (spec.isAvailable) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (spec.isAvailable) "AVAILABLE" else "NOT PRESENT",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                            color = statusColor
                        )
                    }
                }
            }

            // Hardware details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(8.dp)
            ) {
                Text(
                    text = "Hardware Device: ${spec.hardwareModel}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (spec.isAvailable && spec.hardwareVendor != "N/A") {
                    Text(
                        text = "Vendor: ${spec.hardwareVendor} • Max Range: ${String.format("%.1f", spec.maxRange)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = spec.description,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Dependent Tools
            if (spec.dependentToolNames.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (spec.isAvailable) "Enables Tools:" else "Required For (Unavailable):",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (spec.isAvailable) MaterialTheme.colorScheme.primary else Color(0xFFDC2626)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        spec.dependentToolNames.forEach { toolName ->
                            Surface(
                                color = if (spec.isAvailable) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else Color(0xFFFEE2E2),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(0.5.dp, if (spec.isAvailable) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color(0xFFFCA5A5))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (spec.isAvailable) Icons.Default.Check else Icons.Default.Block,
                                        contentDescription = null,
                                        tint = if (spec.isAvailable) MaterialTheme.colorScheme.primary else Color(0xFFDC2626),
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = toolName,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = if (spec.isAvailable) MaterialTheme.colorScheme.onPrimaryContainer else Color(0xFF991B1B)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dialog shown when a user attempts to launch a tool that requires an unavailable hardware sensor.
 */
@Composable
fun SensorUnavailableDialog(
    toolTitle: String,
    missingSensors: List<HardwareSensorSpec>,
    onDismiss: () -> Unit,
    onOpenSensorsDetector: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.SensorsOff,
                contentDescription = null,
                tint = Color(0xFFDC2626),
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = "Tool Hardware Unavailable",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "The \"$toolTitle\" requires physical on-device measurement hardware that is not present on this phone:",
                    style = MaterialTheme.typography.bodyMedium
                )

                missingSensors.forEach { sensor ->
                    Surface(
                        color = Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(sensor.name, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF991B1B))
                                Text(sensor.description, style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = Color(0xFF7F1D1D))
                            }
                        }
                    }
                }

                Text(
                    text = "To inspect all hardware capabilities on this phone, visit the Sensors Detector.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    onOpenSensorsDetector()
                }
            ) {
                Text("Sensors Detector")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    )
}
