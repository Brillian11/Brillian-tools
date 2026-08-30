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
                        Text(if (currentPage == totalPages) AppLocalization.t("btn_get_started", isIndonesian) else AppLocalization.t("btn_next", isIndonesian))
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
                        1 -> IntroPage(isIndonesian)
                        2 -> TermsPage(
                            isIndonesian = isIndonesian,
                            isTermsAccepted = isTermsAccepted,
                            onTermsAcceptedChange = { isTermsAccepted = it }
                        )
                        3 -> PermissionsPage(isIndonesian)
                        4 -> SettingsPage(
                            isIndonesian = isIndonesian,
                            selectedUnits = selectedUnits,
                            onUnitsChange = { selectedUnits = it },
                            selectedPrecision = selectedPrecision,
                            onPrecisionChange = { selectedPrecision = it }
                        )
                        5 -> ProfilePage(
                            isIndonesian = isIndonesian,
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
                        text = AppLocalization.t("welcome_ai_title", isIndonesian),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Text(
                    text = AppLocalization.t("welcome_ai_desc", isIndonesian),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (downloadedModelId.isNullOrEmpty()) {
                    Text(
                        text = AppLocalization.t("welcome_ai_no_weights", isIndonesian),
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
                                text = "${AppLocalization.t("welcome_ai_downloading", isIndonesian)} ${(downloadProgress * 100).toInt()}%",
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
                            Text(AppLocalization.t("welcome_ai_btn_download", isIndonesian))
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
                                text = "$modelName ${AppLocalization.t("welcome_ai_ready", isIndonesian)}",
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
                        Text(AppLocalization.t("welcome_ai_btn_delete", isIndonesian))
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
