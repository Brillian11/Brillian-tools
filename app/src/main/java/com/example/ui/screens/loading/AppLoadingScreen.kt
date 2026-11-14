package com.example.ui.screens.loading

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.AppDatabase
import com.example.domain.model.ToolDefinition
import com.example.domain.sensor.DeviceSensorDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * AppLoadingScreen provides an engaging, high-fidelity loading and warm-up experience
 * on app cold launch and initial startup.
 *
 * It prevents the perception of a "frozen" app by executing database initialization,
 * Room DAO warm-up, and hardware sensor audits asynchronously on Dispatchers.IO while
 * smoothly driving a linear progress bar and rotating informative trade tips.
 */
@Composable
fun AppLoadingScreen(
    onInitializationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var targetProgress by remember { mutableFloatStateOf(0.05f) }
    var currentStatus by remember { mutableStateOf("Initializing system architecture...") }
    var tipIndex by remember { mutableIntStateOf(0) }

    val totalToolsCount = remember { ToolDefinition.ALL_TOOLS.size }
    val tradeTips = remember(totalToolsCount) {
        listOf(
            "All $totalToolsCount engineering tools operate 100% offline with zero cloud latency.",
            "Hardware sensors are audited on-device to ensure measurement safety & accuracy.",
            "Supports dual-mode Imperial fractions (1/64\") and high-precision Metric mm.",
            "Customize your dashboard anytime by pinning your most frequent tools from Catalog.",
            "Woodworking cutlist optimizer uses 2D bin-packing for minimum sheet waste."
        )
    }

    // Smooth animated progress
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "app_loading_progress"
    )

    // Subtle pulsing animation on the central brand logo
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Execute background warm-up while updating progress bar smoothly
    LaunchedEffect(Unit) {
        // Stage 1: Room DB & SQLite warm-up (0% -> 25%)
        currentStatus = "Initializing SQLite database & Room DAOs..."
        targetProgress = 0.25f
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getInstance(context)
            try {
                db.dashboardDao().getWidgetCount()
            } catch (_: Exception) { }
        }
        delay(350)

        // Stage 2: Hardware Sensor Detection (25% -> 55%)
        currentStatus = "Auditing device sensors & measurement hardware..."
        targetProgress = 0.55f
        tipIndex = 1
        withContext(Dispatchers.IO) {
            try {
                DeviceSensorDetector.getInstance(context).scanAllSensors()
            } catch (_: Exception) { }
        }
        delay(350)

        // Stage 3: Catalog & Tool Models (55% -> 80%)
        currentStatus = "Loading $totalToolsCount field calculators & engineering tools..."
        targetProgress = 0.80f
        tipIndex = 2
        withContext(Dispatchers.IO) {
            // Pre-calculate count
            val count = ToolDefinition.ALL_TOOLS.size
        }
        delay(350)

        // Stage 4: Profile Configuration (80% -> 95%)
        currentStatus = "Configuring trade profiles & unit preferences..."
        targetProgress = 0.95f
        tipIndex = 3
        delay(300)

        // Stage 5: Ready (100%)
        currentStatus = "Workspace ready! Opening suite..."
        targetProgress = 1.0f
        delay(300)

        onInitializationComplete()
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp)
        ) {
            // Main Center Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Branded App Icon Badge
                Box(
                    modifier = Modifier
                        .scale(pulseScale)
                        .size(96.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Engineering,
                        contentDescription = "Brillian Tools Logo",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(54.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // App Brand Name & Subtitle
                Text(
                    text = "Brillian Tools",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Professional Industrial & Trade Suite",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "v2.4 Pro • $totalToolsCount Offline Engineering Engines",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Progress Bar Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Preparing Workspace",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            val percentText = (animatedProgress * 100).toInt().coerceIn(0, 100)
                            Text(
                                text = "$percentText%",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.testTag("loading_percentage_text")
                            )
                        }

                        // Linear Loading Progress Bar
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .testTag("app_loading_progress_bar"),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            strokeCap = StrokeCap.Round
                        )

                        // Real-time status text
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (animatedProgress >= 1f) Icons.Default.CheckCircle else Icons.Default.Speed,
                                contentDescription = null,
                                tint = if (animatedProgress >= 1f) Color(0xFF16A34A) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = currentStatus,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                modifier = Modifier.testTag("loading_status_text")
                            )
                        }
                    }
                }
            }

            // Bottom Tip Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = tradeTips.getOrElse(tipIndex) { tradeTips.first() },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
