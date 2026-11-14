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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.example.ui.utils.AppLocalization
import com.example.domain.sensor.DeviceSensorDetector
import com.example.ui.screens.sensors.SensorDetailCard
import com.example.ui.screens.sensors.SensorAuditSummaryCard

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WelcomeScreen(
    viewModel: DashboardViewModel,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentPage by remember { mutableStateOf(1) }
    val totalPages = 6

    // State for Settings & Profile Picker
    var isTermsAccepted by remember { mutableStateOf(false) }
    var selectedUnits by remember { mutableStateOf("Imperial") }
    var selectedPrecision by remember { mutableStateOf("1/16\"") }
    var selectedProfile by remember { mutableStateOf("General") }
    var isCompleting by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val isIndonesian = userSettings.languageCode == "id"

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
                            Text(AppLocalization.t("btn_back", isIndonesian))
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
                                // Final Page: save and complete with animated loading feedback
                                isCompleting = true
                                viewModel.completeOnboarding(selectedProfile, selectedUnits)
                                coroutineScope.launch {
                                    kotlinx.coroutines.delay(1200)
                                    onFinished()
                                }
                            }
                        },
                        enabled = (currentPage != 2 || isTermsAccepted) && !isCompleting,
                        modifier = Modifier
                            .testTag("welcome_next_button")
                            .minimumInteractiveComponentSize()
                    ) {
                        Text(if (currentPage == totalPages) AppLocalization.t("btn_get_started", isIndonesian) else AppLocalization.t("btn_next", isIndonesian))
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        if (isCompleting) {
            AlertDialog(
                onDismissRequest = {},
                confirmButton = {},
                shape = RoundedCornerShape(24.dp),
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Engineering,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Text(
                            text = "Configuring $selectedProfile Workspace",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Pinning relevant trade tools, initializing offline calculation engines, and applying unit standards...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
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
                        1 -> IntroPage(isIndonesian)
                        2 -> TermsPage(
                            isIndonesian = isIndonesian,
                            isTermsAccepted = isTermsAccepted,
                            onTermsAcceptedChange = { isTermsAccepted = it }
                        )
                        3 -> PermissionsPage(isIndonesian)
                        4 -> SensorsDetectorWizardPage(isIndonesian)
                        5 -> SettingsPage(
                            isIndonesian = isIndonesian,
                            selectedUnits = selectedUnits,
                            onUnitsChange = { selectedUnits = it },
                            selectedPrecision = selectedPrecision,
                            onPrecisionChange = { selectedPrecision = it }
                        )
                        6 -> ProfilePage(
                            isIndonesian = isIndonesian,
                            selectedProfile = selectedProfile,
                            onProfileChange = { selectedProfile = it },
                            selectedEngine = userSettings.aiModel,
                            onEngineChange = { viewModel.updateAiModel(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IntroPage(isIndonesian: Boolean) {
    Icon(
        imageVector = Icons.Default.Construction,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .size(100.dp)
            .padding(top = 40.dp)
    )

    Text(
        text = AppLocalization.t("welcome_title", isIndonesian),
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 16.dp)
    )

    Text(
        text = AppLocalization.t("welcome_subtitle", isIndonesian),
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
                title = AppLocalization.t("welcome_bullet1_title", isIndonesian),
                description = AppLocalization.t("welcome_bullet1_desc", isIndonesian)
            )
            IntroBullet(
                icon = Icons.Default.CloudQueue,
                title = AppLocalization.t("welcome_bullet2_title", isIndonesian),
                description = AppLocalization.t("welcome_bullet2_desc", isIndonesian)
            )
            IntroBullet(
                icon = Icons.Default.History,
                title = AppLocalization.t("welcome_bullet3_title", isIndonesian),
                description = AppLocalization.t("welcome_bullet3_desc", isIndonesian)
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
    isIndonesian: Boolean,
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
        text = AppLocalization.t("welcome_terms_title", isIndonesian),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    Text(
        text = AppLocalization.t("welcome_terms_desc", isIndonesian),
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
                text = AppLocalization.t("welcome_terms_body", isIndonesian),
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
            text = AppLocalization.t("welcome_terms_checkbox", isIndonesian),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun PermissionsPage(isIndonesian: Boolean) {
    Icon(
        imageVector = Icons.Default.Security,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .size(80.dp)
            .padding(top = 20.dp)
    )

    Text(
        text = AppLocalization.t("welcome_permissions_title", isIndonesian),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    Text(
        text = AppLocalization.t("welcome_permissions_desc", isIndonesian),
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
                title = AppLocalization.t("welcome_perm1_title", isIndonesian),
                desc = AppLocalization.t("welcome_perm1_desc", isIndonesian)
            )
            PermissionRow(
                icon = Icons.Default.PhotoCamera,
                title = AppLocalization.t("welcome_perm2_title", isIndonesian),
                desc = AppLocalization.t("welcome_perm2_desc", isIndonesian)
            )
            PermissionRow(
                icon = Icons.Default.Mic,
                title = AppLocalization.t("welcome_perm3_title", isIndonesian),
                desc = AppLocalization.t("welcome_perm3_desc", isIndonesian)
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
    isIndonesian: Boolean,
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
        text = AppLocalization.t("welcome_settings_title", isIndonesian),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    Text(
        text = AppLocalization.t("welcome_settings_desc", isIndonesian),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = AppLocalization.t("welcome_unit_system", isIndonesian),
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
                    text = AppLocalization.t("welcome_imperial_label", isIndonesian),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = AppLocalization.t("welcome_imperial_desc", isIndonesian),
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
                    text = AppLocalization.t("welcome_metric_label", isIndonesian),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = AppLocalization.t("welcome_metric_desc", isIndonesian),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = AppLocalization.t("welcome_precision_title", isIndonesian),
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
    isIndonesian: Boolean,
    selectedProfile: String,
    onProfileChange: (String) -> Unit,
    selectedEngine: String,
    onEngineChange: (String) -> Unit
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
        text = AppLocalization.t("welcome_profile_title", isIndonesian),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    Text(
        text = AppLocalization.t("welcome_profile_desc", isIndonesian),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(8.dp))

    val profiles = listOf(
        ProfileOption("Woodworker", Icons.Default.Engineering, AppLocalization.t("profile_woodworker_title", isIndonesian), AppLocalization.t("profile_woodworker_sub", isIndonesian), AppLocalization.t("profile_woodworker_desc", isIndonesian)),
        ProfileOption("Civil Engineer", Icons.Default.Foundation, AppLocalization.t("profile_civil_title", isIndonesian), AppLocalization.t("profile_civil_sub", isIndonesian), AppLocalization.t("profile_civil_desc", isIndonesian)),
        ProfileOption("Electrician", Icons.Default.FlashOn, AppLocalization.t("profile_electrician_title", isIndonesian), AppLocalization.t("profile_electrician_sub", isIndonesian), AppLocalization.t("profile_electrician_desc", isIndonesian)),
        ProfileOption("Mechanical", Icons.Default.PrecisionManufacturing, AppLocalization.t("profile_mechanical_title", isIndonesian), AppLocalization.t("profile_mechanical_sub", isIndonesian), AppLocalization.t("profile_mechanical_desc", isIndonesian)),
        ProfileOption("Painter", Icons.Default.Palette, AppLocalization.t("profile_painter_title", isIndonesian), AppLocalization.t("profile_painter_sub", isIndonesian), AppLocalization.t("profile_painter_desc", isIndonesian)),
        ProfileOption("Metalworker", Icons.Default.LocalFireDepartment, AppLocalization.t("profile_metalworker_title", isIndonesian), AppLocalization.t("profile_metalworker_sub", isIndonesian), AppLocalization.t("profile_metalworker_desc", isIndonesian)),
        ProfileOption("General", Icons.Default.Build, AppLocalization.t("profile_general_title", isIndonesian), AppLocalization.t("profile_general_sub", isIndonesian), AppLocalization.t("profile_general_desc", isIndonesian))
    )

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
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = AppLocalization.t("welcome_ai_title", isIndonesian),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Text(
                    text = AppLocalization.t("welcome_ai_desc", isIndonesian),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Option 1: Built-in Offline Engine (Recommended)
                val isOfflineSelected = selectedEngine == "built_in_engine"
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isOfflineSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface
                    ),
                    border = if (isOfflineSelected) null else CardDefaults.outlinedCardBorder(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEngineChange("built_in_engine") }
                        .testTag("welcome_option_offline_engine")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RadioButton(
                            selected = isOfflineSelected,
                            onClick = { onEngineChange("built_in_engine") }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = AppLocalization.t("welcome_ai_offline_title", isIndonesian),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isOfflineSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = AppLocalization.t("welcome_ai_offline_desc", isIndonesian),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isOfflineSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Option 2: Cloud AI Assistant
                val isCloudSelected = selectedEngine != "built_in_engine"
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCloudSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface
                    ),
                    border = if (isCloudSelected) null else CardDefaults.outlinedCardBorder(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEngineChange("gemini-2.0-flash") }
                        .testTag("welcome_option_cloud_ai")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RadioButton(
                            selected = isCloudSelected,
                            onClick = { onEngineChange("gemini-2.0-flash") }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = AppLocalization.t("welcome_ai_cloud_title", isIndonesian),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isCloudSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = AppLocalization.t("welcome_ai_cloud_desc", isIndonesian),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isCloudSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Status Badge
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
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
                            text = if (isOfflineSelected) AppLocalization.t("welcome_ai_ready", isIndonesian)
                            else "Cloud AI selected (API key configurable in Settings)",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
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

@Composable
fun SensorsDetectorWizardPage(isIndonesian: Boolean) {
    val context = LocalContext.current
    val detector = remember { DeviceSensorDetector.getInstance(context) }
    val report by detector.auditReport.collectAsState()

    Icon(
        imageVector = Icons.Default.Sensors,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .size(72.dp)
            .padding(top = 16.dp)
    )

    Text(
        text = if (isIndonesian) "Detektor Sensor Perangkat" else "Device Sensors Detector",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.primary
    )

    Text(
        text = if (isIndonesian)
            "Pemeriksaan sensor otomatis mendeteksi ketersediaan perangkat keras di ponsel ini. Alat yang membutuhkan sensor yang tidak tersedia akan ditandai tidak dapat digunakan secara otomatis."
        else
            "Automatic diagnostic scan to audit hardware measurement sensors on this device. Tools requiring missing sensors will be gracefully made unavailable.",
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(8.dp))

    SensorAuditSummaryCard(
        report = report,
        onRescan = { detector.refresh() }
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = if (isIndonesian) "HASIL DIAGNOSTIK SENSOR" else "HARDWARE SENSORS DETECTED",
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.1.sp
        ),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth()
    )

    report.sensors.forEach { spec ->
        SensorDetailCard(spec = spec)
    }

    Spacer(modifier = Modifier.height(16.dp))
}
