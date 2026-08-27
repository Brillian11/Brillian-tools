package com.example.ui.screens.dashboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.ViewModelFactory
import com.example.ui.screens.sensors.CalculatorScreen
import com.example.ui.screens.sensors.CalculatorViewModel
import com.example.ui.screens.tools.QuickNotesScreen
import com.example.ui.screens.tools.QuickNotesViewModel
import com.example.ui.screens.tools.UnitConverterScreen
import com.example.ui.screens.tools.UnitConverterViewModel
import com.example.ui.screens.work.WorkTrackingScreen
import com.example.ui.screens.work.WorkTrackingViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainTabScreen(
    dashboardViewModel: DashboardViewModel,
    factory: ViewModelFactory,
    onNavigateToTool: (String) -> Unit,
    onNavigateToCustomize: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()

    // Instantiate tab ViewModels cleanly from our centralized factory
    val notesViewModel: QuickNotesViewModel = viewModel(factory = factory)
    val workViewModel: WorkTrackingViewModel = viewModel(factory = factory)
    val calculatorViewModel: CalculatorViewModel = viewModel(factory = factory)
    val converterViewModel: UnitConverterViewModel = viewModel(factory = factory)

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("main_bottom_nav_bar")
            ) {
                val tabs = listOf(
                    Triple("Tools", Icons.Default.Build, "nav_tab_tools"),
                    Triple("Notes", Icons.Default.StickyNote2, "nav_tab_notes"),
                    Triple("Work", Icons.Default.Work, "nav_tab_work"),
                    Triple("Calculator", Icons.Default.Calculate, "nav_tab_calculator")
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
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
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
                                text = { Text("Scientific Calc") },
                                modifier = Modifier.testTag("sub_tab_calc")
                            )
                            Tab(
                                selected = activeSubTab == 1,
                                onClick = { activeSubTab = 1 },
                                text = { Text("Unit Converter") },
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
            }
        }
    }
}
