package com.example.ui.screens.tools

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.ViewModelFactory
import com.example.ui.screens.sensors.UsbProCameraScreen
import com.example.ui.screens.sensors.UsbProCameraViewModel

@Composable
fun MultipurposeCameraHubScreen(
    onNavigateToTool: (String) -> Unit,
    isIndonesian: Boolean,
    factory: ViewModelProvider.Factory? = null,
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val resolvedFactory = factory ?: remember(context) { ViewModelFactory(context) }
    val cameraViewModel: UsbProCameraViewModel = viewModel(factory = resolvedFactory)
    UsbProCameraScreen(
        viewModel = cameraViewModel,
        onNavigateBack = onClose,
        onNavigateToTool = onNavigateToTool,
        modifier = modifier.fillMaxSize()
    )
}
