package com.example.ui.screens.dashboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.geometry.Offset
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.animation.Crossfade

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.ViewModelFactory
import com.example.ui.screens.sensors.CalculatorScreen
import com.example.ui.screens.sensors.CalculatorViewModel
import com.example.ui.screens.tools.QuickNotesScreen
import com.example.ui.screens.tools.QuickNotesViewModel
import com.example.ui.screens.tools.UnitConverterScreen
import com.example.ui.screens.tools.UnitConverterViewModel
import com.example.ui.screens.tools.MultipurposeCameraHubScreen
import com.example.ui.screens.work.WorkTrackingScreen
import com.example.ui.screens.work.WorkTrackingViewModel
import com.example.ui.screens.library.LibraryScreen
import com.example.ui.screens.library.LibraryViewModel
import com.example.ui.screens.ai.BrillianAiScreen
import kotlinx.coroutines.launch

import com.example.ui.utils.AppLocalization

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainTabScreen(
    dashboardViewModel: DashboardViewModel,
    factory: ViewModelFactory,
    onNavigateToTool: (String) -> Unit,
    onNavigateToCustomize: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userSettings by dashboardViewModel.userSettings.collectAsState()
    val isIndonesian = userSettings.languageCode == "id"
    val showAiScreen by dashboardViewModel.isCopilotOpen.collectAsState()

    androidx.activity.compose.BackHandler(enabled = showAiScreen) {
        dashboardViewModel.setCopilotOpen(false)
    }

    val pagerState = rememberPagerState(pageCount = { 5 })
    val coroutineScope = rememberCoroutineScope()

    var showCameraHubScreen by remember { mutableStateOf(false) }

    androidx.activity.compose.BackHandler(enabled = showCameraHubScreen) {
        showCameraHubScreen = false
    }

    var isFabExpanded by remember { mutableStateOf(true) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -5f) {
                    isFabExpanded = false
                } else if (available.y > 5f) {
                    isFabExpanded = true
                }
                return Offset.Zero
            }
        }
    }


    // Instantiate tab ViewModels cleanly from our centralized factory
    val notesViewModel: QuickNotesViewModel = viewModel(factory = factory)
    val workViewModel: WorkTrackingViewModel = viewModel(factory = factory)
    val calculatorViewModel: CalculatorViewModel = viewModel(factory = factory)
    val converterViewModel: UnitConverterViewModel = viewModel(factory = factory)
    val libraryViewModel: LibraryViewModel = viewModel(factory = factory)

    Scaffold(
        bottomBar = {
            if (!showAiScreen && !showCameraHubScreen) {
                NavigationBar(
                    modifier = Modifier.testTag("main_bottom_nav_bar")
                ) {
                    val tabs = listOf(
                        Triple(AppLocalization.t("nav_tools", isIndonesian), Icons.Default.Build, "nav_tab_tools"),
                        Triple(AppLocalization.t("nav_notes", isIndonesian), Icons.Default.StickyNote2, "nav_tab_notes"),
                        Triple(AppLocalization.t("nav_work", isIndonesian), Icons.Default.Work, "nav_tab_work"),
                        Triple(AppLocalization.t("nav_calculator", isIndonesian), Icons.Default.Calculate, "nav_tab_calculator"),
                        Triple(AppLocalization.t("nav_library", isIndonesian), Icons.Default.ViewInAr, "nav_tab_library")
                    )

                    tabs.forEachIndexed { index, (label, icon, tag) ->
                        NavigationBarItem(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                            modifier = Modifier.testTag(tag)
                        )
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().nestedScroll(nestedScrollConnection)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) { page ->
            when (page) {
                0 -> {
                    DashboardScreen(
                        viewModel = dashboardViewModel,
                        onNavigateToTool = onNavigateToTool,
                        onNavigateToCustomize = onNavigateToCustomize,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                1 -> {
                    QuickNotesScreen(
                        viewModel = notesViewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                2 -> {
                    WorkTrackingScreen(
                        viewModel = workViewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                3 -> {
                    // Calculator tab with inner sub-tab toggle for Scientific Calculator & Unit Converter
                    var activeSubTab by remember { mutableStateOf(0) } // 0 = Calc, 1 = Unit Converter
                    Column(modifier = Modifier.fillMaxSize()) {
                        TabRow(
                            selectedTabIndex = activeSubTab,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Tab(
                                selected = activeSubTab == 0,
                                onClick = { activeSubTab = 0 },
                                text = { Text(AppLocalization.t("scientific_calc", isIndonesian)) },
                                modifier = Modifier.testTag("sub_tab_calc")
                            )
                            Tab(
                                selected = activeSubTab == 1,
                                onClick = { activeSubTab = 1 },
                                text = { Text(AppLocalization.t("unit_converter", isIndonesian)) },
                                modifier = Modifier.testTag("sub_tab_converter")
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            if (activeSubTab == 0) {
                                CalculatorScreen(
                                    viewModel = calculatorViewModel,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                UnitConverterScreen(
                                    viewModel = converterViewModel,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
                4 -> {
                    LibraryScreen(
                        viewModel = libraryViewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

            // Floating Buttons in bottom left above navbar (Only shown on Tools tab, page 0)
            if (!showAiScreen && !showCameraHubScreen && pagerState.currentPage == 0) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 16.dp)
                        .padding(bottom = innerPadding.calculateBottomPadding()),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Full Floating Camera Button
                    Surface(
                        onClick = { onNavigateToTool("tool_usb_pro_camera") },
                        modifier = Modifier
                            .size(56.dp)
                            .testTag("btn_floating_camera"),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        shadowElevation = 6.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.PhotoCamera,
                                contentDescription = "Camera Studio",
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                    
                    // Full Floating AI FAB
                    Surface(
                        onClick = { dashboardViewModel.setCopilotOpen(true) },
                        modifier = Modifier
                            .size(56.dp)
                            .testTag("btn_floating_ai"),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        shadowElevation = 6.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.SmartToy,
                                contentDescription = "AI Copilot",
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
            }

            // Camera Hub Screen Overlay
            AnimatedVisibility(
                visible = showCameraHubScreen,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                MultipurposeCameraHubScreen(
                    onNavigateToTool = onNavigateToTool,
                    isIndonesian = isIndonesian,
                    onClose = { showCameraHubScreen = false },
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                )
            }

            // AI Screen Overlay
            AnimatedVisibility(
                visible = showAiScreen,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                BrillianAiScreen(
                    currentRoute = "dashboard",
                    isOnline = true,
                    aiProvider = userSettings.aiProvider,
                    apiKey = userSettings.aiApiKey,
                    selectedModel = userSettings.aiModel,
                    onModelSelected = { modelId ->
                        dashboardViewModel.updateAiModel(modelId)
                    },
                    onNavigateToTool = { route ->
                        // Keep showAiScreen = true so that returning from the backstack returns directly to the AI Chatbox
                        onNavigateToTool(route)
                    },
                    onClose = { dashboardViewModel.setCopilotOpen(false) },
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                )
            }
        }
    }
}
