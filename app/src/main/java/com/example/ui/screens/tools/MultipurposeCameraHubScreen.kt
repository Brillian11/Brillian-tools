package com.example.ui.screens.tools

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.sensors.UsbProCameraScreen
import com.example.ui.screens.sensors.UsbProCameraViewModel

@Composable
fun MultipurposeCameraHubScreen(
    onNavigateToTool: (String) -> Unit,
    isIndonesian: Boolean,
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val cameraViewModel: UsbProCameraViewModel = viewModel()
    UsbProCameraScreen(
        viewModel = cameraViewModel,
        onNavigateBack = onClose,
        onNavigateToTool = onNavigateToTool,
        modifier = modifier.fillMaxSize()
    )
}
