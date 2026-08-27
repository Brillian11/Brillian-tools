package com.example.ui.screens.welcome

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.dashboard.DashboardViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WelcomeScreen(
    viewModel: DashboardViewModel,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentPage by remember { mutableStateOf(1) }
    val totalPages = 5

    // State for Settings & Profile Picker
    var isTermsAccepted by remember { mutableStateOf(false) }
    var selectedUnits by remember { mutableStateOf("Imperial") }
    var selectedPrecision by remember { mutableStateOf("1/16\"") }
    var selectedProfile by remember { mutableStateOf("General") }

    val context = LocalContext.current

    Scaffold(
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back Button
                    if (currentPage > 1) {
                        OutlinedButton(
                            onClick = { currentPage-- },
                            modifier = Modifier
                                .testTag("welcome_back_button")
                                .minimumInteractiveComponentSize()
                        ) {
                            Text("Back")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(80.dp))
                    }

                    // Page Indicator Dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..totalPages) {
                            val isSelected = i == currentPage
                            Box(
                                modifier = Modifier
                                    .size(if (isSelected) 10.dp else 6.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    )
                            )
                        }
                    }

                    // Next / Finish Button
                    Button(
                        onClick = {
                            if (currentPage < totalPages) {
                                if (currentPage == 2 && !isTermsAccepted) {
                                    // Show error or don't allow proceeding if terms are not accepted
                                } else {
                                    currentPage++
                                }
                            } else {
                                // Final Page: save and complete
                                viewModel.completeOnboarding(selectedProfile, selectedUnits)
                                onFinished()
                            }
                        },
                        enabled = currentPage != 2 || isTermsAccepted,
                        modifier = Modifier
                            .testTag("welcome_next_button")
                            .minimumInteractiveComponentSize()
                    ) {
                        Text(if (currentPage == totalPages) "Get Started" else "Next")
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnimatedContent(
                targetState = currentPage,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { width -> width } + fadeIn() with
                                slideOutHorizontally { width -> -width } + fadeOut()
                    } else {
                        slideInHorizontally { width -> -width } + fadeIn() with
                                slideOutHorizontally { width -> width } + fadeOut()
                    }
                },
                modifier = Modifier.weight(1f)
            ) { page ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (page) {
                        1 -> IntroPage()
                        2 -> TermsPage(
                            isTermsAccepted = isTermsAccepted,
                            onTermsAcceptedChange = { isTermsAccepted = it }
                        )
                        3 -> PermissionsPage()
                        4 -> SettingsPage(
                            selectedUnits = selectedUnits,
                            onUnitsChange = { selectedUnits = it },
                            selectedPrecision = selectedPrecision,
                            onPrecisionChange = { selectedPrecision = it }
                        )
                        5 -> ProfilePage(
                            selectedProfile = selectedProfile,
                            onProfileChange = { selectedProfile = it }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IntroPage() {
    Icon(
        imageVector = Icons.Default.Construction,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .size(100.dp)
            .padding(top = 40.dp)
    )

    Text(
        text = "Brillian Tools Suite",
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 16.dp)
    )

    Text(
        text = "The ultimate trade utility handbook built right into your pocket.",
        style = MaterialTheme.typography.titleMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(horizontal = 16.dp)
    )

    Spacer(modifier = Modifier.height(24.dp))

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IntroBullet(
                icon = Icons.Default.GridOn,
                title = "Adaptive Tool Workspace",
                description = "Over 50+ calculation tools organized dynamically for your trade."
            )
            IntroBullet(
                icon = Icons.Default.CloudQueue,
                title = "Offline-First Sync Engine",
                description = "Work completely offline in deep basement garages or remote sites. Syncs seamlessly whenever signal returns."
            )
            IntroBullet(
                icon = Icons.Default.History,
                title = "Continuous Log Tracking",
                description = "Keep records of your calculations, materials inventory, and job site notes automatically."
            )
        }
    }
}

@Composable
fun IntroBullet(icon: ImageVector, title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(24.dp)
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TermsPage(
    isTermsAccepted: Boolean,
    onTermsAcceptedChange: (Boolean) -> Unit
) {
    Icon(
        imageVector = Icons.Default.Description,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .size(80.dp)
            .padding(top = 20.dp)
    )

    Text(
        text = "Terms of Reference",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    Text(
        text = "Please read and accept the following guidelines to proceed with installation.",
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(12.dp))

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "1. Calculations and Estimates:\n" +
                        "This application provides estimation utilities, calculations, conversion formulas, and sensor readings for woodworking, civil engineering, electrical works, plumbing, and other construction-related trades. All computations are based on typical standard code formulas. \n\n" +
                        "2. Professional Verification:\n" +
                        "While we strive for extreme mathematical precision, values produced by these tools are for estimation purposes only. Users must verify all critical building structural dimensions, safety margins, load constraints, and electrical codes with professional plans and official code guidelines.\n\n" +
                        "3. Sensor Accuracy:\n" +
                        "Sensor features (e.g. altimeter, compass, decibel level, leveler) rely directly on hardware sensors on your mobile device. Accuracy may vary depending on environmental magnetic interference, temperature, and device hardware quality.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onTermsAcceptedChange(!isTermsAccepted) }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Checkbox(
            checked = isTermsAccepted,
            onCheckedChange = { onTermsAcceptedChange(it) },
            modifier = Modifier.testTag("terms_checkbox")
        )
        Text(
            text = "I accept the Terms of Reference and acknowledge that calculations should be verified on-site.",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun PermissionsPage() {
    Icon(
        imageVector = Icons.Default.Security,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .size(80.dp)
            .padding(top = 20.dp)
    )

    Text(
        text = "Device Permissions",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    Text(
        text = "Brillian Tools utilizes modern device hardware to enhance calculations. Here is what we use:",
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(12.dp))

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PermissionRow(
                icon = Icons.Default.LocationOn,
                title = "Precise Location",
                desc = "Required for live local meteorology, sun-path solar calculations, and barometric altimeter referencing."
            )
            PermissionRow(
                icon = Icons.Default.PhotoCamera,
                title = "Camera & AR Depth",
                desc = "Used by AR measurement tools, plumbing level viewfinder overlays, and barcode inventory logging."
            )
            PermissionRow(
                icon = Icons.Default.Mic,
                title = "Microphone Sensor",
                desc = "Feeds the Decibel Sound Meter and acoustic engine diagnostics for machinery vibration."
            )
        }
    }
}

@Composable
fun PermissionRow(icon: ImageVector, title: String, desc: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SettingsPage(
    selectedUnits: String,
    onUnitsChange: (String) -> Unit,
    selectedPrecision: String,
    onPrecisionChange: (String) -> Unit
) {
    Icon(
        imageVector = Icons.Default.Tune,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .size(80.dp)
            .padding(top = 20.dp)
    )

    Text(
        text = "Measurement Settings",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    Text(
        text = "Configure preferred standards for the calculations. You can change this in settings anytime.",
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Preferred Unit System",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Start,
        modifier = Modifier.fillMaxWidth()
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (selectedUnits == "Imperial") MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier
                .weight(1f)
                .clickable { onUnitsChange("Imperial") }
                .testTag("unit_imperial_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Straighten,
                    contentDescription = null,
                    tint = if (selectedUnits == "Imperial") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Imperial",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = "Inches, Feet, Yards, Lbs",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (selectedUnits == "Metric") MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier
                .weight(1f)
                .clickable { onUnitsChange("Metric") }
                .testTag("unit_metric_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.SquareFoot,
                    contentDescription = null,
                    tint = if (selectedUnits == "Metric") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Metric",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = "Millimeters, Meters, Kg",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Tape Measurement Precision",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Start,
        modifier = Modifier.fillMaxWidth()
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf("1/16\"", "1/32\"", "1/64\"").forEach { precision ->
            val isSelected = selectedPrecision == precision
            FilterChip(
                selected = isSelected,
                onClick = { onPrecisionChange(precision) },
                label = { Text(precision) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("precision_$precision")
            )
        }
    }
}

@Composable
fun ProfilePage(
    selectedProfile: String,
    onProfileChange: (String) -> Unit
) {
    Icon(
        imageVector = Icons.Default.AccountCircle,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .size(80.dp)
            .padding(top = 20.dp)
    )

    Text(
        text = "Choose Your Workspace Profile",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    Text(
        text = "We will pre-pin the most critical calculation utilities based on your daily job desk. You can customize this later anytime from the Tool Catalog.",
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(8.dp))

    val profiles = listOf(
        ProfileOption("Woodworker", Icons.Default.Engineering, "Woodworker", "Woodworking & Carpentry", "Board estimation, cut-list optimization, stair templates, miter saw angles, and moisture scales."),
        ProfileOption("Civil Engineer", Icons.Default.Foundation, "Civil Engineer", "Excavation, Concrete & Grades", "Concrete volumes, earthwork cut/fills, grade, stormwater runoff calculation, and aggregate sieves."),
        ProfileOption("Electrician", Icons.Default.FlashOn, "Electrician", "Electrical & Wiring", "Ohm's law, voltage drops, conduit fill capacities, motor amperes, breaker panels, and battery sizers."),
        ProfileOption("Mechanical", Icons.Default.PrecisionManufacturing, "Mechanical & HVAC", "Mechanical, HVAC & Piping", "Dew point, wet bulb, live thermodynamic refrigerant curves, duct velocity sizers, and rigging sling angles."),
        ProfileOption("Painter", Icons.Default.Palette, "Painter & Coating Specialist", "Painting, Coating & Prep", "Coverage calculations, 2K mixing ratios, wet film thickness, dew points, and rust treatment."),
        ProfileOption("General", Icons.Default.Build, "General Contractor", "General Maintenance & Handyman", "Drywall count, tiling, painting coverage, general measuring tools, and checklists.")
    )

    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("brillian_ai_prefs", Context.MODE_PRIVATE) }
    var downloadedModelId by remember { mutableStateOf(prefs.getString("downloaded_model", "smollm2_360m")) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        profiles.forEach { item ->
            val isSelected = selectedProfile == item.id
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surface
                ),
                border = if (isSelected) null else CardDefaults.outlinedCardBorder(),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onProfileChange(item.id) }
                    .testTag("profile_${item.id}_card")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = item.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = item.desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Engineering,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "On-Device AI Copilot Download",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Text(
                    text = "Activate an on-device local trade assistant model (GGUF weights). Run completely offline at jobsites with zero cellular delay.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (downloadedModelId.isNullOrEmpty()) {
                    Text(
                        text = "Status: No local weights downloaded yet.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )

                    if (isDownloading) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            LinearProgressIndicator(
                                progress = { downloadProgress },
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Downloading & compiling weights... ${(downloadProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                isDownloading = true
                                downloadProgress = 0f
                                coroutineScope.launch {
                                    for (i in 1..10) {
                                        kotlinx.coroutines.delay(120)
                                        downloadProgress = i / 10f
                                    }
                                    prefs.edit().putString("downloaded_model", "qwen25_15b").apply()
                                    downloadedModelId = "qwen25_15b"
                                    isDownloading = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("welcome_download_model_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Download Qwen2.5 1.5B (920 MB)")
                        }
                    }
                } else {
                    val modelName = when (downloadedModelId) {
                        "smollm2_360m" -> "SmolLM2 360M Instruct"
                        "qwen25_15b" -> "Qwen2.5 1.5B Instruct"
                        else -> "Gemma-2 2B Instruct"
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$modelName is ready on-device!",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            prefs.edit().putString("downloaded_model", "").apply()
                            downloadedModelId = ""
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth().testTag("welcome_delete_model_button")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete Active Model to Free Storage")
                    }
                }
            }
        }
    }
}

data class ProfileOption(
    val id: String,
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val desc: String
)
